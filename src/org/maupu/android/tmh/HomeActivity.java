package org.maupu.android.tmh;

import java.util.Date;
import java.util.GregorianCalendar;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {
	//private static String PREF_KEY_APP_INIT;
	
	private DatabaseHelper dbHelper = TmhApplication.getDatabaseHelper();
	//private Account selectedAccount;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(R.layout.main);
		//customTB.setName("Operation");
		//customTB.setIcon(R.drawable.ic_stat_categories);
		
		// initialize if needed
		init();
				
		addTab(ViewPagerOperationActivity.class, R.string.tab_operation, R.drawable.ic_tab_home);
		//addTab(ManageOperationActivity.class, R.string.tab_credit, R.drawable.ic_tab_credit);
		addTab(StatsActivity.class, R.string.tab_stats, R.drawable.ic_tab_trombone);

		//getTabHost().setCurrentTab(1);
	}

	private void addTab(Class<?> activity, int titleId, int drawableId) {
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		intent = new Intent().setClass(this, activity);
		spec = tabHost.newTabSpec(getString(titleId)).setIndicator(getString(titleId),
				res.getDrawable(drawableId)).setContent(intent);
		tabHost.addTab(spec);
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
			currency.setTauxEuro(1.0f);
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
}