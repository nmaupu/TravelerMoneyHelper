package org.maupu.android.tmh;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

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
    private int max_cat_displayed = 4;
    private SlidingUpPanelLayout slidingPanel;
    private ImageView accountImage;
    private Date dateBegin, dateEnd;
    private Set<Integer> exceptedCategories;
    private TextView tvDateBegin, tvDateEnd;
    private TextView tvDuration;
    private TextView marker;
    private Category currentSelectedCategory;

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

    // Avg / total info
    private TextView tvAvg1Currency, tvAvg1Amount, tvAvg2Currency, tvAvg2Amount;
    private TextView tvAvg1Cat, tvAvg1CatCurrency, tvAvg1CatAmount, tvAvg2CatCurrency, tvAvg2CatAmount;
    private TextView tvTotal1Currency, tvTotal1Amount, tvTotal2Currency, tvTotal2Amount;
    private TextView tvTotal1Cat, tvTotal1CatCurrency, tvTotal1CatAmount, tvTotal2CatCurrency, tvTotal2CatAmount;

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

        // Charts
        initPieChart();
        initDetailsChart();
        reinitializeData();
        buildChartsData();
    }

    private void initPieChart() {
        pieChart = (PieChart)findViewById(R.id.pie_chart);
        marker = (TextView)findViewById(R.id.marker);
        pieChart.setHardwareAccelerationEnabled(true);
        pieChart.setTransparentCircleAlpha(150);
        pieChart.setCenterText(getResources().getString(R.string.categories));
        pieChart.setCenterTextSize(15f);
        pieChart.setUsePercentValues(true);
        pieChart.setDescription("");
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.d(TAG, "Highlight : " + h.getDataSetIndex() + " " + h.getXIndex());
                StatsCategoryValues scv = (StatsCategoryValues) e.getData();
                StringBuilder sb = new StringBuilder();
                Iterator<Category> it = scv.getCategories().iterator();
                while (it.hasNext()) {
                    sb.append(it.next().getName());
                    sb.append("\n");
                }

                marker.setText(sb.toString());

                currentSelectedCategory = scv.getFirstCategory();
                refreshDetailsChart();
                refreshInfo();
            }

            @Override
            public void onNothingSelected() {
                marker.setText("Nothing selected");
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

        reinitializeData();
        refreshDates();
        refreshPieChart();
        refreshDetailsChart();
        refreshInfo();
    }

    @Override
    public void refreshAfterCurrentAccountChanged() {
        reinitializeData();
        buildChartsData();
        refreshDisplay();
    }

    /**
     * Reinit all data in the view
     */
    public void reinitializeData() {
//        if(! loadExceptedCategories())
            autoSetExceptedCategories();

//        if(! loadDates())
        autoSetDates();
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
        try {
            Operation o = new Operation();
            Integer[] cats = o.getExceptCategoriesAuto(StaticData.getCurrentAccount());
            exceptedCategories = new HashSet<>(Arrays.asList(cats));
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
        Currency mainCur = StaticData.getMainCurrency();
        Account currentAcc = StaticData.getCurrentAccount();
        String mainCurrencySymbol = mainCur != null ? mainCur.getShortName() : "N/A";
        String currencySymbol = currentAcc != null ? currentAcc.getCurrency().getShortName() : "N/A";

        String currentCatName = "N/A";
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
        int n = chartsDataList.size();

        Iterator<StatsCategoryValues> it = chartsDataList.iterator();
        int x = 0;
        while(it.hasNext()) {
            it.next().setColor(COLORS[x % COLORS.length]);
            x++;
        }

        if(n > max_cat_displayed) {
            StatsCategoryValues scv = chartsDataList.get(max_cat_displayed);
            scv.setName(getResources().getString(R.string.misc));
            // Remove from charts data, fusion everything and reintegrate it
            statsData.remove(scv.getFirstCategory().getId());
            for(int i= max_cat_displayed +1; i<n; i++) {
                StatsCategoryValues curScv = chartsDataList.get(i);
                scv.fusionWith(curScv);
                statsData.remove(curScv.getFirstCategory().getId());
            }
            statsData.put(scv.getFirstCategory().getId(), scv);
        }
    }

    private void refreshPieChart() {
        if(statsData == null || statsData.keySet() == null || statsData.size() ==0)
            return;

        List<String> xEntries = new ArrayList<>();
        List<Entry> yEntries = new ArrayList<>();
        int[] colors = new int[statsData.size()];
        int i = 0;
        Iterator<StatsCategoryValues> it = statsData.values().iterator();
        while(it.hasNext()) {
            StatsCategoryValues scv = it.next();
            colors[i] = scv.getColor();
            xEntries.add(scv.getName());
            yEntries.add(new Entry(scv.summarize(), i, scv));
            i++;
        }

        PieDataSet dataSet = new PieDataSet(yEntries, getResources().getString(R.string.categories));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setSliceSpace(1.5f);
        dataSet.setColors(colors);
        dataSet.setSelectionShift(10f);

        PieData pd = new PieData(xEntries, dataSet);
        pd.setValueTextSize(10f);
        pd.setValueFormatter(new PercentFormatter());

        pieChart.setData(pd);
        pieChart.animateY(1000);
        pieChart.highlightValue(new Highlight(0, 0), true);
        pieChart.invalidate();
    }

    public void refreshDetailsChart() {
        /** Construct all curves from statsData **/
        List<String> xEntries = StatsCategoryValues.buildXEntries(dateBegin, dateEnd);
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
            LineDataSet lds = new LineDataSet(scv.getYEntries(), scv.getName());
            lds.setColor(scv.getColor());
            lds.setCircleColor(scv.getColor());
            lds.setLineWidth(LINE_WIDTH * 2);
            lds.setDrawCircleHole(false);
            lds.setCircleSize(3f);
            lds.enableDashedLine(20f, 8f, 0f);
            dataSets.add(lds);
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
}
