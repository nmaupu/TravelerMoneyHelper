package org.maupu.android.tmh.ui;

import android.util.Log;

import org.maupu.android.tmh.util.TmhLogger;

public final class CurrencyISO4217 {
    public static final Class TAG = CurrencyISO4217.class;
	private String code;
	private String name;
	
	public CurrencyISO4217(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public String toString() {
		return name;
	}

    public String getCurrencySymbol() {
        try {
            java.util.Currency cur = java.util.Currency.getInstance(code);
            return cur.getSymbol();
        } catch (IllegalArgumentException iae) {
            // Not a supported ISO4217, so we do not have a symbol available
            TmhLogger.e(TAG, code + "/" + name + " is not a valid ISO4217 currency !");
            return code;
        }
    }
}
