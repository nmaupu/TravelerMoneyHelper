package org.maupu.android.tmh;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.StaticPrefs;
import org.maupu.android.tmh.databinding.ActivityPreferencesBinding;
import org.maupu.android.tmh.dialog.ImportDBDialogPreference;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AbstractOpenExchangeRates;
import org.maupu.android.tmh.ui.async.AsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.DriveServiceHelper;
import org.maupu.android.tmh.util.ImportExportUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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


        EditTextPreference newDatabase = preferencesFragment.findPreference(StaticData.PREF_NEW_DATABASE);
        newDatabase.setOnPreferenceChangeListener(this);
        newDatabase.setOnPreferenceClickListener(this);

        ListPreference listDatabases = preferencesFragment.findPreference(StaticData.PREF_DATABASE);
        listDatabases.setOnPreferenceClickListener(this);
        listDatabases.setOnPreferenceChangeListener(this);

        String currentDB = PreferencesActivity.getStringValue(StaticData.PREF_DATABASE);
        MultiSelectListPreference listDatabasesForDeletion = preferencesFragment.findPreference(StaticData.PREF_MANAGE_DB);
        listDatabasesForDeletion.setOnPreferenceChangeListener(this);
        listDatabasesForDeletion.setOnPreferenceClickListener(this);

        refreshDbLists(null);

        ListPreference listCategory = preferencesFragment.findPreference(StaticData.PREF_WITHDRAWAL_CATEGORY);
        listCategory.setOnPreferenceClickListener(this);
        listCategory.setEntries(getAllCategoriesEntries());
        listCategory.setEntryValues(getAllCategoriesEntryValues());

        EditTextPreference editTextPreference = preferencesFragment.findPreference(StaticData.PREF_OER_EDIT);
        editTextPreference.setOnPreferenceChangeListener(this);
        editTextPreference.setOnPreferenceClickListener(this);

        EditTextPreference editTextExportDb = preferencesFragment.findPreference(StaticData.PREF_EXPORT_DB);
        editTextExportDb.setOnPreferenceChangeListener(this);
        editTextExportDb.setOnPreferenceClickListener(this);

        ImportDBDialogPreference importDBDialogPreference = preferencesFragment.findPreference(StaticData.PREF_IMPORT_DB);
        importDBDialogPreference.setOnPreferenceChangeListener(this);
        importDBDialogPreference.setOnPreferenceClickListener(this);

        SwitchPreference driveActivationSwitch = preferencesFragment.findPreference(StaticData.PREF_DRIVE_ACTIVATE);
        driveActivationSwitch.setOnPreferenceClickListener(this);

        Preference driveUpload = preferencesFragment.findPreference("drive_upload");
        driveUpload.setOnPreferenceClickListener(
                preference -> {
                    GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                    if (googleSignInAccount != null) {

                        GoogleAccountCredential googleAccountCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
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
                            public Map<Integer, Object> handleRefreshBackground() {
                                String filepath = "/data/data/org.maupu.android.tmh/files/yala.png";
                                DriveServiceHelper.upload(googleDriveService, filepath)
                                        .addOnSuccessListener(file -> Snackbar.make(preferencesFragment.requireView(), "File uploaded successfully !", Snackbar.LENGTH_LONG).show())
                                        .addOnFailureListener(e -> Snackbar.make(preferencesFragment.requireView(), "Fail to upload file ! err=" + e, Snackbar.LENGTH_LONG).show());
                                return null;
                            }

                            @Override
                            public void handleRefreshEnding(Map<Integer, Object> results) {

                            }
                        }, true);
                        refresher.execute();

                    }
                    return true;
                });

        // google sign in initialization
        // requesting Drive scope that can create/update/delete our own files only (DriveScopes.DRIVE_FILE)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        googleSignInClient = GoogleSignIn.getClient(preferencesFragment.requireContext(), gso);
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(preferencesFragment.requireContext());
        if (googleSignInAccount != null) {
            PreferenceCategory c = preferencesFragment.findPreference(StaticData.PREF_BACKUP_CATEGORY);
            c.setTitle(preferencesFragment.getString(R.string.pref_backup_category) + " (" + googleSignInAccount.getEmail() + ")");
            driveActivationSwitch.setChecked(true);
        }

        googleSignInStartForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    } else {
                        ((SwitchPreference) preferencesFragment.findPreference(StaticData.PREF_DRIVE_ACTIVATE)).setChecked(false);
                    }
                });
    }

    private void refreshDbLists(String currentDB) {
        String curDB = currentDB;
        if (curDB == null) {
            curDB = PreferencesActivity.getStringValue(StaticData.PREF_DATABASE);
        }
        ListPreference listDatabases = preferencesFragment.findPreference(StaticData.PREF_DATABASE);
        listDatabases.setEntries(DatabaseHelper.getAllDatabasesListEntries());
        listDatabases.setEntryValues(DatabaseHelper.getAllDatabasesListEntryValues());
        listDatabases.setValue(PreferencesActivity.getStringValue(StaticData.PREF_DATABASE));

        MultiSelectListPreference listDatabasesForDeletion = preferencesFragment.findPreference(StaticData.PREF_MANAGE_DB);
        listDatabasesForDeletion.setValues(new HashSet<>());
        listDatabasesForDeletion.setEntries(DatabaseHelper.getAllDatabasesListEntries(DatabaseHelper.DEFAULT_DATABASE_NAME, curDB));
        listDatabasesForDeletion.setEntryValues(DatabaseHelper.getAllDatabasesListEntryValues(DatabaseHelper.DEFAULT_DATABASE_NAME, curDB));
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

    private CharSequence[] getAllCategoriesEntries() {
        CharSequence[] ret;

        Category dummy = new Category();
        Cursor cursor = dummy.fetchAll();
        cursor.moveToFirst();

        ret = new CharSequence[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            //int idxId = cursor.getColumnIndexOrThrow(CategoryData.KEY_ID);
            int idxName = cursor.getColumnIndexOrThrow(CategoryData.KEY_NAME);
            ret[i] = cursor.getString(idxName);
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (StaticData.PREF_NEW_DATABASE.equals(preference.getKey())) {
            ((EditTextPreference) preference).setText("");
        } else if (StaticData.PREF_EXPORT_DB.equals(preference.getKey())) {
            Calendar cal = Calendar.getInstance();
            String dateString = DateUtil.dateToStringForFilename(cal.getTime());
            String filename = new StringBuilder(DatabaseHelper.getPreferredDatabaseName())
                    .append("-")
                    .append(dateString)
                    .append(".db")
                    .toString();
            ((EditTextPreference) preference).setText(filename);
        } else if (StaticData.PREF_DRIVE_ACTIVATE.equals(preference.getKey())) {
            SwitchPreference pref = (SwitchPreference) preference;
            if (pref.isChecked()) {
                // pop sign in intent up
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInStartForResult.launch(signInIntent);

                // TODO display more options
            } else {
                if (googleSignInAccount != null) {
                    googleSignInClient.signOut().addOnCompleteListener(command -> {
                        Snackbar.make(
                                preferencesFragment.requireContext(),
                                preferencesFragment.requireView(),
                                preferencesFragment.getString(R.string.pref_google_signout_successfully) + " (" + googleSignInAccount.getEmail() + ")",
                                Snackbar.LENGTH_LONG).show();

                        PreferenceCategory c = preferencesFragment.findPreference(StaticData.PREF_BACKUP_CATEGORY);
                        c.setTitle(R.string.pref_backup_category);
                    });
                }
            }
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (StaticData.PREF_DATABASE.equals(preference.getKey())) {
            TmhLogger.d(TAG, "Opening database " + newValue);
            TmhApplication.changeOrCreateDatabase((String) newValue);
            Currency cur = StaticData.getDefaultMainCurrency();
            if (cur != null)
                StaticData.setMainCurrency(cur.getId());
            StaticPrefs.loadCurrentStaticPrefs();
        } else if (StaticData.PREF_NEW_DATABASE.equals(preference.getKey())) {
            if (!"".equals(newValue)) {
                TmhLogger.d(TAG, "Creating new database " + newValue);

                String dbName = DatabaseHelper.DATABASE_PREFIX + (newValue);

                // creating a new DB
                TmhApplication.changeOrCreateDatabase(dbName);

                // Resetting edit text to empty string
                refreshDbLists(null);
                ListPreference dbPref = preferencesFragment.findPreference(StaticData.PREF_DATABASE);
                dbPref.setValue(dbName);
            }
        } else if (StaticData.PREF_OER_EDIT.equals(preference.getKey())) {
            try {
                if (!"".equals((String) newValue) && AbstractOpenExchangeRates.isValidApiKey(this, (String) newValue)) {
                    // Set value
                    StaticData.setPreferenceValueString(StaticData.PREF_OER_EDIT, (String) newValue);
                    StaticData.setPreferenceValueBoolean(StaticData.PREF_OER_VALID, true);
                } else {
                    // not valid
                    StaticData.setPreferenceValueBoolean(StaticData.PREF_OER_VALID, false);
                    SimpleDialog.errorDialog(this, getString(R.string.error), getString(R.string.error_oer_apikey_invalid), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            } catch (IOException ioe) {
                // Error, cannot verify api key - network issue
                StaticData.setPreferenceValueBoolean(StaticData.PREF_OER_VALID, false);
                SimpleDialog.errorDialog(this, getString(R.string.error), getString(R.string.error_network_issue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        } else if (StaticData.PREF_EXPORT_DB.equals(preference.getKey())) {
            TmhLogger.d(TAG, "Calling export db " + newValue);
            if (!ImportExportUtil.exportCurrentDatabase(newValue.toString(), findViewById(android.R.id.content))) {
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.database_export_failed) + " [" + newValue + "]",
                        Snackbar.LENGTH_LONG).show();
            }
        } else if (StaticData.PREF_IMPORT_DB.equals(preference.getKey())) {
            // After importing the db, reload it
            String db = DatabaseHelper.DATABASE_PREFIX + (newValue);
            TmhApplication.changeOrCreateDatabase(db);

            // Resetting edit text to empty string
            ListPreference dbPref = preferencesFragment.findPreference(StaticData.PREF_DATABASE);
            dbPref.setEntries(DatabaseHelper.getAllDatabasesListEntries());
            dbPref.setEntryValues(DatabaseHelper.getAllDatabasesListEntryValues());
            dbPref.setValue(db);

            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.database_created_successfuly) + " [" + newValue + "]",
                    Snackbar.LENGTH_LONG).show();
        } else if (StaticData.PREF_MANAGE_DB.equals(preference.getKey())) {
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
                                    R.string.databases_deleted_successfuly,
                                    Snackbar.LENGTH_LONG).show();
                        }).show();
            }
        }

        // Reset stuff
        if (StaticData.PREF_DATABASE.equals(preference.getKey()) ||
                StaticData.PREF_NEW_DATABASE.equals(preference.getKey()) ||
                StaticData.PREF_IMPORT_DB.equals(preference.getKey())) {
            StaticData.invalidateCurrentAccount();
            resetToHome();
        } else if (StaticData.PREF_MANAGE_DB.equals(preference.getKey())) {
            // Update categories for this db
            ListPreference catPref = preferencesFragment.findPreference(StaticData.PREF_WITHDRAWAL_CATEGORY);
            catPref.setEntries(getAllCategoriesEntries());
            catPref.setEntryValues(getAllCategoriesEntryValues());

            // At this stage the StaticData should be loaded from db
            // Selecting correct value or null if undefined
            CharSequence wcIndex = null;
            if (StaticData.getWithdrawalCategory() != null && StaticData.getWithdrawalCategory().getId() != null) {
                wcIndex = String.valueOf(StaticData.getWithdrawalCategory().getId());
                catPref.setValue(wcIndex.toString());
            } else {
                catPref.setValue(null);
            }

            // currentDB preference is not yet applied at this stage so we need to get it from newValue
            // if we just changed it
            String curDB = PreferencesActivity.getStringValue(StaticData.PREF_DATABASE);
            if (StaticData.PREF_DATABASE.equals(preference.getKey()))
                curDB = (String) newValue;
            refreshDbLists(curDB);

        }

        return true;
    }

    public static String getStringValue(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TmhApplication.getAppContext());
        return sharedPreferences.getString(key, "");
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            googleSignInAccount = completedTask.getResult(ApiException.class);
            if (googleSignInAccount != null) {
                Snackbar.make(
                        preferencesFragment.requireContext(),
                        preferencesFragment.requireView(),
                        preferencesFragment.getString(R.string.pref_google_signin_successfully) + " (" + googleSignInAccount.getEmail() + ")",
                        Snackbar.LENGTH_LONG).show();

                // Change category title
                PreferenceCategory c = preferencesFragment.findPreference(StaticData.PREF_BACKUP_CATEGORY);
                c.setTitle(preferencesFragment.getString(R.string.pref_backup_category) + " (" + googleSignInAccount.getEmail() + ")");
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG.getName(), "signInResult:failed code=" + e.getStatusCode());
        }
    }
}
