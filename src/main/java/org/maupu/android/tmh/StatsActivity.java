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
import java.util.List;
import java.util.Map;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.DialogHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.DateGalleryAdapter;
import org.maupu.android.tmh.ui.widget.StatsCursorAdapter;
import org.maupu.android.tmh.util.DateUtil;

import android.annotation.SuppressLint;
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
	private StatsGraphView statGraphView = null;
	private StatsCursorAdapter statsCursorAdapter = null;

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
		//super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
			LinearLayout graphLayout = (LinearLayout)findViewById(R.id.graph);
			statGraphView = new StatsGraphView(this, graphLayout);
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
		galleryDateBegin.setSelection(defaultPosition);

		galleryDateEnd.setAdapter(adapter2);
		galleryDateEnd.setSelection(defaultPosition+1);

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
		Cursor cursor = null;

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

		switch(currentGroupBy) {
		case GROUP_BY_DATE:
			cursor = dummyOp.sumOperationsGroupByDay(account, beg, end, StaticData.getStatsExpectedCategoriesToArray());
			break;
		case GROUP_BY_CATEGORY:
			cursor = dummyOp.sumOperationsGroupByCategory(account, beg, end, StaticData.getStatsExpectedCategoriesToArray());
			break;
		}

		results.put(0, cursor);
		return results;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> results) {
		String[] from = null;
		switch(currentGroupBy) {
		case GROUP_BY_DATE:
			from = new String[]{"dateString", "amountString"};
			break;
		case GROUP_BY_CATEGORY:
			from = new String[]{CategoryData.KEY_NAME, "amountString"};
			break;
		}

		closeStatsCursorAdapterIfNeeded();

		// Set cursor from selected period
		statsCursorAdapter = new StatsCursorAdapter(this,
				R.layout.stats_item,
				(Cursor)results.get(0),
				from,
				new int[]{R.id.text, R.id.amount});
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
	}

	private void closeStatsCursorAdapterIfNeeded() {
		try {
			statsCursorAdapter.getCursor().close();
		} catch (NullPointerException npe) {
			// Nothing to be done
		}
	}
}
