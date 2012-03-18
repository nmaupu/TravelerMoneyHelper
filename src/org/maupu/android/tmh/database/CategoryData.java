package org.maupu.android.tmh.database;


public class CategoryData extends APersistedData {
	public static final String KEY_NAME="name";
	
	public static final String TABLE_NAME = "category";
	private static final String CREATE_TABLE = 
			"CREATE TABLE " + TABLE_NAME + "(" +
			KEY_ID   + " INTEGER primary key autoincrement," +
			KEY_NAME + " TEXT NOT NULL" +
			")";
	
	public CategoryData() {
		super(TABLE_NAME, CREATE_TABLE);
	}
}
