package org.maupu.android.tmh.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.util.ImageUtil;

import java.util.HashMap;


public class AccountData extends APersistedData {
    public static final String KEY_NAME = "name";
    @Deprecated
    private static final String KEY_ICON = "icon";
    public static final String KEY_ICON_BYTES = "iconBytes";
    public static final String KEY_ID_CURRENCY = "idCurrency";
    public static final String KEY_BALANCE = "balance";

    public static final String TABLE_NAME = "account";
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME;
    private static final String CREATE_TABLE_OPTS = KEY_ID + " INTEGER primary key autoincrement," +
            KEY_NAME + " TEXT NOT NULL," +
            KEY_ICON_BYTES + " BLOB," +
            KEY_ID_CURRENCY + " INTEGER NOT NULL, " +
            KEY_BALANCE + " REAL NOT NULL DEFAULT 0";

    public AccountData() {
        super(TABLE_NAME, CREATE_TABLE + "(" + CREATE_TABLE_OPTS + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 11 && oldVersion < 11)
            upgrade11(db, oldVersion, newVersion);
        else if (newVersion == 15 && oldVersion < 15)
            upgrade15(db, oldVersion, newVersion);
    }

    private void upgrade11(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion != 11 || oldVersion >= 11)
            return;

        // Adding balance to account and upgrade all account balances
        db.beginTransaction();
        try {
            // Alter table, add column and set default to 0
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_BALANCE + " REAL NOT NULL DEFAULT 0");

            /* First try, direct ? -> not working, fffuuuuuu */
            // Upgrading balance
            /* SELECT case when sum(o.amount/o.currencyValue) IS NULL then 0 else sum(o.amount/o.currencyValue) end as balance from account a LEFT JOIN operation o ON a._id=o.idAccount GROUP BY o.idAccount; */
            /* update account set balance=(SELECT case when sum(o.amount/o.currencyValue) IS NULL then 0 else sum(o.amount/o.currencyValue) end as balance from operation o WHERE account._id=o.idAccount GROUP BY o.idAccount); */
            /* */


            // Compute all balances for each existing operations (currencyValue is the value of currency operation when inserted)
            // If operation currency is the same as account currency, not ratio is done
            // Example : for a vietnam account (dong), all operation for this account are summed directly without changing to euro
            // Because all operations are already dong. So Balance for this account is in dong
            String query = "select a.*, case when o.amount IS NULL then 0 else (case when o.idCurrency=a.idCurrency then sum(o.amount) else sum(o.amount/o.currencyValue) end) end as _balance from account a LEFT JOIN operation o ON a._id=o.idAccount GROUP BY a._id;";
            Cursor cursor = db.rawQuery(query, null);
            cursor.moveToFirst();

            do {
                int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID_CURRENCY);
                int idxBalance = cursor.getColumnIndexOrThrow("_balance");

                ContentValues args = new ContentValues();
                args.put(AccountData.KEY_BALANCE, cursor.getInt(idxBalance));
                db.update(AccountData.TABLE_NAME, args, AccountData.KEY_ID + "=" + cursor.getInt(idxId), null);
            } while (cursor.moveToNext());

            db.setTransactionSuccessful();
        } catch (SQLException sqle) {
            // Something wrong occurred
            sqle.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private void upgrade15(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion != 15 || oldVersion >= 15)
            return;

        // Copy old icons
        Cursor accounts = new Account().fetchAll();
        accounts.moveToFirst();
        HashMap<Integer, Bitmap> icons = new HashMap<>();
        for (int i = 0; i < accounts.getCount(); i++) {
            int idxId = accounts.getColumnIndexOrThrow(AccountData.KEY_ID);
            int idxIcon = accounts.getColumnIndexOrThrow(AccountData.KEY_ICON);
            int id = accounts.getInt(idxId);
            Bitmap icon = ImageUtil.getBitmapIcon(TmhApplication.getAppContext(), accounts.getString(idxIcon));
            if (icon != null)
                icons.put(id, icon);
            accounts.moveToNext();
        }

        db.beginTransaction();
        try {
            // No DROP COLUMN in android sqlite, so need a copy of the whole table to a new one
            String backupTable = TABLE_NAME + "_backup";
            db.execSQL("CREATE TEMPORARY TABLE " + backupTable + "(" + CREATE_TABLE_OPTS + ")");
            db.execSQL("INSERT INTO " + backupTable + " SELECT * FROM " + TABLE_NAME);
            db.execSQL("DROP TABLE " + TABLE_NAME);
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + CREATE_TABLE_OPTS + ")");
            db.execSQL("INSERT INTO " + TABLE_NAME + " SELECT * FROM " + backupTable);
            db.execSQL("DROP TABLE " + backupTable);

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            db.endTransaction();
        }

        accounts = new Account().fetchAll();
        accounts.moveToFirst();
        for (int i = 0; i < accounts.getCount(); i++) {
            Account acc = new Account();
            acc.toDTO(accounts);
            Bitmap icon = icons.get(acc.getId());
            acc.setIconBytes(ImageUtil.getBytesFromBitmap(icon));
            acc.update();
            accounts.moveToNext();
        }
    }
}
