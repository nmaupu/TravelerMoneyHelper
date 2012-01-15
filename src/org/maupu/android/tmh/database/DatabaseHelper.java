package org.maupu.android.tmh.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Expense;
import org.maupu.android.tmh.database.object.User;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	protected static final String DATABASE_NAME = "TravelerMoneyHelper_appdata";
	protected static final int DATABASE_VERSION = 4;
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
	
	public void createSampleData() {
		// If something in users, don't do anything
		User dummy = new User();
		Cursor c = dummy.fetchAll(this);
		if(c.getCount() > 0)
			return;
		
		User user = new User();
		user.setName("Nicolas");
		user.insert(this);
		
		user.setName("Marianne");
		user.insert(this);
		
		Category category = new Category();
		category.setName("Courses");
		category.insert(this);
		
		category.setName("Alimentation");
		category.insert(this);
		
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
		
		category.toDTO(this, category.fetch(this, 1));
		user.toDTO(this, user.fetch(this, 1));
		currency.toDTO(this, currency.fetch(this, 1));
		Expense expense = new Expense();
		expense.setAmount(10f);
		expense.setUser(user);
		expense.setCategory(category);
		expense.setCurrency(currency);
		expense.setType(ExpenseData.EXPENSE_TYPE_DEFAULT);
		expense.setCurrencyValueOnCreated(1f);
		expense.setDate(new Date());
		expense.insert(this);
		
		expense.setAmount(15f);
		expense.insert(this);
		
		expense.setAmount(22f);
		expense.insert(this);
	}
}
