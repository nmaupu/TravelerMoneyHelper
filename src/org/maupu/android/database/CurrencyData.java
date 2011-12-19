package org.maupu.android.database;

import android.content.Context;

public class CurrencyData extends APersistedData {
	public static final String KEY_LONG_NAME="longName";
	public static final String KEY_SHORT_NAME="shortName";
	public static final String KEY_ICON="icon";
	public static final String KEY_TAUX_EURO="tauxEuro";
	public static final String KEY_LAST_UPDATE="lastUpdate";
	
	public static final String TABLE_NAME = "currency";
	private static final String CREATE_TABLE = 
			"CREATE TABLE " + TABLE_NAME + "(" +
			KEY_ID          + " INTEGER primary key autoincrement," +
			KEY_LONG_NAME   + " TEXT," +
			KEY_SHORT_NAME  + " TEXT," +
			KEY_ICON        + " TEXT," +
			KEY_TAUX_EURO   + " REAL," +
			KEY_LAST_UPDATE + " TEXT" +
			")";
	
	public CurrencyData(Context ctx) {
		super(ctx, TABLE_NAME, CREATE_TABLE);
	}
}
