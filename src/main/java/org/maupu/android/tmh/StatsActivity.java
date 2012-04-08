package org.maupu.android.tmh;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.DateGalleryAdapter;
import org.maupu.android.tmh.ui.widget.StatsCursorAdapter;
import org.maupu.android.tmh.util.DateUtil;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
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

public class StatsActivity extends TmhActivity implements OnItemSelectedListener {
	private ListView listView;
	private Gallery galleryDate;
	private Gallery galleryDateBegin;
	private Gallery galleryDateEnd;
	private LinearLayout layoutAdvancedGallery;
	private AlertDialog alertDialogWithdrawalCategory;

	public StatsActivity() {
		if(StaticData.getStatsDateBeg() == null || StaticData.getStatsDateEnd() == null) {
			Date now = new GregorianCalendar().getTime();
			StaticData.setStatsDateBeg(DateUtil.getFirstDayOfMonth(now));
			StaticData.setStatsDateEnd(DateUtil.getLastDayOfMonth(now));
			StaticData.setStatsAdvancedFilter(false);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setActionBarContentView(R.layout.stats_activity);

		listView = (ListView)findViewById(R.id.list);
		galleryDate = (Gallery)findViewById(R.id.gallery_date);
		layoutAdvancedGallery = (LinearLayout)findViewById(R.id.layout_period);
		galleryDateBegin = (Gallery)findViewById(R.id.gallery_date_begin);
		galleryDateEnd = (Gallery)findViewById(R.id.gallery_date_end);

		alertDialogWithdrawalCategory = SimpleDialog.errorDialog(this, getString(R.string.warning), getString(R.string.default_category_warning)).create();
		
		initHeaderGalleries();
		refreshHeaderGallery();
		refreshDisplay();
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
		SimpleDateFormat dateFirst = new SimpleDateFormat("dd/MM/yyyy");
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
		cal.add(Calendar.MONTH, -7);
		for(int i=0; i<12; i++) {
			cal.add(Calendar.MONTH, 1);
			dates.add(cal.getTime());
		}
		
		galleryDate.setAdapter(new DateGalleryAdapter(this, dates));
		galleryDate.setSelection(dates.size()/2);
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
			break;
		case R.id.item_now:
			galleryDate.setSelection(galleryDate.getAdapter().getCount()/2, true);
			((DateGalleryAdapter)galleryDate.getAdapter()).notifyDataSetChanged();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Override
	public void refreshDisplay() {
		Account account = StaticData.getCurrentAccount();

		//Date now = new GregorianCalendar().getTime();
		Operation dummyOp = new Operation();
		
		Integer withdrawalCat = StaticData.getWithdrawalCategory();
		Integer[] cats = null;
		if(withdrawalCat == null) {
			if(! alertDialogWithdrawalCategory.isShowing())
				alertDialogWithdrawalCategory.show();
		} else {
			cats = new Integer[1];
			cats[0] = withdrawalCat;
		}
		
		Cursor cursor = dummyOp.sumOperationsGroupByDay(account,
				StaticData.getStatsDateBeg(),
				StaticData.getStatsDateEnd(),
				cats);

		// Set cursor from selected period
		StatsCursorAdapter adapter = new StatsCursorAdapter(this,
				R.layout.stats_item,
				cursor,
				new String[]{"dateString", "amountString"},
				new int[]{R.id.date, R.id.amount});
		listView.setAdapter(adapter);
	}

	@Override
	protected Intent onAddClicked() {
		return null; 
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
			Date dateSelected = (Date)adapter.getItem(position);
			StaticData.setStatsDateBeg(DateUtil.getFirstDayOfMonth(dateSelected));
			StaticData.setStatsDateEnd(DateUtil.getLastDayOfMonth(dateSelected));
		}

		refreshDisplay();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}
}
