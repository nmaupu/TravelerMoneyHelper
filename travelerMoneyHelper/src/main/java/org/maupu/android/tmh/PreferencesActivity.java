package org.maupu.android.tmh;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.StaticPrefs;
import org.maupu.android.tmh.databinding.ActivityPreferencesBinding;
import org.maupu.android.tmh.dialog.ImportDBDialogPreference;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AbstractOpenExchangeRates;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.ImportExportUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

public class PreferencesActivity extends AppCompatActivity implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final Class<PreferencesActivity> TAG = PreferencesActivity.class;
    //private static final String FILENAME = "mainPrefs";
    private boolean dbChanged = false;

    private ActivityPreferencesBinding binding;
    private PreferencesFragment preferencesFragment;

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
            if (this.dbChanged) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                this.finish();
            } else {
                onBackPressed();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        TmhApplication.getDatabaseHelper().close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (dbChanged) {
            // if db changed, restart from dashboard
            startActivity(new Intent(this, TmhApplication.HOME_ACTIVITY_CLASS));
        }

        super.onPause();
    }

    private CharSequence[] getAllAccountsEntryValues() {
        CharSequence[] ret;
        Account dummy = new Account();
        Cursor cursor = dummy.fetchAll();
        cursor.moveToFirst();

        ret = new CharSequence[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
            //int idxName = cursor.getColumnIndexOrThrow(AccountData.KEY_NAME);
            ret[i] = String.valueOf(cursor.getInt(idxId));
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

    private CharSequence[] getAllAccountEntries() {
        CharSequence[] ret;
        Account dummy = new Account();
        Cursor cursor = dummy.fetchAll();
        cursor.moveToFirst();

        ret = new CharSequence[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            //int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
            int idxName = cursor.getColumnIndexOrThrow(AccountData.KEY_NAME);
            ret[i] = cursor.getString(idxName);
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
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

                Toast.makeText(
                        TmhApplication.getAppContext(),
                        getString(R.string.database_created_successfuly) + " [" + newValue + "]",
                        Toast.LENGTH_LONG).show();
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
            if (!ImportExportUtil.exportCurrentDatabase(newValue.toString())) {
                Toast.makeText(
                        TmhApplication.getAppContext(),
                        getString(R.string.database_export_failed) + " [" + newValue + "]",
                        Toast.LENGTH_LONG).show();
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

            Toast.makeText(
                    TmhApplication.getAppContext(),
                    getString(R.string.database_created_successfuly) + " [" + newValue + "]",
                    Toast.LENGTH_LONG).show();
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
                            Toast.makeText(TmhApplication.getAppContext(), R.string.databases_deleted_successfuly, Toast.LENGTH_LONG).show();
                        }).show();
            }
        }

        if (StaticData.PREF_DATABASE.equals(preference.getKey()) ||
                StaticData.PREF_NEW_DATABASE.equals(preference.getKey()) ||
                StaticData.PREF_IMPORT_DB.equals(preference.getKey()) ||
                StaticData.PREF_MANAGE_DB.equals(preference.getKey())) {
            dbChanged = true;
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
                SimpleDialog.errorDialog(this, getString(R.string.warning), getString(R.string.default_category_warning)).show();
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
}
