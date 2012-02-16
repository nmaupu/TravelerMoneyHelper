package org.maupu.android.tmh.ui;

import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;


public abstract class StaticData {
	private static Account currentAccount = new Account();
	private static boolean isValidCurrentAccount = false;
	private static Integer defaultAccountId;
	private static Category currentSelectedCategory = new Category();
	public static final String STATIC_DATA_PREFS_FILENAME = "staticPreferences";
	
	public static void setCurrentAccount(Context context, final DatabaseHelper dbHelper, Account account) {
		if(account == null || account.getId() == null)
			return;
		
		//getCurrentAccount(context, dbHelper);
		SharedPreferences prefs = getPrefs(context);
		Editor editor = prefs.edit();
		editor.putInt("account", account.getId());
		editor.commit();
		
		// Fetch and fill currentAccount
		getCurrentAccount(context, dbHelper);
	}
	
	/**
	 * Allow current account to be invalidated
	 */
	public static void invalidateCurrentAccount() {
		isValidCurrentAccount = false;
	}
	
	public static Account getCurrentAccount(Context context, final DatabaseHelper dbHelper) {
		SharedPreferences prefs = getPrefs(context);
		Integer id = prefs.getInt("account", getDefaultAccount(dbHelper));
		
		if(currentAccount.getId() == null || currentAccount.getId() != id || !isValidCurrentAccount) {
			Cursor c = currentAccount.fetch(id);
			currentAccount.toDTO(c);
			isValidCurrentAccount = true;
		} else if (id == null) {
			return null;
		}
		
		return currentAccount;
	}
	
	private static Integer getDefaultAccount(final DatabaseHelper dbHelper) {
		if(defaultAccountId == null) {
			Cursor cursor = currentAccount.fetchAll();
			if(cursor.getCount() == 0)
				return null;
			
			cursor.moveToFirst();
			int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
			defaultAccountId = cursor.getInt(idxId);
		}
		
		return defaultAccountId;
	}
	
	public static Category getCurrentSelectedCategory(Context context, final DatabaseHelper dbHelper) {
		SharedPreferences prefs = getPrefs(context);
		Integer id = prefs.getInt("currentCategory", -1);
		
		if(id == null || id == -1) {
			return null;
		} else {
			 Cursor result = currentSelectedCategory.fetch(id);
			 
			 if(result == null)
				 return null;
			 
			 currentSelectedCategory.toDTO(result);
			 return currentSelectedCategory;
		}
	}
	
	public static void setCurrentSelectedCategory(Context context, final DatabaseHelper dbHelper, Category category) {
		if(category == null || category.getId() == null)
			return;
		
		//getCurrentSelectedCategory(context, dbHelper);
		SharedPreferences prefs = getPrefs(context);
		Editor editor = prefs.edit();
		editor.putInt("currentCategory", category.getId());
		editor.commit();
		
		// Fetch and fill currentCategory
		getCurrentSelectedCategory(context, dbHelper);
	}
	
	private static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(STATIC_DATA_PREFS_FILENAME, Context.MODE_PRIVATE);
	}
}
