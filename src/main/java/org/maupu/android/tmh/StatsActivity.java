package org.maupu.android.tmh;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.maupu.android.tmh.AddOrEditAccountActivity.AppsAdapter;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.CheckableCursorAdapter;
import org.maupu.android.tmh.ui.widget.DateGalleryAdapter;
import org.maupu.android.tmh.ui.widget.StatsCursorAdapter;
import org.maupu.android.tmh.util.DateUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

public class StatsActivity extends TmhActivity implements OnItemSelectedListener {
	private ListView listView;
	private Gallery galleryDate;
	private Gallery galleryDateBegin;
	private Gallery galleryDateEnd;
	private LinearLayout layoutAdvancedGallery;
	private AlertDialog alertDialogWithdrawalCategory;
	private final static String LAST_MONTH_SELECTED = "lastMonthSelectedItemPosition";
	private CheckableCursorAdapter categoryChooserAdapter = null;
	private AlertDialog dialogCategoryChooser = null;
	private List<Integer> exceptedCategories = new ArrayList<Integer>();

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
		setTitle(R.string.activity_title_statistics);

		listView = (ListView)findViewById(R.id.list);
		galleryDate = (Gallery)findViewById(R.id.gallery_date);
		layoutAdvancedGallery = (LinearLayout)findViewById(R.id.layout_period);
		galleryDateBegin = (Gallery)findViewById(R.id.gallery_date_begin);
		galleryDateEnd = (Gallery)findViewById(R.id.gallery_date_end);


		final TmhActivity activity = this;
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
			//Intent intent = new Intent(this, CategoryChooserActivity.class);
			//startActivity(intent);
			LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			/*View popupView = inflater.inflate(R.layout.category_chooser, null, false);
			final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);


			popupWindow.showAtLocation(this.getContentView(), Gravity.TOP, 50, 50);
			 */

			if(dialogCategoryChooser == null) {
				AlertDialog.Builder builder;
				View layout = inflater.inflate(R.layout.category_chooser, (ViewGroup) findViewById(R.id.layout_root));
				builder = new AlertDialog.Builder(this);
				builder.setView(layout);

				final ListView list = (ListView)layout.findViewById(R.id.list);
				if(categoryChooserAdapter == null) {
					Category cat = new Category();
					Cursor cursor = cat.fetchAll();
					categoryChooserAdapter = new CheckableCursorAdapter(this, 
							R.layout.category_item,
							cursor, 
							new String[]{CategoryData.KEY_NAME}, 
							new int[]{R.id.name});
					list.setAdapter(categoryChooserAdapter);
				}

				dialogCategoryChooser = builder.create();
				dialogCategoryChooser.setCancelable(false);
				Button btnValidate = (Button)layout.findViewById(R.id.btn_validate);
				Button.OnClickListener listener = new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (v.getId() == R.id.btn_validate) {
							exceptedCategories.clear();
							CheckableCursorAdapter adapter = (CheckableCursorAdapter)list.getAdapter(); 
							Integer[] ints = adapter.getCheckedPositions();
							for(int i : ints) {
								Cursor c = (Cursor)adapter.getItem(i);
								Category cat = new Category();
								cat.toDTO(c);
								exceptedCategories.add(cat.getId());
								Log.d(StatsActivity.class.getName(), "category "+cat+" is checked");
							}
							refreshDisplay();
							dialogCategoryChooser.dismiss();
						}
					}
				};

				btnValidate.setOnClickListener(listener);
			}
			
			dialogCategoryChooser.show();
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

		//Category withdrawalCat = StaticData.getWithdrawalCategory();
		Integer[] cats = new Integer[exceptedCategories.size()];
		for(int i=0; i<cats.length; i++) {
			cats[i] = exceptedCategories.get(i);
		}
		/*if(withdrawalCat == null) {
			if(! alertDialogWithdrawalCategory.isShowing())
				alertDialogWithdrawalCategory.show();
		} else {
			cats = new Integer[1];
			cats[0] = withdrawalCat.getId();
		}*/


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

		Cursor cursor = dummyOp.sumOperationsGroupByDay(account,
				beg,
				end,
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
}
