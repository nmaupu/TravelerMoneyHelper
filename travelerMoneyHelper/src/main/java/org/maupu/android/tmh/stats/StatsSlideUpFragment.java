package org.maupu.android.tmh.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ui.DialogHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.StatsAdapter;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.text.ParseException;
import java.util.Date;

public class StatsSlideUpFragment extends Fragment {
    private final static Class TAG = StatsSlideUpFragment.class;
    public static final String ARG_TYPE = "ARG_TYPE";

    private StatsData statsData;

    public StatsSlideUpFragment() {
    }

    public void setStatsData(StatsData statsData) {
        this.statsData = statsData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.stats_fragment_list, container, false);

        Bundle args = getArguments();
        final int type = args.getInt(StatsSlideUpFragment.ARG_TYPE);

        final ListView listView = rootView.findViewById(R.id.list);

        StatsAdapter statsAdapter = new StatsAdapter(rootView.getContext(), statsData, type);
        listView.setAdapter(statsAdapter);
        if (statsData == null) {
            return rootView;
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            TmhLogger.d(TAG, "onItemClick called - type=" + type + ", position=" + position);

            // Getting corresponding StatsCategoryValues elements
            StatsAdapter adapter = (StatsAdapter) listView.getAdapter();
            StatsCategoryValues scv = (StatsCategoryValues) adapter.getItem(position);

            if (type == StatsAdapter.TYPE_CATEGORY) {
                DialogHelper.popupDialogStatsDetails(
                        this.getContext(),
                        statsData.getDateBegin(),
                        statsData.getDateEnd(),
                        scv.getCategoryIds(),
                        StaticData.getStatsExceptedCategoriesToArray()
                );
            } else if (type == StatsAdapter.TYPE_DAY) {
                try {
                    String dateString = scv.getName();
                    Date dateBegin = DateUtil.resetDateToBeginingOfDay(DateUtil.stringNoTimeToDate(dateString));
                    Date dateEnd = DateUtil.resetDateToEndOfDay(DateUtil.stringNoTimeToDate(dateString));
                    DialogHelper.popupDialogStatsDetails(
                            this.getContext(),
                            dateBegin,
                            dateEnd,
                            null,
                            StaticData.getStatsExceptedCategoriesToArray());
                } catch (ParseException pe) {
                    pe.printStackTrace();
                } catch (NullPointerException npe) {
                    // Nothing to do
                }
            }
        });

        return rootView;
    }
}
