package org.maupu.android.tmh.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
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

		// Loading list
		currencyISO4217List = new ArrayList<CurrencyISO4217>();
		InputStream inputStream = ctx.getResources().openRawResource(R.raw.currencies);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

		try {
			String line;
			while ((line = br.readLine()) != null) {
				Log.d("", "Current line = "+line);
				StringTokenizer st = new StringTokenizer(line, "|");
				currencyISO4217List.add(new CurrencyISO4217(st.nextToken(), st.nextToken()));
			}
			br.close();
		} catch (IOException ioe) {
			Log.e("CurrencyHelper", ioe.getMessage());
		}
		
		return currencyISO4217List;
	}

	public static String convertToEuro(float value, float rate) {
		numberFormat.setMinimumFractionDigits(2);
		return numberFormat.format(value / rate);
	}


}
