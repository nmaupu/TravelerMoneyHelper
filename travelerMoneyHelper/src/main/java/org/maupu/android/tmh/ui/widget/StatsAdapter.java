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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        statsList = new ArrayList<>(statsData.values());
        dates = StatsCategoryValues.buildXEntries(statsData.getDateBegin(), statsData.getDateEnd());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        if(statsList == null || statsData == null)
            return null;

        View row = convertView != null ? convertView : inflater.inflate(R.layout.stats_item_details, parent, false);
        TextView tvText = (TextView)row.findViewById(R.id.text);
        TextView tvAmount = (TextView)row.findViewById(R.id.amount);
        TextView tvCurrency = (TextView)row.findViewById(R.id.currency);
        TextView tvAmountConverted = (TextView)row.findViewById(R.id.amount_converted);
        TextView tvCurrencyConverted = (TextView)row.findViewById(R.id.currency_converted);

        StatsCategoryValues scv = (StatsCategoryValues)getItem(position);
        if(scv == null)
            return null;

        if(type == TYPE_CATEGORY) {
            Double sum = scv.summarize().doubleValue();
            tvText.setText(scv.getName());
            tvAmount.setText(NumberUtil.formatDecimal(sum));
            tvCurrency.setText(scv.getCurrency().getShortName());

            tvAmountConverted.setText(NumberUtil.formatDecimal(sum / scv.getRate()));
            tvCurrencyConverted.setText(StaticData.getMainCurrency().getShortName());
        } else if (type == TYPE_DAY) {
            String dateString = scv.getName();
            Double sum = scv.summarize().doubleValue(); // Summarize only one element
            tvText.setText(dateString);
            tvAmount.setText(NumberUtil.formatDecimal(sum));
            tvCurrency.setText(scv.getCurrency().getShortName());

            tvAmountConverted.setText(NumberUtil.formatDecimal(sum / scv.getRate()));
            tvCurrencyConverted.setText(StaticData.getMainCurrency().getShortName());

        }

        return row;
    }

    @Override
    public int getCount() {
        if(type == TYPE_CATEGORY)
            return statsList.size();
        else if(type == TYPE_DAY)
            return DateUtil.getNumberOfDaysBetweenDates(statsData.getDateBegin(), statsData.getDateEnd());

        return 0;
    }

    @Override
    public Object getItem(int position) {
        switch (type) {
            case TYPE_CATEGORY:
                return statsList.get(position);
            case TYPE_DAY:
                return statsData.sumForDate(dates.get(position));
            default:
                return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
