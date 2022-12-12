package org.maupu.android.tmh.database.object;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.APersistedData;
import org.maupu.android.tmh.database.cache.SimpleObjectCache;

import java.io.Serializable;

public abstract class BaseObject implements Validator, Serializable, Comparable {
    private static final long serialVersionUID = 1L;
    protected static SimpleObjectCache cache;
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

    public abstract BaseObject copy();

    /**
     * Convert database cursor to dto and set all attributes to local instance
     *
     * @param cursor
     */
    public abstract BaseObject toDTOWithDb(SQLiteDatabase db, Cursor cursor) throws IllegalArgumentException;

    public BaseObject toDTO(Cursor cursor) throws IllegalArgumentException {
        return toDTOWithDb(TmhApplication.getDatabaseHelper().getDb(), cursor);
    }

    /**
     * Method returning name of object (used in spinners for instance)
     */
    public abstract String toString();

    /**
     * Method returning default order column
     *
     * @return a String representing default order column
     */
    public abstract String getDefaultOrderColumn();

    /**
     * Method returning default order. ie. ASC or DESC
     *
     * @return a String containing order ASC or DESC
     */
    public String getDefaultOrder() {
        return "ASC";
    }

    public BaseObject getFromCache() {
        BaseObject ret = cache.getBaseDTO(SimpleObjectCache.constructKey(getTableName(), getId()));

        if (ret == null) {
            cache.addDTO(this);
            return this;
        } else {
            return ret;
        }
    }

    public boolean insert() {
        return insertWithDb(TmhApplication.getDatabaseHelper().getDb());
    }

    public boolean insertWithDb(SQLiteDatabase db) {
        if (!validate())
            return false;

        long id = db.insert(getTableName(), null, createContentValues());
        boolean b = id > 0;

        if (b) {
            // Refetch it to store all fields
            Cursor c = this.fetch((int) id);
            this.toDTO(c);
        }

        return b;
    }

    public boolean update() {
        return updateWithDb(TmhApplication.getDatabaseHelper().getDb());
    }

    public boolean updateWithDb(SQLiteDatabase db) {
        if (!validate() || getId() == null)
            return false;

        return db.update(getTableName(), createContentValues(), APersistedData.KEY_ID + "=" + getId(), null) > 0;
    }

    public boolean insertOrUpdate() {
        return insertOrUpdateWithDb(TmhApplication.getDatabaseHelper().getDb());
    }

    public boolean insertOrUpdateWithDb(SQLiteDatabase db) {
        Cursor c = fetchWithDB(db, getId());
        if (c.getCount() > 0) {
            return updateWithDb(db);
        }
        return insertWithDb(db);
    }

    public boolean delete() {
        return deleteWithDb(TmhApplication.getDatabaseHelper().getDb());
    }

    public boolean deleteWithDb(SQLiteDatabase db) {
        return db.delete(getTableName(), APersistedData.KEY_ID + "=" + getId(), null) > 0;
    }

    public int deleteAll() {
        return deleteAllWithDb(TmhApplication.getDatabaseHelper().getDb());
    }

    public int deleteAllWithDb(SQLiteDatabase db) {
        return db.delete(getTableName(), "", null);
    }

    public Cursor fetch(Integer id) {
        return fetchWithDB(TmhApplication.getDatabaseHelper().getDb(), id);
    }

    public Cursor fetchWithDB(SQLiteDatabase db, Integer id) {
        Cursor c = db.query(getTableName(), null, APersistedData.KEY_ID + "=" + id, null, null, null, null);
        if (c != null)
            c.moveToFirst();

        return c;
    }

    public Cursor fetchAll() {
        return fetchAllOrderBy(getDefaultOrderColumn(), getDefaultOrder());
    }

    public Cursor fetchAllOrderBy(String column, String order) {
        StringBuilder query = new StringBuilder("select * from ")
                .append(getTableName())
                .append(" order by ").append(column)
                .append(" ").append(order);
        //return TmhApplication.getDatabaseHelper().getDb().query(getTableName(), null, null, null, null, null, columnName);
        return TmhApplication.getDatabaseHelper().getDb().rawQuery(query.toString(), null);
    }

    public Cursor fetchAllExcept(Integer[] ids) {
        if (ids == null || ids.length == 0)
            return fetchAll();

        StringBuffer buf = new StringBuffer();
        buf.append(APersistedData.KEY_ID);
        buf.append(" NOT IN(");
        for (int i = 0; i < ids.length; i++) {
            buf.append(ids[i]);
            if (i < ids.length - 1)
                buf.append(",");
        }
        buf.append(")");
        return TmhApplication.getDatabaseHelper().getDb().query(getTableName(), null, buf.toString(), null, null, null, getDefaultOrderColumn());
    }

    public int getCount() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT count(" + APersistedData.KEY_ID + ") count FROM ");
        builder.append(getTableName());

        Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(builder.toString(), null);
        if (c == null) {
            return 0;
        } else {
            c.moveToFirst();
            int idx = c.getColumnIndexOrThrow("count");
            return c.getInt(idx);
        }
    }
}
