package org.maupu.android.tmh.ui;

import android.content.Context;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.util.NumberUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.text.NumberFormat;
import java.util.List;
import java.util.StringTokenizer;


public abstract class CurrencyHelper {
    private static final Class TAG = CurrencyHelper.class;
    private static NumberFormat numberFormat = NumberFormat.getInstance();
    private static List<CurrencyISO4217> currencyISO4217List;


    public static List<CurrencyISO4217> getListCurrencyISO4217(Context ctx) {
        if (currencyISO4217List != null)
            return currencyISO4217List;

        RawFileHelper<CurrencyISO4217> rfh = new RawFileHelper<>(ctx, R.raw.currencies);
        rfh.setListener(item -> {
            String line = (String) item;
            TmhLogger.d(TAG, "CurrencyHelper" + "Current line = " + line);
            StringTokenizer st = new StringTokenizer(line, "|");
            return new CurrencyISO4217(st.nextToken(), st.nextToken());
        });

        // Loading list
        currencyISO4217List = rfh.getRawFile();
        return currencyISO4217List;
    }

    public static String currencyConversion(Double value, Double rate) {
        return NumberUtil.formatDecimal(value / rate);
    }

    public static String currencyConversionToMainCurrency(Double value, Currency currency) {
        if (currency == null)
            return null;

        Double rate = currency.getRateCurrencyLinked();
        if (rate == null)
            return null;

        return NumberUtil.formatDecimal(value / rate);
    }
}
