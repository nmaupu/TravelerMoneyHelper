package org.maupu.android.tmh;

import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;

import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity implements OnPreferenceClickListener {
	private DatabaseHelper dbHelper = new DatabaseHelper(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
		
		dbHelper.openReadable();
		
		ListPreference listAccount = (ListPreference)findPreference("account");
		listAccount.setOnPreferenceClickListener(this);
		listAccount.setEntries(getAllAccountEntries());
		listAccount.setEntryValues(getAllAccountsEntryValues());
	}
	
	private CharSequence[] getAllAccountsEntryValues() {
		CharSequence[] ret;
		Account dummy = new Account();
		Cursor cursor = dummy.fetchAll(dbHelper);
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
		Cursor cursor = dummy.fetchAll(dbHelper);
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
