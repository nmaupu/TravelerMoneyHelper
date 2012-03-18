package org.maupu.android.tmh;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
	//private static final String FILENAME = "mainPrefs";
	public static final String PREF_NEW_DATABASE = "new_database";
	public static final String PREF_DATABASE = "database";
	public static final String PREF_ACCOUNT = "account";
	public static final String PREF_CATEGORY = "category";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
		
		EditTextPreference newDatabase = (EditTextPreference)findPreference(PREF_NEW_DATABASE);
		newDatabase.setOnPreferenceChangeListener(this);
		newDatabase.setOnPreferenceClickListener(this);

		ListPreference listDatabases = (ListPreference)findPreference(PREF_DATABASE);
		listDatabases.setOnPreferenceClickListener(this);
		listDatabases.setOnPreferenceChangeListener(this);
		listDatabases.setEntries(getAllDatabasesListEntries());
		listDatabases.setEntryValues(getAllDatabasesListEntryValues());
		listDatabases.setValue(PreferencesActivity.getStringValue(PREF_DATABASE));

		ListPreference listAccount = (ListPreference)findPreference(PREF_ACCOUNT);
		listAccount.setOnPreferenceClickListener(this);
		listAccount.setEntries(getAllAccountEntries());
		listAccount.setEntryValues(getAllAccountsEntryValues());

		ListPreference listCategory = (ListPreference)findPreference(PREF_CATEGORY);
		listCategory.setOnPreferenceClickListener(this);
		listCategory.setEntries(getAllCategoriesEntries());
		listCategory.setEntryValues(getAllCategoriesEntryValues());
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

		return ret;
	}
	
	private CharSequence[] getAllDatabasesListEntries() {
		CharSequence[] list = TmhApplication.getAppContext().databaseList();
		CharSequence[] ret = new CharSequence[list.length];
		
		for(int i=0; i<list.length; i++) {
			String[] vals = ((String)list[i]).split(DatabaseHelper.DATABASE_PREFIX);
			ret[i] = vals[1];
		}
		
		return ret;
	}
	
	private CharSequence[] getAllDatabasesListEntryValues() {
		return TmhApplication.getAppContext().databaseList();
	}

	@Override
	protected void onDestroy() {
		TmhApplication.getDatabaseHelper().close();
		super.onDestroy();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(PREF_NEW_DATABASE.equals(preference.getKey())) {
			((EditTextPreference)preference).setText("");
			((EditTextPreference)preference).getEditText().setText("");
		}
		
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(PREF_DATABASE.equals(preference.getKey())) {
			Log.d(PreferencesActivity.class.getName(), "Opening database "+newValue);
			TmhApplication.changeOrCreateDatabase((String)newValue);
		} else if (PREF_NEW_DATABASE.equals(preference.getKey())) {
			if(! "".equals((String)newValue)) {
				Log.d(PreferencesActivity.class.getName(), "Creating new database "+newValue);
				
				String dbName = DatabaseHelper.DATABASE_PREFIX+((String)newValue);
				
				// creating a new DB
				TmhApplication.changeOrCreateDatabase(dbName);
				
				// Resetting edit text to emtpy string
				ListPreference dbPref = (ListPreference)findPreference(PREF_DATABASE);
				dbPref.setEntries(getAllDatabasesListEntries());
				dbPref.setEntryValues(getAllDatabasesListEntryValues());
				dbPref.setValue(dbName);
				
				Toast.makeText(
						TmhApplication.getAppContext(), 
						getString(R.string.database_created_successfuly)+" ["+newValue+"]", 
						Toast.LENGTH_LONG).show();
			}
		}

		return true;
	}
	
	public static String getStringValue(String key) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TmhApplication.getAppContext());
		return sharedPreferences.getString(key, "");
	}
}
