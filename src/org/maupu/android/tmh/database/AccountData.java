package org.maupu.android.tmh.database;

import android.database.sqlite.SQLiteDatabase;


public class AccountData extends APersistedData {
	public static final String KEY_NAME="name";
	public static final String KEY_ICON="icon";
	public static final String KEY_ID_CURRENCY="idCurrency";
	
	public static final String TABLE_NAME = "account";
	private static final String CREATE_TABLE = 
			"CREATE TABLE " + TABLE_NAME + "(" +
			KEY_ID   + " INTEGER primary key autoincrement," +
			KEY_NAME + " TEXT NOT NULL," +
			KEY_ICON + " TEXT," +
			KEY_ID_CURRENCY + " INTEGER NOT NULL " +
			")";
	
	public AccountData() {
		super(TABLE_NAME, CREATE_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {	
	}
}
