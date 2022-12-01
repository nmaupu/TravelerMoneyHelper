package org.maupu.android.tmh.stats;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.TmhActivity;

public class StatsViewPager extends ViewPager implements IStatsPanel, IStatsDataChangedListener {
    private static final String TAG = StatsViewPager.class.getName();
    private TmhActivity context;
    private StatsFragmentPagerAdapter statsFragmentPagerAdapter;

    public StatsViewPager(Context context) {
        super(context);
        this.context = (TmhActivity)context;
    }

    public StatsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = (TmhActivity)context;
    }

    @Override
    public void initPanel() {
        statsFragmentPagerAdapter = new StatsFragmentPagerAdapter(
                context.getSupportFragmentManager(), getResources()
        );
        setAdapter(statsFragmentPagerAdapter);
    }

    @Override
    public void refreshPanel(final StatsData data) {
        if(data == null || data.size() == 0)
            return;

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statsFragmentPagerAdapter.setData(data);
                statsFragmentPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStatsDataChanged(StatsData data) {
        refreshPanel(data);
    }
}

class StatsFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private String[] titles;
    private StatsData data;
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_DAY = 1;

    public StatsFragmentPagerAdapter(FragmentManager fm, Resources resources) {
        super(fm);
        titles = new String[2];
        titles[TYPE_CATEGORY] = resources.getString(R.string.stats_tab_by_categories);
        titles[TYPE_DAY] = resources.getString(R.string.stats_tab_by_days);
    }

    public void setData(StatsData data) {
        this.data = data;
    }

    @Override
    public Fragment getItem(int position) {
        StatsFragment fragment = new StatsFragment();
        fragment.setStatsData(data);
        Bundle args = new Bundle();
        args.putInt(StatsFragment.ARG_TYPE, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
