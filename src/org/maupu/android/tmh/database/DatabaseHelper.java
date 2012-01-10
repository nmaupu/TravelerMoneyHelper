package org.maupu.android.tmh.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	protected static final String DATABASE_NAME = "TravelerMoneyHelper_appdata";
	protected static final int DATABASE_VERSION = 2;
	private static List<APersistedData> persistedData = new ArrayList<APersistedData>();
	private Context context;
	private SQLiteDatabase db;
	
	public DatabaseHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = ctx;
		
		persistedData.add(new CategoryData(ctx));
		persistedData.add(new CurrencyData(ctx));
		persistedData.add(new ExpenseData(ctx));
		persistedData.add(new UserData(ctx));
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for(APersistedData data : persistedData) {
			data.onCreate(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for(APersistedData data : persistedData) {
			data.onUpgrade(db, oldVersion, newVersion);
		}
	}

	public Context getContext() {
		return context;
	}
	
	public void openWritable() {
		db = this.getWritableDatabase();
	}
	
	public void openReadable() {
		db = this.getReadableDatabase();
	}
	
	public void close() {
		super.close();
	}
	
	public SQLiteDatabase getDb() {
		return db;
	}
	
	public static String formatDateForSQL(Date date) {
		return dateFormat.format(date);
	}
	
	public static Date toDate(String sqlDate) {
		try {
			return dateFormat.parse(sqlDate);
		} catch (ParseException pe) {
			return null;
		}
	}
}
