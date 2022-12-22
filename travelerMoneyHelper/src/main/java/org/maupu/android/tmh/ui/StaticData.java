package org.maupu.android.tmh.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract class StaticData {
    private static Account currentAccount = new Account();
    private static boolean isValidCurrentAccount = false;
    private static Integer defaultAccountId;
    private static Category currentSelectedCategory = new Category();
    private static Set<Integer> statsExceptedCategories = new HashSet<>();
    private static boolean statsAdvancedFilter = false;
    private static Date currentOperationDatePickerDate = null;
    private static Currency mainCurrency = null;

    public static final String PREF_CURRENT_ACCOUNT = "current_account";
    public static final String PREF_NEW_DATABASE = "new_database";
    public static final String PREF_DATABASE = "database";
    public static final String PREF_MANAGE_DB = "manage_db";
    public static final String PREF_DEF_ACCOUNT = "def_account";
    public static final String PREF_WITHDRAWAL_CATEGORY = "category_withdrawal";
    public static final String PREF_CURRENT_OPERATION_DATE_PICKER = "current_op_date";
    public static final String PREF_CURRENT_SELECTED_CATEGORY = "current_category";
    public static final String PREF_MAIN_CURRENCY = "main_currency";
    public static final String PREF_STATS_DATE_BEG = "statsDateBeg";
    public static final String PREF_STATS_DATE_END = "statsDateEnd";
    public static final String PREF_OER_EDIT = "oer_apikey_edit_text";
    public static final String PREF_OER_VALID = "oer_apikey_valid";
    public static final String PREF_EXPORT_DB = "export_filename";
    public static final String PREF_IMPORT_DB = "import_db";
    public static final String PREF_BACKUP_CATEGORY = "backup_category";
    public static final String PREF_DRIVE_ACTIVATE = "drive_activate";

    // Keep tracking item selected in navigation drawer
    public static int navigationDrawerItemSelected = 0;

    // Keep tracking parameters of statsActivity
    public static boolean showGraph = true;


    /**
     * Allow current account to be invalidated
     */
    public static void invalidateCurrentAccount() {
        isValidCurrentAccount = false;
    }

    public static void setCurrentOperationDatePickerDate(Date currentOperationDatePickerDate) {
        if (currentOperationDatePickerDate == null)
            return;

        setPreferenceValueLong(PREF_CURRENT_OPERATION_DATE_PICKER, currentOperationDatePickerDate.getTime());
    }

    public static Set<Integer> getStatsExceptedCategories() {
        return statsExceptedCategories;
    }

    public static Integer[] getStatsExceptedCategoriesToArray() {
        if (statsExceptedCategories == null || statsExceptedCategories.size() == 0)
            return new Integer[0];

        return statsExceptedCategories.toArray(new Integer[0]);
    }

    public static Date getCurrentOperationDatePickerDate() {
        SharedPreferences prefs = getPrefs();
        Long date = prefs.getLong(PREF_CURRENT_OPERATION_DATE_PICKER, -1);

        if (date == -1)
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

    public static Date getDateField(String key) {
        SharedPreferences prefs = getPrefs();
        Long date = prefs.getLong(key, -1);
        return date == -1 ? null : new Date(date);
    }

    public static void setDateField(String key, Date date) {
        if (date == null || key == null || "".equals(key))
            return;

        setPreferenceValueLong(key, date.getTime());
    }

    public static void setDateField(String key, Long timestamp) {
        if (timestamp == null || key == null || "".equals(key))
            return;

        setPreferenceValueLong(key, timestamp);
    }

    public static void setCurrentAccount(Account account) {
        int id = account == null || account.getId() == null ? -1 : account.getId();

        setPreferenceValueInt(PREF_CURRENT_ACCOUNT, id);

        // Reset stats' expected categories list
        statsExceptedCategories.clear();

        // Fetch and fill currentAccount
        invalidateCurrentAccount();
        getCurrentAccount();
    }

    public static Account getCurrentAccount() {
        int id = getPreferenceValueInt(PREF_CURRENT_ACCOUNT);
        if (id < 0) {
            id = getDefaultAccountId();
        }

        if (currentAccount.getId() == null || currentAccount.getId() != id || !isValidCurrentAccount) {
            Cursor c = currentAccount.fetch(id);
            currentAccount.toDTO(c);
            c.close();
            isValidCurrentAccount = true;
        } else if (id < 0) {
            return null;
        }

        return currentAccount;
    }

    private static Integer getDefaultAccountId() {
        if (defaultAccountId == null || defaultAccountId == -1 || !isValidCurrentAccount) {
            // Default is first one
            Cursor cursor = currentAccount.fetchAll();
            if (cursor.getCount() == 0) {
                cursor.close();
                return -1;
            }

            cursor.moveToFirst();
            int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
            defaultAccountId = cursor.getInt(idxId);
            cursor.close();
        }

        return defaultAccountId;
    }

    public static Category getCurrentSelectedCategory() {
        Integer id = getPreferenceValueInt(PREF_CURRENT_SELECTED_CATEGORY);

        if (id < 0) {
            return null;
        } else {
            Cursor result = currentSelectedCategory.fetch(id);

            if (result == null || result.getCount() != 1) {
                setCurrentSelectedCategory(null);
                if (result != null)
                    result.close();
                return null;
            }

            currentSelectedCategory.toDTO(result);
            result.close();
            return currentSelectedCategory;
        }
    }

    public static void setCurrentSelectedCategory(Category category) {
        int catId = -1;
        if (category != null && category.getId() != null) {
            catId = category.getId();
        }

        setPreferenceValueInt(PREF_CURRENT_SELECTED_CATEGORY, catId);

        // Fetch and fill currentCategory
        getCurrentSelectedCategory();
    }

    private static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(TmhApplication.getAppContext());
    }

    // Here, we store a String because PreferenceActivity use string as entryValues
    public static Category getWithdrawalCategory() {
        String result = getPreferenceValueString(StaticData.PREF_WITHDRAWAL_CATEGORY);

        try {
            if (result == null || Integer.parseInt(result) < 0)
                return null;
        } catch (NumberFormatException nfe) {
            // result does not contain a correct id
            return null;
        }

        Category category = null;
        Cursor cursor = null;
        try {
            int id = Integer.parseInt(result);
            category = new Category();
            cursor = category.fetch(id);

            if (cursor == null || cursor.getCount() != 1) {
                // Invalidate current one
                setWithdrawalCategory(null);
                return null;
            }

            category.toDTO(cursor);
        } catch (NumberFormatException nfe) {
            // not an integer
            setWithdrawalCategory(null);
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
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

    public static Currency getMainCurrency() {
        int id = getPreferenceValueInt(PREF_MAIN_CURRENCY);
        if (id < 0) {
            Currency c = getDefaultMainCurrency();
            id = c == null ? -1 : c.getId();
        }

        if (id >= 0 && (mainCurrency == null || mainCurrency.getId() == null || mainCurrency.getId() != id)) {
            if (mainCurrency == null)
                mainCurrency = new Currency();
            Cursor c = mainCurrency.fetch(id);
            mainCurrency.toDTO(c);
            c.close();
        } else if (id < 0) {
            mainCurrency = null;
        }

        return mainCurrency;
    }

    public static Currency getDefaultMainCurrency() {
        Currency cur = new Currency();
        Cursor c = cur.fetchAllOrderBy(CurrencyData.KEY_ID, "ASC");

        if (c == null || c.getCount() == 0) {
            if (c != null)
                c.close();

            return null;
        }

        c.moveToFirst();
        cur.toDTO(c);
        c.close();

        return cur;
    }

    public static void setMainCurrency(Integer idCurrency) {
        if (idCurrency == null)
            return;

        Currency cur = new Currency();
        Cursor c = cur.fetch(idCurrency);

        if (c == null)
            return;

        cur.toDTO(c);
        c.close();
        mainCurrency = cur;

        setPreferenceValueInt(PREF_MAIN_CURRENCY, cur.getId());
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

    public static Float getPreferenceValueFloat(String key) {
        SharedPreferences prefs = getPrefs();
        return prefs.getFloat(key, -1);
    }

    public static void setPreferenceValueBoolean(String key, boolean value) {
        SharedPreferences prefs = getPrefs();
        Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setPreferenceValueInt(String key, int value) {
        SharedPreferences prefs = getPrefs();
        Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setPreferenceValueString(String key, String value) {
        SharedPreferences prefs = getPrefs();
        Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setPreferenceValueLong(String key, Long value) {
        SharedPreferences prefs = getPrefs();
        Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void setPreferenceValueFloat(String key, Float value) {
        SharedPreferences prefs = getPrefs();
        Editor editor = prefs.edit();
        editor.putFloat(key, value);
        editor.commit();
    }
}
