package org.maupu.android.tmh;

import greendroid.graphics.drawable.ActionBarDrawable;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.NormalActionBarItem;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.DialogHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.DateGalleryAdapter;
import org.maupu.android.tmh.ui.widget.StatsCursorAdapter;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.NumberUtil;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

@SuppressLint("UseSparseArrays")
public class StatsActivity extends TmhActivity implements OnItemSelectedListener {
	private ListView listView;
	private Gallery galleryDate;
	private Gallery galleryDateBegin;
	private Gallery galleryDateEnd;
	private LinearLayout layoutAdvancedGallery;
	//private AlertDialog alertDialogWithdrawalCategory;
	private final static String LAST_MONTH_SELECTED = "lastMonthSelectedItemPosition";
	//private CheckableCursorAdapter categoryChooserAdapter = null;
	//private AlertDialog dialogCategoryChooser = null;
	private boolean resetDialogCategoryChooser = false;
	private final static int GROUP_BY_DATE=0;
	private final static int GROUP_BY_CATEGORY=1;
	private int currentGroupBy = GROUP_BY_DATE;
	private QuickActionGrid quickActionGridFilter = null;
	private LinearLayout graphViewLayout = null;
	private StatsGraphView statGraphView = null;
	private TableLayout statsTextLayout = null;
	private StatsCursorAdapter statsCursorAdapter = null;
	private boolean showGraph = true;
	//private TextView tvStatsTotal = null;
	//private TextView tvStatsAverage = null;

	public StatsActivity() {
		if(StaticData.getStatsDateBeg() == null || StaticData.getStatsDateEnd() == null) {
			Date now = new GregorianCalendar().getTime();
			StaticData.setStatsDateBeg(DateUtil.getFirstDayOfMonth(now));
			StaticData.setStatsDateEnd(DateUtil.getLastDayOfMonth(now));
			StaticData.setStatsAdvancedFilter(false);
		}


		try {
			if(! DialogHelper.isCheckableCursorAdapterInit())
				StaticData.getStatsExpectedCategories().add(StaticData.getWithdrawalCategory().getId());
		} catch (NullPointerException npe) {
			// Nothing to do here if null
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setActionBarContentView(R.layout.stats_activity);
		setTitle(R.string.activity_title_statistics);

		// force portrait
		super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		/* Display a custom icon as action bar item */
		int drawableId = R.drawable.action_bar_graph;
		int descriptionId = R.string.graph;

		final Drawable d = new ActionBarDrawable(getActionBar().getContext(), drawableId);
		ActionBarItem abiGraph = getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(d).setContentDescription(descriptionId);
		addActionBarItem(abiGraph, TmhApplication.ACTION_BAR_GRAPH);
		/* End of displaying a custom icon in action bar item */
		// Change list type (sum by category or by date)
		addActionBarItem(Type.Settings, TmhApplication.ACTION_BAR_GROUPBY);
		// Possibility to change between accounts
		addActionBarItem(Type.Group, TmhApplication.ACTION_BAR_ACCOUNT);
		//
		quickActionGridFilter = new QuickActionGrid(this);
		quickActionGridFilter.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_list, R.string.date));
		quickActionGridFilter.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_sort_by_size, R.string.category));

		quickActionGridFilter.setOnQuickActionClickListener(new OnQuickActionClickListener() {
			@Override
			public void onQuickActionClicked(QuickActionWidget widget, int position) {
				currentGroupBy = position;
				refreshDisplay();
			}
		});


		//
		listView = (ListView)findViewById(R.id.list);
		galleryDate = (Gallery)findViewById(R.id.gallery_date);
		layoutAdvancedGallery = (LinearLayout)findViewById(R.id.layout_period);
		galleryDateBegin = (Gallery)findViewById(R.id.gallery_date_begin);
		galleryDateEnd = (Gallery)findViewById(R.id.gallery_date_end);
		//tvStatsTotal = (TextView)findViewById(R.id.stats_total_value);
		//tvStatsAverage = (TextView)findViewById(R.id.stats_avg_value);



		/*final TmhActivity activity = this;
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(activity, PreferencesActivity.class));
				activity.finish();
			}
		};


		alertDialogWithdrawalCategory = SimpleDialog.errorDialog(
				this, 
				getString(R.string.warning), 
				getString(R.string.default_category_warning),
				listener).create();
		 */

		initHeaderGalleries();
		refreshHeaderGallery();
		refreshDisplay();
	}

	@Override
	protected void onDestroy() {
		closeStatsCursorAdapterIfNeeded();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(statGraphView == null) {
			graphViewLayout = (LinearLayout)findViewById(R.id.graph);
			statGraphView = new StatsGraphView(this, graphViewLayout);
			statsTextLayout = (TableLayout)findViewById(R.id.stats_text);
		} else {
			refreshDisplay();
		}
	}

	public StatsGraphView getStatsGraphView() {
		return statGraphView;
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch(item.getItemId()) {
		case TmhApplication.ACTION_BAR_GROUPBY:
			quickActionGridFilter.show(item.getItemView());
			break;
		case TmhApplication.ACTION_BAR_ACCOUNT:
			DialogHelper.popupDialogAccountChooser(this);
			resetDialogCategoryChooser = true;
			break;
		case TmhApplication.ACTION_BAR_GRAPH:
			// Toggle graph / text stats
			toggleGraphAndText();
			break;
		default:
			return super.onHandleActionBarItemClick(item, position);
		}

		return true;
	}

	private void initHeaderGalleries() {
		List<Date> datesAdvanced = new ArrayList<Date>();
		List<Date> dates = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();

		// Advance selection
		galleryDate.setVisibility(View.GONE);
		layoutAdvancedGallery.setVisibility(View.VISIBLE);

		cal.add(Calendar.MONTH, -1);
		Date dateBegMin = DateUtil.getFirstDayOfMonth(cal.getTime());

		cal.add(Calendar.MONTH, 1);
		Date dateMediumMin = DateUtil.getFirstDayOfMonth(cal.getTime());

		cal.add(Calendar.MONTH, 1);
		Date dateEndMin = DateUtil.getFirstDayOfMonth(cal.getTime());

		Calendar tmpCal = Calendar.getInstance();
		tmpCal.setTime(dateBegMin);
		int maxDateBeg = tmpCal.getActualMaximum(Calendar.DAY_OF_MONTH);
		tmpCal.setTime(dateMediumMin);
		int maxDateMedium = tmpCal.getActualMaximum(Calendar.DAY_OF_MONTH);
		tmpCal.setTime(dateEndMin);
		int maxDateEnd = tmpCal.getActualMaximum(Calendar.DAY_OF_MONTH);

		int defaultPosition = 0;

		cal.setTime(dateBegMin);
		for(int i=0; i<maxDateBeg; i++) {
			datesAdvanced.add(cal.getTime());
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}

		Calendar calNow = Calendar.getInstance();
		int d = calNow.get(Calendar.DAY_OF_MONTH);
		int m = calNow.get(Calendar.MONTH);
		int y = calNow.get(Calendar.YEAR);
		Date now = new GregorianCalendar(y,m,d,0,0,0).getTime();

		cal.setTime(dateMediumMin);
		for(int i=0; i<maxDateMedium; i++) {
			datesAdvanced.add(cal.getTime());

			if(cal.getTimeInMillis() == now.getTime())
				defaultPosition = maxDateBeg + i;

			cal.add(Calendar.DAY_OF_MONTH, 1);
		}

		cal.setTime(dateEndMin);
		for(int i=0; i<maxDateEnd; i++) {
			datesAdvanced.add(cal.getTime());
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}

		DateGalleryAdapter adapter1 = new DateGalleryAdapter(this, datesAdvanced);
		//SimpleDateFormat dateFirst = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat dateFirst = SimpleDateFormat.getDateInstance();
		adapter1.setFirstDateFormat(dateFirst);
		adapter1.setSecondDateFormat(null);

		DateGalleryAdapter adapter2 = new DateGalleryAdapter(this, datesAdvanced);
		adapter2.setFirstDateFormat(dateFirst);
		adapter2.setSecondDateFormat(null);

		galleryDateBegin.setAdapter(adapter1);
		galleryDateBegin.setSelection(defaultPosition-1);

		galleryDateEnd.setAdapter(adapter2);
		galleryDateEnd.setSelection(defaultPosition);

		galleryDateBegin.setOnItemSelectedListener(this);
		galleryDateEnd.setOnItemSelectedListener(this);


		// Normal selection (by entire month)
		cal = Calendar.getInstance();
		//cal.add(Calendar.MONTH, -7);
		cal.add(Calendar.MONTH, -24);
		for(int i=0; i<25; i++) {
			cal.add(Calendar.MONTH, 1);
			dates.add(cal.getTime());
		}

		//defaultPosition = dates.size()/2;
		defaultPosition = dates.size()-2;
		StaticData.setPreferenceValueInt(StatsActivity.LAST_MONTH_SELECTED, defaultPosition);
		galleryDate.setAdapter(new DateGalleryAdapter(this, dates));
		galleryDate.setSelection(defaultPosition);
		galleryDate.setOnItemSelectedListener(this);
	}

	private void refreshHeaderGallery() {
		if(galleryDateBegin.getAdapter() == null || galleryDateEnd.getAdapter() == null)
			initHeaderGalleries();

		if(StaticData.isStatsAdvancedFilter()) {
			galleryDate.setVisibility(View.GONE);
			layoutAdvancedGallery.setVisibility(View.VISIBLE);
		} else {
			layoutAdvancedGallery.setVisibility(View.GONE);
			galleryDate.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stats_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.item_period:
			StaticData.setStatsAdvancedFilter(!StaticData.isStatsAdvancedFilter());
			refreshHeaderGallery();
			refreshDisplay();
			break;
		case R.id.item_now:
			//galleryDate.setSelection(galleryDate.getAdapter().getCount()/2, true);
			galleryDate.setSelection(galleryDate.getAdapter().getCount()-2, true);
			((DateGalleryAdapter)galleryDate.getAdapter()).notifyDataSetChanged();
			refreshDisplay();
			break;
		case R.id.item_categories:
			DialogHelper.popupDialogCategoryChooser(this, resetDialogCategoryChooser, true, true);
			resetDialogCategoryChooser = false;
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// Update items view (background color for selected item)
		Gallery gallery = (Gallery) parent;

		if(StaticData.isStatsAdvancedFilter()) {
			DateGalleryAdapter adapter1 = (DateGalleryAdapter)galleryDateBegin.getAdapter();
			adapter1.notifyDataSetChanged();

			DateGalleryAdapter adapter2 = (DateGalleryAdapter)galleryDateEnd.getAdapter();
			adapter2.notifyDataSetChanged();

			if(gallery == galleryDateBegin)
				StaticData.setStatsDateBeg((Date)adapter1.getItem(position));
			else if(gallery == galleryDateEnd)
				StaticData.setStatsDateEnd((Date)adapter2.getItem(position));
		} else {
			// Update display for this period
			DateGalleryAdapter adapter = (DateGalleryAdapter)gallery.getAdapter();
			adapter.notifyDataSetChanged();
			StaticData.setPreferenceValueInt(StatsActivity.LAST_MONTH_SELECTED, position);
		}

		refreshDisplay();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		Map<Integer, Object> results = new HashMap<Integer, Object>();
		Cursor cursorData = null;
		Cursor cursorStatsTotal = null;

		Account account = StaticData.getCurrentAccount();
		Operation dummyOp = new Operation();

		Date beg = null, end = null;
		if(! StaticData.isStatsAdvancedFilter()) {
			int oldPos = StaticData.getPreferenceValueInt(StatsActivity.LAST_MONTH_SELECTED);
			if(oldPos != -1) {
				Date dateSelected = (Date)galleryDate.getAdapter().getItem(oldPos);
				beg = DateUtil.getFirstDayOfMonth(dateSelected);
				end = DateUtil.getLastDayOfMonth(dateSelected);
			}
		} else {
			beg = StaticData.getStatsDateBeg();
			end = StaticData.getStatsDateEnd();
		}
		
		Date now = new GregorianCalendar().getTime();
		if(end.getTime() - now.getTime() <= 0)
			now = end; 	// end is before now, we truncate date to now to have a good average
						// Otherwise, we keep configured end date
		
		long diff = Math.abs(now.getTime() - beg.getTime());
		long nbDays = diff / 86400000;

		switch(currentGroupBy) {
		case GROUP_BY_DATE:
			cursorData = dummyOp.sumOperationsGroupByDay(account, beg, now, StaticData.getStatsExpectedCategoriesToArray());
			break;
		case GROUP_BY_CATEGORY:
			cursorData = dummyOp.sumOperationsGroupByCategory(account, beg, now, StaticData.getStatsExpectedCategoriesToArray());
			break;
		}

		// Get total
		Map<String,StatsData> statsList = new HashMap<String, StatsData>();

		
		cursorStatsTotal = dummyOp.sumOperationsByPeriod(account, beg, now, StaticData.getStatsExpectedCategoriesToArray());
		int cursorStatsSize = cursorStatsTotal.getCount();
		if(cursorStatsTotal != null && cursorStatsSize > 0) {
			for(int i = 0; i<cursorStatsSize; i++) {
				int idxSum = cursorStatsTotal.getColumnIndexOrThrow(Operation.KEY_SUM);
				int idxCurrencyName = cursorStatsTotal.getColumnIndexOrThrow(CurrencyData.KEY_SHORT_NAME);
				int idxCurrencyRate = cursorStatsTotal.getColumnIndexOrThrow(CurrencyData.KEY_CURRENCY_LINKED);
				Double sum = cursorStatsTotal.getDouble(idxSum);
				String currencyStr = cursorStatsTotal.getString(idxCurrencyName);
				Double rate = cursorStatsTotal.getDouble(idxCurrencyRate);
				Double averageByDay = nbDays > 0 ? sum / nbDays : 0;

				// put data and move to next entry
				statsList.put(currencyStr, new StatsData(currencyStr, sum, averageByDay, rate));
				cursorStatsTotal.moveToNext();
			}
		}

		results.put(0, cursorData);
		results.put(1, statsList);

		return results;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> results) {
		String[] from = null;
		int[] to = null;
		switch(currentGroupBy) {
		case GROUP_BY_DATE:
			from = new String[]{"dateString", "amountString"};
			to = new int[]{R.id.text, R.id.amount};
			break;
		case GROUP_BY_CATEGORY:
			from = new String[]{CategoryData.KEY_NAME, "amountString", "avg"};
			to = new int[]{R.id.text, R.id.amount, R.id.average};
			break;
		}

		closeStatsCursorAdapterIfNeeded();

		// Set cursor from selected period
		statsCursorAdapter = new StatsCursorAdapter(this,
				R.layout.stats_item,
				(Cursor)results.get(0),
				from,
				to);
		listView.setAdapter(statsCursorAdapter);

		// refresh graphical view
		if(statGraphView != null) {
			statGraphView.clear();
			String colName = currentGroupBy == GROUP_BY_CATEGORY ? CategoryData.KEY_NAME : "dateString";
			String colValue = "amountString";

			int count = statsCursorAdapter.getCount();
			for (int i = 0; i < count; i++) {
				Cursor c = (Cursor)statsCursorAdapter.getItem(i);

				int idxColName = c.getColumnIndex(colName);
				int idxColValue = c.getColumnIndex(colValue);

				String name = c.getString(idxColName);
				double value = c.getDouble(idxColValue);

				statGraphView.addToSeries(Color.rgb((int)(Math.random()*100%255), (int)(Math.random()*100%255), (int)(Math.random()*100%255)),
						name, 
						Math.abs(value));
			}

			statGraphView.refresh();
		}

		// Getting stats data
		@SuppressWarnings("unchecked")
		Map<String,StatsData> stats = (Map<String,StatsData>)results.get(1);
		Set<String> entries = stats.keySet(); 
		Iterator<String> it = entries.iterator();
		// Remove all views but headers
		statsTextLayout.removeViews(1, statsTextLayout.getChildCount()-1);
		
		
		TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.weight = 1;
		params.width = 0;
		
		while (it.hasNext()) {
			String key = it.next();
			StatsData d = stats.get(key);

			TextView tvStatsTotal = new TextView(this);
			TextView tvStatsAverage = new TextView(this);
			tvStatsTotal.setLayoutParams(params);
			tvStatsAverage.setLayoutParams(params);

			tvStatsTotal.setText(NumberUtil.formatDecimal(d.total) + " " + d.currencyShortName);
			tvStatsAverage.setText(NumberUtil.formatDecimal(d.average) + " " + d.currencyShortName);

			TableRow row = new TableRow(this);
			row.addView(tvStatsTotal);
			row.addView(tvStatsAverage);
			statsTextLayout.addView(row);
		}
	}

	private void closeStatsCursorAdapterIfNeeded() {
		try {
			statsCursorAdapter.getCursor().close();
		} catch (NullPointerException npe) {
			// Nothing to be done
		}
	}

	private void toggleGraphAndText() {
		showGraph = !showGraph;

		graphViewLayout.setVisibility(showGraph ? LinearLayout.VISIBLE : LinearLayout.GONE);
		statsTextLayout.setVisibility(!showGraph ? LinearLayout.VISIBLE : LinearLayout.GONE);
	}

	private class StatsData {
		public String currencyShortName;
		public Double average;
		public Double total;
		public Double rate;

		public StatsData(String currencyShortName, Double total, Double average, Double rate) {
			this.currencyShortName = currencyShortName;
			this.total = total;
			this.average = average;
			this.rate = rate;
		}
	}
}