package org.maupu.android.tmh.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.database.object.Account;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat dateFormatNoHour = new SimpleDateFormat("yyyy-MM-dd");
	protected static final String DATABASE_NAME = "TravelerMoneyHelper_appdata";
	protected static final int DATABASE_VERSION = 5;
	private static List<APersistedData> persistedData = new ArrayList<APersistedData>();
	private Context context;
	private SQLiteDatabase db;
	
	public DatabaseHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = ctx;
		
		persistedData.add(new CategoryData(ctx));
		persistedData.add(new CurrencyData(ctx));
		persistedData.add(new OperationData(ctx));
		persistedData.add(new AccountData(ctx));
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
	
	public static String formatDateForSQLNoHour(Date date) {
		return dateFormatNoHour.format(date);
	}
	
	public static Date toDate(String sqlDate) {
		try {
			return dateFormat.parse(sqlDate);
		} catch (ParseException pe) {
			return null;
		}
	}
	
	public void createSampleData() {
		// If something in accounts, don't do anything
		Account dummy = new Account();
		Cursor c = dummy.fetchAll(this);
		if(c.getCount() > 0)
			return;
		
		Currency currency = new Currency();
		currency.setLastUpdate(new Date());
		currency.setLongName("Euro");
		currency.setShortName("e");
		currency.setTauxEuro(1f);
		currency.insert(this);
		
		currency.setLongName("Dollar");
		currency.setShortName("$");
		currency.setTauxEuro(1.4f);
		currency.insert(this);
		
		Cursor cCurrency = currency.fetch(this, 1);
		currency.toDTO(this, cCurrency);
		
		Account account = new Account();
		account.setName("Nicolas");
		account.setCurrency(currency);
		account.insert(this);
		
		account.setName("Marianne");
		account.insert(this);
		
		Category category = new Category();
		category.setName("Courses");
		category.insert(this);
		
		category.setName("Alimentation");
		category.insert(this);
		
		
		
		category.toDTO(this, category.fetch(this, 1));
		account.toDTO(this, account.fetch(this, 1));
		currency.toDTO(this, currency.fetch(this, 1));
		Operation op = new Operation();
		op.setAmount(10f);
		op.setAccount(account);
		op.setCategory(category);
		op.setCurrency(currency);
		op.setCurrencyValueOnCreated(1f);
		op.setDate(new Date());
		op.insert(this);
		
		op.setAmount(15f);
		op.insert(this);
		
		op.setAmount(22f);
		op.insert(this);
	}
}
