package org.maupu.android.tmh;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.tabs.TabLayout;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apmem.tools.layouts.FlowLayout;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.stats.CategoriesLineChart;
import org.maupu.android.tmh.stats.CategoriesPieChart;
import org.maupu.android.tmh.stats.InfoPanel;
import org.maupu.android.tmh.stats.StatsCategoryValues;
import org.maupu.android.tmh.stats.StatsData;
import org.maupu.android.tmh.stats.StatsViewPager;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;
import org.maupu.android.tmh.ui.widget.CustomDatePickerDialog;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class representing stats activity
 */
public class StatsActivity extends TmhActivity {
    private static final Class<StatsActivity> TAG = StatsActivity.class;
    private final static int DRAWER_ITEM_STATS_AUTO = TmhApplication.getIdentifier("DRAWER_ITEM_STATS_AUTO");
    private final static int DRAWER_ITEM_STATS_DATE_BEGIN = TmhApplication.getIdentifier("DRAWER_ITEM_STATS_DATE_BEGIN");
    private final static int DRAWER_ITEM_STATS_DATE_END = TmhApplication.getIdentifier("DRAWER_ITEM_STATS_DATE_END");
    private final static int DRAWER_ITEM_STATS_DATE_RESET = TmhApplication.getIdentifier("DRAWER_ITEM_STATS_DATE_RESET");
    private final static int DIALOG_DATE_BEGIN = TmhApplication.getIdentifier("DIALOG_DATE_BEGIN");
    private final static int DIALOG_DATE_END = TmhApplication.getIdentifier("DIALOG_DATE_END");
    private final int max_cat_displayed = 4;
    private SlidingUpPanelLayout slidingPanel;
    private Date dateBegin, dateEnd;

    /**
     * Primary panel
     **/
    private TextView tvMisc;
    private CategoriesPieChart pieChart;
    private CategoriesLineChart detailsChart;
    private InfoPanel infoPanel;

    // Charts data handling
    public static final int[] MY_JOYFUL_COLORS = {
            Color.rgb(217, 80, 138), Color.rgb(254, 149, 7), Color.rgb(170, 164, 80),
            Color.rgb(106, 167, 134), Color.rgb(53, 194, 209)
    };
    private final StatsData statsData = new StatsData(MY_JOYFUL_COLORS);

    // Categories list and chooser
    private FlowLayout layoutCategories;

    /**
     * Secondary panel
     **/
    private TextView tvDateBegin, tvDateEnd;
    private TextView tvDuration;
    private ImageView accountImage;
    private TextView accountName;
    private TabLayout tabLayout;
    private StatsViewPager statsViewPager;

    public StatsActivity() {
        super(R.layout.stats, R.string.fragment_title_statistics);
    }

    @Override
    public int whatIsMyDrawerIdentifier() {
        return TmhActivity.DRAWER_ITEM_STATS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TmhLogger.d(TAG, "onCreate called");
        try {
            super.onCreate(savedInstanceState);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }


        slidingPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingPanel.setDragView(R.id.layout_title);

        /** Primary panel **/
        tvMisc = (TextView) findViewById(R.id.misc);
        tvDateBegin = (TextView) findViewById(R.id.date_begin);
        tvDateEnd = (TextView) findViewById(R.id.date_end);
        tvDuration = (TextView) findViewById(R.id.duration);

        // Categories chooser
        layoutCategories = (FlowLayout) findViewById(R.id.layout_categories);

        // info
        infoPanel = new InfoPanel(this);
        statsData.addOnStatsDataChangedListener(infoPanel);
        infoPanel.initPanel();

        // Charts
        initPieChart();
        initDetailsChart();

        /** Secondary panel **/
        accountImage = (ImageView) findViewById(R.id.account_image);
        accountName = (TextView) findViewById(R.id.account_name);
        statsViewPager = (StatsViewPager) findViewById(R.id.viewpager);
        statsViewPager.initPanel();
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(statsViewPager);
        statsData.addOnStatsDataChangedListener(statsViewPager);

        statsData.setCatToHighlight(null);
        reloadInputData();
        if (!loadDates())
            autoSetDates();
        rebuildStatsData(true, false);
        refreshDisplay();
    }

    @Override
    protected void onDestroy() {
        TmhLogger.d(TAG, "onDestroy called");
        // Called when exiting / changing screen orientation
        super.onDestroy();
    }

    private void initPieChart() {
        pieChart = (CategoriesPieChart) findViewById(R.id.pie_chart);
        statsData.addOnStatsDataChangedListener(pieChart);
        pieChart.initPanel();
        final TmhActivity thisInstance = this;
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(final Entry e, int dataSetIndex, Highlight h) {
                TmhLogger.d(TAG, "Highlight : x=" + h.getXIndex() + ", dataset=" + h.getDataSetIndex());
                final boolean cur = statsData.isChartAnim();
                statsData.disableChartAnim();

                IAsyncActivityRefresher refresher = new IAsyncActivityRefresher() {
                    @Override
                    public Map<Integer, Object> handleRefreshBackground() {
                        StatsCategoryValues scv = (StatsCategoryValues) e.getData();
                        statsData.setCatToHighlight(scv.getFirstCategory());
                        // TODO : Don't redraw everything but replace by a highlight method
                        detailsChart.refreshPanel(statsData);
                        infoPanel.refreshPanel(statsData);

                        return null;
                    }

                    @Override
                    public void handleRefreshEnding(Map<Integer, Object> results) {
                        statsData.setChartAnim(cur);
                    }
                };

                AsyncActivityRefresher asyncTask = new AsyncActivityRefresher(thisInstance, refresher, false);
                asyncTask.execute();
            }

            @Override
            public void onNothingSelected() {
                final boolean cur = statsData.isChartAnim();
                statsData.disableChartAnim();

                IAsyncActivityRefresher refresher = new IAsyncActivityRefresher() {
                    @Override
                    public Map<Integer, Object> handleRefreshBackground() {
                        statsData.setCatToHighlight(null);
                        // TODO : Don't redraw everything but replace by a highlight method
                        detailsChart.refreshPanel(statsData);
                        infoPanel.refreshPanel(statsData);

                        return null;
                    }

                    @Override
                    public void handleRefreshEnding(Map<Integer, Object> results) {
                        statsData.setChartAnim(cur);
                    }
                };

                AsyncActivityRefresher asyncTask = new AsyncActivityRefresher(thisInstance, refresher, false);
                asyncTask.execute();
            }
        });
    }

    private void initDetailsChart() {
        detailsChart = (CategoriesLineChart) findViewById(R.id.details_chart);
        statsData.addOnStatsDataChangedListener(detailsChart);
        detailsChart.initPanel();
    }

    @Override
    public IDrawerItem[] buildNavigationDrawer() {
        return new IDrawerItem[]{
                createSecondaryDrawerItem(
                        DRAWER_ITEM_STATS_DATE_BEGIN,
                        R.drawable.ic_today_black,
                        R.string.date_change_begin),
                createSecondaryDrawerItem(
                        DRAWER_ITEM_STATS_DATE_END,
                        R.drawable.ic_event_black,
                        R.string.date_change_end),
                createSecondaryDrawerItem(
                        DRAWER_ITEM_STATS_DATE_RESET,
                        R.drawable.ic_refresh_black,
                        R.string.date_change_reset),
                createSecondaryDrawerItem(
                        DRAWER_ITEM_STATS_AUTO,
                        R.drawable.ic_cat_except_black,
                        R.string.stats_item_cat_auto),
        };
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem item) {
        if (item.getIdentifier() == DRAWER_ITEM_STATS_AUTO) {
            statsData.setCatToHighlight(null);
            autoSetExceptedCategories();
            rebuildStatsData(true, false);
            refreshDisplay();
        } else if (item.getIdentifier() == DRAWER_ITEM_STATS_DATE_BEGIN) {
            showDialog(DIALOG_DATE_BEGIN);
        } else if (item.getIdentifier() == DRAWER_ITEM_STATS_DATE_END) {
            showDialog(DIALOG_DATE_END);
        } else if (item.getIdentifier() == DRAWER_ITEM_STATS_DATE_RESET) {
            autoSetDates();
            rebuildStatsData(true, false);
            refreshDisplay();
        }

        return super.onItemClick(view, position, item);
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        int y, m, d;

        Calendar cal = Calendar.getInstance();

        if (id == DIALOG_DATE_BEGIN) {
            cal.setTime(dateBegin);
        } else if (id == DIALOG_DATE_END) {
            cal.setTime(dateEnd);
        }

        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DAY_OF_MONTH);

        final Context thisInstance = this;
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Date d = cal.getTime();

                if (id == DIALOG_DATE_BEGIN) {
                    Date dateToSet = DateUtil.resetDateToBeginingOfDay(d);
                    if (dateToSet.before(dateEnd))
                        dateBegin = dateToSet;
                    else
                        Toast.makeText(thisInstance, getString(R.string.error_date_begin_after_end), Toast.LENGTH_SHORT).show();
                    TmhLogger.d(TAG, "Changing date begin to : " + dateBegin);
                } else if (id == DIALOG_DATE_END) {
                    Date dateToSet = DateUtil.resetDateToEndOfDay(d);
                    if (dateToSet.after(dateBegin))
                        dateEnd = dateToSet;
                    else
                        Toast.makeText(thisInstance, getString(R.string.error_date_end_before_begin), Toast.LENGTH_SHORT).show();
                    TmhLogger.d(TAG, "Changing date end to : " + dateEnd);
                }

                saveDates();
                refreshDates();
                rebuildStatsData(true, false);
                refreshDisplay();
            }
        };

        return new CustomDatePickerDialog(this, listener, y, m, d);
    }


    /**
     * Reinit all inputs in the view (exceptedCategories, dates)
     */
    public void resetInputData() {
        autoSetExceptedCategories();
        autoSetDates();
    }

    /**
     * Reload input data from saved changes (exceptedCategories, dates)
     */
    public void reloadInputData() {
        if (!loadDates())
            autoSetDates();

        // If excepted cat is null, trying to auto set them
        if (StaticData.getStatsExceptedCategories().size() == 0)
            autoSetExceptedCategories();
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        Account currentAccount = StaticData.getCurrentAccount();

        if (currentAccount != null) {
            ImageViewHelper.setIcon(this, accountImage, currentAccount.getIcon());
            accountName.setText(currentAccount.getName());
        } else {
            accountImage.setImageResource(R.drawable.tmh_icon_48);
            accountName.setText("");
        }

        statsData.enableChartAnim();
        infoPanel.refreshPanel(statsData);
        statsViewPager.refreshPanel(statsData);
        pieChart.refreshPanel(statsData);
        detailsChart.refreshPanel(statsData);
        refreshLayoutCategories();
        refreshDates();
    }

    @Override
    public void refreshAfterCurrentAccountChanged() {
        autoSetDates();
        statsData.setCatToHighlight(null);
        resetInputData();
        rebuildStatsData(true, false);
        refreshDisplay();
    }

    private void autoSetDates() {
        Account currentAccount = StaticData.getCurrentAccount();
        if (currentAccount == null)
            return;

        // Begin date
        Operation dummyOp = new Operation();
        dateBegin = dummyOp.getFirstDate(currentAccount, StaticData.getStatsExceptedCategoriesToArray());
        if (dateBegin == null) // No operation yet
            return;
        dateBegin = DateUtil.resetDateToBeginingOfDay(dateBegin);

        Date now = DateUtil.getCurrentDate();
        DateUtil.resetDateToEndOfDay(now);

        // Set end date to yesterday (avoid computing averages with current day as it is not finished yet)
        Date d = dummyOp.getLastDate(currentAccount, StaticData.getStatsExceptedCategoriesToArray());
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        /*
        In the case of last date is before now, we have to include the last day and not
        sub 1 to it (no more data in db and now > d)
        (perhaps travel finished or too early in the next day and no new data available yet)
        */
        Date dateToSet = d.before(now) ? d : cal.getTime();

        /*
        Setting dateEnd to yesterday if we are still after dateBegin
        Otherwise, this means that operations are on only one day.
        */
        dateEnd = dateToSet.after(dateBegin) ? dateToSet : d;
        dateEnd = DateUtil.resetDateToEndOfDay(dateEnd);

        // Save dates for further use
        saveDates();

        // Refresh dates display
        refreshDates();

        TmhLogger.d(TAG, "Auto dates computed beg=" + dateBegin + ", end=" + dateEnd);
    }

    private void refreshDates() {
        if (dateBegin != null && dateEnd != null) {
            tvDateBegin.setText(DateUtil.dateToStringNoTime(dateBegin));
            tvDateEnd.setText(DateUtil.dateToStringNoTime(dateEnd));
            tvDuration.setText(
                    getResources().getString(R.string.duration) + " : " +
                            DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd) + " " +
                            getResources().getString(R.string.day_));
        }
    }

    private boolean loadDates() {
        Date db = StaticData.getDateField(StaticData.PREF_STATS_DATE_BEG);
        Date de = StaticData.getDateField(StaticData.PREF_STATS_DATE_END);
        if (db != null && de != null) {
            dateBegin = db;
            dateEnd = de;
        }

        // Verifying if saved dates are within account dates range
        Operation dummyOp = new Operation();
        Date dateFirstOperation = dummyOp.getFirstDate(StaticData.getCurrentAccount(), null);
        Date dateLastOperation = dummyOp.getLastDate(StaticData.getCurrentAccount(), null);
        if (dateFirstOperation != null) { // Some operations on that account
            dateFirstOperation = DateUtil.resetDateToBeginingOfDay(dateFirstOperation);
        }
        if (dateLastOperation != null) {
            dateLastOperation = DateUtil.resetDateToEndOfDay(dateLastOperation);
        }

        return db != null && de != null && de.after(db) && (dateFirstOperation == null || (dateFirstOperation.after(de))) && (dateLastOperation == null || dateLastOperation.before(db));
    }

    private void saveDates() {
        StaticData.setDateField(StaticData.PREF_STATS_DATE_BEG, dateBegin);
        StaticData.setDateField(StaticData.PREF_STATS_DATE_END, dateEnd);
    }

    private void autoSetExceptedCategories() {
        autoSetExceptedCategories(null);
    }

    private void autoSetExceptedCategories(Set<Integer> currentExceptedToKeep) {
        try {
            Operation o = new Operation();
            Integer[] cats = o.getExceptCategoriesAuto(StaticData.getCurrentAccount());
            Set<Integer> exceptedCats = new HashSet<>(Arrays.asList(cats));

            if (currentExceptedToKeep != null && currentExceptedToKeep.size() > 0)
                exceptedCats.addAll(currentExceptedToKeep);

            StaticData.getStatsExceptedCategories().clear();
            StaticData.getStatsExceptedCategories().addAll(exceptedCats);
        } catch (NullPointerException npe) {
            // Nothing to do here if null
        }
    }

    private void refreshLayoutCategories() {
        final Set<Integer> exceptedCategories = StaticData.getStatsExceptedCategories();
        layoutCategories.removeAllViews();
        Cursor c;
        if (StaticData.getCurrentAccount() != null && StaticData.getCurrentAccount().getId() != null)
            c = new Category().fetchAllCategoriesUsedByAccountOperations(StaticData.getCurrentAccount().getId());
        else
            c = new Category().fetchAll();
        if (c == null)
            return;

        c.moveToFirst();
        do {
            final Category cat = new Category();
            cat.toDTO(c);

            int drawableBg = R.drawable.shape_button_rounded_ok;
            if (exceptedCategories.contains(cat.getId()))
                drawableBg = R.drawable.shape_button_rounded_ko;

            // View
            final View buttonView = LayoutInflater.from(this).inflate(R.layout.stats_shape_button_rounded, null);

            // Root layout = button
            final LinearLayout ll = (LinearLayout) buttonView.findViewById(R.id.button);
            //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int m = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
            TmhLogger.d(TAG, "Margin calculated (not used yet) = " + m + " px");
            //lp.setMargins(m, m, m, m);
            ll.setBackground(buttonView.getResources().getDrawable(drawableBg));
            ll.setTag(cat);
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Category cat = (Category) v.getTag();
                    if (exceptedCategories.contains(cat.getId())) {
                        exceptedCategories.remove(cat.getId());
                        ll.setBackground(getResources().getDrawable(R.drawable.shape_button_rounded_ok));
                    } else {
                        exceptedCategories.add(cat.getId());
                        ll.setBackground(getResources().getDrawable(R.drawable.shape_button_rounded_ko));
                    }

                    rebuildStatsData(false);
                }
            });

            // TextView
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tvText = (TextView) buttonView.findViewById(R.id.text);
                    tvText.setText(cat.getName());
                    //layoutCategories.addView(buttonView, lp);
                    layoutCategories.addView(buttonView);
                }
            });

        } while (c.moveToNext());

        c.close();
    }

    private void rebuildStatsData(boolean chartAnim) {
        rebuildStatsData(chartAnim, true);
    }

    private void rebuildStatsData(final boolean chartAnim, final boolean forwardEvent) {
        TmhLogger.d(TAG, "rebuildStatsData called with chartAnim=" + chartAnim);

        /* Handle that big process in a thread */
        IAsyncActivityRefresher refresher = new IAsyncActivityRefresher() {
            @Override
            public Map<Integer, Object> handleRefreshBackground() {
                statsData.setChartAnim(chartAnim);
                statsData.rebuildChartsData(
                        StaticData.getStatsExceptedCategories(),
                        dateBegin, dateEnd,
                        max_cat_displayed,
                        getResources().getString(R.string.misc),
                        forwardEvent);
                return null;
            }

            @Override
            public void handleRefreshEnding(Map<Integer, Object> results) {
                String miscCatText = statsData.getMiscCategoryText();
                if (miscCatText == null)
                    miscCatText = getResources().getString(R.string.NA);
                tvMisc.setText(miscCatText);
            }
        };
        AsyncActivityRefresher asyncRefresher = new AsyncActivityRefresher(this, refresher, true);
        asyncRefresher.execute();
    }
}
