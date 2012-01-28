package org.maupu.android.tmh.ui.widget;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.maupu.android.tmh.ViewPagerTestActivity;
import org.maupu.android.tmh.database.DatabaseHelper;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

/**
 * A ViewPagerAdapter to display operations by month
 * @author nmaupu
 *
 */
public class ViewPagerOperationAdapter extends PagerAdapter {
	private ViewPagerTestActivity ctx;
	private DatabaseHelper dbHelper;
	private final static int count = 25;
	private final static int offset = -1 * (count/2);
	private Map<Integer, OperationPagerItem> items = new HashMap<Integer, OperationPagerItem>();

	public ViewPagerOperationAdapter(ViewPagerTestActivity ctx, DatabaseHelper dbHelper) {
		this.ctx = ctx;
		this.dbHelper = dbHelper;
	}

	@Override
	public Object instantiateItem(View pager, int position) {
		OperationPagerItem opi = items.get(position);
		
		if(opi == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.MONTH, offset+position);
			Date date = cal.getTime();

			LayoutInflater inflater = (LayoutInflater)pager.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			opi = new OperationPagerItem(ctx, inflater, dbHelper, date);
			items.put(position, opi);
		}
		
		((ViewPager) pager).addView(opi.getView());
		return opi.getView();
	}

	@Override
	public void destroyItem(View pager, int position, Object view) {
		((ViewPager)pager).removeView((View)view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void finishUpdate(View view) {}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public int getCount() {
		return count;
	}
	
	public void refreshItemView(int position) {
		// Refreshing previous, current and next page
		doRefreshItemView(position-1);
		doRefreshItemView(position);
		doRefreshItemView(position+1);
	}
	
	private void doRefreshItemView(int position) {
		Log.d(ViewPagerOperationAdapter.class.getName(), "Refreshing item number "+position);
		OperationPagerItem item = items.get(position);
		if(item != null) {
			item.refreshDisplay();
		}
	}
}