package org.maupu.android.tmh.database.object;

import java.text.ParseException;
import java.util.Date;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.util.DateUtil;
import org.maupu.android.tmh.database.util.QueryBuilder;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class Operation extends BaseObject {
	private static final long serialVersionUID = 1L;
	public static final String KEY_SUM="sum";
	private Float amount;
	private String description;
	private Date date;
	private Account account;
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
	public Account getAccount() {
		return account;
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
	public void setAccount(Account account) {
		this.account = account;
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
		cv.put(OperationData.KEY_AMOUNT, this.getAmount());
		cv.put(OperationData.KEY_DESCRIPTION, this.getDescription());
		if(this.getDate() != null)
			cv.put(OperationData.KEY_DATE, DatabaseHelper.formatDateForSQL(this.getDate()));
		if(this.getAccount() != null)
			cv.put(OperationData.KEY_ID_ACCOUNT, this.getAccount().getId());
		if(this.getCategory() != null)
			cv.put(OperationData.KEY_ID_CATEGORY, this.getCategory().getId());
		if(this.getCurrency() != null)
			cv.put(OperationData.KEY_ID_CURRENCY, this.getCurrency().getId());
		cv.put(OperationData.KEY_CURRENCY_VALUE, this.getCurrencyValueOnCreated());

		return cv;
	}

	@Override
	public String getTableName() {
		return OperationData.TABLE_NAME;
	}

	@Override
	public BaseObject toDTO(DatabaseHelper dbHelper, Cursor cursor) throws IllegalArgumentException {
		this.reset();
		int idxId = cursor.getColumnIndexOrThrow(OperationData.KEY_ID);
		int idxAmount = cursor.getColumnIndexOrThrow(OperationData.KEY_AMOUNT);
		int idxDesctipion = cursor.getColumnIndexOrThrow(OperationData.KEY_DESCRIPTION);
		int idxDate = cursor.getColumnIndexOrThrow(OperationData.KEY_DATE);
		int idxAccount = cursor.getColumnIndexOrThrow(OperationData.KEY_ID_ACCOUNT);
		int idxCategory = cursor.getColumnIndexOrThrow(OperationData.KEY_ID_CATEGORY);
		int idxCurrency = cursor.getColumnIndexOrThrow(OperationData.KEY_ID_CURRENCY);
		int idxCurrencyValueOnCreated = cursor.getColumnIndexOrThrow(OperationData.KEY_CURRENCY_VALUE);

		Account account = new Account();
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
			account = (Account)account.toDTO(dbHelper, account.fetch(dbHelper, cursor.getInt(idxAccount)));
			category = (Category)category.toDTO(dbHelper, category.fetch(dbHelper, cursor.getInt(idxCategory)));
			currency = (Currency)currency.toDTO(dbHelper, currency.fetch(dbHelper, cursor.getInt(idxCurrency)));

			this.setAccount(account);
			this.setCategory(category);
			this.setCurrency(currency);

			this.setCurrencyValueOnCreated(cursor.getFloat(idxCurrencyValueOnCreated));
		}

		return super.getFromCache();
	}

	public Cursor fetchAll(final DatabaseHelper dbHelper) {
		QueryBuilder qsb = getDefaultQueryBuilder();
		return dbHelper.getDb().rawQuery(qsb.getStringBuilder().toString(), null);
	}

	public Cursor fetchByPeriod(final DatabaseHelper dbHelper, Date dateBegin, Date dateEnd, int accountId) {
		QueryBuilder qsb = getDefaultQueryBuilder();

		String sBeg = DatabaseHelper.formatDateForSQL(dateBegin);
		String sEnd = DatabaseHelper.formatDateForSQL(dateEnd);
		qsb.append("AND a."+AccountData.KEY_ID+"="+accountId+" ");
		qsb.append("AND o.date BETWEEN '"+sBeg+"' AND '"+sEnd+"' ");
		qsb.append("ORDER BY o."+OperationData.KEY_DATE+" ASC ");

		Log.d(Operation.class.getName(), "fetching by date : begin = "+sBeg+", end="+sEnd);
		Log.d(Operation.class.getName(), qsb.getStringBuilder().toString());
		
		return dbHelper.getDb().rawQuery(qsb.getStringBuilder().toString(), null);
	}

	private QueryBuilder getDefaultQueryBuilder() {
		QueryBuilder qsb = new QueryBuilder(new StringBuilder("select "));
		qsb.setCurrentTableAlias("o");
		qsb.addSelectToQuery(OperationData.KEY_ID).append(",")
		.addSelectToQuery(OperationData.KEY_AMOUNT).append(",")
		.addSelectToQuery(OperationData.KEY_DESCRIPTION).append(",")
		.addSelectToQuery(OperationData.KEY_ID_ACCOUNT).append(",")
		.addSelectToQuery(OperationData.KEY_ID_CATEGORY).append(",")
		.addSelectToQuery(OperationData.KEY_ID_CURRENCY).append(",")
		.addSelectToQuery(OperationData.KEY_CURRENCY_VALUE).append(",");

		qsb.append("strftime('%d-%m-%Y %H:%M:%S', o.date) date, ");

		qsb.setCurrentTableAlias("a");
		qsb.addSelectToQuery(AccountData.KEY_ICON).append(",")
		.addSelectToQuery(AccountData.KEY_NAME, "account").append(",");

		qsb.setCurrentTableAlias("ca");
		qsb.addSelectToQuery(CategoryData.KEY_NAME, "category").append(",");

		qsb.append("ROUND(o."+OperationData.KEY_AMOUNT+"/o."+OperationData.KEY_CURRENCY_VALUE+",2) euroAmount, ");
		qsb.append("ROUND(o.amount,2)||' '||c.shortName amountString, ");
		qsb.append("strftime('%d-%m-%Y', o.date) dateString ");
		qsb.append("from "+CategoryData.TABLE_NAME+" as ca, "+AccountData.TABLE_NAME+" as a, "+OperationData.TABLE_NAME+" as o, "+CurrencyData.TABLE_NAME+" as c ");
		qsb.append("where o.idCategory=ca._id and o.idAccount=a._id and o.idCurrency=c._id ");

		return qsb;
	}

	public Cursor fetchByMonth(final DatabaseHelper dbHelper, Date date, int accountId) {
		Date dateBegin = DateUtil.getFirstDayOfMonth(date);
		Date dateEnd = DateUtil.getLastDayOfMonth(date);

		return fetchByPeriod(dbHelper, dateBegin, dateEnd, accountId);
	}
	
	public Cursor sumOperationByMonth(final DatabaseHelper dbHelper, Date date, String operationType) {
		return sumOperationByPeriod(dbHelper, DateUtil.getFirstDayOfMonth(date), DateUtil.getLastDayOfMonth(date), operationType);
	}
	
	public Cursor sumOperationByPeriod(final DatabaseHelper dbHelper, Date dateBegin, Date dateEnd, String operationType) {
		String sBeg = DatabaseHelper.formatDateForSQL(dateBegin);
		String sEnd = DatabaseHelper.formatDateForSQL(dateEnd);
		
		QueryBuilder qb = new QueryBuilder(new StringBuilder("SELECT "));
		qb.append("sum("+OperationData.KEY_AMOUNT+") "+Operation.KEY_SUM+", ");
		qb.append(AccountData.KEY_NAME+" ");
		qb.append("FROM "+OperationData.TABLE_NAME+" o, "+AccountData.TABLE_NAME+" a ");
		qb.append("WHERE o."+OperationData.KEY_DATE+" BETWEEN '"+sBeg+"' AND '"+sEnd+"' ");
		qb.append("AND o."+OperationData.KEY_ID_ACCOUNT+"=a."+AccountData.KEY_ID+" ");
		qb.append("GROUP BY o."+OperationData.KEY_ID_ACCOUNT);
		
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
		this.account = null;
		this.currencyValueOnCreation = null;
	}
	
	@Override
	public String toString() {
		return ""+this.getId();
	}
	@Override
	public String getDefaultOrderColumn() {
		return OperationData.KEY_DATE;
	}
}
