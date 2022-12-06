package org.maupu.android.tmh.ui.widget;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import org.maupu.android.tmh.TmhFragment;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A ViewPagerAdapter to display operations by month
 *
 * @author nmaupu
 */
@SuppressLint("UseSparseArrays")
public class ViewPagerOperationAdapter extends PagerAdapter {
    private static final Class TAG = ViewPagerOperationAdapter.class;
    public final static int DEFAULT_COUNT = 25;
    private Map<Integer, OperationPagerItem> items = new HashMap<>();
    private int count;
    private int offset;
    private Date startDate;
    private TmhFragment parentFragment;

    public ViewPagerOperationAdapter(TmhFragment parentFragment) {
        this(parentFragment, DEFAULT_COUNT);
    }

    public ViewPagerOperationAdapter(TmhFragment parentFragment, int count) {
        this(parentFragment, count, DateUtil.getCurrentDate());
    }

    public ViewPagerOperationAdapter(TmhFragment parentFragment, int count, Date startDate) {
        this.parentFragment = parentFragment;
        this.count = count % 2 == 0 ? count + 1 : count;
        this.offset = -1 * (this.count / 2);
        this.startDate = startDate;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        OperationPagerItem opi = items.get(position);

        if (opi == null) {
            Date date = null;
            if (this.startDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(this.startDate);
                cal.add(Calendar.MONTH, offset + position);
                date = cal.getTime();
            }

            LayoutInflater inflater = LayoutInflater.from(parentFragment.getContext());
            // if date is null, all operations will be displayed
            opi = new OperationPagerItem(parentFragment, date);
            items.put(position, opi);
        }

        container.addView(opi.getView());
        return opi.getView();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
        container.removeView((View) view);
        items.get(position).closeCursors();
        items.remove(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }


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
        doRefreshItemView(position - 1);
        doRefreshItemView(position);
        doRefreshItemView(position + 1);
    }

    private void doRefreshItemView(int position) {
        OperationPagerItem item = items.get(position);
        if (item != null) {
            TmhLogger.d(TAG, "Refreshing item number " + position);
            item.refreshDisplay();
        }
    }
}
