package org.maupu.android.tmh;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apmem.tools.layouts.FlowLayout;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.dialog.DatePickerDialogFragment;
import org.maupu.android.tmh.stats.CategoriesLineChart;
import org.maupu.android.tmh.stats.CategoriesPieChart;
import org.maupu.android.tmh.stats.InfoPanel;
import org.maupu.android.tmh.stats.StatsCategoryValues;
import org.maupu.android.tmh.stats.StatsData;
import org.maupu.android.tmh.stats.StatsViewPager;
import org.maupu.android.tmh.ui.ApplicationDrawer;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.ImageUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StatsFragment extends TmhFragment {
    private static final Class<StatsFragment> TAG = StatsFragment.class;

    private final static int DIALOG_DATE_BEGIN = TmhApplication.getIdentifier("DIALOG_DATE_BEGIN");
    private final static int DIALOG_DATE_END = TmhApplication.getIdentifier("DIALOG_DATE_END");

    private final int max_cat_displayed = 4;
    private SlidingUpPanelLayout slidingPanel;
    private Date dateBegin, dateEnd;
    private MenuProvider menuProvider;

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

    public StatsFragment() {
        super(R.layout.stats);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        requireActivity().setTitle(R.string.fragment_title_statistics);
        setupMenu();

        ApplicationDrawer.getInstance().setOnAccountChangeListener(() -> {
            autoSetDates();
            statsData.setCatToHighlight(null);
            resetInputData();
            rebuildStatsData(true);
            refreshDisplay();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TmhLogger.d(TAG, "onCreate called");
        try {
            super.onCreate(savedInstanceState);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }


        slidingPanel = view.findViewById(R.id.sliding_layout);
        slidingPanel.setDragView(R.id.layout_title);

        // Primary panel
        tvMisc = view.findViewById(R.id.misc);
        tvDateBegin = view.findViewById(R.id.date_begin);
        tvDateEnd = view.findViewById(R.id.date_end);
        tvDuration = view.findViewById(R.id.duration);

        // Categories chooser
        layoutCategories = view.findViewById(R.id.layout_categories);

        // info
        infoPanel = new InfoPanel(requireActivity());
        statsData.addOnStatsDataChangedListener(infoPanel);
        infoPanel.initPanel();

        // Charts
        initPieChart(view);
        initDetailsChart(view);

        // Secondary panel
        accountImage = view.findViewById(R.id.account_image);
        accountName = view.findViewById(R.id.account_name);
        statsViewPager = view.findViewById(R.id.viewpager);
        statsViewPager.initPanel();
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(statsViewPager);
        statsData.addOnStatsDataChangedListener(statsViewPager);

        statsData.setCatToHighlight(null);
        reloadInputData();
        if (!loadDates())
            autoSetDates();
        rebuildStatsData(true, false);
        refreshDisplay();
    }

    private void setupMenu() {
        if (menuProvider == null) {
            menuProvider = new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.stats_menu, menu);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    if (R.id.stats_action_auto_cats == menuItem.getItemId()) {
                        statsData.setCatToHighlight(null);
                        autoSetExceptedCategories();
                        rebuildStatsData(true, false);
                        refreshDisplay();
                    } else if (R.id.stats_action_date_begin == menuItem.getItemId()) {
                        createDateDialog(DIALOG_DATE_BEGIN);
                    } else if (R.id.stats_action_date_end == menuItem.getItemId()) {
                        createDateDialog(DIALOG_DATE_END);
                    } else if (R.id.stats_action_reset_dates == menuItem.getItemId()) {
                        autoSetDates();
                        rebuildStatsData(true, false);
                        refreshDisplay();
                    }

                    return false;
                }
            };
        }

        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void initPieChart(View view) {
        pieChart = view.findViewById(R.id.pie_chart);
        statsData.addOnStatsDataChangedListener(pieChart);
        pieChart.initPanel();
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

                AsyncActivityRefresher asyncTask = new AsyncActivityRefresher(getActivity(), refresher, false);
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

                AsyncActivityRefresher asyncTask = new AsyncActivityRefresher(getActivity(), refresher, false);
                asyncTask.execute();
            }
        });
    }

    private void initDetailsChart(View view) {
        detailsChart = view.findViewById(R.id.details_chart);
        statsData.addOnStatsDataChangedListener(detailsChart);
        detailsChart.initPanel();
    }

    private void createDateDialog(final int id) {
        int y, m, d;

        Calendar cal = Calendar.getInstance();

        if (dateBegin == null)
            dateBegin = cal.getTime();
        if (dateEnd == null)
            dateEnd = cal.getTime();
        if (dateEnd.before(dateBegin))
            dateEnd = dateBegin;

        if (id == DIALOG_DATE_BEGIN) {
            cal.setTime(dateBegin);
        } else if (id == DIALOG_DATE_END) {
            cal.setTime(dateEnd);
        }

        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialogFragment(y, m, d, (view, year, month, dayOfMonth) -> {
            Calendar _cal = Calendar.getInstance();
            _cal.set(Calendar.YEAR, year);
            _cal.set(Calendar.MONTH, month);
            _cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Date date = _cal.getTime();


            if (id == DIALOG_DATE_BEGIN) {
                Date dateToSet = DateUtil.resetDateToBeginingOfDay(date);
                if (dateToSet.before(dateEnd))
                    dateBegin = dateToSet;
                else
                    Snackbar.make(
                            getView(),
                            getString(R.string.error_date_begin_after_end),
                            Snackbar.LENGTH_SHORT).show();
                TmhLogger.d(TAG, "Changing date begin to : " + dateBegin);
            } else if (id == DIALOG_DATE_END) {
                Date dateToSet = DateUtil.resetDateToEndOfDay(date);
                if (dateToSet.after(dateBegin))
                    dateEnd = dateToSet;
                else
                    Snackbar.make(
                            getView(),
                            getString(R.string.error_date_end_before_begin),
                            Snackbar.LENGTH_SHORT).show();
                TmhLogger.d(TAG, "Changing date end to : " + dateEnd);
            }

            saveDates();
            refreshDates();
            rebuildStatsData(true, false);
            refreshDisplay();
        }).show(getChildFragmentManager(), DatePickerDialogFragment.TAG);
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
            ImageUtil.setIcon(accountImage, currentAccount.getIcon());
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
            final View buttonView = LayoutInflater.from(requireActivity()).inflate(R.layout.stats_shape_button_rounded, null);

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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tvText = buttonView.findViewById(R.id.text);
                    tvText.setText(cat.getName());
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
        AsyncActivityRefresher asyncRefresher = new AsyncActivityRefresher(requireActivity(), refresher, true);
        asyncRefresher.execute();
    }
}
