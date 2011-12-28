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
		
		return dbHelper.getDb().insert(getTableName(), null, createContentValues()) > 0;
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
		return dbHelper.getDb().query(getTableName(), null, null, null, null, null, null);
	}
}
