package org.maupu.android.tmh.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public abstract class NumberUtil {
    private static DecimalFormat NF = init();

    public static String formatDecimal(Double value) {
        if (value == null)
            return "NaN";

        return NF.format(value);
    }

    public static Double parseDecimal(String value) {
        return Double.parseDouble(value);
    }

    private static DecimalFormat init() {
        // Get current locale instance
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        df.setDecimalSeparatorAlwaysShown(false);
        df.applyPattern(",##0.00");
        return df;
    }
}
