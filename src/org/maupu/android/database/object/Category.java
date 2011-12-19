package org.maupu.android.database.object;

import org.maupu.android.database.CategoryData;
import org.maupu.android.database.DatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;

public class Category extends BaseObject {
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
	public BaseObject toDTO(DatabaseHelper dbHelper, Cursor cursor) throws IllegalArgumentException {
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
		return ! getName().trim().equals("");
	}
}