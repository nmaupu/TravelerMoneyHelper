package org.maupu.android.tmh;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Inflater;

import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.NavigationDrawerIconItem;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.CustomDatePickerDialog;
import org.maupu.android.tmh.ui.widget.IconArrayAdapter;
import org.maupu.android.tmh.ui.widget.ViewPagerOperationAdapter;
import org.maupu.android.tmh.util.DateUtil;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MenuItem;
import android.widget.DatePicker;

public class ViewPagerOperationActivity extends TmhActivity implements OnPageChangeListener, DatePickerDialog.OnDateSetListener {

    private ViewPagerOperationAdapter adapter;
    private ViewPagerOperationAdapter adapterRaw;
	private int currentPosition;
    private ViewPager viewpager;

    private final static String STATIC_DATA_PREVIOUS_MONTH_CHOSEN = "VPA_PreviousMonthChoosen";
    private final static String STATIC_DATA_PREVIOUS_YEAR_CHOSEN = "VPA_PreviousYearChoosen";
    private final static String STATIC_DATA_PREVIOUS_DAY_CHOSEN = "VPA_PreviousDayChoosen";

    public final static int LIST_RAW = 0;
    public final static int LIST_BY_MONTH = 1;
    public final static String STATIC_DATA_LIST_STATUS = "StaticDataVPOAListStatus";

    private final static String DRAWER_ITEM_LIST_TYPE = UUID.randomUUID().toString();
    private final static String DRAWER_ITEM_CHOOSE_MONTH = UUID.randomUUID().toString();
    private final static String DRAWER_ITEM_AUTO = UUID.randomUUID().toString();
    private final static String DRAWER_ITEM_TODAY = UUID.randomUUID().toString();

    public ViewPagerOperationActivity() {
        super(R.layout.viewpager_operation, R.string.activity_title_viewpager_operation);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// Force portrait
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*
		// Action bar items
		//addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Edit), TmhApplication.ACTION_BAR_EDIT);
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Withdrawal), TmhApplication.ACTION_BAR_ADD_WITHDRAWAL);
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Add), TmhApplication.ACTION_BAR_ADD);
		*/

        // Setting automatically to the month corresponding to last operation for this account
        Operation dummyOp = new Operation();
        Date autoLast = dummyOp.getLastDate(StaticData.getCurrentAccount(), null);
        autoLast = autoLast == null ? DateUtil.getCurrentDate() : autoLast;

		adapter = new ViewPagerOperationAdapter(this, ViewPagerOperationAdapter.DEFAULT_COUNT, autoLast); // operations by month
        adapterRaw = new ViewPagerOperationAdapter(this, 1, null); // all operations at once

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

		viewpager = (ViewPager)findViewById(R.id.viewpager);
		setViewpagerAdapter(a);

		viewpager.setOnPageChangeListener(this);
	}

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure nav drawer operation item is selected
        Integer position = getPositionNavigationDrawerItem(TmhActivity.DRAWER_ITEM_OPERATIONS);
        if(position == null)
            position = 0;
        ((IconArrayAdapter) super.drawerList.getAdapter()).selectItem(position);
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(ViewPagerOperationActivity.class.getName(), "onActivityResult resultCode : " + resultCode);
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
        ((ViewPagerOperationAdapter) viewpager.getAdapter()).refreshItemView(currentPosition);
	}

	// Not used
	@Override
	public Map<Integer, Object> handleRefreshBackground() { return null; }
	@Override
	public void handleRefreshEnding(Map<Integer, Object> c) { }

    private void changeOperationsListType(int statusType) {
        ViewPagerOperationAdapter a = this.adapter;

        switch(statusType) {
            case LIST_BY_MONTH:
                Log.d(ViewPagerOperationActivity.class.getName(), "Setting list type to By month");
                StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_BY_MONTH);
                a = this.adapter;
                break;
            case LIST_RAW:
                Log.d(ViewPagerOperationActivity.class.getName(), "Setting list type to All");
                StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_RAW);
                a = this.adapterRaw;
                break;
        }

        setViewpagerAdapter(a);
        refreshDisplay();
    }

    public void setViewpagerAdapter(ViewPagerOperationAdapter adapter) {
        if(viewpager != null) {
            if(StaticData.getPreferenceValueInt(STATIC_DATA_LIST_STATUS) == LIST_BY_MONTH) {
                this.adapter = adapter;
            } else {
                this.adapterRaw = adapter;
            }

            viewpager.setAdapter(adapter);
            currentPosition = adapter.getCount() / 2;
            viewpager.setCurrentItem(currentPosition);
        }
    }

    /**
     * Call to refresh view when static current account is changed
     */
    public void notifyChangeCurrentAccount() {
        Operation dummyOp = new Operation();
        Date autoLast = dummyOp.getLastDate(StaticData.getCurrentAccount(), null);
        adapter = new ViewPagerOperationAdapter(this, ViewPagerOperationAdapter.DEFAULT_COUNT, autoLast); // operations by month
        adapterRaw = new ViewPagerOperationAdapter(this, 1, null); // all operations at once

        changeOperationsListType(StaticData.getPreferenceValueInt(STATIC_DATA_LIST_STATUS));
    }

    @Override
    public NavigationDrawerIconItem[] buildNavigationDrawer() {
        return new NavigationDrawerIconItem[] {
                createSmallNavigationDrawerItem(
                        DRAWER_ITEM_AUTO,
                        R.drawable.ic_event_black,
                        R.string.menu_item_auto),
                createSmallNavigationDrawerItem(
                        DRAWER_ITEM_TODAY,
                        R.drawable.ic_today_black,
                        R.string.today),
                createSmallNavigationDrawerItem(
                        DRAWER_ITEM_CHOOSE_MONTH,
                        R.drawable.ic_period_black,
                        R.string.menu_item_choose_month),
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
        } else if(item.getTag() == DRAWER_ITEM_CHOOSE_MONTH) {
            int month = StaticData.getPreferenceValueInt(STATIC_DATA_PREVIOUS_MONTH_CHOSEN);
            int year = StaticData.getPreferenceValueInt(STATIC_DATA_PREVIOUS_YEAR_CHOSEN);
            int day = StaticData.getPreferenceValueInt(STATIC_DATA_PREVIOUS_DAY_CHOSEN);

            Calendar cal = Calendar.getInstance();
            if(month == -1 || year == -1 || day == -1) {
                // Set current date
                cal.setTime(new GregorianCalendar().getTime());
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
            }

            new CustomDatePickerDialog(this, this, year, month, day).show();
        } else if(item.getTag() == DRAWER_ITEM_AUTO) {
            StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_BY_MONTH);
            Operation dummyOp = new Operation();
            Date autoLast = dummyOp.getLastDate(StaticData.getCurrentAccount(), null);
            setViewpagerAdapter(new ViewPagerOperationAdapter(this, ViewPagerOperationAdapter.DEFAULT_COUNT, autoLast));
            refreshDisplay();
        } else if(item.getTag() == DRAWER_ITEM_TODAY) {
            StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_BY_MONTH);
            setViewpagerAdapter(new ViewPagerOperationAdapter(this));
            refreshDisplay();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        int realMonth = monthOfYear+1;
        Log.d(ViewPagerOperationActivity.class.getName(), "Date has been set to : " + year + "/" + realMonth + "/" + dayOfMonth);
        StaticData.setPreferenceValueInt(STATIC_DATA_PREVIOUS_MONTH_CHOSEN, monthOfYear);
        StaticData.setPreferenceValueInt(STATIC_DATA_PREVIOUS_YEAR_CHOSEN, year);
        StaticData.setPreferenceValueInt(STATIC_DATA_PREVIOUS_DAY_CHOSEN, dayOfMonth);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime());

        StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_BY_MONTH);
        setViewpagerAdapter(
                new ViewPagerOperationAdapter(this, ViewPagerOperationAdapter.DEFAULT_COUNT, cal.getTime())
        );
        refreshDisplay();
    }
}
