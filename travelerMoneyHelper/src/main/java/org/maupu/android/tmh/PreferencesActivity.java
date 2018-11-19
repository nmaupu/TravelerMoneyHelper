package org.maupu.android.tmh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.ImportPreference;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AbstractOpenExchangeRates;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.ImportExportUtil;
import org.maupu.android.tmh.util.TmhLogger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final Class TAG = PreferencesActivity.class;
	//private static final String FILENAME = "mainPrefs";
	private boolean dbChanged = false;

	private ImportPreference importPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);

		EditTextPreference newDatabase = (EditTextPreference)findPreference(StaticData.PREF_NEW_DATABASE);
		newDatabase.setOnPreferenceChangeListener(this);
		newDatabase.setOnPreferenceClickListener(this);

		ListPreference listDatabases = (ListPreference)findPreference(StaticData.PREF_DATABASE);
		listDatabases.setOnPreferenceClickListener(this);
		listDatabases.setOnPreferenceChangeListener(this);
		listDatabases.setEntries(getAllDatabasesListEntries());
		listDatabases.setEntryValues(getAllDatabasesListEntryValues());
		listDatabases.setValue(PreferencesActivity.getStringValue(StaticData.PREF_DATABASE));

		ListPreference listAccount = (ListPreference)findPreference(StaticData.PREF_DEF_ACCOUNT);
		listAccount.setOnPreferenceClickListener(this);
		listAccount.setEntries(getAllAccountEntries());
		listAccount.setEntryValues(getAllAccountsEntryValues());

		ListPreference listCategory = (ListPreference)findPreference(StaticData.PREF_WITHDRAWAL_CATEGORY);
		listCategory.setOnPreferenceClickListener(this);
		listCategory.setEntries(getAllCategoriesEntries());
		listCategory.setEntryValues(getAllCategoriesEntryValues());
		
		EditTextPreference editTextPreference = (EditTextPreference)findPreference(StaticData.PREF_OER_EDIT);
		editTextPreference.setOnPreferenceChangeListener(this);
		editTextPreference.setOnPreferenceClickListener(this);

        EditTextPreference editTextExportDb = (EditTextPreference)findPreference(StaticData.PREF_EXPORT_DB);
        editTextExportDb.setOnPreferenceChangeListener(this);
        editTextExportDb.setOnPreferenceClickListener(this);

        importPreference = (ImportPreference)findPreference(StaticData.PREF_IMPORT_DB);
        importPreference.setOnPreferenceChangeListener(this);
        importPreference.setParentActivity(this);
	}

    @Override
	protected void onDestroy() {
		TmhApplication.getDatabaseHelper().close();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		if(dbChanged) {
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
		for (int i=0; i<cursor.getCount(); i++) {
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
		for (int i=0; i<cursor.getCount(); i++) {
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
		for (int i=0; i<cursor.getCount(); i++) {
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
		for (int i=0; i<cursor.getCount(); i++) {
			int idxId = cursor.getColumnIndexOrThrow(CategoryData.KEY_ID);
			//int idxName = cursor.getColumnIndexOrThrow(CategoryData.KEY_NAME);
			ret[i] = String.valueOf(cursor.getInt(idxId));
			cursor.moveToNext();
		}

		cursor.close();
		return ret;
	}

    /**
     * Get either list of databases ready to be printed to user
     * or stored database filename depending on prettyPrint parameter.
     * @param prettyPrint Weather getting all databases ready to be printed to user or raw filenames
     * @return List of all available databases
     */
    private CharSequence[] getAllDatabases(boolean prettyPrint) {
        // For an obscure reason, databaseList() returns strange results on some device
        // such as [TravelerMoneyHelper_appdata, TravelerMoneyHelper_appdata_default, 0]
        // instead of [TravelerMoneyHelper_appdata_default]
        // What is 0 ? What is TravelerMoneyHelper_appdata ? who knows !
        CharSequence[] list = TmhApplication.getAppContext().databaseList();
        //CharSequence[] ret = new CharSequence[list.length];
        List<String> listEntries = new ArrayList<String>();

        TmhLogger.d(TAG, "Number of databases returned : " + list.length);
        for(int i=0; i<list.length; i++) {
            String[] vals = ((String)list[i]).split(DatabaseHelper.DATABASE_PREFIX);
            // If database name is not correct (no prefix), array is wrong so we denied this DB
            // We also discard journal DB
            if(vals.length == 2 && ! vals[1].contains("-journal")) {
                TmhLogger.d(TAG, "Database filename : " + list[i] + " - database name : " + vals[1]);
                if(prettyPrint)
                    listEntries.add(vals[1]);
                else
                    listEntries.add(list[i].toString());
            }
        }

        return listEntries.toArray(new String[0]);
    }

	private CharSequence[] getAllDatabasesListEntries() {
		return getAllDatabases(true);
	}

	private CharSequence[] getAllDatabasesListEntryValues() {
        return getAllDatabases(false);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(StaticData.PREF_NEW_DATABASE.equals(preference.getKey())) {
			((EditTextPreference)preference).setText("");
			((EditTextPreference)preference).getEditText().setText("");
		} else if(StaticData.PREF_OER_EDIT.equals(preference.getKey())) {
			((EditTextPreference)preference).getEditText().requestFocus();
		} else if(StaticData.PREF_EXPORT_DB.equals(preference.getKey())) {
            Calendar cal = Calendar.getInstance();
            String dateString = DateUtil.dateToStringForFilename(cal.getTime());
            String filename = new StringBuilder("tmh-").append(dateString).append(".db").toString();
            ((EditTextPreference)preference).getEditText().setText(filename);
            ((EditTextPreference)preference).getEditText().requestFocus();
        }

		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(StaticData.PREF_DATABASE.equals(preference.getKey())) {
			TmhLogger.d(TAG, "Opening database " + newValue);
			TmhApplication.changeOrCreateDatabase((String) newValue);
            Currency cur = StaticData.getDefaultMainCurrency();
            if(cur != null)
                StaticData.setMainCurrency(cur.getId());
		} else if (StaticData.PREF_NEW_DATABASE.equals(preference.getKey())) {
			if(! "".equals(newValue)) {
				TmhLogger.d(TAG, "Creating new database "+newValue);

				String dbName = DatabaseHelper.DATABASE_PREFIX+(newValue);

				// creating a new DB
				TmhApplication.changeOrCreateDatabase(dbName);

				// Resetting edit text to emtpy string
				ListPreference dbPref = (ListPreference)findPreference(StaticData.PREF_DATABASE);
				dbPref.setEntries(getAllDatabasesListEntries());
				dbPref.setEntryValues(getAllDatabasesListEntryValues());
				dbPref.setValue(dbName);

				Toast.makeText(
						TmhApplication.getAppContext(), 
						getString(R.string.database_created_successfuly)+" ["+newValue+"]", 
						Toast.LENGTH_LONG).show();
			}
		} else if (StaticData.PREF_OER_EDIT.equals(preference.getKey())) {
			try {
					if(! "".equals((String)newValue) && AbstractOpenExchangeRates.isValidApiKey(this, (String)newValue)) {
						// Set value
						StaticData.setPreferenceValueString(StaticData.PREF_OER_EDIT, (String)newValue);
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
            ImportExportUtil.exportCurrentDatabase(newValue.toString());
		} else if (StaticData.PREF_IMPORT_DB.equals(preference.getKey())) {
            // After importing the db, reload it
            String db = DatabaseHelper.DATABASE_PREFIX+(newValue);
            TmhApplication.changeOrCreateDatabase(db);

            // Resetting edit text to emtpy string
            ListPreference dbPref = (ListPreference)findPreference(StaticData.PREF_DATABASE);
            dbPref.setEntries(getAllDatabasesListEntries());
            dbPref.setEntryValues(getAllDatabasesListEntryValues());
            dbPref.setValue(db);

            Toast.makeText(
                    TmhApplication.getAppContext(),
                    getString(R.string.database_created_successfuly)+" ["+newValue+"]",
                    Toast.LENGTH_LONG).show();
        }

		if(StaticData.PREF_DATABASE.equals(preference.getKey()) ||
                StaticData.PREF_NEW_DATABASE.equals(preference.getKey()) ||
                StaticData.PREF_IMPORT_DB.equals(preference.getKey())) {
			dbChanged = true;
			// Update categories for this db 
			ListPreference catPref = (ListPreference)findPreference(StaticData.PREF_WITHDRAWAL_CATEGORY);
			catPref.setEntries(getAllCategoriesEntries());
			catPref.setEntryValues(getAllCategoriesEntryValues());
			catPref.setValue(null);
			
			SimpleDialog.errorDialog(this, getString(R.string.warning), getString(R.string.default_category_warning)).show();
		}

		return true;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                TmhLogger.d(TAG, "Uri: "+uri.toString());
                importPreference.getDbFilename().setText(uri.toString(), TextView.BufferType.EDITABLE);
                importPreference.setDatabaseUri(uri);
            }
        }
    }

    public static String getStringValue(String key) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TmhApplication.getAppContext());
		return sharedPreferences.getString(key, "");
	}
}
