package org.maupu.android.tmh.database.object;

import org.maupu.android.tmh.database.CategoryData;

import android.content.ContentValues;
import android.database.Cursor;

public class Category extends BaseObject {
	private static final long serialVersionUID = 1L;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public ContentValues createContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(CategoryData.KEY_NAME, this.getName());
		
		return cv;
	}

	@Override
	public String getTableName() {
		return CategoryData.TABLE_NAME;
	}
	
	@Override
	public BaseObject toDTO(Cursor cursor) throws IllegalArgumentException {
		this.reset();
		int idxId = cursor.getColumnIndexOrThrow(CategoryData.KEY_ID);
		int idxName = cursor.getColumnIndexOrThrow(CategoryData.KEY_NAME);
		
		if(! cursor.isClosed() && ! cursor.isBeforeFirst() && ! cursor.isAfterLast()) {
			this._id = cursor.getInt(idxId);
			this.setName(cursor.getString(idxName));
		}
		
		return super.getFromCache();
	}

	@Override
	public boolean validate() {
		return getName() != null && ! "".equals(getName().trim());
	}

	@Override
	public void reset() {
		super._id = null;
		this.name = null;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public String getDefaultOrderColumn() {
		return CategoryData.KEY_NAME;
	}
}