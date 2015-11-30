package org.maupu.android.tmh.ui.widget;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.maupu.android.tmh.ViewPagerOperationActivity;
import org.maupu.android.tmh.util.DateUtil;

import android.annotation.SuppressLint;
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
@SuppressLint("UseSparseArrays")
public class ViewPagerOperationAdapter extends PagerAdapter {
	private ViewPagerOperationActivity ctx;
	public final static int DEFAULT_COUNT = 25;
	private Map<Integer, OperationPagerItem> items = new HashMap<Integer, OperationPagerItem>();
    private int count;
    private int offset;
    private Date startDate;

    public ViewPagerOperationAdapter(ViewPagerOperationActivity ctx) {
        this(ctx, DEFAULT_COUNT);
    }

    public ViewPagerOperationAdapter(ViewPagerOperationActivity ctx, int count) {
        this(ctx, count, DateUtil.getCurrentDate());
    }

    public ViewPagerOperationAdapter(ViewPagerOperationActivity ctx, int count, Date startDate) {
        this.ctx = ctx;
        this.count = count%2 == 0 ? count+1 : count;
        this.offset = -1 * (this.count / 2);
        this.startDate = startDate;
    }

	@Override
	public Object instantiateItem(View pager, int position) {
		OperationPagerItem opi = items.get(position);
		
		if(opi == null) {
            Date date = null;
            if(this.startDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(this.startDate);
                cal.add(Calendar.MONTH, offset + position);
                date = cal.getTime();
            }

			LayoutInflater inflater = (LayoutInflater)pager.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // if date is null, all operations will be displayed
			opi = new OperationPagerItem(ctx, inflater, date);
			items.put(position, opi);
		}
		
		((ViewPager) pager).addView(opi.getView());
		return opi.getView();
	}

	@Override
	public void destroyItem(View pager, int position, Object view) {
		((ViewPager)pager).removeView((View)view);
		items.get(position).closeCursors();
		items.remove(position);
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
		// Refreshing previous, current and next pages
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