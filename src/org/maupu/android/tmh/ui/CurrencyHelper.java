package org.maupu.android.tmh.ui;

import java.text.NumberFormat;
import java.util.List;
import java.util.StringTokenizer;

import org.maupu.android.tmh.R;

import android.content.Context;
import android.util.Log;


public abstract class CurrencyHelper {
	private static NumberFormat numberFormat = NumberFormat.getInstance();
	private static List<CurrencyISO4217> currencyISO4217List;


	public static List<CurrencyISO4217> getListCurrencyISO4217(Context ctx) {
		if(currencyISO4217List != null)
			return currencyISO4217List;
		
		RawFileHelper<CurrencyISO4217> rfh = new RawFileHelper<CurrencyISO4217>(ctx, R.raw.currencies);
		rfh.setCallback(new ICallback<CurrencyISO4217>() {
			@Override
			public CurrencyISO4217 callback(Object item) {
				String line = (String) item;
				Log.d("CurrencyHelper", "Current line = "+line);
				StringTokenizer st = new StringTokenizer(line, "|");
				return new CurrencyISO4217(st.nextToken(), st.nextToken());
			}
		});

		// Loading list
		currencyISO4217List = rfh.getRawFile();
		return currencyISO4217List;
	}

	public static String convertToEuro(float value, float rate) {
		numberFormat.setMinimumFractionDigits(2);
		return numberFormat.format(value / rate);
	}
}
