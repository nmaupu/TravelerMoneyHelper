package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class TmhActivity extends Activity {
	protected DatabaseHelper dbHelper = new DatabaseHelper(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper.openWritable();
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
		case R.id.item_user:
			startActivityFromMenu(ManageUserActivity.class);
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
	 */
	protected abstract void onAddClicked();
}
