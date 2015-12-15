package org.maupu.android.tmh.database.object;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.OperationData;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Category extends BaseObject {
	private static final long serialVersionUID = 1L;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    @Override
    public BaseObject copy() {
        Category c = new Category();
        c._id = super.getId();
        c.setName(name);
        return c;
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
	public BaseObject toDTOWithDb(SQLiteDatabase db, Cursor cursor) throws IllegalArgumentException {
		this.reset();
		int idxId = cursor.getColumnIndexOrThrow(CategoryData.KEY_ID);
		int idxName = cursor.getColumnIndexOrThrow(CategoryData.KEY_NAME);
		
		if(! cursor.isClosed() && ! cursor.isBeforeFirst() && ! cursor.isAfterLast()) {
			this._id = cursor.getInt(idxId);
			this.setName(cursor.getString(idxName));
		}
		
		return super.getFromCache();
	}
	
	public Cursor fetchAllCategoriesUsedByAccountOperations(int accountId) {
		StringBuffer query = new StringBuffer(
                "SELECT DISTINCT cat." + CategoryData.KEY_ID + ", cat." + CategoryData.KEY_NAME + " ")
				.append("FROM " + CategoryData.TABLE_NAME + " cat ")
				.append("LEFT JOIN " + OperationData.TABLE_NAME + " o ON o." + OperationData.KEY_ID_CATEGORY + "=cat." + CategoryData.KEY_ID + " ")
				.append("LEFT JOIN " + AccountData.TABLE_NAME + " acc ON acc." + AccountData.KEY_ID + "=o." + OperationData.KEY_ID_ACCOUNT + " ")
				.append("WHERE acc." + AccountData.KEY_ID + "=? ")
				.append("ORDER BY cat." + CategoryData.KEY_NAME);
		
		Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(query.toString(), new String[]{String.valueOf(accountId)});
		c.moveToFirst();
		return c;
	}
	
	public Cursor fetchByName(String name) {
		StringBuffer query = new StringBuffer();
		query.append("SELECT " + CategoryData.KEY_ID + ", " + CategoryData.KEY_NAME + " FROM ")
			.append(CategoryData.TABLE_NAME)
			.append(" WHERE ")
			.append(CategoryData.KEY_NAME)
			.append(" = '")
			.append(name)
			.append("'");
		
		Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(query.toString(), null);
		c.moveToFirst();
		return c;
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