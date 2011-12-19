package org.maupu.android.database.object;

import java.util.Date;

import org.maupu.android.database.DatabaseHelper;
import org.maupu.android.database.ExpenseData;

import android.content.ContentValues;
import android.database.Cursor;

public class Expense extends BaseObject {
	private Float amount;
	private String description;
	private Date date;
	private User user;
	private Category category;
	private Currency currency;
	
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
	public void setUser(User user) {
		this.user = user;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public void setCurrency(Currency currency) {
		this.currency = currency;
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
		
		return cv;
	}
	
	@Override
	public String getTableName() {
		return ExpenseData.TABLE_NAME;
	}
	
	@Override
	public BaseObject toDTO(DatabaseHelper dbHelper, Cursor cursor) throws IllegalArgumentException {
		int idxId = cursor.getColumnIndexOrThrow(ExpenseData.KEY_ID);
		int idxAmount = cursor.getColumnIndexOrThrow(ExpenseData.KEY_AMOUNT);
		int idxDesctipion = cursor.getColumnIndexOrThrow(ExpenseData.KEY_DESCRIPTION);
		int idxDate = cursor.getColumnIndexOrThrow(ExpenseData.KEY_DATE);
		int idxUser = cursor.getColumnIndexOrThrow(ExpenseData.KEY_ID_USER);
		int idxCategory = cursor.getColumnIndexOrThrow(ExpenseData.KEY_ID_CATEGORY);
		int idxCurrency = cursor.getColumnIndexOrThrow(ExpenseData.KEY_ID_CURRENCY);
		
		User user = new User();
		Category category = new Category();
		Currency currency = new Currency();
		
		if(! cursor.isClosed() && ! cursor.isBeforeFirst() && ! cursor.isAfterLast()) {
			this._id = cursor.getInt(idxId);
			this.setAmount(cursor.getFloat(idxAmount));
			this.setDescription(cursor.getString(idxDesctipion));
			this.setDate(new Date(cursor.getString(idxDate)));
			user = (User)user.toDTO(dbHelper, user.fetch(dbHelper, cursor.getInt(idxUser)));
			category = (Category)category.toDTO(dbHelper, category.fetch(dbHelper, cursor.getInt(idxCategory)));
			currency = (Currency)currency.toDTO(dbHelper, currency.fetch(dbHelper, cursor.getInt(idxCurrency)));
			
			this.setUser(user);
			this.setCategory(category);
			this.setCurrency(currency);
		}
		
		return super.getFromCache();
	}
	
	public Cursor fetchAll(final DatabaseHelper dbHelper) {
		String query = "select e._id _id, u.name user, ca.name category, c.TauxEuro*e.amount tauxEuro, e.amount||' '||c.shortName amount, strftime('%d-%m-%Y', e.date) date "+
				"from category as ca, user as u, expense as e, currency as c "+
				"where e.idCategory=ca._id and e.idUser=u._id and e.idCurrency=c._id";
		return dbHelper.getDb().rawQuery(query, null);
	}
	@Override
	public boolean validate() {
		return true;
	}
}
