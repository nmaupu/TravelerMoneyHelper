package org.maupu.android.tmh.core;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.preference.PreferenceManager;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.database.object.StaticPrefs;
import org.maupu.android.tmh.ui.CurrencyISO4217;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.ImageUtil;
import org.maupu.android.tmh.util.drive.DriveBackupBroadcastReceiver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Locale;

public class TmhApplication extends Application {
    public static final String APP_NAME = "Traveler Money Helper";
    public static final String APP_NAME_SHORT = "tmh";
    public static final boolean LOGGING = false;
    private static Context applicationContext;
    private static DatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        TmhApplication.applicationContext = this.getApplicationContext();
        dbHelper = new DatabaseHelper(DatabaseHelper.getPreferredDatabaseName());

        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(this);
        boolean driveActivated = prefManager.getBoolean(StaticData.PREF_KEY_DRIVE_ACTIVATE, false);
        

        // Setup a test alarm
        Intent broadcastIntent = new Intent(this, DriveBackupBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 2343, broadcastIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        /*alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 3000,
                10000,
                pendingIntent);*/
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, pendingIntent);
    }

    public static Context getAppContext() {
        return applicationContext;
    }

    public static DatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }

    public static boolean checkDatabaseInitialized() {
        return new Category().fetchAll().getCount() > 0 &&
                new Currency().fetchAll().getCount() > 0 &&
                new Account().fetchAll().getCount() > 0;
    }

    /**
     * Initialize or reinitialize a database, removing everything and recreating from scratch.
     * ATTENTION: all data will be erased !
     *
     * @param ctx Context
     */
    public static void initDatabase(Context ctx) {
        new StaticPrefs().deleteAll();
        new Operation().deleteAll();
        new Category().deleteAll();
        new Account().deleteAll();
        new Currency().deleteAll();

        // Create default category
        String catName = ctx.getString(R.string.default_category_withdrawal_name);
        Category defaultCategory = new Category();
        defaultCategory.setName(catName);
        if (defaultCategory.insert()) {
            StaticData.setWithdrawalCategory(defaultCategory);
        }

        String countryCode = TmhApplication.getCountryCode(ctx);
        String countryName = TmhApplication.getCountryNameFromCountryCode(countryCode);
        CurrencyISO4217 currency = CurrencyISO4217.getCurrencyISO4217FromCountryCode(countryCode);

        // Creating the primary Currency
        Currency cur = new Currency();
        cur.setIsoCode(currency.getCode());
        cur.setLongName(currency.getName());
        cur.setShortName(currency.getCurrencySymbol());
        cur.setRateCurrencyLinked(1d);
        cur.setLastUpdate(Calendar.getInstance().getTime());
        cur.insert();

        // Creating the primary Account
        Account acc = new Account();
        acc.setName(countryName);
        acc.setCurrency(cur);
        Drawable flagDrawable = null;
        try {
            Field idField = R.drawable.class.getDeclaredField("flag_" + countryCode.toLowerCase() + "_48");
            int resourceID = idField.getInt(idField);
            flagDrawable = getAppContext().getDrawable(resourceID);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        if (flagDrawable != null)
            acc.setIconBytes(ImageUtil.getBytesFromBitmap(ImageUtil.drawableToBitmap(flagDrawable)));
        acc.insert();
    }

    public static void changeOrCreateDatabase(String dbName) {
        String name = dbName;
        if (dbName != null && !"".equals(dbName) && !dbName.startsWith(DatabaseHelper.DATABASE_PREFIX)) {
            name = DatabaseHelper.DATABASE_PREFIX + dbName;
        }

        // Loading and saving current
        StaticPrefs.saveCurrentStaticPrefs();

        dbHelper.close();
        dbHelper = new DatabaseHelper(name);
        dbHelper.getDb();

        // Load stored StaticData
        StaticPrefs.toStaticData(
                StaticPrefs.loadCurrentStaticPrefs()
        );
    }

    public static int getIdentifier(String s) {
        return s == null ? -1 : Math.abs(s.hashCode());
    }

    public static String getCountryNameFromCountryCode(String code) {
        Locale locale = new Locale("", code);
        return locale.getDisplayCountry();
    }

    // Source: https://stackoverflow.com/a/52327635
    public static String getCountryCode(Context ctx) {
        String countryCode;

        // Try telephony manager first
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            // Query first getSimCountryIso()
            countryCode = tm.getSimCountryIso();
            if (countryCode != null && countryCode.length() == 2)
                return countryCode.toLowerCase();
            if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                countryCode = getCDMACountryIso();
            } else {
                countryCode = tm.getNetworkCountryIso();
            }

            if (countryCode != null && countryCode.length() == 2)
                return countryCode.toLowerCase();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            countryCode = ctx.getResources().getConfiguration().getLocales().get(0).getCountry();
        } else {
            countryCode = ctx.getResources().getConfiguration().locale.getCountry();
        }

        if (countryCode != null && countryCode.length() == 2)
            return countryCode.toLowerCase();

        return "us";
    }

    private static String getCDMACountryIso() {
        try {
            // Try to get country code from SystemProperties private class
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);

            // Get homeOperator that contain MCC + MNC
            String homeOperator = ((String) get.invoke(systemProperties, "ro.cdma.home.operator.numeric"));

            // First three characters (MCC) from homeOperator represents the country code
            int mcc = Integer.parseInt(homeOperator.substring(0, 3));

            // Mapping just countries that actually use CDMA networks
            switch (mcc) {
                case 330:
                    return "PR";
                case 310:
                    return "US";
                case 311:
                    return "US";
                case 312:
                    return "US";
                case 316:
                    return "US";
                case 283:
                    return "AM";
                case 460:
                    return "CN";
                case 455:
                    return "MO";
                case 414:
                    return "MM";
                case 619:
                    return "SL";
                case 450:
                    return "KR";
                case 634:
                    return "SD";
                case 434:
                    return "UZ";
                case 232:
                    return "AT";
                case 204:
                    return "NL";
                case 262:
                    return "DE";
                case 247:
                    return "LV";
                case 255:
                    return "UA";
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException ignored) {
        }

        return null;
    }
}
