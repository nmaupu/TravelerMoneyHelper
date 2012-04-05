package org.maupu.android.tmh.database.object;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.ui.AccountBalance;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.QueryBuilder;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

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
	public boolean update() {
		// Force current account to be fetch again if needed
		StaticData.invalidateCurrentAccount();
		return super.update();
	}

	@Override
	public BaseObject toDTO(Cursor cursor) throws IllegalArgumentException {
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
			Cursor cursorCurrency = c.fetch(cursor.getInt(idxCurrency));
			c.toDTO(cursorCurrency);
			this.setCurrency(c);
		}

		return super.getFromCache();
	}
	
	/*
	 * Get current account balance
	 */
	public Cursor getBalanceCursor() {
		if(getId() == null || getId() < 0)
			return null;
		
		QueryBuilder b = new QueryBuilder(new StringBuilder());
		b.append("SELECT ")
		.append("a."+AccountData.KEY_ID).append(",")
		.append("a."+AccountData.KEY_ID_CURRENCY).append(" account_"+AccountData.KEY_ID_CURRENCY).append(",")
		.append("o."+OperationData.KEY_ID).append(" operation_"+OperationData.KEY_ID).append(",")
		.append("o."+OperationData.KEY_AMOUNT+" operation_"+OperationData.KEY_AMOUNT).append(",")
		.append("o."+OperationData.KEY_ID_CURRENCY+" operation_"+OperationData.KEY_ID_CURRENCY).append(",")
		.append("o."+OperationData.KEY_CURRENCY_VALUE+" operation_"+OperationData.KEY_CURRENCY_VALUE).append(",")
		.append("c."+CurrencyData.KEY_SHORT_NAME+" currency_"+CurrencyData.KEY_SHORT_NAME);
		
		b.append(" FROM ")
		.append(AccountData.TABLE_NAME).append(" a,")
		.append(OperationData.TABLE_NAME).append(" o,")
		.append(CurrencyData.TABLE_NAME).append(" c");
		
		b.append(" WHERE ")
		.append("a."+AccountData.KEY_ID+"="+getId()).append(" AND ")
		.append("o."+OperationData.KEY_ID_ACCOUNT+"=a."+AccountData.KEY_ID).append(" AND ")
		.append("c."+CurrencyData.KEY_ID+"=o."+OperationData.KEY_ID_CURRENCY);
		
		Log.d("Account.getBalance", b.getStringBuilder().toString());
		Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(b.getStringBuilder().toString(), null); 
		c.moveToFirst();
		
		return c;
	}
	
	public AccountBalance getBalance() {
		Cursor cursor = getBalanceCursor();
		AccountBalance accountBalance = new AccountBalance(this);
		
		if(cursor != null) {
			for(int i=0; i<cursor.getCount(); i++) {
				int idxAmount = cursor.getColumnIndexOrThrow("operation_"+OperationData.KEY_AMOUNT);
				int idxIdCurrency = cursor.getColumnIndexOrThrow("operation_"+OperationData.KEY_ID_CURRENCY);
				int idxRate = cursor.getColumnIndexOrThrow("operation_"+OperationData.KEY_CURRENCY_VALUE);
				
				Double amount = cursor.getDouble(idxAmount);
				Double rate = cursor.getDouble(idxRate);
				int idOpCurrency = cursor.getInt(idxIdCurrency);
				
				Double b = accountBalance.get(idOpCurrency) == null ? 0d : accountBalance.get(idOpCurrency);
				
				accountBalance.put(idOpCurrency, b+amount);
				accountBalance.setBalanceRate(accountBalance.getBalanceRate() + (amount / rate));
				
				cursor.moveToNext();
			}
		}
		
		return accountBalance;
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
