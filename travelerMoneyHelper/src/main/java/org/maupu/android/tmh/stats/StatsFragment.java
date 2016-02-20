package org.maupu.android.tmh.stats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.ui.DialogHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.StatsAdapter;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

public class StatsFragment extends Fragment {
    private final static Class TAG = StatsFragment.class;
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
        final int type = args.getInt(StatsFragment.ARG_TYPE);

        final ListView listView = (ListView)rootView.findViewById(R.id.list);
        if(statsData != null) {
            final Fragment thisFragment = this;
            StatsAdapter statsAdapter = new StatsAdapter(rootView.getContext(), statsData, type);
            listView.setAdapter(statsAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TmhLogger.d(TAG, "onItemClick called - type=" + type + ", position=" + position);

                    // Getting corresponding StatsCategoryValues elements
                    StatsAdapter adapter = (StatsAdapter)listView.getAdapter();
                    StatsCategoryValues scv = (StatsCategoryValues)adapter.getItem(position);

                    if(type == StatsAdapter.TYPE_CATEGORY) {
                        DialogHelper.popupDialogStatsDetails(
                                thisFragment.getContext(),
                                statsData.getDateBegin(),
                                statsData.getDateEnd(),
                                scv.getCategoryIds(),
                                StaticData.getStatsExceptedCategoriesToArray()
                        );
                    } else if(type == StatsAdapter.TYPE_DAY) {
                        try {
                            String dateString = scv.getName();
                            Date dateBegin = DateUtil.resetDateToBeginingOfDay(DateUtil.stringNoTimeToDate(dateString));
                            Date dateEnd = DateUtil.resetDateToEndOfDay(DateUtil.stringNoTimeToDate(dateString));
                            DialogHelper.popupDialogStatsDetails(
                                    thisFragment.getContext(),
                                    dateBegin,
                                    dateEnd,
                                    null,
                                    StaticData.getStatsExceptedCategoriesToArray());
                        } catch(ParseException pe) {
                            pe.printStackTrace();
                        }
                    }
                }
            });
        } else {
            StatsAdapter statsAdapter = new StatsAdapter(rootView.getContext(), statsData, type);
            listView.setAdapter(statsAdapter);
        }

        return rootView;
    }
}
