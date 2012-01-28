package org.maupu.android.tmh.database.object;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.AccountData;

import android.content.ContentValues;
import android.database.Cursor;

public class Account extends BaseObject {
	private static final long serialVersionUID = 1L;
	private String name;
	private String icon;
	private Currency currency;

	public String getName() {
		return name;
	}
	public String getIcon() {
		return icon;
	}
	public Currency getCurrency() {
		return currency;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public ContentValues createContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(AccountData.KEY_NAME, this.getName());
		cv.put(AccountData.KEY_ICON, this.getIcon());
		
		if(this.getCurrency() != null)
			cv.put(AccountData.KEY_ID_CURRENCY, this.getCurrency().getId());

		return cv;
	}

	@Override
	public String getTableName() {
		return AccountData.TABLE_NAME;
	}

	@Override
	public BaseObject toDTO(DatabaseHelper dbHelper, Cursor cursor) throws IllegalArgumentException {
		this.reset();
		int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
		int idxName = cursor.getColumnIndexOrThrow(AccountData.KEY_NAME);
		int idxIcon = cursor.getColumnIndexOrThrow(AccountData.KEY_ICON);
		int idxCurrency = cursor.getColumnIndexOrThrow(AccountData.KEY_ID_CURRENCY);

		if(! cursor.isClosed() && ! cursor.isBeforeFirst() && ! cursor.isAfterLast()) {
			super._id = cursor.getInt(idxId);
			this.setName(cursor.getString(idxName));
			this.setIcon(cursor.getString(idxIcon));

			Currency c = new Currency();
			Cursor cursorCurrency = c.fetch(dbHelper, cursor.getInt(idxCurrency));
			c.toDTO(dbHelper, cursorCurrency);
			this.setCurrency(c);
		}

		return super.getFromCache();
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public void reset() {
		super._id = null;
		this.icon = null;
		this.name = null;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	@Override
	public String getDefaultOrderColumn() {
		return AccountData.KEY_NAME;
	}
}
