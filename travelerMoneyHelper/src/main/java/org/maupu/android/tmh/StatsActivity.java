package org.maupu.android.tmh;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apmem.tools.layouts.FlowLayout;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.NumberUtil;
import org.maupu.android.tmh.util.stats.StatsCategoryValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class representing stats activity
 */
public class StatsActivity extends TmhActivity {
    private static final String TAG = StatsActivity.class.getName();
    private static final int LINE_WIDTH = 1;
    private final static int DRAWER_ITEM_AUTO = TmhApplication.getIdentifier("DRAWER_ITEM_AUTO");
    private int max_cat_displayed = 4;
    private SlidingUpPanelLayout slidingPanel;
    private ImageView accountImage;
    private Date dateBegin, dateEnd;
    private Set<Integer> exceptedCategories;
    private TextView tvDateBegin, tvDateEnd;
    private TextView tvDuration;
    private TextView tvMisc;
    private boolean animDetailsChart = true;

    // Charts
    private PieChart pieChart;
    private LineChart detailsChart;

    // Charts data handling
    public static final int[] MY_JOYFUL_COLORS = {
            Color.rgb(217, 80, 138), Color.rgb(254, 149, 7), Color.rgb(170, 164, 80),
            Color.rgb(106, 167, 134), Color.rgb(53, 194, 209)
    };
    private static final int[] COLORS = MY_JOYFUL_COLORS;
    private Map<Integer, StatsCategoryValues> statsData = new HashMap<>();
    private Category currentSelectedCategory;
    private Category biggestCategory;

    // Avg / total info
    private TextView tvAvg1Currency, tvAvg1Amount, tvAvg2Currency, tvAvg2Amount;
    private TextView tvAvg1Cat, tvAvg1CatCurrency, tvAvg1CatAmount, tvAvg2CatCurrency, tvAvg2CatAmount;
    private TextView tvTotal1Currency, tvTotal1Amount, tvTotal2Currency, tvTotal2Amount;
    private TextView tvTotal1Cat, tvTotal1CatCurrency, tvTotal1CatAmount, tvTotal2CatCurrency, tvTotal2CatAmount;

    // Categories list and chooser
    private FlowLayout layoutCategories;

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
        slidingPanel.setAnchorPoint(0.50f);

        accountImage = (ImageView)findViewById(R.id.account_image);

        tvMisc = (TextView)findViewById(R.id.misc);
        tvDateBegin = (TextView)findViewById(R.id.date_begin);
        tvDateEnd = (TextView)findViewById(R.id.date_end);
        tvDuration = (TextView)findViewById(R.id.duration);

        // Info
        tvAvg1Currency = (TextView)findViewById(R.id.text_avg1_currency);
        tvAvg1Amount = (TextView)findViewById(R.id.text_avg1_amount);
        tvAvg2Currency = (TextView)findViewById(R.id.text_avg2_currency);
        tvAvg2Amount = (TextView)findViewById(R.id.text_avg2_amount);
        tvAvg1Cat = (TextView)findViewById(R.id.text_avg1_cat);
        tvAvg1CatCurrency = (TextView)findViewById(R.id.text_avg1_cat_currency);
        tvAvg1CatAmount = (TextView)findViewById(R.id.text_avg1_cat_amount);
        tvAvg2CatCurrency = (TextView)findViewById(R.id.text_avg2_cat_currency);
        tvAvg2CatAmount = (TextView)findViewById(R.id.text_avg2_cat_amount);
        tvTotal1Currency = (TextView)findViewById(R.id.text_total1_currency);
        tvTotal1Amount = (TextView)findViewById(R.id.text_total1_amount);
        tvTotal2Currency = (TextView)findViewById(R.id.text_total2_currency);
        tvTotal2Amount = (TextView)findViewById(R.id.text_total2_amount);
        tvTotal1Cat = (TextView)findViewById(R.id.text_total1_cat);
        tvTotal1CatCurrency = (TextView)findViewById(R.id.text_total1_cat_currency);
        tvTotal1CatAmount = (TextView)findViewById(R.id.text_total1_cat_amount);
        tvTotal2CatCurrency = (TextView)findViewById(R.id.text_total2_cat_currency);
        tvTotal2CatAmount = (TextView)findViewById(R.id.text_total2_cat_amount);

        // Categories chooser
        layoutCategories = (FlowLayout)findViewById(R.id.layout_categories);

        // Charts
        animDetailsChart = true;
        initPieChart();
        initDetailsChart();
        reinitializeData();
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
            // Same as changing account
            refreshAfterCurrentAccountChanged();
        }

        return super.onItemClick(view, position, item);
    }

    private void initPieChart() {
        pieChart = (PieChart)findViewById(R.id.pie_chart);
        pieChart.setHardwareAccelerationEnabled(true);
        pieChart.setTransparentCircleAlpha(150);
        pieChart.setCenterText(getResources().getString(R.string.categories));
        pieChart.setCenterTextSize(15f);
        pieChart.setUsePercentValues(true);
        pieChart.setDescription("");
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.d(TAG, "Highlight : x=" + h.getXIndex() + ", dataset=" + h.getDataSetIndex());
                StatsCategoryValues scv = (StatsCategoryValues)e.getData();
                currentSelectedCategory = scv.getFirstCategory();
                refreshDetailsChart();
                refreshInfo();
            }

            @Override
            public void onNothingSelected() {
                //tvMisc.setText("Nothing selected");
                currentSelectedCategory = null;
                refreshDetailsChart();
                refreshInfo();
            }
        });

        pieChart.getLegend().setEnabled(false);
    }

    private void initDetailsChart() {
        detailsChart = (LineChart)findViewById(R.id.details_chart);
        detailsChart.setHardwareAccelerationEnabled(true);
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        Account currentAccount = StaticData.getCurrentAccount();

        if(currentAccount != null) {
            ImageViewHelper.setIcon(this, accountImage, currentAccount.getIcon());
        } else {
            accountImage.setImageResource(R.drawable.tmh_icon_48);
        }

        animDetailsChart = true;
        refreshDates();
        refreshPieChart(true, currentSelectedCategory);
        refreshDetailsChart();
        refreshLayoutCategories();
        refreshInfo();
    }

    @Override
    public void refreshAfterCurrentAccountChanged() {
        currentSelectedCategory = null;
        biggestCategory = null;
        reinitializeData();
        refreshDisplay();
    }

    /**
     * Reinit all data in the view
     */
    public void reinitializeData() {
        //if(! loadExceptedCategories())
        autoSetExceptedCategories();

        //if(! loadDates())
        autoSetDates();
        buildChartsData();
        refreshLayoutCategories();
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
            tvDuration.setText("Duration : " + DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd)+" day(s)");
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

            if(exceptedCategories == null)
                exceptedCategories = new HashSet<>();
            else
                exceptedCategories.clear();
            exceptedCategories.addAll(exceptedCats);

            saveExceptedCategories();
        } catch (NullPointerException npe) {
            // Nothing to do here if null
        }
    }

    private boolean loadExceptedCategories() {
        Set<Integer> cats = StaticData.getStatsExceptedCategories();

        boolean isOk = cats != null && cats.size() > 0;
        if(isOk)
            exceptedCategories = cats;
        return isOk;
    }
    private void saveExceptedCategories() {
        StaticData.getStatsExceptedCategories().clear();
        StaticData.getStatsExceptedCategories().addAll(exceptedCategories);
    }

    /**
     * Refresh information panel (avg, total, etc ...)
     */
    private void refreshInfo() {
        double total = 0d;
        double avg = 0d;
        double totalCat = 0d;
        double avgCat = 0d;
        double totalConv = 0d;
        double avgConv = 0d;
        double totalCatConv = 0d;
        double avgCatConv = 0d;
        double rate = 0d;
        String nonAvailable = getResources().getString(R.string.NA);
        Currency mainCur = StaticData.getMainCurrency();
        Account currentAcc = StaticData.getCurrentAccount();
        String mainCurrencySymbol = mainCur != null ? mainCur.getShortName() : nonAvailable;
        String currencySymbol = currentAcc != null && currentAcc.getCurrency() != null ? currentAcc.getCurrency().getShortName() : nonAvailable;

        String currentCatName = nonAvailable;
        if(currentSelectedCategory != null && currentSelectedCategory.getName() != null) {
            // Getting possible aggregated name if needed
            StatsCategoryValues scv = statsData.get(currentSelectedCategory.getId());
            currentCatName = scv != null ? scv.getName() : currentSelectedCategory.getName();

            if(currentCatName.length() > 11) {
                currentCatName = currentSelectedCategory.getName().substring(0, 11);
                if (!currentCatName.equals(currentSelectedCategory.getName()))
                    currentCatName += ".";
            }
        }

        if(statsData.values() != null || statsData.values().size() > 0) {
            /** Gran total **/
            Iterator<StatsCategoryValues> it = statsData.values().iterator();
            while(it.hasNext()) {
                StatsCategoryValues scv = it.next();
                total += scv.summarize();
                rate += scv.getRate();
            }
            // Get the average of all rates
            rate /= statsData.size();

            /** Avg by day **/
            int nbDays = DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd);
            avg = total / nbDays;

            /** Total and avg by day by category **/
            if(currentSelectedCategory != null && currentSelectedCategory.getId() != null) {
                StatsCategoryValues scv = statsData.get(currentSelectedCategory.getId());
                if (scv != null) {
                    totalCat = scv.summarize();
                    avgCat = scv.average(dateBegin, dateEnd);
                }
            }

            /** Gran total conversion to main currency **/
            totalConv = total / rate;
            totalCatConv = totalCat / rate;
            avgConv = avg / rate;
            avgCatConv = avgCat / rate;
        }

        /** Setting all data to TextViews **/
        tvAvg1Currency.setText(currencySymbol);
        tvAvg1Amount.setText(NumberUtil.formatDecimal(avg));
        tvAvg2Currency.setText(mainCurrencySymbol);
        tvAvg2Amount.setText(NumberUtil.formatDecimal(avgConv));

        tvAvg1Cat.setText(currentCatName);
        tvAvg1CatCurrency.setText(currencySymbol);
        tvAvg1CatAmount.setText(NumberUtil.formatDecimal(avgCat));
        tvAvg2CatCurrency.setText(mainCurrencySymbol);
        tvAvg2CatAmount.setText(NumberUtil.formatDecimal(avgCatConv));

        tvTotal1Currency.setText(currencySymbol);
        tvTotal1Amount.setText(NumberUtil.formatDecimal(total));
        tvTotal2Currency.setText(mainCurrencySymbol);
        tvTotal2Amount.setText(NumberUtil.formatDecimal(totalConv));

        tvTotal1Cat.setText(currentCatName);
        tvTotal1CatCurrency.setText(currencySymbol);
        tvTotal1CatAmount.setText(NumberUtil.formatDecimal(totalCat));
        tvTotal2CatCurrency.setText(mainCurrencySymbol);
        tvTotal2CatAmount.setText(NumberUtil.formatDecimal(totalCatConv));
    }

    private void buildChartsData() {
        Integer[] exceptedCats = null;
        if(exceptedCategories != null)
            exceptedCats = exceptedCategories.toArray(new Integer[0]);

        Cursor c = new Operation().sumOperationsGroupByDayOrderDateAsc(
                StaticData.getCurrentAccount(),
                dateBegin,
                dateEnd,
                exceptedCats
        );
        if(c == null)
            return;
        if(c.getCount() == 0) {
            c.close();
            return;
        }

        c.moveToFirst();

        statsData = new HashMap<>();
        do {
            int idxAmount = c.getColumnIndexOrThrow("amountString");
            int idxDate = c.getColumnIndexOrThrow("dateString");
            int idxCatId = c.getColumnIndexOrThrow(OperationData.KEY_ID_CATEGORY);
            int idxRate = c.getColumnIndexOrThrow(CurrencyData.KEY_CURRENCY_LINKED);
            String amountString = c.getString(idxAmount);
            String dateString = c.getString(idxDate);
            int catId = c.getInt(idxCatId);
            double rate = c.getDouble(idxRate);

            Category cat = new Category();
            Cursor cursorCat = cat.fetch(catId);
            cat.toDTO(cursorCat);
            cursorCat.close();

            try {
                StatsCategoryValues scv = statsData.get(catId);
                if(scv == null) {
                    scv = new StatsCategoryValues(cat, dateBegin, dateEnd, rate);
                    statsData.put(catId, scv);
                }

                float amount = Math.abs(Float.parseFloat(amountString));
                scv.addValue(dateString, amount);
            } catch(NullPointerException npe) {
            } catch(NumberFormatException nfe) {}
        } while(c.moveToNext());

        c.close();

        List<StatsCategoryValues> chartsDataList = new ArrayList<>(statsData.values());
        Collections.sort(chartsDataList);
        int nbElts = chartsDataList.size();

        // Biggest category is the first one after sorting the list
        if(chartsDataList != null && chartsDataList.size() > 0)
            biggestCategory = chartsDataList.get(0).getFirstCategory();

        // Put color from an array on each stats category value
        Iterator<StatsCategoryValues> it = chartsDataList.iterator();
        int x = 0;
        while(it.hasNext()) {
            it.next().setColor(COLORS[x % COLORS.length]);
            x++;
        }

        // If max nb of displayed slice is reach, gather remaining categories together
        StatsCategoryValues scvMisc = null;
        if(nbElts > max_cat_displayed) {
            scvMisc = chartsDataList.get(max_cat_displayed);
            scvMisc.setName(getResources().getString(R.string.misc));
            // Remove from charts data, fusion everything and reintegrate it
            statsData.remove(scvMisc.getFirstCategory().getId());
            for(int i=max_cat_displayed+1; i<nbElts; i++) {
                StatsCategoryValues curScv = chartsDataList.get(i);
                scvMisc.fusionWith(curScv);
                statsData.remove(curScv.getFirstCategory().getId());
            }
            statsData.put(scvMisc.getFirstCategory().getId(), scvMisc);
        }

        // Finally, set text for misc category
        tvMisc.setText(getMiscCategoryText(scvMisc));
    }

    private void refreshPieChart(boolean anim, Category catToHighlight) {
        if(statsData == null || statsData.keySet() == null || statsData.size() ==0) {
            pieChart.invalidate();
            return;
        }

        List<String> xEntries = new ArrayList<>();
        List<Entry> yEntries = new ArrayList<>();
        int[] colors = new int[statsData.size()];

        // Getting value and sorting it from biggest to smallest
        List<StatsCategoryValues> values = new ArrayList<>(statsData.values());
        Collections.sort(values);

        // Mixing list taking big element with smaller one to avoid having all small elts stuck together
        // in pie chart
        // Instead of having in order 1 2 3 4 5 6, we have : 1 4 2 5 3 6
        // 1 -> lim=0; i=(0) j=(1) - ok
        // 1 2 -> lim=1; i=(0,1) j=(2,3)
        // 1 2 3 -> lim=1; i=(0,1) j=(2,3)
        // 1 2 3 4 -> lim=2; i=(0,1,2) j=(3,4,5)
        // 1 2 3 4 5 -> lim=2; i=(0,1,2) j=(3,4,5) - ok
        int nbElts = values.size();
        int lim = nbElts / 2;
        int xIndex = 0;
        for(int i=0; i<=lim; i++) {
            int j = i + lim + 1;
            StatsCategoryValues scvi = values.get(i);
            colors[xIndex] = scvi.getColor();
            xEntries.add(scvi.getName());
            yEntries.add(new Entry(scvi.summarize(), xIndex++, scvi));
            if(j<nbElts) {
                StatsCategoryValues scvj = values.get(j);
                colors[xIndex] = scvj.getColor();
                xEntries.add(scvj.getName());
                yEntries.add(new Entry(scvj.summarize(), xIndex++, scvj));
            }
        }

        PieDataSet dataSet = new PieDataSet(yEntries, getResources().getString(R.string.categories));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setSliceSpace(1.5f);
        dataSet.setColors(colors);
        dataSet.setSelectionShift(5f);

        PieData pd = new PieData(xEntries, dataSet);
        pd.setValueTextSize(10f);
        pd.setValueFormatter(new PercentFormatter());

        pieChart.setData(pd);
        if(anim)
            pieChart.animateY(1000);
        highlightInPieChart(catToHighlight, biggestCategory);
        pieChart.invalidate();
    }

    public void refreshDetailsChart() {
        /** Construct all curves from statsData **/
        List<String> xEntries = StatsCategoryValues.buildXEntries(dateBegin, dateEnd);
        // No data
        if(xEntries == null) {
            detailsChart.invalidate();
            return;
        }

        List<LineDataSet> dataSets = new ArrayList<>();

        /** First, draw others categories **/
        List<StatsCategoryValues> scvs = new ArrayList<>(statsData.values());
        Iterator<StatsCategoryValues> it = scvs.iterator();
        while(it.hasNext()) {
            StatsCategoryValues scv = it.next();

            LineDataSet lds = new LineDataSet(scv.getYEntries(), scv.getName());
            if(currentSelectedCategory == null || currentSelectedCategory.getId() != scv.getFirstCategory().getId()) {
                lds.setColor(scv.getColor());
                lds.setLineWidth(LINE_WIDTH);
                lds.setDrawCircleHole(true);
                lds.setCircleColor(scv.getColor());
                lds.setCircleSize(2f);
                dataSets.add(lds);
            }
        }

        /** Last, draw selected category (displayed on top of others curves) **/
        if(currentSelectedCategory != null && currentSelectedCategory.getId() != null) {
            StatsCategoryValues scv = statsData.get(currentSelectedCategory.getId());
            if(scv != null) {
                LineDataSet lds = new LineDataSet(scv.getYEntries(), scv.getName());
                lds.setColor(scv.getColor());
                lds.setCircleColor(scv.getColor());
                lds.setLineWidth(LINE_WIDTH * 2);
                lds.setDrawCircleHole(false);
                lds.setCircleSize(3f);
                lds.enableDashedLine(20f, 8f, 0f);
                dataSets.add(lds);
            }
        }

        if(animDetailsChart) {
            animDetailsChart = false;
            detailsChart.animateXY(1000, 1000);
        }
        detailsChart.clear();
        detailsChart.notifyDataSetChanged();
        detailsChart.setData(new LineData(xEntries, dataSets));
        if(currentSelectedCategory != null && currentSelectedCategory.getName() != null)
            detailsChart.setDescription(currentSelectedCategory.getName());
        else
            detailsChart.setDescription("");
        detailsChart.setGridBackgroundColor(Color.WHITE);
        detailsChart.invalidate();
    }

    protected String getMiscCategoryText(StatsCategoryValues scvMisc) {
        if(scvMisc == null || scvMisc.getCategories() == null || scvMisc.getCategories().size() == 0)
            return getResources().getString(R.string.NA);
        else if(scvMisc.getCategories().size() == 1)
            return scvMisc.getFirstCategory().getName();

        StringBuilder sb = null;

        Iterator<Category> it = scvMisc.getCategories().iterator();
        while(it.hasNext()) {
            Category cat = it.next();
            if (sb == null) {
                sb = new StringBuilder();
                sb.append(cat.getName());
            } else {
                sb.append("\n").append(cat.getName());
            }
        }

        return sb.toString();
    }

    private void refreshLayoutCategories() {
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
            final View buttonView = LayoutInflater.from(this).inflate(R.layout.shape_button_rounded, null);

            // Root layout = button
            final LinearLayout ll = (LinearLayout)buttonView.findViewById(R.id.button);
            //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int m = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
            Log.d(TAG, "Margin calculated = " + m + " px");
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

                    buildChartsData();
                    // refresh pie chart and highlight previous category if exists
                    refreshPieChart(false, currentSelectedCategory);
                    refreshDetailsChart();
                    refreshInfo();
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

    /**
     * Highlight specified category in pie chart. If not found, highlight another category.
     * If neither found, highlight nothing.
     * 1) If both catToLookFor and catIfNotFound are null, don't highlight anything
     * 2) If catToLookFor is null, try to highlight catIfNotFound (recursive call)
     * 3) If catToLookFor is not found and catIfNotFound is set, trying to highlight catIfNotFound (recursive call)
     * 4) If catToLookFor is not found and catIfNotFound is null, don't highlight anything (recursive call)
     * @param catToLookFor Category to highlight in first priority
     * @param catIfNotFound Category to highlight if catToLookFor is not found
     */
    private void highlightInPieChart(final Category catToLookFor, final Category catIfNotFound) {
        if(catToLookFor == null && catIfNotFound == null) {
            /** Case number 1) **/
            pieChart.highlightValue(null, true);
        } else if(catToLookFor == null) {
            /** Case number 2) **/
            highlightInPieChart(catIfNotFound, null);
        } else {
            /** Case number 3 - Trying to find catToLookFor **/
            // Getting index of element in dataset
            // new Highlight(x axis element, x of the dataset - 0 for my case, I only have one dataset inside
            PieDataSet pds = pieChart.getData().getDataSetByLabel(getResources().getString(R.string.categories), false);
            List<Entry> yEntries = pds.getYVals();
            int x;
            int yEntriesSize = yEntries.size();
            for(x=0; x<yEntriesSize; x++) {
                Entry entry = yEntries.get(x);
                StatsCategoryValues scv = (StatsCategoryValues)entry.getData();

                if(scv != null && scv.contains(catToLookFor.getId())) {
                    // Highlighting found element
                    pieChart.highlightValue(new Highlight(x, 0), true);
                    return;
                }
            }

            // Nothing found, trying catIfNotFound
            highlightInPieChart(catIfNotFound, null);
        }
    }
}
