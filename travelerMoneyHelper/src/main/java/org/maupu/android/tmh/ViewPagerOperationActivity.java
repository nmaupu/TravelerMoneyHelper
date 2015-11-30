package org.maupu.android.tmh;

import greendroid.widget.ActionBarItem;

import java.util.Map;
import java.util.UUID;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.ui.CustomActionBarItem;
import org.maupu.android.tmh.ui.CustomActionBarItem.CustomType;
import org.maupu.android.tmh.ui.NavigationDrawerIconItem;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.ViewPagerOperationAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class ViewPagerOperationActivity extends TmhActivity implements OnPageChangeListener {

    private ViewPagerOperationAdapter adapter;
    private ViewPagerOperationAdapter adapterRaw;
	private int currentPosition;

    private final static int LIST_RAW = 0;
    private final static int LIST_BY_MONTH = 1;
    private final static String STATIC_DATA_LIST_STATUS = "StaticDataVPOAListStatus";

    private final static String DRAWER_ITEM_LIST_TYPE = UUID.randomUUID().toString();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setActionBarContentView(R.layout.viewpager_operation);
		setTitle(getContentView().getResources().getString(R.string.activity_title_viewpager_operation));
		
		// Force portrait
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// Action bar items
		//addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Edit), TmhApplication.ACTION_BAR_EDIT);
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Withdrawal), TmhApplication.ACTION_BAR_ADD_WITHDRAWAL);
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Add), TmhApplication.ACTION_BAR_ADD);

		adapter = new ViewPagerOperationAdapter(this); // operations by month
        adapterRaw = new ViewPagerOperationAdapter(this, 1, null); // all operations

        /** Set adapter **/
        int status = StaticData.getPreferenceValueInt(STATIC_DATA_LIST_STATUS);
        if(status == -1) {
            status = LIST_BY_MONTH;
            StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, status);
        }
        ViewPagerOperationAdapter a;
        if(status == LIST_BY_MONTH) {
            a = adapter;
        } else {
            a = adapterRaw;
        }

		ViewPager vp = (ViewPager)findViewById(R.id.viewpager);
		vp.setAdapter(a);
		currentPosition = a.getCount()/2;
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
		//case TmhApplication.ACTION_BAR_EDIT:
		//	quickActionGrid.show(item.getItemView());
		//	break;
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
        ViewPager vp = (ViewPager)findViewById(R.id.viewpager);
        ((ViewPagerOperationAdapter)vp.getAdapter()).refreshItemView(currentPosition);
	}

	// Not used
	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		return null;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> c) {
	}

    private void changeOperationsListType(int statusType) {
        ViewPager vp = (ViewPager)findViewById(R.id.viewpager);
        ViewPagerOperationAdapter a = adapter;

        switch(statusType) {
            case LIST_BY_MONTH:
                StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_BY_MONTH);
                a = this.adapter;
                break;
            case LIST_RAW:
                StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_RAW);
                a = this.adapterRaw;
                break;
        }

        vp.setAdapter(a);
        currentPosition = a.getCount()/2;
        vp.setCurrentItem(currentPosition);
        refreshDisplay();
    }

    @Override
    public NavigationDrawerIconItem[] buildNavigationDrawer() {
        return new NavigationDrawerIconItem[] {
                createSmallNavigationDrawerItem(
                        DRAWER_ITEM_LIST_TYPE,
                        R.drawable.ic_list_black,
                        R.string.operation_list_type),
        };
    }

    @Override
    public void onNavigationDrawerClick(NavigationDrawerIconItem item) {
        super.onNavigationDrawerClick(item);
        if(item.getTag() == DRAWER_ITEM_LIST_TYPE) {
            changeOperationsListType(
                    StaticData.getPreferenceValueInt(STATIC_DATA_LIST_STATUS) == LIST_BY_MONTH ? LIST_RAW : LIST_BY_MONTH
            );
        }
    }
}
