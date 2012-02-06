package org.maupu.android.tmh.ui;

import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;


public abstract class StaticData {
	private static Account currentAccount = new Account();
	private static boolean isValidCurrentAccount = false;
	private static Integer defaultAccountId;
	public static final String STATIC_DATA_PREFS_FILENAME = "staticPreferences";
	
	public static void replaceCurrentAccount(Context context, final DatabaseHelper dbHelper, Account account) {
		if(account == null || account.getId() == null)
			return;
		
		getCurrentAccount(context, dbHelper);
		SharedPreferences prefs = getPrefs(context);
		Editor editor = prefs.edit();
		editor.putInt("account", account.getId());
		editor.commit();
		
		// Fetch and fill currentAccount
		getCurrentAccount(context, dbHelper);
	}
	
	public static void invalidateCurrentAccount() {
		isValidCurrentAccount = false;
	}
	
	public static Account getCurrentAccount(Context context, final DatabaseHelper dbHelper) {
		SharedPreferences prefs = getPrefs(context);
		Integer id = prefs.getInt("account", getDefaultAccount(dbHelper));
		
		if(currentAccount.getId() == null || currentAccount.getId() != id || !isValidCurrentAccount) {
			Cursor c = currentAccount.fetch(dbHelper, id);
			currentAccount.toDTO(dbHelper, c);
			isValidCurrentAccount = true;
		} else if (id == null) {
			return null;
		}
		
		return currentAccount;
	}
	
	private static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(STATIC_DATA_PREFS_FILENAME, Context.MODE_PRIVATE);
	}
	
	private static Integer getDefaultAccount(final DatabaseHelper dbHelper) {
		if(defaultAccountId == null) {
			Cursor cursor = currentAccount.fetchAll(dbHelper);
			if(cursor.getCount() == 0)
				return null;
			
			cursor.moveToFirst();
			int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
			defaultAccountId = cursor.getInt(idxId);
		}
		
		return defaultAccountId;
	}
}
