package org.maupu.android.tmh.stats;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.object.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoriesPieChart extends PieChart implements IStatsPanel, IStatsDataChangedListener {
    private static final String TAG = CategoriesPieChart.class.getName();

    public CategoriesPieChart(Context context) {
        super(context);
    }

    public CategoriesPieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoriesPieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initPanel() {
        this.setHardwareAccelerationEnabled(true);
        this.setTransparentCircleAlpha(150);
        this.setCenterText(super.getResources().getString(R.string.categories));
        this.setCenterTextSize(15f);
        this.setUsePercentValues(true);
        this.setDescription("");
        this.getLegend().setEnabled(false);
    }

    @Override
    public void onStatsDataChanged(StatsData data) {
        refreshPanel(data);
    }

    @Override
    public void refreshPanel(final StatsData statsData) {
        if (statsData == null || statsData.size() == 0) {
            this.invalidate();
            this.clear();
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
        for (int i = 0; i <= lim; i++) {
            int j = i + lim + 1;
            StatsCategoryValues scvi = values.get(i);
            colors[xIndex] = scvi.getColor();
            xEntries.add(scvi.getName());
            yEntries.add(new Entry(scvi.summarize(), xIndex++, scvi));
            if (j < nbElts) {
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

        this.setData(pd);

        final CategoriesPieChart thisInstance = this;
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (statsData.isChartAnim())
                    thisInstance.animateY(1000);
                highlightInPieChart(statsData.getCatToHighlight(), statsData.getBiggestCategory());
                thisInstance.invalidate();
            }
        });
    }

    /**
     * Highlight specified category in pie chart. If not found, highlight another category.
     * If neither found, highlight nothing.
     * 1) If both catToLookFor and catIfNotFound are null, don't highlight anything
     * 2) If catToLookFor is null, try to highlight catIfNotFound (recursive call)
     * 3) If catToLookFor is not found and catIfNotFound is set, trying to highlight catIfNotFound (recursive call)
     * 4) If catToLookFor is not found and catIfNotFound is null, don't highlight anything (recursive call)
     *
     * @param catToLookFor  Category to highlight in first priority
     * @param catIfNotFound Category to highlight if catToLookFor is not found
     */
    public void highlightInPieChart(final Category catToLookFor, final Category catIfNotFound) {
        if (catToLookFor == null && catIfNotFound == null) {
            /** Case number 1) **/
            this.highlightValue(null, true);
        } else if (catToLookFor == null) {
            /** Case number 2) **/
            highlightInPieChart(catIfNotFound, null);
        } else {
            /** Case number 3 - Trying to find catToLookFor **/
            // Getting index of element in dataset
            // new Highlight(x axis element, x of the dataset - 0 for my case, I only have one dataset inside
            PieDataSet pds = this.getData().getDataSetByLabel(getResources().getString(R.string.categories), false);
            List<Entry> yEntries = pds.getYVals();
            int x;
            int yEntriesSize = yEntries.size();
            for (x = 0; x < yEntriesSize; x++) {
                Entry entry = yEntries.get(x);
                StatsCategoryValues scv = (StatsCategoryValues) entry.getData();

                if (scv != null && scv.contains(catToLookFor.getId())) {
                    // Highlighting found element
                    this.highlightValue(new Highlight(x, 0), true
                    );
                    return;
                }
            }

            // Nothing found, trying catIfNotFound
            highlightInPieChart(catIfNotFound, null);
        }
    }
}
