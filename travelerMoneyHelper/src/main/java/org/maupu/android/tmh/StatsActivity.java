package org.maupu.android.tmh;

import android.database.Cursor;
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
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.stats.StatsData;

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
    private SlidingUpPanelLayout slidingPanel;
    private ImageView accountImage;
    private Date dateBegin, dateEnd;
    private Set<Integer> exceptedCategories;
    private TextView tvDateBegin, tvDateEnd;
    private TextView marker;
    private Category currentSelectedCategory = new Category();

    // Charts
    private PieChart pieChart;
    private LineChart detailsChart;

    public StatsActivity() {
        super(R.layout.stats, R.string.activity_title_statistics);
    }

    @Override
    public int whatIsMyDrawerIdentifier() {
        return TmhActivity.DRAWER_ITEM_STATS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        slidingPanel = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingPanel.setAnchorPoint(0.50f);

        accountImage = (ImageView)findViewById(R.id.account_image);

        tvDateBegin = (TextView)findViewById(R.id.date_begin);
        tvDateEnd = (TextView)findViewById(R.id.date_end);

        // Charts
        initPieChart();
        initDetailsChart();
    }

    private void initPieChart() {
        pieChart = (PieChart)findViewById(R.id.pie_chart);
        marker = (TextView)findViewById(R.id.marker);
        pieChart.setHardwareAccelerationEnabled(true);
        pieChart.setTransparentCircleAlpha(150);
        pieChart.setCenterText(getResources().getString(R.string.categories));
        pieChart.setCenterTextSize(20f);
        pieChart.setUsePercentValues(true);
        pieChart.setDescription("");
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.d(TAG, "Highlight : " + h.getDataSetIndex() + " " + h.getXIndex());
                StatsData<Integer> sd = (StatsData<Integer>) e.getData();
                StringBuilder sb = new StringBuilder();
                Iterator<String> it = sd.getNames().iterator();
                while (it.hasNext()) {
                    sb.append(it.next());
                    sb.append("\n");
                }

                marker.setText(sb.toString());

                Integer catId = sd.getObj();
                if (catId != null) {
                    Cursor c = currentSelectedCategory.fetch(catId);
                    currentSelectedCategory.toDTO(c);
                    c.close();
                    refreshDetailsChart();
                }
            }

            @Override
            public void onNothingSelected() {
                marker.setText("Nothing selected");
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
    }

    @Override
    public void refreshAfterCurrentAccountChanged() {
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

    private void refreshPieChart() {
        Account currentAccount = StaticData.getCurrentAccount();

        Cursor c = new Operation().sumOperationsGroupByCategory(
                currentAccount,
                StaticData.getStatsExceptedCategories().toArray(new Integer[0])
        );
        if(c==null)
            return;

        List<StatsData<Integer>> entries = new ArrayList<>();
        c.moveToFirst();
        do {
            int idxAmount = c.getColumnIndexOrThrow("amountString");
            int idxCategoryName = c.getColumnIndexOrThrow(CategoryData.KEY_NAME);
            int idxCategory = c.getColumnIndexOrThrow(CategoryData.KEY_ID);

            int catId = c.getInt(idxCategory);
            String catName = c.getString(idxCategoryName);
            entries.add(new StatsData(Math.abs(c.getFloat(idxAmount)), catId, catName));

        } while(c.moveToNext());

        // Closing cursor
        c.close();

        Collections.sort(entries);
        int size = entries.size();
        List<String> xEntries = new ArrayList<>();
        List<Entry> yEntries = new ArrayList<>();
        final int MAX = 4;
        StatsData<Integer> aggregated = new StatsData<>(0f, null, null);
        int highlighted = 0;
        for(int i=0; i<size; i++) {
            StatsData<Integer> sd = entries.get(i);
            if(i<MAX) {
                // Store the value
                yEntries.add(new Entry(sd.getStatValue(), i, sd));
                xEntries.add(sd.getName(0));
            } else {
                // Aggregate the value
                aggregated.addStatValue(sd.getStatValue());
                aggregated.addName(sd.getName(0));
            }
        }
        if(size > MAX) {
            // Adding aggregated values, index is MAX
            xEntries.add(getResources().getString(R.string.misc));
            yEntries.add(new Entry(aggregated.getStatValue(), MAX, aggregated));
            highlighted = MAX;
        }

        PieDataSet dataSet = new PieDataSet(yEntries, getResources().getString(R.string.categories));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setSliceSpace(1.5f);
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataSet.setSelectionShift(10f);

        PieData pd = new PieData(xEntries, dataSet);
        pd.setValueTextSize(10f);
        pd.setValueFormatter(new PercentFormatter());

        pieChart.setData(pd);
        pieChart.animateY(1000);
        pieChart.highlightValue(new Highlight(highlighted, 0), true);
        pieChart.invalidate();
    }

    public void refreshDetailsChart() {
        if(currentSelectedCategory == null || currentSelectedCategory.getId() == null)
            return;

        Integer[] exceptedCats = null;
        if(exceptedCategories != null)
            exceptedCats = exceptedCategories.toArray(new Integer[0]);

        Cursor c = new Operation().sumOperationsGroupByDayOrderDateAsc(
                StaticData.getCurrentAccount(),
                StaticData.getDateField(StaticData.PREF_STATS_DATE_BEG),
                StaticData.getDateField(StaticData.PREF_STATS_DATE_END),
                exceptedCats
        );
        if(c == null)
            return;

        c.moveToFirst();

        /** Filling values with some real data **/
        Map<Integer, Map<String, Float>> values = new HashMap<>();
        do {
            int idxAmount = c.getColumnIndexOrThrow("amountString");
            int idxDate = c.getColumnIndexOrThrow("dateString");
            int idxCatId = c.getColumnIndexOrThrow(OperationData.KEY_ID_CATEGORY);
            String amountString = c.getString(idxAmount);
            String dateString = c.getString(idxDate);
            int catId = c.getInt(idxCatId);

            Category cat = new Category();
            Cursor cursorCat = cat.fetch(catId);
            cat.toDTO(cursorCat);
            cursorCat.close();

            try {
                float amount = Math.abs(Float.parseFloat(amountString));

                Map<String, Float> curVal = values.get(catId);
                if(curVal == null) {
                    curVal = new HashMap<>();
                    values.put(catId, curVal);
                }
                Float f = curVal.get(dateString);
                if(f == null || f == 0f)
                    curVal.put(dateString, amount);
                else
                    curVal.put(dateString, f+amount);
            } catch(NullPointerException npe) {
            } catch(NumberFormatException nfe) {}
        } while(c.moveToNext());

        // Closing cursor !
        c.close();

        /** Constructing x labels and y entries **/
        /** Chart is labeled by day **/
        int nbDaysTotal = DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd);
        List<String> xEntries = new ArrayList<>();
        Map<Integer, List<Entry>> yEntries = new HashMap<>();
        Date d = (Date)dateBegin.clone();
        for(int x=0; x<nbDaysTotal; x++) {
            String dateString = DateUtil.dateToStringNoTime(d);
            xEntries.add(dateString);

            Iterator it = values.keySet().iterator();
            while(it.hasNext()) {
                int catId = (int)it.next();
                Map<String, Float> v = values.get(catId);
                Float f = v.get(dateString) == null ? 0f : v.get(dateString);
                List<Entry> curEntries = yEntries.get(catId);
                if(curEntries == null) {
                    curEntries = new ArrayList<>();
                    yEntries.put(catId, curEntries);
                }
                curEntries.add(new Entry(f, x));
            }

            d = DateUtil.addDays(d, 1);
        }

        /** Creating chart based on entries **/
        int[] colors = ColorTemplate.JOYFUL_COLORS;
        int xColor = 0;
        List<LineDataSet> dataSets = new ArrayList<>();
        Iterator it = yEntries.keySet().iterator();
        while(it.hasNext()) {
            Integer key = (Integer)it.next();
            List<Entry> vals = yEntries.get(key);
            Category cat = new Category();
            Cursor cursor = cat.fetch(key);
            cat.toDTO(cursor);
            cursor.close();
            LineDataSet lds = new LineDataSet(vals, cat.getName());
            lds.setAxisDependency(YAxis.AxisDependency.LEFT);
            lds.setColor(colors[(xColor++) % colors.length]);
            if(key == currentSelectedCategory.getId()) {
                lds.setLineWidth(2);
            }
            dataSets.add(lds);
        }


        LineData data = new LineData(xEntries, dataSets);
        data.setValueTextSize(10f);

        detailsChart.setData(data);
        //detailsChart.animateY(1000);
        detailsChart.setDescription("description = " + currentSelectedCategory.getName());

        detailsChart.invalidate();
    }
}
