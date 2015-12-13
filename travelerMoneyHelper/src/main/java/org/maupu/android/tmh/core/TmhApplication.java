package org.maupu.android.tmh.core;

import org.maupu.android.tmh.FirstActivity;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.ui.StaticData;

import android.app.Application;
import android.content.Context;

public class TmhApplication extends Application {
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
		if(dbName != null && !"".equals(dbName) && ! dbName.startsWith(DatabaseHelper.DATABASE_PREFIX)) {
			name = DatabaseHelper.DATABASE_PREFIX+dbName;
		}
		
		dbHelper.close();
		dbHelper = new DatabaseHelper(name);
		dbHelper.getDb();
		
		// Invalidate StaticData
		StaticData.setCurrentAccount(null);
		StaticData.setCurrentSelectedCategory(null);
		StaticData.setWithdrawalCategory(null);
		StaticData.setMainCurrency(null);
	}

    public static int getIdentifier(String s) {
        return s == null ? -1 : Math.abs(s.hashCode());
    }
}
