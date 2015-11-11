package org.maupu.android.tmh.ui;

public final class CurrencyISO4217 {
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
}
