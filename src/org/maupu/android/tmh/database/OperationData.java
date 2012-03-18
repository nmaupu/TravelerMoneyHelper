package org.maupu.android.tmh.database;


public class OperationData extends APersistedData {
	public static final String KEY_AMOUNT="amount";
	public static final String KEY_DESCRIPTION="description";
	public static final String KEY_DATE="date";
	public static final String KEY_ID_ACCOUNT="idAccount";
	public static final String KEY_ID_CATEGORY="idCategory";
	public static final String KEY_ID_CURRENCY="idCurrency";
	// Store also a copy of currency value for each operation
	public static final String KEY_CURRENCY_VALUE="currencyValue";
	
	
	public static final String TABLE_NAME = "operation";
	private static final String CREATE_TABLE = 
			"CREATE TABLE "    + TABLE_NAME + "(" +
			KEY_ID             + " INTEGER primary key autoincrement," +
			KEY_AMOUNT         + " REAL NOT NULL," +
			KEY_DESCRIPTION    + " TEXT," +
			KEY_DATE           + " TEXT NOT NULL," +
			KEY_ID_ACCOUNT     + " INTEGER NOT NULL," +
			KEY_ID_CATEGORY    + " INTEGER NOT NULL," +
			KEY_ID_CURRENCY    + " INTEGER NOT NULL," +
			KEY_CURRENCY_VALUE + " REAL" +
			")";
	
	public OperationData() {
		super(TABLE_NAME, CREATE_TABLE);
	}
}
