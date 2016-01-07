package org.maupu.android.tmh.stats;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.maupu.android.tmh.TmhActivity;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CategoriesLineChart extends LineChart implements IStatsPanel, IStatsDataChangedListener {
    private static final Class TAG = CategoriesLineChart.class;
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
        this.setDrawMarkerViews(false);
        this.setHighlightPerDragEnabled(false);
        this.setHighlightPerTapEnabled(false);
        this.setHardwareAccelerationEnabled(true);
    }

    @Override
    public void onStatsDataChanged(StatsData data) {
        refreshPanel(data);
    }

    @Override
    public void refreshPanel(final StatsData statsData) {
        TmhLogger.d(TAG, "refreshPanel called with animChart = " + statsData.isChartAnim());
        /** Construct all curves from statsData **/
        final List<String> xEntries = StatsCategoryValues.buildXEntries(statsData.getDateBegin(), statsData.getDateEnd());
        // No data
        if(xEntries == null) {
            this.invalidate();
            return;
        }

        final List<LineDataSet> dataSets = new ArrayList<>();

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
                lds.setDrawValues(false);
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
                lds.setDrawValues(false);
                dataSets.add(lds);
            }
        }

        final CategoriesLineChart thisInstance = this;
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (statsData.isChartAnim())
                    thisInstance.animateXY(1000, 1000);
                thisInstance.clear();
                thisInstance.notifyDataSetChanged();
                thisInstance.setData(new LineData(xEntries, dataSets));
                if (statsData.getCatToHighlight() != null && statsData.getCatToHighlight().getName() != null)
                    thisInstance.setDescription(statsData.getCatToHighlight().getName());
                else
                    thisInstance.setDescription("");
                thisInstance.setGridBackgroundColor(Color.WHITE);
                thisInstance.invalidate();
            }
        });
    }
}
