package org.maupu.android.tmh;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class representing stats activity
 */
public class StatsActivity extends TmhActivity {
    private static final String TAG = StatsActivity.class.getName();
    private SlidingUpPanelLayout slidingPanel;
    private ImageView accountImage;
    private TextView dateBegin, dateEnd;
    private TextView marker;

    // Charts
    private PieChart pieChart;

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

        dateBegin = (TextView)findViewById(R.id.date_begin);
        dateEnd = (TextView)findViewById(R.id.date_end);

        // Charts
        pieChart = (PieChart)findViewById(R.id.pie_chart);
        marker = (TextView)findViewById(R.id.marker);
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
        initPieChart();
    }

    @Override
    public void refreshAfterCurrentAccountChanged() {
        refreshDisplay();
    }

    /**
     * Reinit all data in the view
     */
    public void reinitializeData() {
        autoSetExceptedCategories();
        autoSetDates();
    }

    private void autoSetDates() {
        Account currentAccount = StaticData.getCurrentAccount();
        if(currentAccount == null)
            return;

        Operation dummyOp = new Operation();
        Date autoBeg = dummyOp.getFirstDate(currentAccount, StaticData.getStatsExceptedCategoriesToArray());
        Date autoEnd = dummyOp.getLastDate(currentAccount, StaticData.getStatsExceptedCategoriesToArray());

        Log.d(TAG, "Auto dates computed beg=" + autoBeg + ", end=" + autoEnd);

        if(autoBeg != null && autoEnd != null) {
            StaticData.setDateField(StaticData.PREF_STATS_DATE_BEG, autoBeg);
            StaticData.setDateField(StaticData.PREF_STATS_DATE_END, autoEnd);

            dateBegin.setText(DateUtil.dateToStringNoTime(autoBeg));
            dateEnd.setText(DateUtil.dateToStringNoTime(autoEnd));
        }
    }

    private void autoSetExceptedCategories() {
        try {
            Operation o = new Operation();
            Integer[] cats = o.getExceptCategoriesAuto(StaticData.getCurrentAccount());
            StaticData.getStatsExceptedCategories().addAll(Arrays.asList(cats));
        } catch (NullPointerException npe) {
            // Nothing to do here if null
        }
    }

    private void initPieChart() {
        Account currentAccount = StaticData.getCurrentAccount();

        Cursor c = new Operation().sumOperationsGroupByCategory(
                currentAccount,
                StaticData.getStatsExceptedCategories().toArray(new Integer[0])
        );
        if(c==null)
            return;

        List<ValNames> entries = new ArrayList<>();
        c.moveToFirst();
        do {
            int idxAmount = c.getColumnIndexOrThrow("amountString");
            int idxAvg = c.getColumnIndexOrThrow("avg");
            int idxCategory = c.getColumnIndexOrThrow(CategoryData.KEY_NAME);

            String catName = c.getString(idxCategory);
            entries.add(new ValNames(
                    Math.abs(c.getFloat(idxAmount)),
                    catName)
            );

        } while(c.moveToNext());

        Collections.sort(entries);
        int size = entries.size();
        List<String> xEntries = new ArrayList<>();
        List<Entry> yEntries = new ArrayList<>();
        final int MAX = 4;
        ValNames aggregated = new ValNames(0, null);
        int highlighted = 0;
        for(int i=0; i<size; i++) {
            ValNames vn = entries.get(i);
            if(i<MAX) {
                // Store the value
                yEntries.add(new Entry(vn.value, i, vn));
                xEntries.add(vn.names.get(0));
            } else {
                // Aggregate the value
                aggregated.value += vn.value;
                aggregated.names.add(vn.names.get(0));
            }
        }
        if(size > MAX) {
            // Adding aggregated values, index is MAX
            xEntries.add(getResources().getString(R.string.misc));
            yEntries.add(new Entry(aggregated.value, MAX, aggregated));
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
        pieChart.setTransparentCircleAlpha(150);
        pieChart.setCenterText(getResources().getString(R.string.categories));
        pieChart.setCenterTextSize(20f);
        pieChart.setUsePercentValues(true);
        pieChart.setDescription("");
        pieChart.setHardwareAccelerationEnabled(true);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.d(TAG, "Highlight : " + h.getDataSetIndex() + " " + h.getXIndex());
                ValNames vn = (ValNames) e.getData();
                StringBuilder sb = new StringBuilder();
                Iterator<String> it = vn.names.iterator();
                while (it.hasNext()) {
                    sb.append(it.next());
                    sb.append("\n");
                }
                marker.setText(sb.toString());
            }

            @Override
            public void onNothingSelected() {
                marker.setText("Nothing selected");
            }
        });

        pieChart.getLegend().setEnabled(false);
        pieChart.highlightValue(new Highlight(highlighted, 0), true);
        pieChart.invalidate();
    }

    private class ValNames implements Comparable {
        public float value;
        public List<String> names = new ArrayList<>();

        public ValNames(float value, String name) {
            this.value = value;
            if(name != null)
                names.add(name);
        }

        @Override
        public int compareTo(Object another) {
            ValNames vn = (ValNames)another;
            int val1 = (int)vn.value;
            return (int)(val1 - this.value);
        }
    }
}
