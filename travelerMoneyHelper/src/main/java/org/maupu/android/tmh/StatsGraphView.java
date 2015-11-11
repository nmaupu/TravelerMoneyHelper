package org.maupu.android.tmh;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class StatsGraphView {
	private DefaultRenderer mRenderer = null;
	private GraphicalView mGraphicalView = null;
	private LinearLayout mLayout = null;
	private Context mCtx;

	// Warning : Even if we may draw categories, nothing related to TMH here ;)
	private CategorySeries mSeries = new CategorySeries("");
	
	public StatsGraphView(Context ctx, LinearLayout layout) {
		mLayout = layout;
		mCtx = ctx;
		
		initRenderer();
		
		//addToSeries(Color.BLACK, "dummy", 0);
		mGraphicalView = ChartFactory.getPieChartView(mCtx, mSeries, mRenderer);
		layout.addView(mGraphicalView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}
	
	public GraphicalView getGraphicalView() {
		return mGraphicalView;
	}
	
	public LinearLayout getLayout() {
		return mLayout;
	}
	
	public DefaultRenderer getRenderer() {
		return mRenderer;
	}
	
	//
	private void initRenderer() {
		if(mRenderer == null) {
			mRenderer = new DefaultRenderer();
		} else {
			SimpleSeriesRenderer[] series = mRenderer.getSeriesRenderers();
			for(int i=0; i<series.length; i++) {
				mRenderer.removeSeriesRenderer(series[i]);
			}
		}
		
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.argb(255, 255, 255, 255));
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(15);
		mRenderer.setLegendTextSize(15);
		mRenderer.setMargins(new int[] { 10, 10, 10, 10});
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setStartAngle(90);
		mRenderer.setClickEnabled(false);
		mRenderer.setAntialiasing(true);
		mRenderer.setPanEnabled(false);
		mRenderer.setFitLegend(true);
		mRenderer.setClickEnabled(true);
		mRenderer.setSelectableBuffer(10);
	}
	

	/*
	@Override
	protected void onResume() {
		super.onResume();

		if(mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
			mRenderer.setClickEnabled(true);
			mRenderer.setSelectableBuffer(10);
			
			layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		} else {
			refreshDisplay();
		}
	}*/
	
	public void addToSeries(int color, String name, double value) {
		Log.d(StatsGraphView.class.getName(), "Pushing serie : name="+name+", value="+value+", color="+color);
		
		mSeries.add(name, value);
		SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
		renderer.setColor(color);
		mRenderer.addSeriesRenderer(renderer);
	}
	
	public void clear() {
		mSeries.clear();
		initRenderer();
	}
	
	public void refresh() {
		mGraphicalView.repaint();
	}
}
