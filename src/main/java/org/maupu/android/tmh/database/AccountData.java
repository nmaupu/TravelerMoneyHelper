package org.maupu.android.tmh.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class AccountData extends APersistedData {
	public static final String KEY_NAME="name";
	public static final String KEY_ICON="icon";
	public static final String KEY_ID_CURRENCY="idCurrency";
	public static final String KEY_BALANCE="balance"; 
	
	public static final String TABLE_NAME = "account";
	private static final String CREATE_TABLE = 
			"CREATE TABLE " + TABLE_NAME + "(" +
			KEY_ID   + " INTEGER primary key autoincrement," +
			KEY_NAME + " TEXT NOT NULL," +
			KEY_ICON + " TEXT," +
			KEY_ID_CURRENCY + " INTEGER NOT NULL, " +
			KEY_BALANCE + " REAL NOT NULL DEFAULT 0" +
			")";
	
	public AccountData() {
		super(TABLE_NAME, CREATE_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(newVersion == 11 && oldVersion < 11) {
			// Adding balance to account and upgrade all account balances
			db.beginTransaction();
			try {
				// Alter table, add column and set default to 0
				db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_BALANCE + " REAL NOT NULL DEFAULT 0");
				
				/* First try, direct ? -> not working, fffuuuuuu */
				// Upgrading balance
				/* SELECT case when sum(o.amount/o.currencyValue) IS NULL then 0 else sum(o.amount/o.currencyValue) end as balance from account a LEFT JOIN operation o ON a._id=o.idAccount GROUP BY o.idAccount; */
				/* update account set balance=(SELECT case when sum(o.amount/o.currencyValue) IS NULL then 0 else sum(o.amount/o.currencyValue) end as balance from operation o WHERE account._id=o.idAccount GROUP BY o.idAccount); */
				/* */
				
				
				// Compute all balances for each existing operations (currencyValue is the value of currency operation when inserted)
				// If operation currency is the same as account currency, not ratio is done
				// Example : for a vietnam account (dong), all operation for this account are summed directly without changing to euro
				// Because all operations are already dong. So Balance for this account is in dong
				String query = "select a.*, case when o.amount IS NULL then 0 else (case when o.idCurrency=a.idCurrency then sum(o.amount) else sum(o.amount/o.currencyValue) end) end as _balance from account a LEFT JOIN operation o ON a._id=o.idAccount GROUP BY a._id;";
				Cursor cursor = db.rawQuery(query, null);
				cursor.moveToFirst();
				
				do {
					int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID_CURRENCY);
					int idxBalance = cursor.getColumnIndexOrThrow("_balance");
					
					ContentValues args = new ContentValues();
					args.put(AccountData.KEY_BALANCE, cursor.getInt(idxBalance));
					db.update(AccountData.TABLE_NAME, args, AccountData.KEY_ID+"="+cursor.getInt(idxId), null);
				} while(cursor.moveToNext());
				
				db.setTransactionSuccessful();
			} catch (SQLException sqle) {
				// Something wrong occured
				sqle.printStackTrace();
			} finally {
				db.endTransaction();
			}
		}
	}
}
