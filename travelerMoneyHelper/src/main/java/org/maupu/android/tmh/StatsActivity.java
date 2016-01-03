package org.maupu.android.tmh;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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
import org.maupu.android.tmh.util.DateUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class representing stats activity
 */
public class StatsActivity extends TmhActivity {
    private static final String TAG = StatsActivity.class.getName();
    private final static int DRAWER_ITEM_AUTO = TmhApplication.getIdentifier("DRAWER_ITEM_AUTO");
    private int max_cat_displayed = 4;
    private SlidingUpPanelLayout slidingPanel;
    private Date dateBegin, dateEnd;

    /** Primary panel **/
    private TextView tvMisc;
    private CategoriesPieChart pieChart;
    private CategoriesLineChart detailsChart;
    private InfoPanel infoPanel;

    // Charts data handling
    public static final int[] MY_JOYFUL_COLORS = {
            Color.rgb(217, 80, 138), Color.rgb(254, 149, 7), Color.rgb(170, 164, 80),
            Color.rgb(106, 167, 134), Color.rgb(53, 194, 209)
    };
    private StatsData statsData = new StatsData(MY_JOYFUL_COLORS);

    // Categories list and chooser
    private FlowLayout layoutCategories;

    /** Secondary panel **/
    private TextView tvDateBegin, tvDateEnd;
    private TextView tvDuration;
    private ImageView accountImage;
    private TextView accountName;
    private TabLayout tabLayout;
    private StatsViewPager statsViewPager;

    public StatsActivity() {
        super(R.layout.stats, R.string.activity_title_statistics);
    }

    @Override
    public int whatIsMyDrawerIdentifier() {
        return TmhActivity.DRAWER_ITEM_STATS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);

        slidingPanel = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingPanel.setDragView(R.id.layout_title);

        /** Primary panel **/
        tvMisc = (TextView)findViewById(R.id.misc);
        tvDateBegin = (TextView)findViewById(R.id.date_begin);
        tvDateEnd = (TextView)findViewById(R.id.date_end);
        tvDuration = (TextView)findViewById(R.id.duration);

        // Categories chooser
        layoutCategories = (FlowLayout)findViewById(R.id.layout_categories);

        // info
        infoPanel = new InfoPanel(this);
        statsData.addOnStatsDataChangedListener(infoPanel);
        infoPanel.initPanel();

        // Charts
        initPieChart();
        initDetailsChart();

        /** Secondary panel **/
        accountImage = (ImageView)findViewById(R.id.account_image);
        accountName = (TextView)findViewById(R.id.account_name);
        statsViewPager = (StatsViewPager)findViewById(R.id.viewpager);
        statsViewPager.initPanel();
        tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(statsViewPager);
        statsData.addOnStatsDataChangedListener(statsViewPager);

        statsData.setCatToHighlight(null);
        reloadInputData();
        rebuildStatsData(true, false);
        refreshDisplay();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        // Called when exiting / changing screen orientation
        super.onDestroy();
    }

    private void initPieChart() {
        pieChart = (CategoriesPieChart)findViewById(R.id.pie_chart);
        statsData.addOnStatsDataChangedListener(pieChart);
        pieChart.initPanel();
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.d(TAG, "Highlight : x=" + h.getXIndex() + ", dataset=" + h.getDataSetIndex());
                StatsCategoryValues scv = (StatsCategoryValues) e.getData();
                statsData.setCatToHighlight(scv.getFirstCategory());
                // TODO : Don't redraw everything but replace by a highlight method
                boolean cur = statsData.isChartAnim();
                statsData.disableChartAnim();
                detailsChart.refreshPanel(statsData);
                infoPanel.refreshPanel(statsData);
                statsData.setChartAnim(cur);
            }

            @Override
            public void onNothingSelected() {
                //tvMisc.setText("Nothing selected");
                statsData.setCatToHighlight(null);
                // TODO : Don't redraw everything but replace by a highlight method
                boolean cur = statsData.isChartAnim();
                statsData.disableChartAnim();
                detailsChart.refreshPanel(statsData);
                infoPanel.refreshPanel(statsData);
                statsData.setChartAnim(cur);
            }
        });
    }

    private void initDetailsChart() {
        detailsChart = (CategoriesLineChart)findViewById(R.id.details_chart);
        statsData.addOnStatsDataChangedListener(detailsChart);
        detailsChart.initPanel();
    }

    @Override
    public IDrawerItem[] buildNavigationDrawer() {
        return new IDrawerItem[] {
                createSecondaryDrawerItem(
                        DRAWER_ITEM_AUTO,
                        R.drawable.ic_cat_except_black,
                        R.string.stats_item_cat_auto),
        };
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem item) {
        if(item.getIdentifier() == DRAWER_ITEM_AUTO) {
            statsData.setCatToHighlight(null);
            autoSetExceptedCategories();
            rebuildStatsData(true, false);
            refreshDisplay();
        }

        return super.onItemClick(view, position, item);
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
        if(! loadDates())
            autoSetDates();

        // If excepted cat is null, trying to auto set them
        if(StaticData.getStatsExceptedCategories().size() == 0)
            autoSetExceptedCategories();
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        Account currentAccount = StaticData.getCurrentAccount();

        if(currentAccount != null) {
            ImageViewHelper.setIcon(this, accountImage, currentAccount.getIcon());
            accountName.setText(currentAccount.getName());
        } else {
            accountImage.setImageResource(R.drawable.tmh_icon_48);
            accountName.setText("");
        }

        refreshDates();
        statsData.enableChartAnim();
        pieChart.refreshPanel(statsData);
        detailsChart.refreshPanel(statsData);
        infoPanel.refreshPanel(statsData);
        statsViewPager.refreshPanel(statsData);
        refreshLayoutCategories();
    }

    @Override
    public void refreshAfterCurrentAccountChanged() {
        statsData.setCatToHighlight(null);
        resetInputData();
        rebuildStatsData(true, false);
        refreshDisplay();
    }

    private void autoSetDates() {
        Account currentAccount = StaticData.getCurrentAccount();
        if(currentAccount == null)
            return;

        Operation dummyOp = new Operation();
        dateBegin = dummyOp.getFirstDate(currentAccount, StaticData.getStatsExceptedCategoriesToArray());
        dateEnd = dummyOp.getLastDate(currentAccount, StaticData.getStatsExceptedCategoriesToArray());
        saveDates();

        Log.d(TAG, "Auto dates computed beg=" + dateBegin + ", end=" + dateEnd);
        refreshDates();
    }

    private void refreshDates() {
        if(dateBegin != null && dateEnd != null) {
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
        if(db != null && de != null) {
            dateBegin = db;
            dateEnd = de;
        }

        return db != null && de != null;
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

            if(currentExceptedToKeep != null && currentExceptedToKeep.size() > 0)
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
        if(StaticData.getCurrentAccount() != null && StaticData.getCurrentAccount().getId() != null)
            c = new Category().fetchAllCategoriesUsedByAccountOperations(StaticData.getCurrentAccount().getId());
        else
            c = new Category().fetchAll();
        if(c == null)
            return;

        c.moveToFirst();
        do {
            Category cat = new Category();
            cat.toDTO(c);

            int drawableBg = R.drawable.shape_button_rounded_ok;
            if(exceptedCategories.contains(cat.getId()))
                drawableBg = R.drawable.shape_button_rounded_ko;

            // View
            final View buttonView = LayoutInflater.from(this).inflate(R.layout.stats_shape_button_rounded, null);

            // Root layout = button
            final LinearLayout ll = (LinearLayout)buttonView.findViewById(R.id.button);
            //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int m = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
            Log.d(TAG, "Margin calculated (not used yet) = " + m + " px");
            //lp.setMargins(m, m, m, m);
            ll.setBackground(buttonView.getResources().getDrawable(drawableBg));
            ll.setTag(cat);
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Category cat = (Category)v.getTag();
                    if(exceptedCategories.contains(cat.getId())) {
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
            TextView tvText = (TextView)buttonView.findViewById(R.id.text);
            tvText.setText(cat.getName());

            //layoutCategories.addView(buttonView, lp);
            layoutCategories.addView(buttonView);
        } while(c.moveToNext());

        c.close();
    }

    private void rebuildStatsData(boolean chartAnim) {
        rebuildStatsData(chartAnim, true);
    }

    private void rebuildStatsData(boolean chartAnim, boolean forwardEvent) {
        Log.d(TAG, "rebuildStatsData called with chartAnim="+chartAnim);
        statsData.setChartAnim(chartAnim);
        statsData.rebuildChartsData(
                StaticData.getStatsExceptedCategories(),
                dateBegin, dateEnd,
                max_cat_displayed,
                getResources().getString(R.string.misc),
                forwardEvent);

        String miscCatText = statsData.getMiscCategoryText();
        if(miscCatText == null)
            miscCatText = getResources().getString(R.string.NA);
        tvMisc.setText(miscCatText);
    }
}
