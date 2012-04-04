package org.maupu.android.tmh;

import org.maupu.android.tmh.core.TmhApplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class TmhActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TmhApplication.getDatabaseHelper().createSampleData();
		
		/*
		Account account = new Account();
		Cursor cursor = account.fetch(2);
		account.toDTO(cursor);
		
		Category cat = new Category();
		cursor = cat.fetch(2);
		cat.toDTO(cursor);
		
		Currency cur = new Currency();
		cursor = cur.fetch(1);
		cur.toDTO(cursor);
		
		for(int i=1; i<=500; i++) {
			Operation op = new Operation();
			op.setAccount(account);
			op.setAmount(Double.valueOf(i));
			op.setCategory(cat);
			op.setCurrency(cur);
			op.setCurrencyValueOnCreated(cur.getTauxEuro());
			op.setDate(new GregorianCalendar().getTime());
			op.insert();
		}
		*/
	}
	
	@Override
	protected void onDestroy() {
		TmhApplication.getDatabaseHelper().close();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		refreshDisplay();
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.item_categories:
			startActivityFromMenu(ManageCategoryActivity.class);
			break;
		case R.id.item_currencies:
			startActivityFromMenu(ManageCurrencyActivity.class);
			break;
		case R.id.item_account:
			startActivityFromMenu(ManageAccountActivity.class);
			break;
		case R.id.item_refresh:
			refreshDisplay();
			break;
		case R.id.item_home:
			startActivityFromMenu(HomeActivity.class);
			break;
		case R.id.item_add:
			onAddClicked();
			break;
		case R.id.item_options:
			startActivityFromMenu(PreferencesActivity.class);
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
	
	private void startActivityFromMenu(Class<?> cls) {
		startActivity(new Intent(this, cls));
	}
	
	/**
	 * Called when click refresh button on menu
	 */
	public abstract void refreshDisplay();
	/**
	 * Called when add item menu is clicked
	 * @return intent used to call corresponding activity
	 */
	protected abstract Intent onAddClicked();
}
