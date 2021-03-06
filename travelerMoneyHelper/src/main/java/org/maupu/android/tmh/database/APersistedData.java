package org.maupu.android.tmh.database;

import org.maupu.android.tmh.core.TmhApplication;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class APersistedData extends SQLiteOpenHelper {
	public static final String KEY_ID="_id";
	protected String createQuery;
	protected String tableName;
	
	public APersistedData(String tableName, String createQuery) {
		super(TmhApplication.getAppContext(), DatabaseHelper.getPreferredDatabaseName(), null, DatabaseHelper.DATABASE_VERSION);
		this.createQuery = createQuery;
		this.tableName = tableName;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createQuery);
	}
}
