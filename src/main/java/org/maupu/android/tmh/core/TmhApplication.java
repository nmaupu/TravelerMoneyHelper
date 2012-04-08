package org.maupu.android.tmh.core;

import greendroid.app.GDApplication;

import java.util.GregorianCalendar;

import org.maupu.android.tmh.DashboardActivity;
import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;

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
		init();
	}
	
	/**
	 * Called when app init. Creates required database objects and show first launch activity
	 */
	private void init() {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//boolean isAlreadyInit = prefs.getBoolean(PREF_KEY_APP_INIT, false);
		Account account = new Account();
		int nb = account.getCount();
		
		//if(! isAlreadyInit || nb == 0) {
		if(nb == 0) {
			// initialization
			// Create default category
			Category category = new Category();
			category.setName(getString(R.string.withdrawal));
			category.insert();
			
			// Adding default currency (euro)
			Currency currency = new Currency();
			currency.setIsoCode("EUR");
			currency.setLastUpdate(new GregorianCalendar().getTime());
			currency.setLongName("Euro");
			java.util.Currency c = java.util.Currency.getInstance("EUR");
			currency.setShortName(c.getSymbol());
			currency.setTauxEuro(1.0d);
			currency.insert();
			
			// Creating a default account in euro
			account.setCurrency(currency);
			account.setName(getString(R.string.default_account_name));
			account.insert();
			
			/*
			Editor editor = prefs.edit();
			editor.putBoolean(PREF_KEY_APP_INIT, true);
			editor.commit();
			*/
		}
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
