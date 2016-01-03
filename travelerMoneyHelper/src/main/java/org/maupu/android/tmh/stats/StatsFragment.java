package org.maupu.android.tmh.stats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ui.widget.StatsAdapter;

import java.util.ArrayList;

public class StatsFragment extends Fragment {
    private final static String TAG = StatsFragment.class.getName();
    public static final String ARG_TYPE = "ARG_TYPE";

    private StatsData statsData;

    public StatsFragment() {}

    public void setStatsData(StatsData statsData) {
        this.statsData = statsData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.stats_fragment_list, container, false);

        Bundle args = getArguments();
        int argType = args.getInt(StatsFragment.ARG_TYPE);
        int type = StatsFragmentPagerAdapter.TYPE_CATEGORY;
        if(argType == StatsFragmentPagerAdapter.TYPE_DAY) {
            type = StatsFragmentPagerAdapter.TYPE_DAY;
        }
        ListView listView = (ListView)rootView.findViewById(R.id.list);
        if(statsData != null) {
            StatsAdapter statsAdapter = new StatsAdapter(rootView.getContext(), statsData, type);
            listView.setAdapter(statsAdapter);
        }

        return rootView;
    }
}
