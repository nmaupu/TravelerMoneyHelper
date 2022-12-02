package org.maupu.android.tmh.database;

import android.database.sqlite.SQLiteDatabase;

import org.maupu.android.tmh.core.TmhApplication;

public class StaticPrefsData extends APersistedData {
    public static final String KEY_WITHDRAWAL_CATEGORY = "withdrawalCategory";
    public static final String KEY_CURRENT_SELECTED_CATEGORY = "currentSelectedCategory";
    public static final String KEY_STATS_DATE_BEG = "statsDateBeg";
    public static final String KEY_STATS_DATE_END = "statsDateEnd";

    public static final String TABLE_NAME = "static_prefs";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    KEY_ID + " INTEGER primary key autoincrement," +
                    KEY_WITHDRAWAL_CATEGORY + " INTEGER," +
                    KEY_CURRENT_SELECTED_CATEGORY + " INTEGER," +
                    KEY_STATS_DATE_BEG + " TEXT NULL," +
                    KEY_STATS_DATE_END + " TEXT NULL" +
                    ")";

    public StaticPrefsData() { super(TABLE_NAME, CREATE_TABLE); }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Creating table if not exist on current db
        if(newVersion == 14 && oldVersion < 14) {
            this.onCreate(db);
        }
    }
}
