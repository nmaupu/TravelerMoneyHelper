package org.maupu.android.tmh.database;

import android.content.Context;

public class ExpenseData extends APersistedData {
	public static final String KEY_AMOUNT="amount";
	public static final String KEY_DESCRIPTION="description";
	public static final String KEY_DATE="date";
	public static final String KEY_ID_USER="idUser";
	public static final String KEY_ID_CATEGORY="idCategory";
	public static final String KEY_ID_CURRENCY="idCurrency";
	// Store also a copy of currency value for each expense
	public static final String KEY_CURRENCY_VALUE="currencyValue";
	
	public static final String TABLE_NAME = "expense";
	private static final String CREATE_TABLE = 
			"CREATE TABLE " + TABLE_NAME + "(" +
			KEY_ID          + " INTEGER primary key autoincrement," +
			KEY_AMOUNT      + " REAL NOT NULL," +
			KEY_DESCRIPTION + " TEXT," +
			KEY_DATE        + " TEXT NOT NULL," +
			KEY_ID_USER     + " INTEGER NOT NULL," +
			KEY_ID_CATEGORY + " INTEGER NOT NULL," +
			KEY_ID_CURRENCY + " INTEGER NOT NULL," +
			KEY_CURRENCY_VALUE + " REAL" +
			")";
	
	public ExpenseData(Context ctx) {
		super(ctx, TABLE_NAME, CREATE_TABLE);
	}
}
