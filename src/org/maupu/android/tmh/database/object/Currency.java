package org.maupu.android.tmh.database.object;

import java.util.Date;

import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.DatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;

public class Currency extends BaseObject {
	private static final long serialVersionUID = 1L;
	private String longName;
	private String shortName;
	private String icon;
	private Float tauxEuro;
	private Date lastUpdate;
	
	public String getLongName() {
		return longName;
	}
	public String getShortName() {
		return shortName;
	}
	public String getIcon() {
		return icon;
	}
	public Float getTauxEuro() {
		return tauxEuro;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLongName(String longName) {
		this.longName = longName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public void setTauxEuro(Float tauxEuro) {
		this.tauxEuro = tauxEuro;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public ContentValues createContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(CurrencyData.KEY_LONG_NAME, this.getLongName());
		cv.put(CurrencyData.KEY_SHORT_NAME, this.getShortName());
		cv.put(CurrencyData.KEY_ICON, this.getIcon());
		cv.put(CurrencyData.KEY_TAUX_EURO, this.getTauxEuro());
		if(this.getLastUpdate() != null) {
			cv.put(CurrencyData.KEY_LAST_UPDATE, DatabaseHelper.formatDateForSQL(this.getLastUpdate()));
		}
		
		return cv;
	}
	
	@Override
	public String getTableName() {
		return CurrencyData.TABLE_NAME;
	}
	
	@Override
	public BaseObject toDTO(DatabaseHelper dbHelper, Cursor cursor) throws IllegalArgumentException {
		int idxId = cursor.getColumnIndexOrThrow(CurrencyData.KEY_ID);
		int idxLongName = cursor.getColumnIndexOrThrow(CurrencyData.KEY_LONG_NAME);
		int idxShortName = cursor.getColumnIndexOrThrow(CurrencyData.KEY_SHORT_NAME);
		int idxIcon = cursor.getColumnIndexOrThrow(CurrencyData.KEY_ICON);
		int idxTauxEuro = cursor.getColumnIndexOrThrow(CurrencyData.KEY_TAUX_EURO);
		int idxLastUpdate = cursor.getColumnIndexOrThrow(CurrencyData.KEY_LAST_UPDATE);
		
		if(! cursor.isClosed() && ! cursor.isBeforeFirst() && ! cursor.isAfterLast()) {
			this._id = cursor.getInt(idxId);
			this.setLongName(cursor.getString(idxLongName));
			this.setShortName(cursor.getString(idxShortName));
			this.setIcon(cursor.getString(idxIcon));
			this.setTauxEuro(cursor.getFloat(idxTauxEuro));
			String sDate = cursor.getString(idxLastUpdate);
			if(sDate != null) {
				this.setLastUpdate(DatabaseHelper.toDate(sDate));
			}
		}
		
		return super.getFromCache();
	}
	@Override
	public boolean validate() {
		return true;
	}
}
