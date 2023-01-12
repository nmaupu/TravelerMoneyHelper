package org.maupu.android.tmh;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import org.jetbrains.annotations.Contract;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.StaticPrefs;
import org.maupu.android.tmh.databinding.ActivityPreferencesBinding;
import org.maupu.android.tmh.dialog.DriveAutomaticBackupBottomSheetDialog;
import org.maupu.android.tmh.dialog.DriveRestoreDialogPreference;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AbstractAsyncTask;
import org.maupu.android.tmh.ui.async.AbstractOpenExchangeRates;
import org.maupu.android.tmh.ui.async.AsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncUpdater;
import org.maupu.android.tmh.util.TmhLogger;
import org.maupu.android.tmh.util.drive.BackupDbFileHelper;
import org.maupu.android.tmh.util.drive.DriveServiceHelper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

// TODO check for api key OER !

public class PreferencesActivity extends AppCompatActivity implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final Class<PreferencesActivity> TAG = PreferencesActivity.class;

    private ActivityPreferencesBinding binding;
    private PreferencesFragment preferencesFragment;

    // google sign in components
    private ActivityResultLauncher<Intent> googleSignInStartForResult;
    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount googleSignInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);

        binding = ActivityPreferencesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        setTitle(R.string.fragment_title_preferences);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24);

        preferencesFragment = (PreferencesFragment) getSupportFragmentManager().findFragmentById(R.id.preferences_fragment);

        EditTextPreference newDatabase = preferencesFragment.findPreference(StaticData.PREF_KEY_NEW_DATABASE);
        newDatabase.setOnPreferenceChangeListener(this);
        newDatabase.setOnPreferenceClickListener(this);

        ListPreference listDatabases = preferencesFragment.findPreference(StaticData.PREF_KEY_DATABASE);
        listDatabases.setOnPreferenceClickListener(this);
        listDatabases.setOnPreferenceChangeListener(this);

        MultiSelectListPreference listDatabasesForDeletion = preferencesFragment.findPreference(StaticData.PREF_KEY_MANAGE_DB);
        listDatabasesForDeletion.setOnPreferenceChangeListener(this);
        listDatabasesForDeletion.setOnPreferenceClickListener(this);

        EditTextPreference etpRenameDatabase = preferencesFragment.findPreference(StaticData.PREF_KEY_RENAME_DATABASE);
        etpRenameDatabase.setOnPreferenceChangeListener(this);
        etpRenameDatabase.setOnPreferenceClickListener(this);

        refreshDbLists(null);

        ListPreference listCategory = preferencesFragment.findPreference(StaticData.PREF_KEY_WITHDRAWAL_CATEGORY);
        listCategory.setEntries(getAllCategoriesEntries());
        listCategory.setEntryValues(getAllCategoriesEntryValues());

        ListPreference listCurrency = preferencesFragment.findPreference(StaticData.PREF_KEY_MAIN_CURRENCY);
        listCurrency.setOnPreferenceChangeListener((preference, newValue) -> {
            StaticData.setMainCurrency(Integer.parseInt((String) newValue));
            // we should update all currencies to reflect the change
            Cursor c = new Currency().fetchAll();
            c.moveToFirst();
            Currency[] allCurrencies = new Currency[c.getCount()];
            int i = 0;
            do {
                Currency cur = new Currency();
                cur.toDTO(c);
                allCurrencies[i++] = cur;
            } while (c.moveToNext());

            OpenExchangeRatesAsyncUpdater rateUpdater = new OpenExchangeRatesAsyncUpdater(preferencesFragment.requireActivity(), StaticData.getPreferenceValueString(StaticData.PREF_KEY_OER_API_KEY));
            rateUpdater.execute(allCurrencies);
            return true;
        });
        listCurrency.setEntries(getAllCurrenciesEntries());
        listCurrency.setEntryValues(getAllCurrenciesEntryValues());
        if (StaticData.getMainCurrency() != null && StaticData.getMainCurrency().getId() != null) {
            listCurrency.setValue(String.valueOf(StaticData.getMainCurrency().getId()));
        }

        EditTextPreference editTextPreference = preferencesFragment.findPreference(StaticData.PREF_KEY_OER_API_KEY);
        editTextPreference.setOnPreferenceChangeListener(this);
        editTextPreference.setOnPreferenceClickListener(this);

        /*
         * drive and backup options
         */
        PreferenceManager // Ensuring fields is updated when backup alarm is triggering while inside preferences
                .getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
                    if (key.equals(StaticData.KEY_AUTOMATIC_BACKUP_NEXT_ALARM_DATE_TIME)) {
                        setDriveAutomaticBackupSummary(StaticData.getPreferenceValueInt(StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY));
                    }
                });

        SwitchPreference driveActivationSwitch = preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_ACTIVATE);
        driveActivationSwitch.setOnPreferenceClickListener(onPreferenceClickDriveActivate());
        if (driveActivationSwitch.isChecked()) {
            setEnableDriveBackupPrefs(true);
        } else {
            setEnableDriveBackupPrefs(false);
        }

        // Force retention edit text to allow only numbers
        EditTextPreference etpDriveRetention = preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_RETENTION);
        etpDriveRetention.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
        etpDriveRetention.setSummary(getString(R.string.pref_drive_retention_summary) + " (" + StaticData.getPreferenceValueString(StaticData.PREF_KEY_DRIVE_RETENTION) + " " + getString(R.string.days) + ")");
        etpDriveRetention.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(getString(R.string.pref_drive_retention_summary) + " (" + newValue + " " + getString(R.string.days) + ")");
            return true;
        });

        CheckBoxPreference cbpDriveDeleteOld = preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_DELETE_OLD);
        cbpDriveDeleteOld.setOnPreferenceChangeListener((preference, newValue) -> {
            preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_RETENTION).setEnabled((boolean) newValue);
            return true;
        });

        Preference driveUpload = preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_MANUAL_BACKUP);
        driveUpload.setOnPreferenceClickListener(onPreferenceClickDriveUpload());

        Preference automaticBackupsBootNotifPreference = preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_AUTOMATIC_BACKUP_BOOT_NOTIFICATION);
        boolean isNever = (StaticData.getPreferenceValueInt(StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY) == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_NEVER);
        automaticBackupsBootNotifPreference.setEnabled(driveActivationSwitch.isChecked() && !isNever);

        Preference automaticBackupsPreference = preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_AUTOMATIC_BACKUP);
        setDriveAutomaticBackupSummary(StaticData.getPreferenceValueInt(StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY));
        automaticBackupsPreference.setOnPreferenceClickListener(preference -> {
            DriveAutomaticBackupBottomSheetDialog dlg = new DriveAutomaticBackupBottomSheetDialog((v, dialog, opt) -> {
                TmhApplication.alarmManagerHelper.registerDriveBackupAlarm();
                setDriveAutomaticBackupSummary(opt);
                automaticBackupsBootNotifPreference.setEnabled(opt != StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_NEVER);
                dialog.dismiss();
            });
            dlg.show(getSupportFragmentManager(), "ModalPrefAutomaticBackupDialog");
            return true;
        });

        DriveRestoreDialogPreference driveRestoreDialogPreference = preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_RESTORE);
        driveRestoreDialogPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            refreshDbLists(null);
            return true;
        });

        // google sign in initialization
        // requesting Drive scope that can create/update/delete files (DriveScopes.DRIVE)
        // Tried with DRIVE_FILE (only our own app) but in this case, it doesn't work with shared folders...
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE))
                .build();
        googleSignInClient = GoogleSignIn.getClient(preferencesFragment.requireContext(), gso);
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(preferencesFragment.requireContext());
        if (googleSignInAccount != null) {
            PreferenceCategory c = preferencesFragment.findPreference(StaticData.PREF_KEY_BACKUP_CATEGORY);
            c.setTitle(preferencesFragment.getString(R.string.pref_backup_category) + " (" + googleSignInAccount.getEmail() + ")");
            driveActivationSwitch.setChecked(true);
        }

        googleSignInStartForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                        setEnableDriveBackupPrefs(true);
                    } else {
                        ((SwitchPreference) preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_ACTIVATE)).setChecked(false);
                        setEnableDriveBackupPrefs(false);
                    }
                });


    }

    private void setEnableDriveBackupPrefs(boolean isActivated) {
        preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_AUTOMATIC_BACKUP).setEnabled(isActivated);
        preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_AUTOMATIC_BACKUP_BOOT_NOTIFICATION).setEnabled(isActivated);
        preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_BACKUP_FOLDER).setEnabled(isActivated);
        preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_MANUAL_BACKUP).setEnabled(isActivated);
        CheckBoxPreference cbpDeleteOld = preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_DELETE_OLD);
        cbpDeleteOld.setEnabled(isActivated);
        preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_RETENTION).setEnabled(isActivated && cbpDeleteOld.isChecked());
        preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_RESTORE).setEnabled(isActivated);
    }

    private void setDriveAutomaticBackupSummary(int value) {
        Preference pref = preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_AUTOMATIC_BACKUP);
        String summary;
        switch (value) {
            case StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_TEST:
                summary = "test";
                break;
            case StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_DAILY:
                summary = getString(R.string.dialog_prefs_drive_automatic_backup_option_daily);
                break;
            case StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_WEEKLY:
                summary = getString(R.string.dialog_prefs_drive_automatic_backup_option_weekly);
                break;
            case StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_MONTHLY:
                summary = getString(R.string.dialog_prefs_drive_automatic_backup_option_monthly);
                break;
            default:
                summary = getString(R.string.dialog_prefs_drive_automatic_backup_option_never);
        }

        if (TmhApplication.alarmManagerHelper.getNextAlarmDateTime() != null) {
            summary += " (" + TmhApplication.alarmManagerHelper.getNextAlarmDateTimeAsString() + ")";
        }
        pref.setSummary(summary);
    }

    private void refreshDbLists(String currentDB) {
        String curDB = currentDB;
        if (curDB == null) {
            curDB = PreferencesActivity.getStringValue(StaticData.PREF_KEY_DATABASE);
        }
        ListPreference listDatabases = preferencesFragment.findPreference(StaticData.PREF_KEY_DATABASE);
        listDatabases.setEntries(DatabaseHelper.getAllDatabasesListEntries());
        listDatabases.setEntryValues(DatabaseHelper.getAllDatabasesListEntryValues());
        listDatabases.setValue(curDB);

        MultiSelectListPreference listDatabasesForDeletion = preferencesFragment.findPreference(StaticData.PREF_KEY_MANAGE_DB);
        listDatabasesForDeletion.setValues(new HashSet<>());
        listDatabasesForDeletion.setEntries(DatabaseHelper.getAllDatabasesListEntries(curDB));
        listDatabasesForDeletion.setEntryValues(DatabaseHelper.getAllDatabasesListEntryValues(curDB));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void resetToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        TmhApplication.getDatabaseHelper().close();
        super.onDestroy();
    }

    @NonNull
    private CharSequence[] getAllCategoriesEntries() {
        CharSequence[] ret;

        Category dummy = new Category();
        Cursor cursor = dummy.fetchAll();
        cursor.moveToFirst();

        ret = new CharSequence[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            int idxName = cursor.getColumnIndexOrThrow(CategoryData.KEY_NAME);
            ret[i] = cursor.getString(idxName);
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

    @NonNull
    private CharSequence[] getAllCategoriesEntryValues() {
        CharSequence[] ret;

        Category dummy = new Category();
        Cursor cursor = dummy.fetchAll();
        cursor.moveToFirst();

        ret = new CharSequence[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            int idxId = cursor.getColumnIndexOrThrow(CategoryData.KEY_ID);
            //int idxName = cursor.getColumnIndexOrThrow(CategoryData.KEY_NAME);
            ret[i] = String.valueOf(cursor.getInt(idxId));
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

    @NonNull
    private CharSequence[] getAllCurrenciesEntries() {
        CharSequence[] ret;

        Currency dummy = new Currency();
        Cursor cursor = dummy.fetchAll();
        cursor.moveToFirst();

        ret = new CharSequence[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            int idxName = cursor.getColumnIndexOrThrow(CurrencyData.KEY_LONG_NAME);
            ret[i] = cursor.getString(idxName);
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

    @NonNull
    private CharSequence[] getAllCurrenciesEntryValues() {
        CharSequence[] ret;

        Currency dummy = new Currency();
        Cursor cursor = dummy.fetchAll();
        cursor.moveToFirst();

        ret = new CharSequence[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            int idxId = cursor.getColumnIndexOrThrow(CurrencyData.KEY_ID);
            ret[i] = String.valueOf(cursor.getInt(idxId));
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        if (StaticData.PREF_KEY_NEW_DATABASE.equals(preference.getKey())) {
            ((EditTextPreference) preference).setText("");
        } else if (StaticData.PREF_KEY_RENAME_DATABASE.equals(preference.getKey())) {
            ((EditTextPreference) preference).setText(TmhApplication.getDatabaseHelper().getCurrentDbName());
        }

        return true;
    }

    @NonNull
    @Contract(pure = true)
    private Preference.OnPreferenceClickListener onPreferenceClickDriveActivate() {
        return preference -> {
            SwitchPreference pref = (SwitchPreference) preference;
            if (pref.isChecked()) {
                // pop sign in intent up
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInStartForResult.launch(signInIntent);
            } else {
                if (googleSignInAccount != null) {
                    googleSignInClient.signOut().addOnCompleteListener(command -> {
                        Snackbar.make(
                                preferencesFragment.requireContext(),
                                preferencesFragment.requireView(),
                                preferencesFragment.getString(R.string.pref_google_signout_successfully) + " (" + googleSignInAccount.getEmail() + ")",
                                Snackbar.LENGTH_LONG).show();

                        PreferenceCategory c = preferencesFragment.findPreference(StaticData.PREF_KEY_BACKUP_CATEGORY);
                        c.setTitle(R.string.pref_backup_category);
                        setEnableDriveBackupPrefs(false);
                    });
                }
                // Disable and cancel alarm as we are not signed in anymore
                TmhApplication.alarmManagerHelper.disableAndCancelAlarm();
            }
            return true;
        };
    }

    @NonNull
    @Contract(pure = true)
    private Preference.OnPreferenceClickListener onPreferenceClickDriveUpload() {
        return preference -> {
            Context context = preferencesFragment.requireContext();
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);

            if (googleSignInAccount == null)
                return true;

            GoogleAccountCredential googleAccountCredential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE));
            googleAccountCredential.setSelectedAccount(googleSignInAccount.getAccount());

            NetHttpTransport netHttpTransport = null;
            try {
                netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
            Drive googleDriveService = new Drive.Builder(
                    netHttpTransport,
                    GsonFactory.getDefaultInstance(),
                    googleAccountCredential)
                    .setApplicationName(TmhApplication.APP_NAME)
                    .build();

            AsyncActivityRefresher refresher = new AsyncActivityRefresher(preferencesFragment.requireActivity(), new IAsyncActivityRefresher() {
                @Override
                public Map<Integer, Object> handleRefreshBackground(AbstractAsyncTask asyncTask) {
                    // Getting all DBs
                    CharSequence[] listDbNames = DatabaseHelper.getAllDatabases(false);
                    BackupDbFileHelper[] listDbPath = new BackupDbFileHelper[listDbNames.length];
                    for (int i = 0; i < listDbNames.length; i++) {
                        listDbPath[i] = new BackupDbFileHelper(
                                DatabaseHelper.getDbAbsolutePath(
                                        preferencesFragment.requireContext(), listDbNames[i].toString()));
                    }

                    String folderNamePref = ((EditTextPreference) preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_BACKUP_FOLDER)).getText();
                    String retentionPref = ((EditTextPreference) preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_RETENTION)).getText();
                    boolean deleteOldPref = ((CheckBoxPreference) preferencesFragment.findPreference(StaticData.PREF_KEY_DRIVE_DELETE_OLD)).isChecked();
                    int retentionDurationDays = 0;
                    try {
                        retentionDurationDays = Integer.parseInt(retentionPref);
                    } catch (NumberFormatException nfe) {
                        //
                    } finally {
                        if (retentionDurationDays == 0)
                            retentionDurationDays = Integer.parseInt(context.getString(R.string.pref_drive_retention_default));
                    }
                    try {
                        DriveServiceHelper.upload(
                                        googleDriveService,
                                        asyncTask,
                                        folderNamePref == null || "".equals(folderNamePref) ? context.getString(R.string.pref_drive_default_folder) : folderNamePref,
                                        listDbPath,
                                        deleteOldPref ? retentionDurationDays : 0)
                                .addOnSuccessListener(file -> {
                                    if (file != null)
                                        Snackbar.make(preferencesFragment.requireView(), context.getString(R.string.pref_upload_file_to_drive_success), Snackbar.LENGTH_LONG).show();
                                    else
                                        Snackbar.make(preferencesFragment.requireView(), context.getString(R.string.pref_upload_file_to_drive_error), Snackbar.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> Snackbar.make(preferencesFragment.requireView(), context.getString(R.string.pref_upload_file_to_drive_success) + "(" + e + ")", Snackbar.LENGTH_LONG).show());
                    } catch (IOException e) {
                        e.printStackTrace();
                        String err = context.getString(R.string.pref_upload_file_to_drive_error) + " (" + e.getMessage() + ")";
                        Snackbar.make(
                                preferencesFragment.requireContext(),
                                preferencesFragment.requireView(),
                                err,
                                Snackbar.LENGTH_LONG).show();
                    }
                    return null;
                }

                @Override
                public void handleRefreshEnding(Map<Integer, Object> results) {

                }
            }, true, false, false);
            refresher.execute();

            return true;
        };
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        if (StaticData.PREF_KEY_DATABASE.equals(preference.getKey())) {
            TmhLogger.d(TAG, "Opening database " + newValue);
            TmhApplication.changeOrCreateDatabase((String) newValue);
            Currency cur = StaticData.getDefaultMainCurrency();
            if (cur != null)
                StaticData.setMainCurrency(cur.getId());
            StaticPrefs.loadCurrentStaticPrefs();
        } else if (StaticData.PREF_KEY_NEW_DATABASE.equals(preference.getKey())) {
            if (!"".equals(newValue)) {
                TmhLogger.d(TAG, "Creating new database " + newValue);

                String dbName = DatabaseHelper.DATABASE_PREFIX + (newValue);

                // creating a new DB
                TmhApplication.changeOrCreateDatabase(dbName);

                // Resetting edit text to empty string
                refreshDbLists(null);
                ListPreference dbPref = preferencesFragment.findPreference(StaticData.PREF_KEY_DATABASE);
                dbPref.setValue(dbName);
            }
        } else if (StaticData.PREF_KEY_RENAME_DATABASE.equals(preference.getKey())) {
            try {
                TmhApplication.getDatabaseHelper().renameCurrentDatabase((String) newValue);
                TmhApplication.changeOrCreateDatabase((String) newValue, false);

                // Reload like a new db has been chosen
                ListPreference listDatabases = preferencesFragment.findPreference(StaticData.PREF_KEY_DATABASE);
                StaticData.setPreferenceValueString(StaticData.PREF_KEY_DATABASE, DatabaseHelper.DATABASE_PREFIX + (String) newValue);
                refreshDbLists(null);
                onPreferenceChange(listDatabases, newValue);
                Snackbar.make(preferencesFragment.requireContext(),
                                preferencesFragment.requireView(),
                                getString(R.string.database_renamed_successfully),
                                Snackbar.LENGTH_LONG)
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(preferencesFragment.requireContext(),
                                preferencesFragment.requireView(),
                                getString(R.string.error) + ": " + e.getMessage(),
                                Snackbar.LENGTH_LONG)
                        .show();
            }
        } else if (StaticData.PREF_KEY_OER_API_KEY.equals(preference.getKey())) {
            try {
                if (!"".equals(newValue) && AbstractOpenExchangeRates.isValidApiKey(this, (String) newValue)) {
                    // Set value
                    StaticData.setPreferenceValueString(StaticData.PREF_KEY_OER_API_KEY, (String) newValue);
                    StaticData.setPreferenceValueBoolean(StaticData.PREF_OER_API_KEY_VALID, true);
                } else {
                    // not valid
                    StaticData.setPreferenceValueBoolean(StaticData.PREF_OER_API_KEY_VALID, false);
                    SimpleDialog.errorDialog(this, getString(R.string.error), getString(R.string.error_oer_apikey_invalid), (dialog, which) -> dialog.dismiss()).show();
                }
            } catch (IOException ioe) {
                // Error, cannot verify api key - network issue
                StaticData.setPreferenceValueBoolean(StaticData.PREF_OER_API_KEY_VALID, false);
                SimpleDialog.errorDialog(this,
                                getString(R.string.error),
                                getString(R.string.error_network_issue) + " err=" + ioe.getMessage(),
                                (dialog, which) -> dialog.dismiss())
                        .show();
            } catch (Exception e) {
                // Error, cannot verify api key - network issue
                StaticData.setPreferenceValueBoolean(StaticData.PREF_OER_API_KEY_VALID, false);
                SimpleDialog.errorDialog(this,
                                getString(R.string.error),
                                getString(R.string.error) + " err=" + e.getMessage(),
                                (dialog, which) -> dialog.dismiss())
                        .show();
            }
        } else if (StaticData.PREF_KEY_MANAGE_DB.equals(preference.getKey())) {
            HashSet<String> dbList = (HashSet<String>) newValue;
            if (dbList.size() > 0) {
                Iterator<String> dbListIt = dbList.iterator();
                StringBuilder sb = new StringBuilder(getString(R.string.manageable_obj_del_confirm_question)).append(" (");
                while (dbListIt.hasNext()) {
                    sb.append(DatabaseHelper.stripDatabaseFileName(dbListIt.next()));
                    if (dbListIt.hasNext())
                        sb.append(", ");
                }
                sb.append(")");

                SimpleDialog.confirmDialog(this,
                        sb.toString(),
                        (dialog, which) -> {
                            Iterator<String> it = dbList.iterator();
                            while (it.hasNext()) {
                                TmhApplication.getAppContext().deleteDatabase(it.next());
                            }
                            refreshDbLists(null);
                            dialog.dismiss();
                            Snackbar.make(
                                    findViewById(android.R.id.content),
                                    R.string.databases_deleted_successfully,
                                    Snackbar.LENGTH_LONG).show();
                        }).show();
            }
        }

        // Reset stuff
        if (StaticData.PREF_KEY_DATABASE.equals(preference.getKey()) ||
                StaticData.PREF_KEY_NEW_DATABASE.equals(preference.getKey())) {
            StaticData.invalidateCurrentAccount();
            resetToHome();
        } else if (StaticData.PREF_KEY_MANAGE_DB.equals(preference.getKey())) {
            // Update categories for this db
            ListPreference catPref = preferencesFragment.findPreference(StaticData.PREF_KEY_WITHDRAWAL_CATEGORY);
            catPref.setEntries(getAllCategoriesEntries());
            catPref.setEntryValues(getAllCategoriesEntryValues());

            // At this stage the StaticData should be loaded from db
            // Selecting correct value or null if undefined
            CharSequence wcIndex;
            if (StaticData.getWithdrawalCategory() != null && StaticData.getWithdrawalCategory().getId() != null) {
                wcIndex = String.valueOf(StaticData.getWithdrawalCategory().getId());
                catPref.setValue(wcIndex.toString());
            } else {
                catPref.setValue(null);
            }

            // currentDB preference is not yet applied at this stage so we need to get it from newValue
            // if we just changed it
            String curDB = PreferencesActivity.getStringValue(StaticData.PREF_KEY_DATABASE);
            if (StaticData.PREF_KEY_DATABASE.equals(preference.getKey()))
                curDB = (String) newValue;
            refreshDbLists(curDB);

        }

        return true;
    }

    public static String getStringValue(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TmhApplication.getAppContext());
        return sharedPreferences.getString(key, "");
    }

    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            googleSignInAccount = completedTask.getResult(ApiException.class);
            if (googleSignInAccount != null) {
                Snackbar.make(
                        preferencesFragment.requireContext(),
                        preferencesFragment.requireView(),
                        preferencesFragment.getString(R.string.pref_google_signin_successfully) + " (" + googleSignInAccount.getEmail() + ")",
                        Snackbar.LENGTH_LONG).show();

                // Change category title
                PreferenceCategory c = preferencesFragment.findPreference(StaticData.PREF_KEY_BACKUP_CATEGORY);
                c.setTitle(preferencesFragment.getString(R.string.pref_backup_category) + " (" + googleSignInAccount.getEmail() + ")");

                // Set next alarm if needed
                TmhApplication.alarmManagerHelper.registerDriveBackupAlarm();

                // Set summary for automatic backup is needed
                setDriveAutomaticBackupSummary(StaticData.getPreferenceValueInt(StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY));
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG.getName(), "signInResult:failed code=" + e.getStatusCode());
            Snackbar.make(
                    preferencesFragment.requireContext(),
                    preferencesFragment.requireView(),
                    preferencesFragment.getString(R.string.error) + " err=" + e.getMessage(),
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
