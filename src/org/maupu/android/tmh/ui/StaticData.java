package org.maupu.android.tmh.ui;

import java.util.Date;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
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
	private static Date statsDateBeg = null;
	private static Date statsDateEnd = null;
	private static boolean statsAdvancedFilter = false;
	public static final String STATIC_DATA_PREFS_FILENAME = "staticPreferences";
	
	public static void setCurrentAccount(Account account) {
		if(account == null || account.getId() == null)
			return;
		
		SharedPreferences prefs = getPrefs();
		Editor editor = prefs.edit();
		editor.putInt("account", account.getId());
		editor.commit();
		
		// Fetch and fill currentAccount
		getCurrentAccount();
	}
	
	/**
	 * Allow current account to be invalidated
	 */
	public static void invalidateCurrentAccount() {
		isValidCurrentAccount = false;
	}
	
	public static void setStatsDateBeg(Date statsDateBeg) {
		if(statsDateBeg == null)
			return;
		
		SharedPreferences prefs = getPrefs();
		Editor editor = prefs.edit();
		editor.putLong("statsDateBeg", statsDateBeg.getTime());
		editor.commit();
		
		getStatsDateBeg();
	}
	
	public static void setStatsDateEnd(Date statsDateEnd) {
		if(statsDateEnd == null)
			return;
		
		SharedPreferences prefs = getPrefs();
		Editor editor = prefs.edit();
		editor.putLong("statsDateEnd", statsDateEnd.getTime());
		editor.commit();
		
		getStatsDateEnd();
	}
	
	public static boolean isStatsAdvancedFilter() {
		return statsAdvancedFilter;
	}
	
	public static void setStatsAdvancedFilter(boolean statsAdvancedFilter) {
		StaticData.statsAdvancedFilter = statsAdvancedFilter;
	}
	
	public static Date getStatsDateBeg() {
		SharedPreferences prefs = getPrefs();
		Long dateBeg = prefs.getLong("statsDateBeg", -1);
		
		if(dateBeg == -1)
			return null;
		
		statsDateBeg = new Date(dateBeg);
		
		return statsDateBeg;
	}
	
	public static Date getStatsDateEnd() {
		SharedPreferences prefs = getPrefs();
		Long dateEnd = prefs.getLong("statsDateEnd", -1);
		
		if(dateEnd == -1)
			return null;
		
		statsDateEnd = new Date(dateEnd);
		
		return statsDateEnd;
	}
	
	public static Account getCurrentAccount() {
		SharedPreferences prefs = getPrefs();
		Integer id = prefs.getInt("account", getDefaultAccount());
		
		if(currentAccount.getId() == null || currentAccount.getId() != id || !isValidCurrentAccount) {
			Cursor c = currentAccount.fetch(id);
			currentAccount.toDTO(c);
			isValidCurrentAccount = true;
		} else if (id == null) {
			return null;
		}
		
		return currentAccount;
	}
	
	private static Integer getDefaultAccount() {
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
	
	public static Category getCurrentSelectedCategory() {
		SharedPreferences prefs = getPrefs();
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
	
	public static void setCurrentSelectedCategory(Category category) {
		if(category == null || category.getId() == null)
			return;
		
		SharedPreferences prefs = getPrefs();
		Editor editor = prefs.edit();
		editor.putInt("currentCategory", category.getId());
		editor.commit();
		
		// Fetch and fill currentCategory
		getCurrentSelectedCategory();
	}
	
	private static SharedPreferences getPrefs() {
		return TmhApplication.getAppContext().getSharedPreferences(STATIC_DATA_PREFS_FILENAME, Context.MODE_PRIVATE);
	}
}
