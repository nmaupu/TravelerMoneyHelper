package org.maupu.android.tmh.stats;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CategoriesLineChart extends LineChart implements IStatsPanel, IStatsDataChangedListener {
    private static final String TAG = CategoriesLineChart.class.getName();
    private static final int LINE_WIDTH = 1;

    public CategoriesLineChart(Context context) {
        super(context);
    }

    public CategoriesLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoriesLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initPanel() {
        this.setHardwareAccelerationEnabled(true);
    }

    @Override
    public void onStatsDataChanged(StatsData data) {
        refreshPanel(data);
    }

    @Override
    public void refreshPanel(StatsData statsData) {
        Log.d(TAG, "refreshPanel called with animChart = "+statsData.isChartAnim());
        /** Construct all curves from statsData **/
        List<String> xEntries = StatsCategoryValues.buildXEntries(statsData.getDateBegin(), statsData.getDateEnd());
        // No data
        if(xEntries == null) {
            this.invalidate();
            return;
        }

        List<LineDataSet> dataSets = new ArrayList<>();

        /** First, draw others categories **/
        List<StatsCategoryValues> scvs = new ArrayList<>(statsData.values());
        Iterator<StatsCategoryValues> it = scvs.iterator();
        while(it.hasNext()) {
            StatsCategoryValues scv = it.next();

            LineDataSet lds = new LineDataSet(scv.getYEntries(), scv.getName());
            if(statsData.getCatToHighlight() == null || ! statsData.getCatToHighlight().getId().equals(scv.getFirstCategory().getId())) {
                lds.setColor(scv.getColor());
                lds.setLineWidth(LINE_WIDTH);
                lds.setDrawCircleHole(true);
                lds.setCircleColor(scv.getColor());
                lds.setCircleSize(2f);
                dataSets.add(lds);
            }
        }

        /** Last, draw selected category (displayed on top of others curves) **/
        if(statsData.getCatToHighlight() != null && statsData.getCatToHighlight().getId() != null) {
            StatsCategoryValues scv = statsData.get(statsData.getCatToHighlight().getId());
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

        if(statsData.isChartAnim())
            this.animateXY(1000, 1000);
        this.clear();
        this.notifyDataSetChanged();
        this.setData(new LineData(xEntries, dataSets));
        if(statsData.getCatToHighlight() != null && statsData.getCatToHighlight().getName() != null)
            this.setDescription(statsData.getCatToHighlight().getName());
        else
            this.setDescription("");
        this.setGridBackgroundColor(Color.WHITE);
        this.invalidate();
    }
}