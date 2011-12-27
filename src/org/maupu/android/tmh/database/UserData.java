package org.maupu.android.tmh.database;

import android.content.Context;

public class UserData extends APersistedData {
	public static final String KEY_NAME="name";
	public static final String KEY_ICON="icon";
	
	public static final String TABLE_NAME = "user";
	private static final String CREATE_TABLE = 
			"CREATE TABLE " + TABLE_NAME + "(" +
			KEY_ID   + " INTEGER primary key autoincrement," +
			KEY_NAME + " TEXT NOT NULL," +
			KEY_ICON + " TEXT" +
			")";
	
	public UserData(Context ctx) {
		super(ctx, TABLE_NAME, CREATE_TABLE);
	}
}
