package org.maupu.android.tmh.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public abstract class NumberUtil {
	private static DecimalFormat NF = init();
	
	public static String formatDecimal(Double value) {
		if(value == null)
			return "NaN";
		
		return NF.format(value);
	}

	public static String formatDecimalLocale(Double value) {
		if(value == null)
			return "NaN";

        DecimalFormat df = (DecimalFormat)NumberFormat.getInstance();
        df.setDecimalSeparatorAlwaysShown(false);
        df.applyPattern(",##0.00");
		return df.format(value);
	}
	
	public static Double parseDecimal(String value) {
		try {
			return NF.parse(value).doubleValue();
		} catch(ParseException pe) {
			return null;
		}
	}
	
	private static DecimalFormat init() {
		DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(Locale.UK);
        df.setDecimalSeparatorAlwaysShown(false);
        df.applyPattern(",##0.00");
		return df;
	}
}
