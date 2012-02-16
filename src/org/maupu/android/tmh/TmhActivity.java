package org.maupu.android.tmh;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.DatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class TmhActivity extends Activity {
	protected DatabaseHelper dbHelper = TmhApplication.getDatabaseHelper();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//dbHelper.createSampleData();
	}
	
	@Override
	protected void onDestroy() {
		if(dbHelper != null && dbHelper.getDb() != null)
			dbHelper.close();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		refreshDisplay(dbHelper);
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
			refreshDisplay(dbHelper);
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
	 * @param dbHelper
	 */
	public abstract void refreshDisplay(final DatabaseHelper dbHelper);
	/**
	 * Called when add item menu is clicked
	 * @return intent used to call corresponding activity
	 */
	protected abstract Intent onAddClicked();
}
