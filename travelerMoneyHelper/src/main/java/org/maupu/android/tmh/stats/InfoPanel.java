package org.maupu.android.tmh.stats;

import android.view.View;
import android.widget.TextView;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.TmhActivity;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.NumberUtil;

import java.util.Iterator;

public class InfoPanel extends View implements IStatsPanel, IStatsDataChangedListener {
    private static final Class TAG = InfoPanel.class;
    private TmhActivity ctx;

    // Avg / total info
    private TextView tvAvg1Currency, tvAvg1Amount, tvAvg2Currency, tvAvg2Amount;
    private TextView tvAvg1Cat, tvAvg1CatCurrency, tvAvg1CatAmount, tvAvg2CatCurrency, tvAvg2CatAmount;
    private TextView tvTotal1Currency, tvTotal1Amount, tvTotal2Currency, tvTotal2Amount;
    private TextView tvTotal1Cat, tvTotal1CatCurrency, tvTotal1CatAmount, tvTotal2CatCurrency, tvTotal2CatAmount;

    public InfoPanel(TmhActivity context) {
        super(context);
        this.ctx = context;
    }

    @Override
    public void initPanel() {
        tvAvg1Currency = (TextView)ctx.findViewById(R.id.text_avg1_currency);
        tvAvg1Amount = (TextView)ctx.findViewById(R.id.text_avg1_amount);
        tvAvg2Currency = (TextView)ctx.findViewById(R.id.text_avg2_currency);
        tvAvg2Amount = (TextView)ctx.findViewById(R.id.text_avg2_amount);
        tvAvg1Cat = (TextView)ctx.findViewById(R.id.text_avg1_cat);
        tvAvg1CatCurrency = (TextView)ctx.findViewById(R.id.text_avg1_cat_currency);
        tvAvg1CatAmount = (TextView)ctx.findViewById(R.id.text_avg1_cat_amount);
        tvAvg2CatCurrency = (TextView)ctx.findViewById(R.id.text_avg2_cat_currency);
        tvAvg2CatAmount = (TextView)ctx.findViewById(R.id.text_avg2_cat_amount);
        tvTotal1Currency = (TextView)ctx.findViewById(R.id.text_total1_currency);
        tvTotal1Amount = (TextView)ctx.findViewById(R.id.text_total1_amount);
        tvTotal2Currency = (TextView)ctx.findViewById(R.id.text_total2_currency);
        tvTotal2Amount = (TextView)ctx.findViewById(R.id.text_total2_amount);
        tvTotal1Cat = (TextView)ctx.findViewById(R.id.text_total1_cat);
        tvTotal1CatCurrency = (TextView)ctx.findViewById(R.id.text_total1_cat_currency);
        tvTotal1CatAmount = (TextView)ctx.findViewById(R.id.text_total1_cat_amount);
        tvTotal2CatCurrency = (TextView)ctx.findViewById(R.id.text_total2_cat_currency);
        tvTotal2CatAmount = (TextView)ctx.findViewById(R.id.text_total2_cat_amount);
    }

    @Override
    public void onStatsDataChanged(StatsData data) {
        refreshPanel(data);
    }

    @Override
    public void refreshPanel(StatsData statsData) {
        double total = 0d;
        double avg = 0d;
        double totalCat = 0d;
        double avgCat = 0d;
        double totalConv = 0d;
        double avgConv = 0d;
        double totalCatConv = 0d;
        double avgCatConv = 0d;
        final String nonAvailable = getResources().getString(R.string.NA);
        final Currency mainCur = StaticData.getMainCurrency();
        final Account currentAcc = StaticData.getCurrentAccount();
        final String mainCurrencySymbol = mainCur != null ? mainCur.getShortName() : nonAvailable;
        final String currencySymbol = currentAcc != null && currentAcc.getCurrency() != null ? currentAcc.getCurrency().getShortName() : nonAvailable;

        String currentCatName = nonAvailable;
        if(statsData.getCatToHighlight() != null && statsData.getCatToHighlight().getName() != null) {
            // Getting possible aggregated name if needed
            StatsCategoryValues scv = statsData.get(statsData.getCatToHighlight().getId());
            currentCatName = scv != null ? scv.getName() : statsData.getCatToHighlight().getName();

            if(currentCatName.length() > 11) {
                currentCatName = statsData.getCatToHighlight().getName().substring(0, 11);
                if (!currentCatName.equals(statsData.getCatToHighlight().getName()))
                    currentCatName += ".";
            }
        }

        if(statsData.values().size() > 0) {
            /** Gran total **/
            Iterator<StatsCategoryValues> it = statsData.values().iterator();
            while(it.hasNext()) {
                StatsCategoryValues scv = it.next();
                total += scv.summarize();
                totalConv += scv.summarizeConv();
            }

            /** Avg by day **/
            int nbDays = DateUtil.getNumberOfDaysBetweenDates(statsData.getDateBegin(), statsData.getDateEnd());
            avg = total / nbDays;
            avgConv = totalConv / nbDays;

            /** Total and avg by day by category **/
            if(statsData.getCatToHighlight() != null && statsData.getCatToHighlight().getId() != null) {
                StatsCategoryValues scv = statsData.get(statsData.getCatToHighlight().getId());
                if (scv != null) {
                    totalCat = scv.summarize();
                    totalCatConv = scv.summarizeConv();
                    avgCat = scv.average();
                    avgCatConv = scv.averageConv();
                }
            }
        }

        final double fAvg = avg;
        final double fAvgConv = avgConv;
        final String fCurrentCatName = currentCatName;
        final double fAvgCat = avgCat;
        final double fAvgCatConv = avgCatConv;
        final double fTotal = total;
        final double fTotalConv = totalConv;
        final double fTotalCat = totalCat;
        final double fTotalCatConv = totalCatConv;

        this.ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /** Setting all data to TextViews **/
                tvAvg1Currency.setText(currencySymbol);
                tvAvg1Amount.setText(NumberUtil.formatDecimal(fAvg));
                tvAvg2Currency.setText(mainCurrencySymbol);
                tvAvg2Amount.setText(NumberUtil.formatDecimal(fAvgConv));

                tvAvg1Cat.setText(fCurrentCatName);
                tvAvg1CatCurrency.setText(currencySymbol);
                tvAvg1CatAmount.setText(NumberUtil.formatDecimal(fAvgCat));
                tvAvg2CatCurrency.setText(mainCurrencySymbol);
                tvAvg2CatAmount.setText(NumberUtil.formatDecimal(fAvgCatConv));

                tvTotal1Currency.setText(currencySymbol);
                tvTotal1Amount.setText(NumberUtil.formatDecimal(fTotal));
                tvTotal2Currency.setText(mainCurrencySymbol);
                tvTotal2Amount.setText(NumberUtil.formatDecimal(fTotalConv));

                tvTotal1Cat.setText(fCurrentCatName);
                tvTotal1CatCurrency.setText(currencySymbol);
                tvTotal1CatAmount.setText(NumberUtil.formatDecimal(fTotalCat));
                tvTotal2CatCurrency.setText(mainCurrencySymbol);
                tvTotal2CatAmount.setText(NumberUtil.formatDecimal(fTotalCatConv));
            }
        });
    }
}
