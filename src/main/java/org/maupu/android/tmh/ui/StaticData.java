package org.maupu.android.tmh.ui;

import java.util.Date;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;


public abstract class StaticData {
	private static Account currentAccount = new Account();
	private static boolean isValidCurrentAccount = false;
	private static Integer defaultAccountId;
	private static Category currentSelectedCategory = new Category();
	private static Date statsDateBeg = null;
	private static Date statsDateEnd = null;
	private static boolean statsAdvancedFilter = false;
	private static Date currentOperationDatePickerDate = null;
	public static final String PREF_CURRENT_ACCOUNT = "current_account";
	public static final String PREF_NEW_DATABASE = "new_database";
	public static final String PREF_DATABASE = "database";
	public static final String PREF_DEF_ACCOUNT = "def_account";
	public static final String PREF_WITHDRAWAL_CATEGORY = "category_withdrawal";
	public static final String PREF_CURRENT_OPERATION_DATE_PICKER = "current_op_date";
	public static final String PREF_CURRENT_SELECTED_CATEGORY = "current_category";
	
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
	
	public static void setCurrentOperationDatePickerDate(Date currentOperationDatePickerDate) {
		if(currentOperationDatePickerDate == null)
			return;
		
		SharedPreferences prefs = getPrefs();
		Editor editor = prefs.edit();
		editor.putLong(PREF_CURRENT_OPERATION_DATE_PICKER, currentOperationDatePickerDate.getTime());
		editor.commit();
	}
	
	public static Date getCurrentOperationDatePickerDate() {
		SharedPreferences prefs = getPrefs();
		Long date = prefs.getLong(PREF_CURRENT_OPERATION_DATE_PICKER, -1);
		
		if(date == -1)
			return null;
		
		currentOperationDatePickerDate = new Date(date);
		return currentOperationDatePickerDate;
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
	
	public static void setCurrentAccount(Account account) {
		int id = account == null || account.getId() == null ? -1 : account.getId();
		
		SharedPreferences prefs = getPrefs();
		Editor editor = prefs.edit();
		editor.putInt(PREF_CURRENT_ACCOUNT, id);
		editor.commit();
		
		// Fetch and fill currentAccount
		invalidateCurrentAccount();
		getCurrentAccount();
	}
	
	public static Account getCurrentAccount() {
		int id = getPreferenceValueInt(PREF_CURRENT_ACCOUNT);
		if (id < 0) {
			id = getDefaultAccountId();
		}
		
		if(currentAccount.getId() == null || currentAccount.getId() != id || !isValidCurrentAccount) {
			Cursor c = currentAccount.fetch(id);
			currentAccount.toDTO(c);
			isValidCurrentAccount = true;
		} else if (id < 0) {
			return null;
		}
		
		return currentAccount;
	}
	
	private static Integer getDefaultAccountId() {
		if(defaultAccountId == null || defaultAccountId == -1 || !isValidCurrentAccount) {
			// Default is first one
			Cursor cursor = currentAccount.fetchAll();
			if(cursor.getCount() == 0)
				return -1;
			
			cursor.moveToFirst();
			int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
			defaultAccountId = cursor.getInt(idxId);
		}
		
		return defaultAccountId;
	}
	
	public static Category getCurrentSelectedCategory() {
		Integer id = getPreferenceValueInt(PREF_CURRENT_SELECTED_CATEGORY);
		
		if(id < 0) {
			return null;
		} else {
			 Cursor result = currentSelectedCategory.fetch(id);
			 
			 if(result == null || result.getCount() != 1) {
				 setCurrentSelectedCategory(null);
				 return null;
			 }
			 
			 currentSelectedCategory.toDTO(result);
			 return currentSelectedCategory;
		}
	}
	
	public static void setCurrentSelectedCategory(Category category) {
		int catId = category == null ? -1 : category.getId();
		
		SharedPreferences prefs = getPrefs();
		Editor editor = prefs.edit();
		editor.putInt(PREF_CURRENT_SELECTED_CATEGORY, catId);
		editor.commit();
		
		// Fetch and fill currentCategory
		getCurrentSelectedCategory();
	}
	
	private static SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(TmhApplication.getAppContext());
	}
	
	// Here, we store a String because PreferenceActivity use string as entryValues
	public static Category getWithdrawalCategory() {
		String result = getPreferenceValueString(StaticData.PREF_WITHDRAWAL_CATEGORY); 
		
		if(result == null || Integer.parseInt(result) < 0)
			return null;
		
		Category category = null;
		try {
			int id = Integer.parseInt(result);
			category = new Category();
			Cursor cursor = category.fetch(id);
			category.toDTO(cursor);
			if(cursor == null || cursor.getCount() != 1) {
				// Invalidate current one
				setWithdrawalCategory(null);
				return null;
			}
		} catch (NumberFormatException nfe) {
			// not an integer
			setWithdrawalCategory(null);
			return null;
		}
		
		
		return category;
	}
	
	public static void setWithdrawalCategory(Category category) {
		String catId = category == null ? null : String.valueOf(category.getId());
		
		SharedPreferences prefs = getPrefs();
		Editor editor = prefs.edit();
		editor.putString(PREF_WITHDRAWAL_CATEGORY, catId);
		editor.commit();
	}
	
	public static String getPreferenceValueString(String key) {
		SharedPreferences prefs = getPrefs();
		return prefs.getString(key, null);
	}
	
	public static Integer getPreferenceValueInt(String key) {
		SharedPreferences prefs = getPrefs();
		return prefs.getInt(key, -1);
	}
	
	public static Boolean getPreferenceValueBoolean(String key) {
		SharedPreferences prefs = getPrefs();
		return prefs.getBoolean(key, false);
	}
	
	public static Long getPreferenceValueLong(String key) {
		SharedPreferences prefs = getPrefs();
		return prefs.getLong(key, -1);
	}
	
	public static void setPreferenceValueBoolean(String key, boolean value) {
		SharedPreferences prefs = getPrefs();
		Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
}
