package org.maupu.android.tmh.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class DatabaseHelper extends SQLiteOpenHelper {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat dateFormatNoHour = new SimpleDateFormat("yyyy-MM-dd");
	protected static final String DATABASE_NAME = "TravelerMoneyHelper_appdata";
	protected static final int DATABASE_VERSION = 7;
	private static List<APersistedData> persistedData = new ArrayList<APersistedData>();
	
	public DatabaseHelper() {
		super(TmhApplication.getAppContext(), DATABASE_NAME, null, DATABASE_VERSION);
		
		persistedData.add(new CategoryData());
		persistedData.add(new CurrencyData());
		persistedData.add(new OperationData());
		persistedData.add(new AccountData());
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
	
	public void close() {
		super.close();
	}
	
	public SQLiteDatabase getDb() {
		return super.getWritableDatabase();
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
		Cursor c = dummy.fetchAll();
		if(c.getCount() > 0)
			return;
		
		Currency currency = new Currency();
		currency.setLastUpdate(new GregorianCalendar().getTime());
		currency.setLongName("Euro");
		currency.setShortName("e");
		currency.setTauxEuro(1f);
		currency.setIsoCode("EUR");
		currency.insert();
		
		currency.setLongName("Dollar");
		currency.setShortName("$");
		currency.setIsoCode("USD");
		currency.setTauxEuro(1.4f);
		currency.insert();
		
		Cursor cCurrency = currency.fetch(1);
		currency.toDTO(cCurrency);
		
		Account account = new Account();
		account.setName("Nicolas");
		account.setCurrency(currency);
		account.insert();
		
		account.setName("Marianne");
		account.insert();
		
		Category category = new Category();
		category.setName("Courses");
		category.insert();
		
		category.setName("Alimentation");
		category.insert();
		
		
		
		category.toDTO(category.fetch(1));
		account.toDTO(account.fetch(1));
		currency.toDTO(currency.fetch(1));
		Operation op = new Operation();
		op.setAmount(10f);
		op.setAccount(account);
		op.setCategory(category);
		op.setCurrency(currency);
		op.setCurrencyValueOnCreated(1f);
		op.setDate(new GregorianCalendar().getTime());
		op.insert();
		
		op.setAmount(15f);
		op.insert();
		
		op.setAmount(22f);
		op.insert();
	}
}
