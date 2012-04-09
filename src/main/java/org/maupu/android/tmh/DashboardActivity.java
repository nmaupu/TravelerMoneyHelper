package org.maupu.android.tmh;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.StaticData;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class DashboardActivity extends GDActivity implements OnClickListener {
	private Button goButton;
	private ImageView imageCategory;
	private ImageView imageCurrency;
	private ImageView imageAccount;
	private Class<?> addOrEditActivity;
	private boolean categoryIsOk = false;
	private boolean currencyIsOk = false;
	private boolean accountIsOk = false;
	
	private static final String CATEGORY_IS_OK = "categoryIsOk";
	private static final String CURRENCY_IS_OK = "currencyIsOk";
	private static final String ACCOUNT_IS_OK = "accountIsOk";
	
	
	public DashboardActivity() {
		super(greendroid.widget.ActionBar.Type.Empty);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		categoryIsOk = StaticData.getPreferenceValueBoolean(CATEGORY_IS_OK);
		currencyIsOk = StaticData.getPreferenceValueBoolean(CURRENCY_IS_OK);
		accountIsOk = StaticData.getPreferenceValueBoolean(ACCOUNT_IS_OK);
		
		if(! categoryIsOk || ! currencyIsOk || ! accountIsOk) {
			setActionBarContentView(R.layout.welcome_activity);
			isApplicationInit();
		} else {
	    	setActionBarContentView(R.layout.dashboard);
			setTitle(getString(R.string.app_name));
			addActionBarItem(Type.Info, TmhApplication.ACTION_BAR_INFO);
			
			
		    findViewById(R.id.dashboard_button_operations).setOnClickListener(this);
		    findViewById(R.id.dashboard_button_stats).setOnClickListener(this);
	    }
	}
	
	@Override
	protected void onResume() {
		//Toast.makeText(this, "This is onResume method", Toast.LENGTH_LONG).show();
		Category category = new Category();
		StaticData.setPreferenceValueBoolean(CATEGORY_IS_OK, category.fetchAll().getCount()>0);
		
		// There is already a currency (default one, created on startup)
		Currency currency = new Currency();
		StaticData.setPreferenceValueBoolean(CURRENCY_IS_OK, currency.fetchAll().getCount()>0);
		
		Account account = new Account();
		StaticData.setPreferenceValueBoolean(ACCOUNT_IS_OK, account.fetchAll().getCount()>0);
		
		if(isApplicationInit() && goButton != null) {
			goButton.setText(R.string.welcome_letsgo_button);
		}
		
		super.onResume();
	}
	
	private boolean isApplicationInit() {
		categoryIsOk = StaticData.getPreferenceValueBoolean(CATEGORY_IS_OK);
		currencyIsOk = StaticData.getPreferenceValueBoolean(CURRENCY_IS_OK);
		accountIsOk = StaticData.getPreferenceValueBoolean(ACCOUNT_IS_OK);
	    
		//categoryIsOk = currencyIsOk = accountIsOk = true;
		
	    if(! categoryIsOk || ! currencyIsOk || ! accountIsOk) {
	    	goButton = (Button) findViewById(R.id.button);
	    	
	    	if(goButton == null) {
	    		// Not the good activity, restart it
	    		startActivity(new Intent(this, DashboardActivity.class));
		    	finish();
		    	return false;
	    	}
	    	
			goButton.setOnClickListener(this);
			
			imageCategory = (ImageView)findViewById(R.id.category_image);
			imageCurrency = (ImageView)findViewById(R.id.currency_image);
			imageAccount  = (ImageView)findViewById(R.id.account_image);
			
			if(! categoryIsOk) {
				imageCategory.setVisibility(View.VISIBLE);
				imageCategory.setImageResource(R.drawable.puce);
				addOrEditActivity = AddOrEditCategoryActivity.class;
			} else {
				imageCategory.setVisibility(View.VISIBLE);
				imageCategory.setImageResource(R.drawable.validate);
			}
			
			if(! currencyIsOk) {
				if(categoryIsOk) {
					imageCurrency.setVisibility(View.VISIBLE);
					imageCurrency.setImageResource(R.drawable.puce);
					addOrEditActivity = AddOrEditCurrencyActivity.class;
				} else {
					imageCurrency.setVisibility(View.INVISIBLE);
				}
			} else {
				imageCurrency.setVisibility(View.VISIBLE);
				imageCurrency.setImageResource(R.drawable.validate);
			}
			
			if(! accountIsOk) {
				if(categoryIsOk && currencyIsOk) {
					imageAccount.setVisibility(View.VISIBLE);
					imageAccount.setImageResource(R.drawable.puce);
					addOrEditActivity = AddOrEditAccountActivity.class;
				} else {
					imageAccount.setVisibility(View.INVISIBLE);
				}
			} else {
				imageAccount.setVisibility(View.VISIBLE);
				imageAccount.setImageResource(R.drawable.validate);
			}
	    } else if (imageCategory != null) {
	    	// Account created !
	    	Account account = new Account();
	    	Cursor c = account.fetchAll();
	    	c.moveToFirst();
	    	account.toDTO(c);
	    	StaticData.setCurrentAccount(account);
	    	imageCategory.setImageResource(R.drawable.validate);
	    	imageCurrency.setImageResource(R.drawable.validate);
	    	imageAccount.setImageResource(R.drawable.validate);
	    }
	    
	    return categoryIsOk && currencyIsOk && accountIsOk;
	}
	
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch(item.getItemId()) {
		case TmhApplication.ACTION_BAR_INFO:
			// TODO About dialog
			break;
		default:
			return super.onHandleActionBarItemClick(item, position);
		}
		
		return true;
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		
		switch(v.getId()) {
		case R.id.dashboard_button_operations:
			intent = new Intent(DashboardActivity.this, ViewPagerOperationActivity.class);
			break;
		case R.id.dashboard_button_stats:
			intent = new Intent(DashboardActivity.this, StatsActivity.class);
			break;
		case R.id.button:
			if(! isApplicationInit()) {
				intent = new Intent(DashboardActivity.this, addOrEditActivity);
				intent.putExtra(AddOrEditActivity.EXTRA_APP_INIT, true);
			} else {
				startActivity(new Intent(DashboardActivity.this, DashboardActivity.class));
				finish();
			}
			break;
		}
		
		if(intent != null)
			startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dashboard_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.item_preferences:
			startActivity(new Intent(this, PreferencesActivity.class));
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
}
