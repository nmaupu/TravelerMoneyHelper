package org.maupu.android.tmh;

import greendroid.widget.ActionBarItem;
import greendroid.widget.QuickActionGrid;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.ui.CustomActionBarItem;
import org.maupu.android.tmh.ui.CustomActionBarItem.CustomType;
import org.maupu.android.tmh.ui.widget.ViewPagerOperationAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class ViewPagerOperationActivity extends TmhActivity implements OnPageChangeListener {
	
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
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch(item.getItemId()) {
		case TmhApplication.ACTION_BAR_ADD:
			// Add operation
			startActivityForResult(new Intent(this, AddOrEditOperationActivity.class), 0);
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

	/*
	@Override
	protected Intent onAddClicked() {
		

		return intent;
	}*/

	@Override
	public void onPageScrollStateChanged(int position) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int position) {
		currentPosition = position;
	}
}
