package org.maupu.android.tmh;

import greendroid.widget.ActionBarItem;
import greendroid.widget.QuickActionGrid;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.ui.CustomActionBarItem;
import org.maupu.android.tmh.ui.CustomActionBarItem.CustomType;
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
	
	private static final int menuItemWidthdrawalId = 1234;
	private ViewPagerOperationAdapter adapter;
	private int currentPosition;
	private QuickActionGrid quickActionGrid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setActionBarContentView(R.layout.viewpager_operation);
		setTitle("Operations");

		quickActionGrid = createQuickActionGridEdition();
		
		// Action bar items
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getActionBar(), CustomType.Edit), TmhApplication.ACTION_BAR_EDIT);
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getActionBar(), CustomType.Withdrawal), TmhApplication.ACTION_BAR_ADD_WITHDRAWAL);
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getActionBar(), CustomType.Add), TmhApplication.ACTION_BAR_ADD);

		
		adapter = new ViewPagerOperationAdapter(this);
		ViewPager vp = (ViewPager)findViewById(R.id.viewpager);
		vp.setAdapter(adapter);
		currentPosition = adapter.getCount()/2;
		vp.setCurrentItem(currentPosition);

		vp.setOnPageChangeListener(this);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Recreate menu to add withdrawal entry
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		menu.add(Menu.NONE, menuItemWidthdrawalId, Menu.NONE, R.string.menu_item_withdrawal).setIcon(R.drawable.ic_menu_set_as);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == menuItemWidthdrawalId) {
			Log.d("ViewPagerOperationAdapter", "Starting withdrawal activity");
			Intent intent = new Intent(this, WithdrawalActivity.class);
			startActivity(intent);
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch(item.getItemId()) {
		case TmhApplication.ACTION_BAR_ADD:
			onAddClicked();
			break;
		case TmhApplication.ACTION_BAR_EDIT:
			quickActionGrid.show(item.getItemView());
			break;
		case TmhApplication.ACTION_BAR_ADD_WITHDRAWAL:
			startActivityFromMenu(WithdrawalActivity.class);
			break;
		default:
			return super.onHandleActionBarItemClick(item, position);	
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("onActivityResult", "message="+resultCode);
		refreshDisplay();
	}

	@Override
	public void refreshDisplay() {
		adapter.refreshItemView(currentPosition);
	}

	@Override
	protected Intent onAddClicked() {
		// Add operation
		Intent intent = new Intent(this, AddOrEditOperationActivity.class);
		intent.putExtra("account", StaticData.getCurrentAccount());
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
