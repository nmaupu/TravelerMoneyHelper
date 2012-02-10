package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.ViewPagerOperationAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ViewPagerOperationActivity extends TmhActivity implements OnPageChangeListener {
	private static final int menuItemFilterId = 1234;
	private ViewPagerOperationAdapter adapter;
	private int currentPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main2);

		adapter = new ViewPagerOperationAdapter(this, dbHelper);
		ViewPager vp = (ViewPager)findViewById(R.id.viewpager);
		vp.setAdapter(adapter);
		currentPosition = adapter.getCount()/2;
		vp.setCurrentItem(currentPosition);

		vp.setOnPageChangeListener(this);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Recreate menu to add filter icon
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		menu.add(Menu.NONE, menuItemFilterId, Menu.NONE, R.string.menu_item_filter).setIcon(R.drawable.ic_menu_view);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == menuItemFilterId) {
			Log.d("ViewPagerOperationAdapter", "Filter menu item clicked");
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("onActivityResult", "message="+resultCode);
		refreshDisplay(dbHelper);
	}

	@Override
	public void refreshDisplay(DatabaseHelper dbHelper) {
		adapter.refreshItemView(currentPosition);
	}

	@Override
	protected Intent onAddClicked() {
		// Add operation
		Intent intent = new Intent(this, AddOrEditOperationActivity.class);
		intent.putExtra("account", StaticData.getCurrentAccount(this, dbHelper));
		this.startActivityForResult(intent, 0);
		
		return intent;
	}

	@Override
	public void onPageScrollStateChanged(int position) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int position) {
		currentPosition = position;
	}
}
