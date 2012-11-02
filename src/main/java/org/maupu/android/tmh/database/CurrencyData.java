package org.maupu.android.tmh.database;

import android.database.sqlite.SQLiteDatabase;


public class CurrencyData extends APersistedData {
	public static final String KEY_LONG_NAME="longName";
	public static final String KEY_SHORT_NAME="shortName";
	public static final String KEY_ICON="icon";
	public static final String KEY_CURRENCY_LINKED="rateCurrencyLinked";
	public static final String KEY_LAST_UPDATE="lastUpdate";
	public static final String KEY_ISO_CODE="isoCode";
	
	public static final String TABLE_NAME = "currency";
	private static final String CREATE_TABLE = 
			"CREATE TABLE "       + TABLE_NAME + "(" +
			KEY_ID                + " INTEGER primary key autoincrement," +
			KEY_LONG_NAME         + " TEXT," +
			KEY_SHORT_NAME        + " TEXT," +
			KEY_ICON              + " TEXT," +
			KEY_CURRENCY_LINKED   + " REAL," +
			KEY_LAST_UPDATE       + " TEXT," +
			KEY_ISO_CODE          + " TEXT NOT NULL" +
			")";
	
	public CurrencyData() {
		super(TABLE_NAME, CREATE_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion < 10 && newVersion >= 10) {
			// tauxEuro becomes rateCurrencyLinked
			db.beginTransaction();
			try {
				
				StringBuilder colsNew = new StringBuilder();
				colsNew.append(KEY_ID).append(",");
				colsNew.append(KEY_LONG_NAME).append(",");
				colsNew.append(KEY_SHORT_NAME).append(",");
				colsNew.append(KEY_ICON).append(",");
				colsNew.append(KEY_CURRENCY_LINKED).append(",");
				colsNew.append(KEY_LAST_UPDATE).append(",");
				colsNew.append(KEY_ISO_CODE);
				
				StringBuilder colsOld = new StringBuilder();
				colsOld.append(KEY_ID).append(",");
				colsOld.append(KEY_LONG_NAME).append(",");
				colsOld.append(KEY_SHORT_NAME).append(",");
				colsOld.append(KEY_ICON).append(",");
				colsOld.append("tauxEuro").append(",");
				colsOld.append(KEY_LAST_UPDATE).append(",");
				colsOld.append(KEY_ISO_CODE);
				
				db.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO " + TABLE_NAME + "bak");
				db.execSQL(CREATE_TABLE);
				
				db.execSQL("INSERT INTO " + TABLE_NAME + "(" + colsNew.toString() + ") " +
						"SELECT " + colsOld.toString() + 
						" FROM " + TABLE_NAME+"bak");
				db.execSQL("DROP TABLE " + TABLE_NAME + "bak");
				
				// Setting mainCurrency to the first one created if exists
				
				/*Currency cur = new Currency();
				Cursor c = db.query(cur.getTableName(), null, null, null, null, null, CurrencyData.KEY_ID);
				c.moveToFirst();
				cur.toDTO(c);
				StaticData.setMainCurrency(cur.getId());
				*/
				
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}
}
