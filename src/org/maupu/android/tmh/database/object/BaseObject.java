package org.maupu.android.tmh.database.object;

import java.io.Serializable;

import org.maupu.android.tmh.database.APersistedData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.cache.SimpleObjectCache;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class BaseObject implements Validator, Serializable {
	private static final long serialVersionUID = 1L;
	static SimpleObjectCache cache;
	protected Integer _id;

	/**
	 * Default constructor
	 */
	public BaseObject() {
		cache = new SimpleObjectCache();
	}

	public Integer getId() {
		return _id;
	}

	public abstract ContentValues createContentValues();
	public abstract String getTableName();
	public abstract void reset();

	/**
	 * Convert database cursor to dto and set all attributes to local instance
	 * @param cursor
	 */
	public abstract BaseObject toDTO(final DatabaseHelper dbHelper, Cursor cursor) throws IllegalArgumentException;
	
	/**
	 * Method returning name of object (used in spinners for instance)
	 */
	public abstract String toString();
	
	/**
	 * Method returning default order column
	 * @return a String representing default order column
	 */
	public abstract String getDefaultOrderColumn();

	public BaseObject getFromCache() {
		BaseObject ret = cache.getBaseDTO(SimpleObjectCache.constructKey(getTableName(), getId()));

		if(ret == null) {
			cache.addDTO(this);
			return this;
		} else {
			return ret;
		}
	}

	public boolean insert(final DatabaseHelper dbHelper) {
		if(! validate())
			return false;
		
		long id = dbHelper.getDb().insert(getTableName(), null, createContentValues());
		boolean b = id > 0;
		
		if(b) {
			// Refetch it to store all fields
			Cursor c = this.fetch(dbHelper, (int)id);
			this.toDTO(dbHelper, c);
		}
		
		return b;
	}

	public boolean update(final DatabaseHelper dbHelper) {
		if(! validate() || getId() == null)
			return false;
		
		return dbHelper.getDb().update(getTableName(), createContentValues(), APersistedData.KEY_ID+"="+getId(), null) > 0;
	}

	public boolean delete(final DatabaseHelper dbHelper) {
		return dbHelper.getDb().delete(getTableName(), APersistedData.KEY_ID+"="+getId(), null) > 0;
	}

	public Cursor fetch(final DatabaseHelper dbHelper, Integer id) {
		Cursor c = dbHelper.getDb().query(getTableName(), null, APersistedData.KEY_ID+"="+id, null, null, null, null);
		if(c != null)
			c.moveToFirst();

		return c;
	}

	public Cursor fetchAll(final DatabaseHelper dbHelper) {
		return dbHelper.getDb().query(getTableName(), null, null, null, null, null, getDefaultOrderColumn());
	}
	
	public int getCount(final DatabaseHelper dbHelper) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT count("+APersistedData.KEY_ID+") count FROM ");
		builder.append(getTableName());
		
		Cursor c = dbHelper.getDb().rawQuery(builder.toString(), null);
		if(c == null) {
			return 0;
		} else {
			c.moveToFirst();
			int idx = c.getColumnIndexOrThrow("count");
			return c.getInt(idx);
		}
	}
}
