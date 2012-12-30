package org.maupu.android.tmh;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class StatsGraphActivity extends TmhActivity {
	private DefaultRenderer mRenderer = null;
	private GraphicalView mChartView;

	// Warning : Even if we may draw categories, nothing related to TMH here ;)
	private CategorySeries mSeries = new CategorySeries("");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setActionBarContentView(R.layout.stats_graph_activity);
		setTitle(R.string.activity_title_statistics);

		initRenderer();
		
		/* Getting series from extra
		 * Series are store as a triple color, name, value inside a arrays named
		 * respectively colors, names and values
		 */
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		
		String[] names = b.getStringArray("names");
		int[] colors = b.getIntArray("colors");
		double[] values = b.getDoubleArray("values");
		
		for(int i=0; i<names.length; i++) {
			addToSeries(colors[i], names[i], values[i]);
		}
	}
	
	private void initRenderer() {
		mRenderer = new DefaultRenderer();
		
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.argb(100, 0, 0, 0));
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(15);
		mRenderer.setLegendTextSize(15);
		mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setStartAngle(90);
		mRenderer.setClickEnabled(false);
		mRenderer.setAntialiasing(true);
		mRenderer.setPanEnabled(false);
		mRenderer.setFitLegend(true);
	}

	@Override
	public void refreshDisplay() {
		if(mChartView != null)
			mChartView.repaint();
	}

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
	}
	
	public void addToSeries(int color, String name, double value) {
		Log.d(StatsGraphActivity.class.getName(), "Pushing serie : name="+name+", value="+value+", color="+color);
		if(mRenderer == null)
			initRenderer();
		
		mSeries.add(name, value);
		SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
		renderer.setColor(color);
		mRenderer.addSeriesRenderer(renderer);
	}
	
	public void clearSeries() {
		mSeries.clear();
		initRenderer();
	}
}
