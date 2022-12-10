package org.maupu.android.tmh;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.ViewPager;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.dialog.DatePickerDialogFragment;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.ViewPagerOperationAdapter;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public class ViewPagerOperationFragment extends TmhFragment implements ViewPager.OnPageChangeListener {
    private static final Class<ViewPagerOperationFragment> TAG = ViewPagerOperationFragment.class;
    private ViewPagerOperationAdapter adapter;
    private ViewPagerOperationAdapter adapterRaw;
    private int currentPosition;
    private ViewPager viewpager;

    private final static String STATIC_DATA_PREVIOUS_MONTH_CHOSEN = "VPO_PreviousMonthChoosen";
    private final static String STATIC_DATA_PREVIOUS_YEAR_CHOSEN = "VPO_PreviousYearChoosen";
    private final static String STATIC_DATA_PREVIOUS_DAY_CHOSEN = "VPO_PreviousDayChoosen";

    public final static int LIST_RAW = 0;
    public final static int LIST_BY_MONTH = 1;
    public final static String STATIC_DATA_LIST_STATUS = "StaticDataVPOAListStatus";

    private final static int DRAWER_ITEM_CHOOSE_MONTH = TmhApplication.getIdentifier("DRAWER_ITEM_CHOOSE_MONTH");
    private final static int DRAWER_ITEM_LIST_TYPE = TmhApplication.getIdentifier("DRAWER_ITEM_LIST_TYPE");
    private final static int DRAWER_ITEM_AUTO = TmhApplication.getIdentifier("DRAWER_ITEM_AUTO");
    private final static int DRAWER_ITEM_TODAY = TmhApplication.getIdentifier("DRAWER_ITEM_TODAY");

    private MenuProvider menuProvider;

    public ViewPagerOperationFragment() {
        super(R.layout.viewpager_operation);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().setTitle(R.string.activity_title_viewpager_operation);
        setupMenu();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Force portrait
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Setting automatically to the month corresponding to last operation for this account
        Operation dummyOp = new Operation();
        Date autoLast = dummyOp.getLastDate(StaticData.getCurrentAccount(), null);
        autoLast = autoLast == null ? DateUtil.getCurrentDate() : autoLast;

        adapter = new ViewPagerOperationAdapter(this, ViewPagerOperationAdapter.DEFAULT_COUNT, autoLast); // operations by month
        adapterRaw = new ViewPagerOperationAdapter(this, 1, null); // all operations at once

        /** Set adapter **/
        int status = StaticData.getPreferenceValueInt(STATIC_DATA_LIST_STATUS);
        if (status == -1) {
            status = LIST_BY_MONTH;
            StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, status);
        }
        ViewPagerOperationAdapter a;
        if (status == LIST_BY_MONTH) {
            a = adapter;
        } else {
            a = adapterRaw;
        }

        viewpager = (ViewPager) view.findViewById(R.id.viewpager);
        setViewpagerAdapter(a);

        viewpager.setOnPageChangeListener(this);
    }

    private void setupMenu() {
        if (menuProvider == null) {
            final TmhFragment fragment = this;
            menuProvider = new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.viewpager_operation_menu, menu);
                    menu.add(0, DRAWER_ITEM_AUTO, 1, R.string.menu_item_auto).setIcon(R.drawable.ic_event_black);
                    menu.add(0, DRAWER_ITEM_TODAY, 1, R.string.today).setIcon(R.drawable.ic_today_black);
                    menu.add(0, DRAWER_ITEM_CHOOSE_MONTH, 1, R.string.menu_item_choose_month).setIcon(R.drawable.ic_period_black);
                    menu.add(0, DRAWER_ITEM_LIST_TYPE, 1, R.string.operation_list_type).setIcon(R.drawable.ic_list_black);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    if (R.id.action_add == menuItem.getItemId()) {
                        ((MainActivity) requireActivity()).changeFragment(AddOrEditOperationFragment.class, true, null);
                    } else if (R.id.action_withdrawal == menuItem.getItemId()) {
                        // TODO withdrawl when fragment is done
                    } else if (DRAWER_ITEM_TODAY == menuItem.getItemId()) {
                        StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_BY_MONTH);
                        setViewpagerAdapter(new ViewPagerOperationAdapter(fragment));
                        refreshDisplay();
                    } else if (DRAWER_ITEM_AUTO == menuItem.getItemId()) {
                        StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_BY_MONTH);
                        Operation dummyOp = new Operation();
                        Date autoLast = dummyOp.getLastDate(StaticData.getCurrentAccount(), null);
                        setViewpagerAdapter(new ViewPagerOperationAdapter(fragment, ViewPagerOperationAdapter.DEFAULT_COUNT, autoLast));
                        refreshDisplay();
                    } else if (DRAWER_ITEM_CHOOSE_MONTH == menuItem.getItemId()) {
                        int month = StaticData.getPreferenceValueInt(STATIC_DATA_PREVIOUS_MONTH_CHOSEN);
                        int year = StaticData.getPreferenceValueInt(STATIC_DATA_PREVIOUS_YEAR_CHOSEN);
                        int day = StaticData.getPreferenceValueInt(STATIC_DATA_PREVIOUS_DAY_CHOSEN);

                        Calendar cal = Calendar.getInstance();
                        if (month == -1 || year == -1 || day == -1) {
                            // Set current date
                            cal.setTime(new GregorianCalendar().getTime());
                            year = cal.get(Calendar.YEAR);
                            month = cal.get(Calendar.MONTH);
                            day = cal.get(Calendar.DAY_OF_MONTH);
                        }

                        new DatePickerDialogFragment(year, month, day, (view, y, m, dom) -> {
                            onDateSet(y, m, dom);
                        }).show(getChildFragmentManager(), DatePickerDialogFragment.TAG);
                    } else if (DRAWER_ITEM_LIST_TYPE == menuItem.getItemId()) {
                        changeOperationsListType(StaticData.getPreferenceValueInt(STATIC_DATA_LIST_STATUS) == LIST_BY_MONTH ? LIST_RAW : LIST_BY_MONTH);
                    }

                    return true;
                }
            };
        }
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onPageScrollStateChanged(int position) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

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
    public Map<Integer, Object> handleRefreshBackground() {
        return null;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> c) {
    }

    private void changeOperationsListType(int statusType) {
        ViewPagerOperationAdapter a = this.adapter;

        switch (statusType) {
            case LIST_BY_MONTH:
                TmhLogger.d(TAG, "Setting list type to By month");
                StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_BY_MONTH);
                a = this.adapter;
                break;
            case LIST_RAW:
                TmhLogger.d(TAG, "Setting list type to All");
                StaticData.setPreferenceValueInt(STATIC_DATA_LIST_STATUS, LIST_RAW);
                a = this.adapterRaw;
                break;
        }

        setViewpagerAdapter(a);
        refreshDisplay();
    }

    public void setViewpagerAdapter(ViewPagerOperationAdapter adapter) {
        if (viewpager != null) {
            if (StaticData.getPreferenceValueInt(STATIC_DATA_LIST_STATUS) == LIST_BY_MONTH) {
                this.adapter = adapter;
            } else {
                this.adapterRaw = adapter;
            }

            viewpager.setAdapter(adapter);
            currentPosition = adapter.getCount() / 2;
            viewpager.setCurrentItem(currentPosition);
        }
    }

    // TODO Handle refresh when current account changes
    /*@Override
    public void refreshAfterCurrentAccountChanged() {
        super.refreshAfterCurrentAccountChanged();
        Operation dummyOp = new Operation();
        Date autoLast = dummyOp.getLastDate(StaticData.getCurrentAccount(), null);
        adapter = new ViewPagerOperationAdapter(this, ViewPagerOperationAdapter.DEFAULT_COUNT, autoLast); // operations by month
        adapterRaw = new ViewPagerOperationAdapter(this, 1, null); // all operations at once

        changeOperationsListType(StaticData.getPreferenceValueInt(STATIC_DATA_LIST_STATUS));
    }*/

    public void onDateSet(int year, int monthOfYear, int dayOfMonth) {
        int realMonth = monthOfYear + 1;
        TmhLogger.d(TAG, "Date has been set to : " + year + "/" + realMonth + "/" + dayOfMonth);
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
