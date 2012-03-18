package org.maupu.android.tmh.core;

import org.maupu.android.tmh.database.DatabaseHelper;

import android.app.Application;
import android.content.Context;

public class TmhApplication extends Application {
	private static Context applicationContext;
	private static DatabaseHelper dbHelper;
	
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
	}
}
