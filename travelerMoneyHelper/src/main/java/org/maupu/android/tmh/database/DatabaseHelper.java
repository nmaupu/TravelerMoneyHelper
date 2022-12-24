package org.maupu.android.tmh.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import org.maupu.android.tmh.PreferencesActivity;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.TmhLogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public final class DatabaseHelper extends SQLiteOpenHelper {
    public static final Class<DatabaseHelper> TAG = DatabaseHelper.class;

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat dateFormatNoHour = new SimpleDateFormat("yyyy-MM-dd");
    public static final String DATABASE_PREFIX = "TravelerMoneyHelper_appdata_";
    public static final String DEFAULT_DATABASE_NAME = DATABASE_PREFIX + "default";
    public static final int DATABASE_VERSION = 16;
    private final List<APersistedData> persistedData = new ArrayList<>();

    public DatabaseHelper(String dbName) {
        super(TmhApplication.getAppContext(), dbName, null, DATABASE_VERSION);

        persistedData.add(new CategoryData());
        persistedData.add(new CurrencyData());
        persistedData.add(new OperationData());
        persistedData.add(new AccountData());
        persistedData.add(new StaticPrefsData());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (APersistedData data : persistedData) {
            data.onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (APersistedData data : persistedData) {
            data.onUpgrade(db, oldVersion, newVersion);
        }
    }

    public void close() {
        try {
            super.close();
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
        }
    }

    public SQLiteDatabase getDb() {
        return super.getWritableDatabase();
    }

    public String getCurrentDbPath() {
        return getDb().getPath();
    }

    public static String formatDateForSQL(Date date) {
        return dateFormat.format(date);
    }

    public static String formatDateForSQLNoHour(Date date) {
        return dateFormatNoHour.format(date);
    }

    public static Date toDate(String sqlDate) {
        try {
            return dateFormat.parse(sqlDate);
        } catch (ParseException pe) {
            return null;
        }
    }

    public void createSampleData() {
        // If something in accounts, don't do anything
        Account dummy = new Account();
        Cursor c = dummy.fetchAll();
        if (c.getCount() > 0)
            return;

        Currency currency = new Currency();
        currency.setLastUpdate(new GregorianCalendar().getTime());
        currency.setLongName("Euro");
        currency.setShortName("e");
        currency.setRateCurrencyLinked(1d);
        currency.setIsoCode("EUR");
        currency.insert();

        currency.setLongName("Dollar");
        currency.setShortName("$");
        currency.setIsoCode("USD");
        currency.setRateCurrencyLinked(1.4d);
        currency.insert();

        Cursor cCurrency = currency.fetch(1);
        currency.toDTO(cCurrency);

        Account account = new Account();
        account.setName("Nico");
        account.setCurrency(currency);
        account.insert();

        account.setName("Bicou");
        account.insert();

        Category category = new Category();
        category.setName("Courses");
        category.insert();

        category.setName("Alimentation");
        category.insert();


        category.toDTO(category.fetch(1));
        account.toDTO(account.fetch(1));
        currency.toDTO(currency.fetch(1));
        Operation op = new Operation();
        op.setAmount(10d);
        op.setAccount(account);
        op.setCategory(category);
        op.setCurrency(currency);
        op.setCurrencyValueOnCreated(1d);
        op.setDate(new GregorianCalendar().getTime());
        op.insert();

        op.setAmount(15d);
        op.insert();

        op.setAmount(22d);
        op.insert();
    }

    public static String getPreferredDatabaseName() {
        String db = PreferencesActivity.getStringValue(StaticData.PREF_KEY_DATABASE);
        if (db == null || "".equals(db)) {
            // Return default db name
            return DEFAULT_DATABASE_NAME;
        } else {
            return db;
        }
    }

    /**
     * Get either list of databases ready to be printed to user
     * or stored database filename depending on prettyPrint parameter.
     *
     * @param prettyPrint Weather getting all databases ready to be printed to user or raw filenames
     * @return List of all available databases
     */
    public static CharSequence[] getAllDatabases(boolean prettyPrint, String... exceptions) {
        // For an obscure reason, databaseList() returns strange results on some device
        // such as [TravelerMoneyHelper_appdata, TravelerMoneyHelper_appdata_default, 0]
        // instead of [TravelerMoneyHelper_appdata_default]
        // What is 0 ? What is TravelerMoneyHelper_appdata ? who knows !
        CharSequence[] list = TmhApplication.getAppContext().databaseList();
        //CharSequence[] ret = new CharSequence[list.length];
        List<String> listEntries = new ArrayList<>();

        TmhLogger.d(TAG, "Number of databases returned : " + list.length);
        for (int i = 0; i < list.length; i++) {
            String[] vals = ((String) list[i]).split(DatabaseHelper.DATABASE_PREFIX);
            // If database name is not correct (no prefix), array is wrong so we denied this DB
            // We also discard journal DB
            if (vals.length == 2 && !vals[1].contains("-journal") && !stringIsContainedInArray(vals[1], exceptions)) {
                TmhLogger.d(TAG, "Database filename : " + list[i] + " - database name : " + vals[1]);
                if (prettyPrint)
                    listEntries.add(vals[1]);
                else
                    listEntries.add(list[i].toString());
            }
        }

        return listEntries.toArray(new String[0]);
    }

    /**
     * Remove database prefix from given db name
     *
     * @param name Database name to strip
     * @return Database name
     */
    public static String stripDatabaseFileName(String name) {
        String[] vals = ((String) name).split(DatabaseHelper.DATABASE_PREFIX);
        if (vals.length >= 2) {
            return vals[1];
        }
        return name;
    }

    private static boolean stringIsContainedInArray(String s, String... list) {
        for (int i = 0; i < list.length; i++) {
            if (s.contains(stripDatabaseFileName(list[i])))
                return true;
        }
        return false;
    }

    /**
     * Get all databases name ready to be displayed to a user
     *
     * @return List of all available databases
     */
    public static CharSequence[] getAllDatabasesListEntries(String... exceptions) {
        return getAllDatabases(true, exceptions);
    }

    /**
     * Get all databases filenames
     *
     * @return List of all available databases
     */
    public static CharSequence[] getAllDatabasesListEntryValues(String... exceptions) {
        return getAllDatabases(false, exceptions);
    }

    /**
     * Get absolute path of a given database
     *
     * @param dbName
     * @return The absolute path of the database or null if not found
     */
    public static String getDbAbsolutePath(Context context, String dbName) {
        return context.getDatabasePath(dbName).getAbsolutePath();
    }
}
