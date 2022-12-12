package org.maupu.android.tmh.core;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import org.maupu.android.tmh.FirstActivity;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.StaticPrefs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class TmhApplication extends Application {
    public static final String APP_NAME = "TravelerMoneyHelper";
    public static final String APP_NAME_SHORT = "tmh";
    public static final boolean LOGGING = false;
    private static Context applicationContext;
    private static DatabaseHelper dbHelper;

    public static final Class<?> HOME_ACTIVITY_CLASS = FirstActivity.class;

    @Override
    public void onCreate() {
        super.onCreate();
        TmhApplication.applicationContext = this.getApplicationContext();
        dbHelper = new DatabaseHelper(DatabaseHelper.getPreferredDatabaseName());
    }

    public static Context getAppContext() {
        return applicationContext;
    }

    public static DatabaseHelper getDatabaseHelper() {
        return dbHelper;
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
        } catch (ClassNotFoundException ignored) {
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (NullPointerException ignored) {
        }

        return null;
    }
}
