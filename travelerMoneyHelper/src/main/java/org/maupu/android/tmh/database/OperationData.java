package org.maupu.android.tmh.database;

import android.database.sqlite.SQLiteDatabase;


public class OperationData extends APersistedData {
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_DATE = "date";
    public static final String KEY_IS_CASH = "isCash";
    public static final String KEY_ID_ACCOUNT = "idAccount";
    public static final String KEY_ID_CATEGORY = "idCategory";
    public static final String KEY_ID_CURRENCY = "idCurrency";
    // Store also a copy of currency value for each operation
    public static final String KEY_CURRENCY_VALUE = "currencyValue";
    public static final String KEY_LINK_TO = "idOperationLinked";
    public static final String KEY_GROUP_UUID = "groupUUID";

    public static final String TABLE_NAME = "operation";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    KEY_ID + " INTEGER primary key autoincrement," +
                    KEY_AMOUNT + " REAL NOT NULL," +
                    KEY_DESCRIPTION + " TEXT," +
                    KEY_DATE + " TEXT NOT NULL," +
                    KEY_IS_CASH + " INTEGER NOT NULL DEFAULT 1," +
                    KEY_ID_ACCOUNT + " INTEGER NOT NULL," +
                    KEY_ID_CATEGORY + " INTEGER NOT NULL," +
                    KEY_ID_CURRENCY + " INTEGER NOT NULL," +
                    KEY_CURRENCY_VALUE + " REAL," +
                    KEY_LINK_TO + " INTEGER," +
                    KEY_GROUP_UUID + " TEXT DEFAULT NULL" +
                    ")";

    public OperationData() {
        super(TABLE_NAME, CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion >= 1 && newVersion < 8) {
            // Adding column for operation linking
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_LINK_TO + " INTEGER");
        }

        if (oldVersion < 16 && newVersion >= 16) {
            // Adding column isCash to determine if an operation is paid by cash or using credit/debit card
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_IS_CASH + " INTEGER NOT NULL DEFAULT 1");
        }

        if (oldVersion < 19 && newVersion >= 19) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_GROUP_UUID + " TEXT DEFAULT NULL");
        }
    }
}
