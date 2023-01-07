package org.maupu.android.tmh.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTimeConstants;
import org.maupu.android.tmh.R;
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
    private static final Account currentAccount = new Account();
    private static boolean isValidCurrentAccount = false;
    private static Integer defaultAccountId;
    private static final Category currentSelectedCategory = new Category();
    private static final Set<Integer> statsExceptedCategories = new HashSet<>();
    private static Currency mainCurrency = null;

    public static final String PREF_KEY_NEW_DATABASE = TmhApplication.getAppContext().getString(R.string.pref_key_new_database);
    public static final String PREF_KEY_DATABASE = TmhApplication.getAppContext().getString(R.string.pref_key_database);
    public static final String PREF_KEY_MANAGE_DB = TmhApplication.getAppContext().getString(R.string.pref_key_manage_db);
    public static final String PREF_KEY_WITHDRAWAL_CATEGORY = TmhApplication.getAppContext().getString(R.string.pref_key_category_withdrawal);
    public static final String PREF_KEY_OER_API_KEY = TmhApplication.getAppContext().getString(R.string.pref_key_oer_apikey_edit_text);
    public static final String PREF_KEY_BACKUP_CATEGORY = TmhApplication.getAppContext().getString(R.string.pref_key_backup_category);
    public static final String PREF_KEY_DRIVE_ACTIVATE = TmhApplication.getAppContext().getString(R.string.pref_key_drive_activate);
    public static final String PREF_KEY_DRIVE_BACKUP_FOLDER = TmhApplication.getAppContext().getString(R.string.pref_key_drive_backup_folder);
    public static final String PREF_KEY_DRIVE_MANUAL_BACKUP = TmhApplication.getAppContext().getString(R.string.pref_key_drive_manual_backup);
    public static final String PREF_KEY_DRIVE_RETENTION = TmhApplication.getAppContext().getString(R.string.pref_key_drive_retention);
    public static final String PREF_KEY_DRIVE_DELETE_OLD = TmhApplication.getAppContext().getString(R.string.pref_key_drive_delete_old);
    public static final String PREF_KEY_DRIVE_AUTOMATIC_BACKUP = TmhApplication.getAppContext().getString(R.string.pref_key_drive_automatic_backup);
    public static final String PREF_KEY_DRIVE_AUTOMATIC_BACKUP_BOOT_NOTIFICATION = TmhApplication.getAppContext().getString(R.string.pref_key_drive_automatic_backup_boot_notification);
    public static final String PREF_KEY_RENAME_DATABASE = TmhApplication.getAppContext().getString(R.string.pref_key_rename_database);
    public static final String PREF_KEY_DRIVE_RESTORE = TmhApplication.getAppContext().getString(R.string.pref_key_drive_restore);
    public static final String PREF_KEY_MAIN_CURRENCY = TmhApplication.getAppContext().getString(R.string.pref_key_main_currency);

    // Google Drive automatic backups
    public static final String PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY = "pref_drive_automatic_backup_freq";
    public static final int PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_TEST = 0;
    public static final int PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_NEVER = -1;
    public static final int PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_DAILY = 2;
    public static final int PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_WEEKLY = 3;
    public static final int PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_MONTHLY = 4;

    public static final String PREF_CURRENT_ACCOUNT = "current_account";
    public static final String PREF_CURRENT_OPERATION_DATE_PICKER = "current_op_date";
    public static final String PREF_CURRENT_SELECTED_CATEGORY = "current_category";
    public static final String PREF_MAIN_CURRENCY = "main_currency";
    public static final String PREF_STATS_DATE_BEG = "statsDateBeg";
    public static final String PREF_STATS_DATE_END = "statsDateEnd";
    public static final String PREF_OER_API_KEY_VALID = "oer_apikey_valid";
    public static final String PREF_DRIVE_BACKUP_FOLDER_ID = "drive_backup_folder_id";


    // Keep tracking item selected in navigation drawer
    public static int navigationDrawerItemSelected = 0;

    // Keep tracking parameters of statsActivity
    public static boolean showGraph = true;

    // Automatic backup defaults
    public static int BACKUP_DEFAULT_HOUR_OF_DAY = 22;
    public static int BACKUP_DEFAULT_DAY_OF_WEEK = DateTimeConstants.SUNDAY;
    public static int BACKUP_DEFAULT_DAY_OF_MONTH = 1;

    public static final String KEY_AUTOMATIC_BACKUP_NEXT_ALARM_DATE_TIME = "automatic_backup_next_alarm_date_time";

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

    @NonNull
    public static Integer[] getStatsExceptedCategoriesToArray() {
        if (statsExceptedCategories == null || statsExceptedCategories.size() == 0)
            return new Integer[0];

        return statsExceptedCategories.toArray(new Integer[0]);
    }

    @Nullable
    public static Date getCurrentOperationDatePickerDate() {
        SharedPreferences prefs = getPrefs();
        Long date = prefs.getLong(PREF_CURRENT_OPERATION_DATE_PICKER, -1);

        if (date == -1)
            return null;

        return new Date(date);
    }

    @Nullable
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

    @Nullable
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

    @Nullable
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

    public static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(TmhApplication.getAppContext());
    }

    // Here, we store a String because PreferenceActivity use string as entryValues
    @Nullable
    public static Category getWithdrawalCategory() {
        String result = getPreferenceValueString(StaticData.PREF_KEY_WITHDRAWAL_CATEGORY);

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
        editor.putString(PREF_KEY_WITHDRAWAL_CATEGORY, catId);
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

    @Nullable
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

    @NonNull
    public static Integer getPreferenceValueInt(String key) {
        SharedPreferences prefs = getPrefs();
        return prefs.getInt(key, -1);
    }

    @NonNull
    public static Boolean getPreferenceValueBoolean(String key) {
        SharedPreferences prefs = getPrefs();
        return prefs.getBoolean(key, false);
    }

    @NonNull
    public static Long getPreferenceValueLong(String key) {
        SharedPreferences prefs = getPrefs();
        return prefs.getLong(key, -1);
    }

    @NonNull
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

    public static boolean isOerApiKeyValid() {
        String apiKey = getPreferenceValueString(StaticData.PREF_KEY_OER_API_KEY);
        Boolean apiKeyValid = getPreferenceValueBoolean(StaticData.PREF_OER_API_KEY_VALID);
        return apiKeyValid != null && apiKeyValid && apiKey != null && !"".equals(apiKey);
    }
}
