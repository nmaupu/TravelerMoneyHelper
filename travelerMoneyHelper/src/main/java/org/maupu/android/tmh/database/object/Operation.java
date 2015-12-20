package org.maupu.android.tmh.database.object;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.filter.OperationFilter;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.QueryBuilder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

public class Operation extends BaseObject {
	private static final long serialVersionUID = 1L;
	public static final String KEY_SUM="sum";
	public static final int COLOR_POSITIVE_AMOUNT = Color.parseColor("#3fab37");
	public static final int COLOR_NEGATIVE_AMOUNT = Color.parseColor("#d03636");
	private Double amount;
	private String description;
	private Date date;
	private Account account;
	private Category category;
	private Currency currency;
	private Double currencyValueOnCreation;
	private String type;
	private Integer linkToOperation;
	private OperationFilter filter = new OperationFilter();
	
	public Operation() {
		super();
	}
	
	public Operation(OperationFilter filter) {
		this();
		this.filter = filter;
	}

    @Override
    public BaseObject copy() {
        Operation o = new Operation();
        o._id = super.getId();
        o.setAmount(amount);
        o.setDescription(description);
        o.setDate((Date) date.clone());
        o.setAccount((Account) account.copy());
        o.setCategory((Category) category.copy());
        o.setCurrency((Currency) currency.copy());
        o.setCurrencyValueOnCreated(currencyValueOnCreation);
        o.setType(type);
        o.setLinkToOperation(linkToOperation);
        return o;
    }

    @Override
	public String getDefaultOrder() {
		return "DESC";
	}

	public OperationFilter getFilter() {
		return filter;
	}
	
	public Double getAmount() {
		return amount;
	}
	public String getDescription() {
		return description;
	}
	public Date getDate() {
		return date;
	}
	public Integer getLinkToOperation() {
		return linkToOperation;
	}

	public void setAmount(Double amount) {
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
	public Double getCurrencyValueOnCreated() {
		return currencyValueOnCreation;
	}
	public void setCurrencyValueOnCreated(Double value) {
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
	public void setLinkToOperation(Integer linkToOperation) {
		this.linkToOperation = linkToOperation;
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
		cv.put(OperationData.KEY_LINK_TO, this.getLinkToOperation());

		return cv;
	}

	@Override
	public String getTableName() {
		return OperationData.TABLE_NAME;
	}

	@Override
	public BaseObject toDTOWithDb(SQLiteDatabase db, Cursor cursor) throws IllegalArgumentException {
		this.reset();
		int idxId = cursor.getColumnIndexOrThrow(OperationData.KEY_ID);
		int idxAmount = cursor.getColumnIndexOrThrow(OperationData.KEY_AMOUNT);
		int idxDesctipion = cursor.getColumnIndexOrThrow(OperationData.KEY_DESCRIPTION);
		int idxDate = cursor.getColumnIndexOrThrow(OperationData.KEY_DATE);
		int idxAccount = cursor.getColumnIndexOrThrow(OperationData.KEY_ID_ACCOUNT);
		int idxCategory = cursor.getColumnIndexOrThrow(OperationData.KEY_ID_CATEGORY);
		int idxCurrency = cursor.getColumnIndexOrThrow(OperationData.KEY_ID_CURRENCY);
		int idxCurrencyValueOnCreated = cursor.getColumnIndexOrThrow(OperationData.KEY_CURRENCY_VALUE);
		int idxLinkToOperation = cursor.getColumnIndexOrThrow(OperationData.KEY_LINK_TO);

		Account account = new Account();
		Category category = new Category();
		Currency currency = new Currency();

		if(! cursor.isClosed() && ! cursor.isBeforeFirst() && ! cursor.isAfterLast()) {
			this._id = cursor.getInt(idxId);
			this.setAmount(cursor.getDouble(idxAmount));
			this.setDescription(cursor.getString(idxDesctipion));
			try {
				this.setDate(DateUtil.StringSQLToDate(cursor.getString(idxDate)));
			} catch(ParseException pe) {
				this.setDate(null);
			}
			account = (Account)account.toDTOWithDb(db, account.fetch(cursor.getInt(idxAccount)));
			category = (Category)category.toDTOWithDb(db, category.fetch(cursor.getInt(idxCategory)));
			currency = (Currency)currency.toDTOWithDb(db, currency.fetch(cursor.getInt(idxCurrency)));

			this.setAccount(account);
			this.setCategory(category);
			this.setCurrency(currency);

			this.setCurrencyValueOnCreated(cursor.getDouble(idxCurrencyValueOnCreated));
			if(idxLinkToOperation != -1)
				this.setLinkToOperation(cursor.getInt(idxLinkToOperation));
		}

		return super.getFromCache();
	}

	public Cursor fetchAll() {
		QueryBuilder qsb = filter.getQueryBuilder().append("ORDER BY o." + OperationData.KEY_DATE + " ASC");
		return TmhApplication.getDatabaseHelper().getDb().rawQuery(qsb.getStringBuilder().toString(), null);
	}

	public Cursor fetchByPeriod(Date dateBegin, Date dateEnd) {
		return fetchByPeriod(dateBegin, dateEnd, "o." + OperationData.KEY_DATE + " DESC", -1);
	}
	
	public Cursor fetchByPeriod(Date dateBegin, Date dateEnd, String order, int limit) {
		QueryBuilder qsb = filter.getQueryBuilder();
		//QueryBuilder myQsb = new QueryBuilder(qsb.getStringBuilder());

		String sBeg = DatabaseHelper.formatDateForSQL(dateBegin);
		String sEnd = DatabaseHelper.formatDateForSQL(dateEnd);
		//qsb.append("AND a."+AccountData.KEY_ID+"="+accountId+" ");
		qsb.append("AND o.date BETWEEN '"+sBeg+"' AND '"+sEnd+"' ");
		String sLimit = limit > 0 ? " limit "+limit : "";
		qsb.append("ORDER BY " + order + " " + sLimit);

		Log.d(Operation.class.getName(), "fetching by date : begin = "+sBeg+", end="+sEnd);
		Log.d(Operation.class.getName(), qsb.getStringBuilder().toString());
		
		return TmhApplication.getDatabaseHelper().getDb().rawQuery(qsb.getStringBuilder().toString(), null);
	}

	public Cursor fetchByMonth(Date date) {
		Date dateBegin = DateUtil.getFirstDayOfMonth(date);
		Date dateEnd = DateUtil.getLastDayOfMonth(date);

		return fetchByPeriod(dateBegin, dateEnd);
	}

    /**
     * Select and sum all amounts grouping by currency for a given account
     * @param account
     * @param exceptCategories
     * @return Sum of all operations for given parameters
     */
    public Cursor sumOperations(Account account, Integer[] exceptCategories) {
        return sumOperationsByPeriod(account, null, null, exceptCategories);
    }
	
	/**
	 * Select and sum all amounts grouping by currency for a given account and for a given month
	 * @param account
	 * @param date
	 * @return Sum of all operations for given parameters
	 */
	public Cursor sumOperationsByMonth(Account account, Date date, Integer[] exceptCategories) {
		return sumOperationsByPeriod(account, DateUtil.getFirstDayOfMonth(date), DateUtil.getLastDayOfMonth(date), exceptCategories);
	}
	
	/**
	 * Select and sum all amounts grouping by currency for a given account and for a given period
	 * @param dateBegin
	 * @param dateEnd
	 * @return Sum of all operations for given parameters
	 */
	public Cursor sumOperationsByPeriod(Account account, Date dateBegin, Date dateEnd, Integer[] exceptCategories) {
		if(account == null || account.getId() == null)
			return null;

        String sBeg = null;
        String sEnd = null;
        if(dateBegin != null)
		   sBeg = DatabaseHelper.formatDateForSQL(dateBegin);
        if(dateEnd != null)
            sEnd = DatabaseHelper.formatDateForSQL(dateEnd);
		
		QueryBuilder qb = new QueryBuilder(new StringBuilder("SELECT "));
		qb.append("sum("+OperationData.KEY_AMOUNT+") "+Operation.KEY_SUM+", ");
		qb.append("c."+CurrencyData.KEY_CURRENCY_LINKED+", ");
		qb.append("c."+CurrencyData.KEY_SHORT_NAME+" ");
		qb.append("FROM "+OperationData.TABLE_NAME+" o, "+AccountData.TABLE_NAME+" a, "+CurrencyData.TABLE_NAME+" c ");
		qb.append("WHERE a."+AccountData.KEY_ID+"="+account.getId()+" ");
        if(sBeg != null && sEnd != null)
		    qb.append("AND o."+OperationData.KEY_DATE+" BETWEEN '"+sBeg+"' AND '"+sEnd+"' ");
		qb.append("AND o." + OperationData.KEY_ID_ACCOUNT + "=a." + AccountData.KEY_ID + " ");
		qb.append("AND o." + OperationData.KEY_ID_CURRENCY + "=c." + CurrencyData.KEY_ID + " ");
		
		if(exceptCategories != null && exceptCategories.length > 0) {
			StringBuilder b = new StringBuilder();
			for(int i=0; i<exceptCategories.length; i++) {
				int catId = exceptCategories[i];
				b.append(catId);
				if(i<exceptCategories.length-1)
					b.append(",");
			}
			qb.append("AND o."+OperationData.KEY_ID_CATEGORY+" NOT IN("+b.toString()+") ");
		}
		
		qb.append("GROUP BY o." + OperationData.KEY_ID_CURRENCY);
		
		Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(qb.getStringBuilder().toString(), null);
		c.moveToFirst();
		return c;
	}

	public Integer[] getExceptCategoriesAuto(Account account) {
		if(account == null)
			return null;

		List<Integer> exceptCategories = new ArrayList<Integer>();

		QueryBuilder qb = new QueryBuilder(new StringBuilder("SELECT "));
		qb.append("c."+CategoryData.KEY_ID+" ");
		qb.append("FROM "+OperationData.TABLE_NAME+" o, "+CategoryData.TABLE_NAME+" c, "+AccountData.TABLE_NAME+" a ");
		qb.append("WHERE o."+OperationData.KEY_ID_ACCOUNT+"=a."+AccountData.KEY_ID+" ");
		qb.append("AND c."+CategoryData.KEY_ID+"=o."+OperationData.KEY_ID_CATEGORY+" ");
		qb.append("AND a."+AccountData.KEY_ID+"="+account.getId()+" ");
		qb.append("AND o." + OperationData.KEY_AMOUNT + ">0 ");

		Log.d(Operation.class.getName(), qb.getStringBuilder().toString());

		Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(qb.getStringBuilder().toString(), null);
		c.moveToFirst();
		int idx;
        try {
            do {
                idx = c.getColumnIndexOrThrow(CategoryData.KEY_ID);
                exceptCategories.add(c.getInt(idx));
            } while (c.moveToNext());
        } catch(CursorIndexOutOfBoundsException cioobe) {
            // Cursor is empty, nothing special to do
            Log.e(Operation.class.getName(), "Error in calling getExceptCategoriesAuto, cursor empty ?");
        }

		return exceptCategories.toArray(new Integer[0]);
	}

	public Date getFirstDate(Account account, Integer[] exceptCategories) {
		return getFirstOrLastDate(account, exceptCategories, true);
	}

	public Date getLastDate(Account account, Integer[] exceptCategories) {
		return getFirstOrLastDate(account, exceptCategories, false);
	}

	public Date getFirstOrLastDate(Account account, Integer[] exceptCategories, boolean first) {
		if(account == null || account.getId() == null)
			return null;

		String order = first ? "ASC" : "DESC";

		QueryBuilder qb = new QueryBuilder(new StringBuilder("SELECT "));
		qb.append("o."+OperationData.KEY_DATE+" ");
		qb.append("FROM "+OperationData.TABLE_NAME+" o, "+AccountData.TABLE_NAME+" a, "+CategoryData.TABLE_NAME+" ");
		qb.append("WHERE a."+AccountData.KEY_ID+"="+account.getId()+" ");
		qb.append("AND a."+AccountData.KEY_ID+"=o."+OperationData.KEY_ID_ACCOUNT+" ");

		if(exceptCategories != null && exceptCategories.length > 0) {
			StringBuilder b = new StringBuilder();
			for(int i=0; i<exceptCategories.length; i++) {
				int catId = exceptCategories[i];
				b.append(catId);
				if(i<exceptCategories.length-1)
					b.append(",");
			}
			qb.append("AND o."+OperationData.KEY_ID_CATEGORY+" NOT IN("+b.toString()+") ");
		}

		qb.append("ORDER BY o." + OperationData.KEY_DATE + " " + order + " ");
		qb.append("LIMIT 1 ");

		Log.d(Operation.class.getName(), qb.getStringBuilder().toString());

		Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(qb.getStringBuilder().toString(), null);
		c.moveToFirst();
		int idxDate = c.getColumnIndexOrThrow(OperationData.KEY_DATE);
		Date d;
		try {
			d = DateUtil.StringSQLToDate(c.getString(idxDate));
		} catch(ParseException pe) {
			d = null;
		} catch(CursorIndexOutOfBoundsException cioobe) {
            d = null;
        }

		return d;
	}
	
	public Cursor sumOperationsGroupByDay(Account account, Date dateBegin, Date dateEnd, Integer[] exceptCategories) {
		if(account == null || account.getId() == null)
			return null;
		
		String sBeg = DatabaseHelper.formatDateForSQL(dateBegin);
		String sEnd = DatabaseHelper.formatDateForSQL(dateEnd);
		
		QueryBuilder qb = new QueryBuilder(new StringBuilder("SELECT "));
		qb.append("o."+OperationData.KEY_ID+", ");
		qb.append("o."+OperationData.KEY_DATE+", ");
		qb.append("sum("+OperationData.KEY_AMOUNT+") amountString, ");
		qb.append("c."+CurrencyData.KEY_CURRENCY_LINKED+", ");
		qb.append("c."+CurrencyData.KEY_SHORT_NAME+", ");
		qb.append("strftime('%d-%m-%Y', o."+OperationData.KEY_DATE+") dateString ");
		qb.append("FROM "+OperationData.TABLE_NAME+" o, "+AccountData.TABLE_NAME+" a, "+CurrencyData.TABLE_NAME+" c ");
		qb.append("WHERE a."+AccountData.KEY_ID+"="+account.getId()+" ");
		qb.append("AND o."+OperationData.KEY_DATE+" BETWEEN '"+sBeg+"' AND '"+sEnd+"' ");
		qb.append("AND o."+OperationData.KEY_ID_ACCOUNT+"=a."+AccountData.KEY_ID+" ");
		qb.append("AND o."+OperationData.KEY_ID_CURRENCY+"=c."+CurrencyData.KEY_ID+" ");
		
		if(exceptCategories != null && exceptCategories.length > 0) {
			StringBuilder b = new StringBuilder();
			for(int i=0; i<exceptCategories.length; i++) {
				// If a cat is deleted, some items may be null. So we put -1 which does not exist in db
				int catId = exceptCategories[i] == null ? -1 : exceptCategories[i];
				b.append(catId);
				if(i<exceptCategories.length-1)
					b.append(",");
			}
			qb.append("AND o."+OperationData.KEY_ID_CATEGORY+" NOT IN("+b.toString()+") ");
		}
		
		qb.append("GROUP BY o."+OperationData.KEY_ID_CURRENCY+", dateString").append(" "); // group by dateString instead of date because of hours and minutes
		qb.append("ORDER BY o." + OperationData.KEY_DATE + " DESC ");
		
		Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(qb.getStringBuilder().toString(), null);
		c.moveToFirst();
		return c;
	}

    /**
     * Sum all operations and group by categories for a specific account.
     * Begin date and end date are automatically computed.
     * @param account
     * @param exceptCategories
     * @return A cursor corresponding to the request
     */
    public Cursor sumOperationsGroupByCategory(Account account, Integer[] exceptCategories) {
        Operation o = new Operation();
        Date dateBegin = o.getFirstDate(account, exceptCategories);
        Date dateEnd = o.getLastDate(account, exceptCategories);
        return sumOperationsGroupByCategory(account, dateBegin, dateEnd, exceptCategories);
    }

    /**
     * Sum all operations and group by categories for a specific account.
     * @param account
     * @param dateBegin
     * @param dateEnd
     * @param exceptCategories
     * @return A cursor corresponding to the request
     */
	public Cursor sumOperationsGroupByCategory(Account account, Date dateBegin, Date dateEnd, Integer[] exceptCategories) {
		if(account == null || account.getId() == null)
			return null;
		
		String sBeg = DatabaseHelper.formatDateForSQL(dateBegin);
		String sEnd = DatabaseHelper.formatDateForSQL(dateEnd);
		
		long nbDays = DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd);
		
		QueryBuilder qb = new QueryBuilder(new StringBuilder("SELECT "));
		qb.append("o."+OperationData.KEY_ID+", ");
		qb.append("sum("+OperationData.KEY_AMOUNT+") amountString, ");
		qb.append("sum("+OperationData.KEY_AMOUNT+") / " + nbDays + " avg, ");
		qb.append("c."+CurrencyData.KEY_CURRENCY_LINKED+", ");
		qb.append("c."+CurrencyData.KEY_SHORT_NAME+", ");
		qb.append("cat."+CategoryData.KEY_NAME+", ");
		qb.append("strftime('%d-%m-%Y', o."+OperationData.KEY_DATE+") dateString ");
		qb.append("FROM "+OperationData.TABLE_NAME+" o, "+AccountData.TABLE_NAME+" a, "+CurrencyData.TABLE_NAME+" c, "+CategoryData.TABLE_NAME+" cat ");
		qb.append("WHERE a."+AccountData.KEY_ID+"="+account.getId()+" ");
		qb.append("AND o."+OperationData.KEY_DATE+" BETWEEN '"+sBeg+"' AND '"+sEnd+"' ");
		qb.append("AND o."+OperationData.KEY_ID_ACCOUNT+"=a."+AccountData.KEY_ID+" ");
		qb.append("AND o."+OperationData.KEY_ID_CURRENCY+"=c."+CurrencyData.KEY_ID+" ");
		qb.append("AND cat."+CategoryData.KEY_ID+"=o."+OperationData.KEY_ID_CATEGORY+" ");
		
		if(exceptCategories != null && exceptCategories.length > 0) {
			StringBuilder b = new StringBuilder();
			for(int i=0; i<exceptCategories.length; i++) {
				Integer catId = exceptCategories[i];
				if(catId == null) // Verify that nothing is null in array passed
					catId = -1;
				
				b.append(catId);
				
				if(i<exceptCategories.length-1)
					b.append(",");
			}
			qb.append("AND o."+OperationData.KEY_ID_CATEGORY+" NOT IN("+b.toString()+") ");
		}
		
		qb.append("GROUP BY o."+OperationData.KEY_ID_CATEGORY+", o."+OperationData.KEY_ID_CURRENCY).append(" ");
		qb.append("ORDER BY cat."+CategoryData.KEY_NAME+" ASC ");
		
		Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(qb.getStringBuilder().toString(), null);
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
		this.linkToOperation = null;
	}
	
	@Override
	public String toString() {
		return ""+this.getId();
	}

	@Override
	public String getDefaultOrderColumn() {
		return OperationData.KEY_DATE;
	}
	
	public static boolean linkTwoOperations(Operation op1, Operation op2) {
		op1.setLinkToOperation(op2.getId());
		return op1.update();
	}
	
	@Override
	public boolean update() {
		Integer linkTo = getLinkToOperation();
		
		if(linkTo != null) {
			// Fetching linked operation
			Operation opLinked = new Operation();
			Cursor c = opLinked.fetch(linkTo);
			opLinked.toDTO(c);
			opLinked.setLinkToOperation(getId());
			opLinked.setAmount(getAmount()*(-1));
			opLinked.setDate(getDate());
			opLinked.setCurrencyValueOnCreated(getCurrencyValueOnCreated());
			opLinked.updateWithoutLink();
		}
	
		return super.update();
	}
	
	private boolean updateWithoutLink() {
		return super.update();
	}
	
	@Override
	public boolean delete() {
		Integer linkTo = getLinkToOperation();
		
		if(linkTo != null) {
			Operation opLinked = new Operation();
			Cursor c = opLinked.fetch(linkTo);
			opLinked.toDTO(c);
			opLinked.deleteWithoutLink();
		}
		
		return super.delete();
	}
	
	private boolean deleteWithoutLink() {
		return super.delete();
	}
}
