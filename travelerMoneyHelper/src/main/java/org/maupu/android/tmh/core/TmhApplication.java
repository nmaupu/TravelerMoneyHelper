package org.maupu.android.tmh.core;

import greendroid.app.GDApplication;

import org.maupu.android.tmh.ViewPagerOperationActivity;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.ui.StaticData;

import android.content.Context;

public class TmhApplication extends GDApplication {
	private static Context applicationContext;
	private static DatabaseHelper dbHelper;
	
	public static final int ACTION_BAR_ADD = 0;
	public static final int ACTION_BAR_EDIT = 1;
	public static final int ACTION_BAR_ADD_WITHDRAWAL = 2;
	public static final int ACTION_BAR_SAVE = 3;
	public static final int ACTION_BAR_SAVE_AND_ADD = 4;
	public static final int ACTION_BAR_CANCEL = 5;
	public static final int ACTION_BAR_INFO = 6;
	public static final int ACTION_BAR_GROUPBY = 7;
	public static final int ACTION_BAR_ACCOUNT = 8;
	public static final int ACTION_BAR_GRAPH = 9;
	
	
	public static final Class<?> HOME_ACTIVITY_CLASS = ViewPagerOperationActivity.class;

	@Override
	public void onCreate() {
		super.onCreate();
		TmhApplication.applicationContext = this.getApplicationContext();
		dbHelper = new DatabaseHelper(DatabaseHelper.getPreferredDatabaseName());
		//initDefaultCurrency();
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
}
