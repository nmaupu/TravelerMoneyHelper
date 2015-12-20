package org.maupu.android.tmh;

import java.text.DateFormat;
import java.text.ParseException;
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
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.DialogHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.CustomDatePickerDialog;
import org.maupu.android.tmh.ui.widget.DateGalleryAdapter;
import org.maupu.android.tmh.ui.widget.StatsCursorAdapter;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.NumberUtil;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class StatsActivityOld extends TmhActivity implements OnItemSelectedListener, OnItemClickListener, OnDateSetListener {
	private static final int DATE_DIALOG_ID = 0;
	private ListView listView;
	private Gallery galleryDate;
	private Gallery galleryDateBegin;
	private Gallery galleryDateEnd;
	private LinearLayout layoutAdvancedGallery;
	private final static String LAST_MONTH_SELECTED = "lastMonthSelectedItemPosition";
	private boolean resetDialogCategoryChooser = false;
	private final static int GROUP_BY_DATE=0;
	private final static int GROUP_BY_CATEGORY=1;
	private int currentGroupBy = GROUP_BY_DATE;
	//private QuickActionGrid quickActionGridFilter = null;
	private LinearLayout graphViewLayout = null;
	private StatsGraphViewOld statGraphView = null;
	private TableLayout statsTextLayout = null;
	private StatsCursorAdapter statsCursorAdapter = null;
	private CustomDatePickerDialog customDatePickerDialog = null;
	private int choosenYear=0, choosenMonth=0, choosenDay=0;
	private Calendar currentDateDisplayed = Calendar.getInstance();

    private final static int DRAWER_ITEM_PERIOD = TmhApplication.getIdentifier("DRAWER_ITEM_PERIOD");
    private final static int DRAWER_ITEM_AUTO = TmhApplication.getIdentifier("DRAWER_ITEM_AUTO");
    private final static int DRAWER_ITEM_CAT_EXCEPT = TmhApplication.getIdentifier("DRAWER_ITEM_CAT_EXCEPT");
    private final static int DRAWER_ITEM_TODAY = TmhApplication.getIdentifier("DRAWER_ITEM_TODAY");

	public StatsActivityOld() {
        super(R.layout.stats_activity_old, R.string.activity_title_statistics);

		if(StaticData.getDateField(StaticData.PREF_STATS_DATE_BEG) == null || StaticData.getDateField(StaticData.PREF_STATS_DATE_END) == null) {
			Date now = new GregorianCalendar().getTime();
			StaticData.setDateField(StaticData.PREF_STATS_DATE_BEG, DateUtil.getFirstDayOfMonth(now));
			StaticData.setDateField(StaticData.PREF_STATS_DATE_END, DateUtil.getLastDayOfMonth(now));
			StaticData.setStatsAdvancedFilter(false);
		}

        if(!DialogHelper.isCheckableCursorAdapterInit()) {
            autoSetExceptedCategories();
        }
	}

    @Override
    public int whatIsMyDrawerIdentifier() {
        return super.DRAWER_ITEM_STATS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // force portrait
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //
        listView = (ListView)findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        galleryDate = (Gallery)findViewById(R.id.gallery_date);
        layoutAdvancedGallery = (LinearLayout)findViewById(R.id.layout_period);
        galleryDateBegin = (Gallery)findViewById(R.id.gallery_date_begin);
        galleryDateEnd = (Gallery)findViewById(R.id.gallery_date_end);

        initHeaderGalleries();
        setViewToAutoDates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stats_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_swap_mode:
                toggleGroupBy();
                break;
            case R.id.action_graph_info:
                toggleGraphAndText();
                break;
            case R.id.action_account:
                DialogHelper.popupDialogAccountChooser(this);
                resetDialogCategoryChooser = true;
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void autoSetExceptedCategories() {
        try {
            Operation o = new Operation();
            Integer[] cats = o.getExceptCategoriesAuto(StaticData.getCurrentAccount());
            for (int i = 0; i < cats.length; i++)
                StaticData.getStatsExceptedCategories().add(cats[i]);
        } catch (NullPointerException npe) {
            // Nothing to do here if null
        }
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
			statGraphView = new StatsGraphViewOld(this, graphViewLayout);
			statsTextLayout = (TableLayout)findViewById(R.id.stats_text);
		} else {
			refreshDisplay();
		}
	}

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem item) {
        if(DRAWER_ITEM_PERIOD == item.getIdentifier()) {
            StaticData.setStatsAdvancedFilter(!StaticData.isStatsAdvancedFilter());
            refreshHeaderGallery();
            refreshDisplay();
        } else if(DRAWER_ITEM_AUTO == item.getIdentifier()) {
            setViewToAutoDates();
        } else if(DRAWER_ITEM_CAT_EXCEPT == item.getIdentifier()) {
            DialogHelper.popupDialogCategoryChooser(this, resetDialogCategoryChooser, true);
            resetDialogCategoryChooser = false;
        } else if(DRAWER_ITEM_TODAY == item.getIdentifier()) {
            // Get info for the current day
            //StaticData.setStatsAdvancedFilter(true);
            Date beg = new Date();
            DateUtil.resetDateToBeginingOfDay(beg);
            Date end = (Date)beg.clone();
            DateUtil.resetDateToEndOfDay(end);
            currentDateDisplayed.setTime(end);
            StaticData.setDateField(StaticData.PREF_STATS_DATE_BEG, beg);
            StaticData.setDateField(StaticData.PREF_STATS_DATE_END, end);

            initHeaderGalleries();

            int pos = ((DateGalleryAdapter)galleryDateBegin.getAdapter()).getItemPosition(beg);
            galleryDateBegin.setSelection(pos);
            ((DateGalleryAdapter)galleryDateBegin.getAdapter()).notifyDataSetChanged();

            pos =  ((DateGalleryAdapter)galleryDateEnd.getAdapter()).getItemPosition(end);
            galleryDateEnd.setSelection(pos);
            ((DateGalleryAdapter)galleryDateEnd.getAdapter()).notifyDataSetChanged();

            refreshHeaderGallery();
            refreshDisplay();
        }

        return super.onItemClick(view, position, item);
    }

    public void setViewToAutoDates() {
        StaticData.setStatsAdvancedFilter(true);

        Operation dummyOp = new Operation();
        Date autoBeg = dummyOp.getFirstDate(StaticData.getCurrentAccount(), StaticData.getStatsExceptedCategoriesToArray());
        Date autoEnd = dummyOp.getLastDate(StaticData.getCurrentAccount(), StaticData.getStatsExceptedCategoriesToArray());
        Log.d(StatsActivityOld.class.getName(), "Auto dates computed beg="+autoBeg+", end="+autoEnd);
        if(autoBeg != null && autoEnd != null) {
            currentDateDisplayed.setTime(autoEnd);
            StaticData.setDateField(StaticData.PREF_STATS_DATE_BEG, autoBeg);
            StaticData.setDateField(StaticData.PREF_STATS_DATE_END, autoEnd);

            initHeaderGalleries();

            int autoPos = ((DateGalleryAdapter)galleryDateBegin.getAdapter()).getItemPosition(autoBeg);
            galleryDateBegin.setSelection(autoPos);
            ((DateGalleryAdapter)galleryDateBegin.getAdapter()).notifyDataSetChanged();

            autoPos = ((DateGalleryAdapter)galleryDateEnd.getAdapter()).getItemPosition(autoEnd);
            galleryDateEnd.setSelection(autoPos);
            ((DateGalleryAdapter)galleryDateEnd.getAdapter()).notifyDataSetChanged();
        }

        refreshHeaderGallery();
        refreshDisplay();
    }

    @Override
    public IDrawerItem[] buildNavigationDrawer() {
        return new IDrawerItem[]{
                createSecondaryDrawerItem(
                        DRAWER_ITEM_TODAY,
                        R.drawable.ic_today_black,
                        R.string.today),
                createSecondaryDrawerItem(
                        DRAWER_ITEM_PERIOD,
                        R.drawable.ic_period_black,
                        R.string.menu_item_period),
                createSecondaryDrawerItem(
                        DRAWER_ITEM_AUTO,
                        R.drawable.ic_event_black,
                        R.string.menu_item_auto),
                createSecondaryDrawerItem(
                        DRAWER_ITEM_CAT_EXCEPT,
                        R.drawable.ic_cat_except_black,
                        R.string.categories_exception),
        };
    }

	private void initHeaderGalleries() {
		List<Date> datesAdvanced = new ArrayList<Date>();
		List<Date> dates = new ArrayList<Date>();
		Calendar cal = (Calendar)currentDateDisplayed.clone();

		// Advance selection
		galleryDate.setVisibility(View.GONE);
		layoutAdvancedGallery.setVisibility(View.VISIBLE);

		cal.add(Calendar.MONTH, -1);
		Date dateBegMin = DateUtil.getFirstDayOfMonth(cal.getTime());

		cal.add(Calendar.MONTH, 1);
		Date dateMediumMin = DateUtil.getFirstDayOfMonth(cal.getTime());

		cal.add(Calendar.MONTH, 1);
		Date dateEndMin = DateUtil.getFirstDayOfMonth(cal.getTime());

		Calendar tmpCal = (Calendar)currentDateDisplayed.clone();
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

		Date now = DateUtil.getCurrentDate();

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
		
		// Listeners
		galleryDateBegin.setOnItemSelectedListener(this);
		galleryDateEnd.setOnItemSelectedListener(this);
		
		// Restore previous selected position
		Date beg = StaticData.getDateField(StaticData.PREF_STATS_DATE_BEG);
		Date end = StaticData.getDateField(StaticData.PREF_STATS_DATE_END);
		if(beg != null && end != null) {
			int pos = ((DateGalleryAdapter)galleryDateBegin.getAdapter()).getItemPosition(beg);
			if(pos != -1) {
				galleryDateBegin.setSelection(pos);
				((DateGalleryAdapter)galleryDateBegin.getAdapter()).notifyDataSetChanged();
			}
			
			pos = ((DateGalleryAdapter)galleryDateEnd.getAdapter()).getItemPosition(end);
			if(pos != -1) {
				galleryDateEnd.setSelection(pos);
				((DateGalleryAdapter)galleryDateEnd.getAdapter()).notifyDataSetChanged();
			}
		}

		// Normal selection (by entire month)
		cal = (Calendar)currentDateDisplayed.clone();
		//cal.add(Calendar.MONTH, -7);
		cal.add(Calendar.MONTH, -24);
		for(int i=0; i<25; i++) {
			cal.add(Calendar.MONTH, 1);
			dates.add(cal.getTime());
		}

		//defaultPosition = dates.size()/2;
		defaultPosition = dates.size()-2;
		StaticData.setPreferenceValueInt(StatsActivityOld.LAST_MONTH_SELECTED, defaultPosition);
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
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// Update items view (background color for selected item)
		Gallery gallery = (Gallery) parent;

		if(StaticData.isStatsAdvancedFilter()) {
			DateGalleryAdapter adapter1 = (DateGalleryAdapter)galleryDateBegin.getAdapter();
			adapter1.notifyDataSetChanged();

			DateGalleryAdapter adapter2 = (DateGalleryAdapter)galleryDateEnd.getAdapter();
			adapter2.notifyDataSetChanged();

			if(gallery == galleryDateBegin) {
				Date beg = DateUtil.resetDateToBeginingOfDay((Date)adapter1.getItem(position));
				StaticData.setDateField(StaticData.PREF_STATS_DATE_BEG, beg);
			} else if(gallery == galleryDateEnd) {
				Date end = DateUtil.resetDateToEndOfDay((Date)adapter2.getItem(position));
				StaticData.setDateField(StaticData.PREF_STATS_DATE_END, end);
			}
		} else {
			// Update display for this period
			DateGalleryAdapter adapter = (DateGalleryAdapter)gallery.getAdapter();
			adapter.notifyDataSetChanged();
            StaticData.setPreferenceValueInt(StatsActivityOld.LAST_MONTH_SELECTED, position);
		}

		refreshDisplay();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void refreshAfterCurrentAccountChanged() {
        super.refreshAfterCurrentAccountChanged();

        autoSetExceptedCategories();
        setViewToAutoDates();
    }

    @Override
	public Map<Integer, Object> handleRefreshBackground() {
        Log.d(StatsActivityOld.class.getName(), "handleRefreshBackground called");
		Map<Integer, Object> results = new HashMap<Integer, Object>();
		Cursor cursorData = null;
		Cursor cursorStatsTotal;

		Account account = StaticData.getCurrentAccount();

		Date beg = null, end = null;
		if(! StaticData.isStatsAdvancedFilter()) {
			int oldPos = StaticData.getPreferenceValueInt(StatsActivityOld.LAST_MONTH_SELECTED);
			if(oldPos != -1) {
				Date dateSelected = (Date)galleryDate.getAdapter().getItem(oldPos);
				beg = DateUtil.getFirstDayOfMonth(dateSelected);
				end = DateUtil.getLastDayOfMonth(dateSelected);
			}
		} else {
			beg = StaticData.getDateField(StaticData.PREF_STATS_DATE_BEG);
			end = StaticData.getDateField(StaticData.PREF_STATS_DATE_END);
		}
		
		// If end date is after now, we use now to have a well computed average
		Date now = new GregorianCalendar().getTime();
		if(end.after(now))
			end = now;
		
		// Getting first data of the month given by date beg and verify that date beg is 
		// not before first data for this account for average being computed well
		Operation dummyOp = new Operation();
		Date firstDate = dummyOp.getFirstDate(account, StaticData.getStatsExceptedCategoriesToArray());
		if(firstDate != null && firstDate.after(beg))
			beg = firstDate;
		// Checking also end date is before current end date to have good amount computation
		Date endDate = dummyOp.getLastDate(account, StaticData.getStatsExceptedCategoriesToArray());
		if(endDate != null && endDate.before(end))
			end = endDate;

		int nbDays = DateUtil.getNumberOfDaysBetweenDates(beg, end);
		Log.d(StatsActivityOld.class.getName(), "Nb days = "+nbDays+" (dateBeg="+beg+", dateEnd="+end+")");
		switch(currentGroupBy) {
            case GROUP_BY_DATE:
                cursorData = dummyOp.sumOperationsGroupByDay(account, beg, end, StaticData.getStatsExceptedCategoriesToArray());
                break;
		    case GROUP_BY_CATEGORY:
			    cursorData = dummyOp.sumOperationsGroupByCategory(account, beg, end, StaticData.getStatsExceptedCategoriesToArray());
			    break;
		}

		// Get total
		Map<String,StatsData> statsList = new HashMap<String, StatsData>();

		cursorStatsTotal = dummyOp.sumOperationsByPeriod(account, beg, end, StaticData.getStatsExceptedCategoriesToArray());

        if(cursorStatsTotal == null)
            return null;

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
        Log.d(StatsActivityOld.class.getName(), "handleRefreshEnding called");

        if(results == null)
            return;

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
				R.layout.stats_item_old,
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
			StatsData statsData = stats.get(key);

			TextView tvStatsTotal = new TextView(this);
			TextView tvStatsAverage = new TextView(this);
			tvStatsTotal.setLayoutParams(params);
			tvStatsAverage.setLayoutParams(params);
			
			
			String currentCurrencyShortName = StaticData.getMainCurrency() != null ? StaticData.getMainCurrency().getShortName() : "NA";
			StringBuilder sb = new StringBuilder();
			sb.append(NumberUtil.formatDecimal(statsData.total))
				.append(" ")
				.append(statsData.currencyShortName)
				.append(" / ")
				.append(NumberUtil.formatDecimal(statsData.total / statsData.rate))
				.append(" ")
				.append(currentCurrencyShortName);

			tvStatsTotal.setText(sb.toString());
			
			sb = new StringBuilder();
			sb.append(NumberUtil.formatDecimal(statsData.average))
				.append(" ")
				.append(statsData.currencyShortName)
				.append(" / ")
				.append(NumberUtil.formatDecimal(statsData.average / statsData.rate))
				.append(currentCurrencyShortName);
			tvStatsAverage.setText(sb.toString());

			TableRow row = new TableRow(this);
			row.addView(tvStatsTotal);
			row.addView(tvStatsAverage);
			statsTextLayout.addView(row);
		}

		showGraph(StaticData.showGraph);
	}

	private void closeStatsCursorAdapterIfNeeded() {
		try {
			statsCursorAdapter.getCursor().close();
		} catch (NullPointerException npe) {
			// Nothing to be done
		}
	}

	private void toggleGraphAndText() {
		showGraph(!StaticData.showGraph);
	}

	private void showGraph(boolean b) {
		StaticData.showGraph = b;
		graphViewLayout.setVisibility(StaticData.showGraph ? LinearLayout.VISIBLE : LinearLayout.GONE);
        statsTextLayout.setVisibility(!StaticData.showGraph ? LinearLayout.VISIBLE : LinearLayout.GONE);
	}

    private void toggleGroupBy() {
        currentGroupBy = currentGroupBy == GROUP_BY_DATE ? GROUP_BY_CATEGORY : GROUP_BY_DATE;
        refreshDisplay();
    }
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		Cursor c = (Cursor)parent.getAdapter().getItem(pos);

		int idx;
		switch(currentGroupBy) {
		case GROUP_BY_DATE:
			idx = c.getColumnIndex(OperationData.KEY_DATE);
			String sDate = c.getString(idx);
			try {
				Date beg = DateUtil.resetDateToBeginingOfDay(DateUtil.StringSQLToDate(sDate));
				Date end = DateUtil.resetDateToEndOfDay((Date)beg.clone());
				DialogHelper.popupDialogStatsDetails(this, beg, end, null, StaticData.getStatsExceptedCategoriesToArray());
			} catch (ParseException pe) {
				pe.printStackTrace();
			}
			break;
		case GROUP_BY_CATEGORY:
			idx = c.getColumnIndex(CategoryData.KEY_NAME);
			String cat = c.getString(idx);
			
			Date beg = null, end = null;
			if(StaticData.isStatsAdvancedFilter()) {
				beg = StaticData.getDateField(StaticData.PREF_STATS_DATE_BEG);
				end = StaticData.getDateField(StaticData.PREF_STATS_DATE_END);
			} else {
				int position = StaticData.getPreferenceValueInt(StatsActivityOld.LAST_MONTH_SELECTED);
				if(position != -1) {
					Date dateSelected = (Date)galleryDate.getAdapter().getItem(position);
					beg = DateUtil.getFirstDayOfMonth(dateSelected);
					end = DateUtil.getLastDayOfMonth(dateSelected);
				}
			}
			
			DialogHelper.popupDialogStatsDetails(this, beg, end, cat, StaticData.getStatsExceptedCategoriesToArray());
			break;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * Dialog (date picker) about changing reference date for stats activity
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DATE_DIALOG_ID:
			if(choosenYear == 0) {
				// Initializing
				Calendar cal = Calendar.getInstance();
				
				choosenYear = cal.get(Calendar.YEAR);
				choosenMonth = cal.get(Calendar.MONTH);
				choosenDay = cal.get(Calendar.DAY_OF_MONTH);
			}
			customDatePickerDialog = new CustomDatePickerDialog(this, this, choosenYear, choosenMonth, choosenDay);
			return customDatePickerDialog;
		}
		
		return null;
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		choosenYear = year;
		choosenMonth = monthOfYear;
		choosenDay = dayOfMonth;
		currentDateDisplayed.set(Calendar.YEAR, choosenYear);
		currentDateDisplayed.set(Calendar.MONTH, choosenMonth);
		currentDateDisplayed.set(Calendar.DAY_OF_MONTH, choosenDay);
		StaticData.setDateField(StaticData.PREF_STATS_DATE_BEG, currentDateDisplayed.getTime());
		StaticData.setDateField(StaticData.PREF_STATS_DATE_END, currentDateDisplayed.getTime());
		
		initHeaderGalleries();
		refreshHeaderGallery();
		refreshDisplay();
	}

	private class StatsData {
		public String currencyShortName;
		public Double average;
		public Double total;
		public double rate;

		public StatsData(String currencyShortName, Double total, Double average, Double rate) {
			this.currencyShortName = currencyShortName;
			this.total = total;
			this.average = average;
			this.rate = rate;
		}
	}
}