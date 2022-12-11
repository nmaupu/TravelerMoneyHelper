package org.maupu.android.tmh.stats;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.maupu.android.tmh.R;

public class StatsViewPager extends ViewPager implements IStatsPanel, IStatsDataChangedListener {
    private static final String TAG = StatsViewPager.class.getName();
    private StatsFragmentPagerAdapter statsFragmentPagerAdapter;
    private FragmentActivity activity;

    public StatsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.activity = (FragmentActivity) context;
    }

    @Override
    public void initPanel() {
        statsFragmentPagerAdapter = new StatsFragmentPagerAdapter(
                this.activity.getSupportFragmentManager(), getResources()
        );
        setAdapter(statsFragmentPagerAdapter);
    }

    @Override
    public void refreshPanel(final StatsData data) {
        activity.runOnUiThread(() -> {
            statsFragmentPagerAdapter.setData(data);
            statsFragmentPagerAdapter.notifyDataSetChanged();
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
        StatsSlideUpFragment fragment = new StatsSlideUpFragment();
        fragment.setStatsData(data);
        Bundle args = new Bundle();
        args.putInt(StatsSlideUpFragment.ARG_TYPE, position);
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
