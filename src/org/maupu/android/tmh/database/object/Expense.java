package org.maupu.android.tmh.database.object;

import java.text.ParseException;
import java.util.Date;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.ExpenseData;
import org.maupu.android.tmh.database.UserData;
import org.maupu.android.tmh.database.util.DateUtil;
import org.maupu.android.tmh.database.util.QueryBuilder;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class Expense extends BaseObject {
	private static final long serialVersionUID = 1L;
	public static final String KEY_SUM="sum";
	private Float amount;
	private String description;
	private Date date;
	private User user;
	private Category category;
	private Currency currency;
	private Float currencyValueOnCreation;
	private String type;

	public Float getAmount() {
		return amount;
	}
	public String getDescription() {
		return description;
	}
	public Date getDate() {
		return date;
	}
	public void setAmount(Float amount) {
		this.amount = amount;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public User getUser() {
		return user;
	}
	public Category getCategory() {
		return category;
	}
	public Currency getCurrency() {
		return currency;
	}
	public Float getCurrencyValueOnCreated() {
		return currencyValueOnCreation;
	}
	public void setCurrencyValueOnCreated(Float value) {
		currencyValueOnCreation = value;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}


	public ContentValues createContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(ExpenseData.KEY_AMOUNT, this.getAmount());
		cv.put(ExpenseData.KEY_DESCRIPTION, this.getDescription());
		if(this.getDate() != null)
			cv.put(ExpenseData.KEY_DATE, DatabaseHelper.formatDateForSQL(this.getDate()));
		if(this.getUser() != null)
			cv.put(ExpenseData.KEY_ID_USER, this.getUser().getId());
		if(this.getCategory() != null)
			cv.put(ExpenseData.KEY_ID_CATEGORY, this.getCategory().getId());
		if(this.getCurrency() != null)
			cv.put(ExpenseData.KEY_ID_CURRENCY, this.getCurrency().getId());
		cv.put(ExpenseData.KEY_CURRENCY_VALUE, this.getCurrencyValueOnCreated());

		if(this.getType() == null)
			cv.put(ExpenseData.KEY_TYPE, ExpenseData.EXPENSE_TYPE_DEFAULT);
		else
			cv.put(ExpenseData.KEY_TYPE, this.getType());

		return cv;
	}

	@Override
	public String getTableName() {
		return ExpenseData.TABLE_NAME;
	}

	@Override
	public BaseObject toDTO(DatabaseHelper dbHelper, Cursor cursor) throws IllegalArgumentException {
		this.reset();
		int idxId = cursor.getColumnIndexOrThrow(ExpenseData.KEY_ID);
		int idxAmount = cursor.getColumnIndexOrThrow(ExpenseData.KEY_AMOUNT);
		int idxDesctipion = cursor.getColumnIndexOrThrow(ExpenseData.KEY_DESCRIPTION);
		int idxDate = cursor.getColumnIndexOrThrow(ExpenseData.KEY_DATE);
		int idxUser = cursor.getColumnIndexOrThrow(ExpenseData.KEY_ID_USER);
		int idxCategory = cursor.getColumnIndexOrThrow(ExpenseData.KEY_ID_CATEGORY);
		int idxCurrency = cursor.getColumnIndexOrThrow(ExpenseData.KEY_ID_CURRENCY);
		int idxCurrencyValueOnCreated = cursor.getColumnIndexOrThrow(ExpenseData.KEY_CURRENCY_VALUE);
		int idxType = cursor.getColumnIndexOrThrow(ExpenseData.KEY_TYPE);

		User user = new User();
		Category category = new Category();
		Currency currency = new Currency();

		if(! cursor.isClosed() && ! cursor.isBeforeFirst() && ! cursor.isAfterLast()) {
			this._id = cursor.getInt(idxId);
			this.setAmount(cursor.getFloat(idxAmount));
			this.setDescription(cursor.getString(idxDesctipion));
			try {
				this.setDate(DateUtil.StringToDate(cursor.getString(idxDate)));
			} catch(ParseException pe) {
				this.setDate(null);
			}
			user = (User)user.toDTO(dbHelper, user.fetch(dbHelper, cursor.getInt(idxUser)));
			category = (Category)category.toDTO(dbHelper, category.fetch(dbHelper, cursor.getInt(idxCategory)));
			currency = (Currency)currency.toDTO(dbHelper, currency.fetch(dbHelper, cursor.getInt(idxCurrency)));

			this.setUser(user);
			this.setCategory(category);
			this.setCurrency(currency);

			this.setCurrencyValueOnCreated(cursor.getFloat(idxCurrencyValueOnCreated));
			this.setType(cursor.getString(idxType));
		}

		return super.getFromCache();
	}

	public Cursor fetchAll(final DatabaseHelper dbHelper) {
		QueryBuilder qsb = getDefaultQueryBuilder();
		return dbHelper.getDb().rawQuery(qsb.getStringBuilder().toString(), null);
	}

	public Cursor fetchByPeriod(final DatabaseHelper dbHelper, Date dateBegin, Date dateEnd) {
		return fetchByPeriod(dbHelper, dateBegin, dateEnd, null);
	}

	public Cursor fetchByPeriod(final DatabaseHelper dbHelper, Date dateBegin, Date dateEnd, String[] expenseTypes) {
		QueryBuilder qsb = getDefaultQueryBuilder();

		String sBeg = DatabaseHelper.formatDateForSQL(dateBegin);
		String sEnd = DatabaseHelper.formatDateForSQL(dateEnd);
		qsb.append("AND e.date BETWEEN '"+sBeg+"' AND '"+sEnd+"' ");
		if(expenseTypes != null && expenseTypes.length >= 1) {
			qsb.append("AND (");
			for(int i=0; i<expenseTypes.length; i++) {
				if(i != 0)
					qsb.append("OR ");
				
				 qsb.append("e.type='" + expenseTypes[i] + "' ");
			}
			qsb.append(") ");
		}

		Log.d(Expense.class.getName(), "fetching by date : begin = "+sBeg+", end="+sEnd);
		Log.d(Expense.class.getName(), qsb.getStringBuilder().toString());
		
		return dbHelper.getDb().rawQuery(qsb.getStringBuilder().toString(), null);
	}

	private QueryBuilder getDefaultQueryBuilder() {
		QueryBuilder qsb = new QueryBuilder(new StringBuilder("select "));
		qsb.setCurrentTableAlias("e");
		qsb.addSelectToQuery(ExpenseData.KEY_ID).append(",")
		.addSelectToQuery(ExpenseData.KEY_AMOUNT).append(",")
		.addSelectToQuery(ExpenseData.KEY_DESCRIPTION).append(",")
		.addSelectToQuery(ExpenseData.KEY_ID_USER).append(",")
		.addSelectToQuery(ExpenseData.KEY_ID_CATEGORY).append(",")
		.addSelectToQuery(ExpenseData.KEY_ID_CURRENCY).append(",")
		.addSelectToQuery(ExpenseData.KEY_CURRENCY_VALUE).append(",")
		.addSelectToQuery(ExpenseData.KEY_TYPE).append(",");

		qsb.append("strftime('%d-%m-%Y %H:%M:%S', e.date) date, ");

		qsb.setCurrentTableAlias("u");
		qsb.addSelectToQuery(UserData.KEY_ICON).append(",")
		.addSelectToQuery(UserData.KEY_NAME, "user").append(",");

		qsb.setCurrentTableAlias("ca");
		qsb.addSelectToQuery(CategoryData.KEY_NAME, "category").append(",");

		qsb.append("ROUND(e."+ExpenseData.KEY_AMOUNT+"/e."+ExpenseData.KEY_CURRENCY_VALUE+",2) euroAmount, ");
		qsb.append("ROUND(e.amount,2)||' '||c.shortName amountString, ");
		qsb.append("strftime('%d-%m-%Y', e.date) dateString ");
		qsb.append("from category as ca, user as u, expense as e, currency as c ");
		qsb.append("where e.idCategory=ca._id and e.idUser=u._id and e.idCurrency=c._id ");

		return qsb;
	}

	public Cursor fetchByMonth(final DatabaseHelper dbHelper, Date date) {
		return fetchByMonth(dbHelper, date, null);
	}
	
	public Cursor fetchByMonth(final DatabaseHelper dbHelper, Date date, String[] expenseTypes) {
		Date dateBegin = DateUtil.getFirstDayOfMonth(date);
		Date dateEnd = DateUtil.getLastDayOfMonth(date);

		return fetchByPeriod(dbHelper, dateBegin, dateEnd, expenseTypes);
	}
	
	public Cursor sumExpenseByMonth(final DatabaseHelper dbHelper, Date date, String expenseType) {
		return sumExpenseByPeriod(dbHelper, DateUtil.getFirstDayOfMonth(date), DateUtil.getLastDayOfMonth(date), expenseType);
	}
	
	public Cursor sumExpenseByPeriod(final DatabaseHelper dbHelper, Date dateBegin, Date dateEnd, String expenseType) {
		String sBeg = DatabaseHelper.formatDateForSQL(dateBegin);
		String sEnd = DatabaseHelper.formatDateForSQL(dateEnd);
		
		QueryBuilder qb = new QueryBuilder(new StringBuilder("SELECT "));
		qb.append("sum("+ExpenseData.KEY_AMOUNT+") "+Expense.KEY_SUM+", ");
		qb.append(UserData.KEY_NAME+" ");
		qb.append("FROM "+ExpenseData.TABLE_NAME+" e, "+UserData.TABLE_NAME+" u ");
		qb.append("WHERE e."+ExpenseData.KEY_DATE+" BETWEEN '"+sBeg+"' AND '"+sEnd+"' ");
		qb.append("AND e."+ExpenseData.KEY_TYPE+"='"+expenseType+"' ");
		qb.append("AND e."+ExpenseData.KEY_ID_USER+"=u."+UserData.KEY_ID+" ");
		qb.append("GROUP BY e."+ExpenseData.KEY_ID_USER);
		
		Cursor c = dbHelper.getDb().rawQuery(qb.getStringBuilder().toString(), null);
		c.moveToFirst();
		return c;
	}

	@Override
	public boolean validate() {
		return true;
	}
	@Override
	public void reset() {
		super._id = null;
		this.amount = null;
		this.category = null;
		this.currency = null;
		this.date = null;
		this.description = null;
		this.user = null;
		this.currencyValueOnCreation = null;
		this.type = ExpenseData.EXPENSE_TYPE_DEFAULT;
	}
}
