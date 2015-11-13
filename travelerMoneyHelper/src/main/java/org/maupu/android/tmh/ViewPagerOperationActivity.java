package org.maupu.android.tmh;

import greendroid.widget.ActionBarItem;
import greendroid.widget.QuickActionGrid;

import java.util.Map;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.ui.CustomActionBarItem;
import org.maupu.android.tmh.ui.CustomActionBarItem.CustomType;
import org.maupu.android.tmh.ui.widget.ViewPagerOperationAdapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class ViewPagerOperationActivity extends TmhActivity implements OnPageChangeListener {
	
	private ViewPagerOperationAdapter adapter;
	private int currentPosition;
	private QuickActionGrid quickActionGrid;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setActionBarContentView(R.layout.viewpager_operation);
		setTitle("Operations");
		
		// Force portrait
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		quickActionGrid = createQuickActionGridEdition();
		
		// Action bar items
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Edit), TmhApplication.ACTION_BAR_EDIT);
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Withdrawal), TmhApplication.ACTION_BAR_ADD_WITHDRAWAL);
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Add), TmhApplication.ACTION_BAR_ADD);

		
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
	public void onPageScrollStateChanged(int position) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int position) {
		currentPosition = position;
	}
	
	@Override
	public void refreshDisplay() {
		adapter.refreshItemView(currentPosition);
	}

	// Not used
	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		return null;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> c) {
	}
}