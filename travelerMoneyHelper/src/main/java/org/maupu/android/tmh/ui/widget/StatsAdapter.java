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
import java.util.Collections;
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
        Collections.sort(statsList);
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
        TextView tvCurrencyAvg = (TextView)row.findViewById(R.id.currency_avg);
        TextView tvAmountAvg = (TextView)row.findViewById(R.id.amount_avg);
        TextView tvCurrencyConvertedAvg = (TextView)row.findViewById(R.id.currency_converted_avg);
        TextView tvAmountConvertedAvg = (TextView)row.findViewById(R.id.amount_converted_avg);

        StatsCategoryValues scv = (StatsCategoryValues)getItem(position);
        if(scv == null)
            return null;

        if(type == TYPE_CATEGORY) {
            Double sum = scv.summarize();
            Double sumConv = scv.summarize(true);
            tvText.setText(scv.getName());

            int nbDays = DateUtil.getNumberOfDaysBetweenDates(scv.getDateBegin(), scv.getDateEnd());

            /** Column 1 **/
            tvCurrency.setText(scv.getCurrency().getShortName());
            tvAmount.setText(NumberUtil.formatDecimal(sum));

            tvCurrencyConverted.setText(StaticData.getMainCurrency().getShortName());
            tvAmountConverted.setText(NumberUtil.formatDecimal(sumConv));

            /** Column 2 **/
            Double avg = scv.average();
            Double avgConv = scv.average(true);
            tvCurrencyAvg.setText(scv.getCurrency().getShortName());
            tvAmountAvg.setText(NumberUtil.formatDecimal(avg));

            tvCurrencyConvertedAvg.setText(StaticData.getMainCurrency().getShortName());
            tvAmountConvertedAvg.setText(NumberUtil.formatDecimal(avgConv));
        } else if (type == TYPE_DAY) {
            Double sum = scv.summarize();
            Double sumConv = scv.summarize(true);
            tvText.setText(scv.getName());

            /** Column 1 **/
            tvAmount.setText(NumberUtil.formatDecimal(sum));
            tvCurrency.setText(scv.getCurrency().getShortName());

            tvAmountConverted.setText(NumberUtil.formatDecimal(sumConv));
            tvCurrencyConverted.setText(StaticData.getMainCurrency().getShortName());

            /** Column 2 **/
            tvCurrencyAvg.setVisibility(View.GONE);
            tvAmountAvg.setVisibility(View.GONE);
            tvCurrencyConvertedAvg.setVisibility(View.GONE);
            tvAmountConvertedAvg.setVisibility(View.GONE);
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
                try {
                    return statsData.sumForDate(DateUtil.stringNoTimeToDate(dates.get(position)));
                } catch (ParseException pe) {
                    pe.printStackTrace();
                    return null;
                }
            default:
                return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
