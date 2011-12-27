package org.maupu.android.tmh.database.object;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.UserData;

import android.content.ContentValues;
import android.database.Cursor;

public class User extends BaseObject {
	private static final long serialVersionUID = 1L;
	private String name;
	private String icon;
	
	public String getName() {
		return name;
	}
	public String getIcon() {
		return icon;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public ContentValues createContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(UserData.KEY_NAME, this.getName());
		cv.put(UserData.KEY_ICON, this.getIcon());
		
		return cv;
	}
	
	@Override
	public String getTableName() {
		return UserData.TABLE_NAME;
	}
	
	@Override
	public BaseObject toDTO(DatabaseHelper dbHelper, Cursor cursor) throws IllegalArgumentException {
		int idxId = cursor.getColumnIndexOrThrow(UserData.KEY_ID);
		int idxName = cursor.getColumnIndexOrThrow(UserData.KEY_NAME);
		int idxIcon = cursor.getColumnIndexOrThrow(UserData.KEY_ICON);
		
		if(! cursor.isClosed() && ! cursor.isBeforeFirst() && ! cursor.isAfterLast()) {
			super._id = cursor.getInt(idxId);
			this.setName(cursor.getString(idxName));
			this.setIcon(cursor.getString(idxIcon));
		}
		
		return super.getFromCache();
	}
	@Override
	public boolean validate() {
		return true;
	}
}
