package org.maupu.android.tmh.core;

import greendroid.app.GDApplication;

import org.maupu.android.tmh.DashboardActivity;
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
	
	
	public static final Class<?> HOME_ACTIVITY_CLASS = DashboardActivity.class;
	
	@Override
	public Class<?> getHomeActivityClass() {
		return HOME_ACTIVITY_CLASS;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		TmhApplication.applicationContext = this.getApplicationContext();
		dbHelper = new DatabaseHelper(DatabaseHelper.getPreferredDatabaseName());
		//initDefaultCurrency();
	}
	
	/*
	private Currency initDefaultCurrency() {
		// Adding default currency (euro) if needed
		Currency currency = new Currency();
		Cursor cursor = currency.fetchAll();
		if(cursor.getCount() > 0)
			return null;
		
		
		currency.setIsoCode("EUR");
		currency.setLastUpdate(new GregorianCalendar().getTime());
		currency.setLongName("Euro");
		java.util.Currency c = java.util.Currency.getInstance("EUR");
		currency.setShortName(c.getSymbol());
		currency.setRateCurrencyLinked(1.0d);
		currency.insert();
		
		return currency;
	}*/
	
	public static Context getAppContext() {
		return applicationContext;
	}
	
	public static DatabaseHelper getDatabaseHelper() {
		return dbHelper;
	}
	
	public static void changeOrCreateDatabase(Context ctx, String dbName) {
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
