package org.maupu.android.tmh.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.stats.StatsCategoryValues;
import org.maupu.android.tmh.stats.StatsData;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.NumberUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsAdapter extends BaseAdapter {
    private static final String TAG = StatsAdapter.class.getName();
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_DAY = 1;
    private StatsData statsData;
    private List<StatsCategoryValues> statsList;
    private List<String> dates;
    private Context context;
    private int type;

    public StatsAdapter(Context context, StatsData statsData, int displayType) {
        this.context = context;
        this.statsData = statsData;
        this.type = displayType;

        if (statsData != null) {
            statsList = new ArrayList<>(statsData.values());
            Collections.sort(statsList);
            dates = StatsCategoryValues.buildXEntries(statsData.getDateBegin(), statsData.getDateEnd());
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        if (statsList == null || statsData == null)
            return null;

        View row = convertView != null ? convertView : inflater.inflate(R.layout.stats_item_details, parent, false);
        TextView tvText = row.findViewById(R.id.text);
        TextView tvAmount = row.findViewById(R.id.amount);
        TextView tvCurrency = row.findViewById(R.id.currency);
        TextView tvAmountConverted = row.findViewById(R.id.amount_converted);
        TextView tvCurrencyConverted = row.findViewById(R.id.currency_converted);
        TextView tvCurrencyAvg = row.findViewById(R.id.currency_avg);
        TextView tvAmountAvg = row.findViewById(R.id.amount_avg);
        TextView tvCurrencyConvertedAvg = row.findViewById(R.id.currency_converted_avg);
        TextView tvAmountConvertedAvg = row.findViewById(R.id.amount_converted_avg);

        StatsCategoryValues scv;
        try {
            scv = (StatsCategoryValues) getItem(position);
            if (scv == null)
                return row;
        } catch (IndexOutOfBoundsException ioobe) {
            return row; // trying to refresh too much
        }

        if (type == TYPE_CATEGORY) {
            Double sum = scv.summarize().doubleValue();
            tvText.setText(scv.getName());

            int nbDays = DateUtil.getNumberOfDaysBetweenDates(scv.getDateBegin(), scv.getDateEnd());

            // Column 1
            tvCurrency.setText(scv.getCurrency().getShortName());
            tvAmount.setText(NumberUtil.formatDecimal(sum));

            tvCurrencyConverted.setText(StaticData.getMainCurrency().getShortName());
            tvAmountConverted.setText(NumberUtil.formatDecimal(sum / scv.getRateAvg()));

            // Column 2
            Double avg = sum / nbDays;
            tvCurrencyAvg.setText(scv.getCurrency().getShortName());
            tvAmountAvg.setText(NumberUtil.formatDecimal(avg));

            tvCurrencyConvertedAvg.setText(StaticData.getMainCurrency().getShortName());
            tvAmountConvertedAvg.setText(NumberUtil.formatDecimal(avg / scv.getRateAvg()));
        } else if (type == TYPE_DAY) {
            String dateString = scv.getName();
            Double sum = scv.summarize().doubleValue(); // Summarize only one element
            tvText.setText(dateString);

            // Column 1
            tvAmount.setText(NumberUtil.formatDecimal(sum));
            tvCurrency.setText(scv.getCurrency().getShortName());

            tvAmountConverted.setText(NumberUtil.formatDecimal(sum / scv.getRateAvg()));
            tvCurrencyConverted.setText(StaticData.getMainCurrency().getShortName());

            // Column 2
            tvCurrencyAvg.setVisibility(View.GONE);
            tvAmountAvg.setVisibility(View.GONE);
            tvCurrencyConvertedAvg.setVisibility(View.GONE);
            tvAmountConvertedAvg.setVisibility(View.GONE);
        }

        return row;
    }

    @Override
    public int getCount() {
        if (type == TYPE_CATEGORY)
            return statsList == null ? 0 : statsList.size();
        else if (type == TYPE_DAY)
            return statsData == null ? 0 : DateUtil.getNumberOfDaysBetweenDates(statsData.getDateBegin(), statsData.getDateEnd());

        return 0;
    }

    @Override
    public Object getItem(int position) {
        switch (type) {
            case TYPE_CATEGORY:
                return statsList == null ? null : statsList.get(position);
            case TYPE_DAY:
                return statsData == null ? null : statsData.sumForDate(dates.get(position));
            default:
                return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
