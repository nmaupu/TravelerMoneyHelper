package org.maupu.android.tmh;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;

import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity implements OnPreferenceClickListener {
	private DatabaseHelper dbHelper = TmhApplication.getDatabaseHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
		
		ListPreference listAccount = (ListPreference)findPreference("account");
		listAccount.setOnPreferenceClickListener(this);
		listAccount.setEntries(getAllAccountEntries());
		listAccount.setEntryValues(getAllAccountsEntryValues());
		
		ListPreference listCategory = (ListPreference)findPreference("category");
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
			int idxName = cursor.getColumnIndexOrThrow(AccountData.KEY_NAME);
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
			int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
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
			int idxId = cursor.getColumnIndexOrThrow(CategoryData.KEY_ID);
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
			int idxName = cursor.getColumnIndexOrThrow(CategoryData.KEY_NAME);
			ret[i] = String.valueOf(cursor.getInt(idxId));
			cursor.moveToNext();
		}
		
		return ret;
	}
	
	@Override
	protected void onDestroy() {
		dbHelper.close();
		super.onDestroy();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {		
		return true;
	}
}
