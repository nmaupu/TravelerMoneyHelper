package org.maupu.android.tmh.database.object;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.ui.AccountBalance;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.ImageUtil;
import org.maupu.android.tmh.util.QueryBuilder;
import org.maupu.android.tmh.util.TmhLogger;

public class Account extends BaseObject {
    private static final long serialVersionUID = 1L;
    private String name;
    private byte[] iconBytes;
    private Currency currency;
    public Double balance;

    public String getName() {
        return name;
    }

    public byte[] getIconBytes() {
        return iconBytes;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Double getBalance() {
        return balance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIconBytes(byte[] iconBytes) {
        this.iconBytes = iconBytes;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setBalance(Double b) {
        balance = b;
    }

    public void addToBalance(Double b) {
        balance += b;
    }

    public Bitmap getIcon() {
        byte[] image = getIconBytes();

        if (image == null || image.length == 0)
            return ImageUtil.drawableToBitmap(TmhApplication.getAppContext().getDrawable(R.drawable.tmh_icon_48));

        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public BaseObject copy() {
        Account a = new Account();
        a._id = super.getId();
        a.setName(name);
        a.setCurrency((Currency) currency.copy());
        a.setBalance(balance);
        if (iconBytes != null)
            a.setIconBytes(iconBytes.clone());

        return a;
    }

    public ContentValues createContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(AccountData.KEY_NAME, this.getName());
        cv.put(AccountData.KEY_ICON_BYTES, this.getIconBytes());

        if (this.getCurrency() != null)
            cv.put(AccountData.KEY_ID_CURRENCY, this.getCurrency().getId());

        return cv;
    }

    @Override
    public String getTableName() {
        return AccountData.TABLE_NAME;
    }

    @Override
    public boolean update() {
        // Force current account to be fetch again if needed
        StaticData.invalidateCurrentAccount();
        return super.update();
    }

    public BaseObject toDTOWithDb(SQLiteDatabase db, Cursor cursor) throws IllegalArgumentException {
        this.reset();
        int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
        int idxName = cursor.getColumnIndexOrThrow(AccountData.KEY_NAME);
        int idxIconBytes = cursor.
                getColumnIndexOrThrow(AccountData.KEY_ICON_BYTES);
        int idxCurrency = cursor.getColumnIndexOrThrow(AccountData.KEY_ID_CURRENCY);
        int idxBalance = cursor.getColumnIndexOrThrow(AccountData.KEY_BALANCE);

        if (!cursor.isClosed() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
            super._id = cursor.getInt(idxId);
            this.setName(cursor.getString(idxName));
            this.setIconBytes(cursor.getBlob(idxIconBytes));
            this.setBalance(cursor.getDouble(idxBalance));

            Currency c = new Currency();
            Cursor cursorCurrency = c.fetchWithDB(db, cursor.getInt(idxCurrency));
            c.toDTOWithDb(db, cursorCurrency);
            cursorCurrency.close();
            this.setCurrency(c);
        }

        return super.getFromCache();
    }

    /*
     * Get current account balance
     */
    public Cursor getComputedBalanceCursor(boolean onlyCash) {
        if (getId() == null || getId() < 0)
            return null;

        QueryBuilder b = new QueryBuilder(new StringBuilder());
        b.append("SELECT ")
                .append("a." + AccountData.KEY_ID).append(",")
                .append("a." + AccountData.KEY_ID_CURRENCY).append(" account_" + AccountData.KEY_ID_CURRENCY).append(",")
                .append("o." + OperationData.KEY_ID).append(" operation_" + OperationData.KEY_ID).append(",")
                .append("o." + OperationData.KEY_AMOUNT + " operation_" + OperationData.KEY_AMOUNT).append(",")
                .append("o." + OperationData.KEY_ID_CURRENCY + " operation_" + OperationData.KEY_ID_CURRENCY).append(",")
                .append("o." + OperationData.KEY_CURRENCY_VALUE + " operation_" + OperationData.KEY_CURRENCY_VALUE).append(",")
                .append("o." + OperationData.KEY_IS_CASH + " operation_" + OperationData.KEY_IS_CASH).append(",")
                .append("c." + CurrencyData.KEY_SHORT_NAME + " currency_" + CurrencyData.KEY_SHORT_NAME);

        b.append(" FROM ")
                .append(AccountData.TABLE_NAME).append(" a,")
                .append(OperationData.TABLE_NAME).append(" o,")
                .append(CurrencyData.TABLE_NAME).append(" c");

        b.append(" WHERE ")
                .append("a." + AccountData.KEY_ID + "=" + getId()).append(" AND ")
                .append("o." + OperationData.KEY_ID_ACCOUNT + "=a." + AccountData.KEY_ID).append(" AND ")
                .append("c." + CurrencyData.KEY_ID + "=o." + OperationData.KEY_ID_CURRENCY);
        if (onlyCash)
            b.append(" AND o." + OperationData.KEY_IS_CASH + "=1");


        TmhLogger.d(Account.class, "Account.getBalance" + b.getStringBuilder().toString());
        Cursor c = TmhApplication.getDatabaseHelper().getDb().rawQuery(b.getStringBuilder().toString(), null);
        c.moveToFirst();

        return c;
    }

    public AccountBalance getComputedBalance(boolean onlyCash) {
        Cursor cursor = getComputedBalanceCursor(onlyCash);
        AccountBalance accountBalance = new AccountBalance(this);

        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                int idxAmount = cursor.getColumnIndexOrThrow("operation_" + OperationData.KEY_AMOUNT);
                int idxIdCurrency = cursor.getColumnIndexOrThrow("operation_" + OperationData.KEY_ID_CURRENCY);
                int idxRate = cursor.getColumnIndexOrThrow("operation_" + OperationData.KEY_CURRENCY_VALUE);

                Double amount = cursor.getDouble(idxAmount);
                Double rate = cursor.getDouble(idxRate);
                int idOpCurrency = cursor.getInt(idxIdCurrency);

                Double b = accountBalance.get(idOpCurrency) == null ? 0d : accountBalance.get(idOpCurrency);

                accountBalance.put(idOpCurrency, b + amount);
                accountBalance.setBalanceRate(accountBalance.getBalanceRate() + (amount / rate));

                cursor.moveToNext();
            }
        }

        return accountBalance;
    }

    public static boolean forceUpdateBalance(Integer... ids) {
        boolean res = false;

        for (Integer id : ids) {
            Account dummy = new Account();
            dummy.fetch(id);

            res = dummy.forceUpdateBalance() && res;
        }

        return res;
    }

    public boolean forceUpdateBalance() {
        if (this._id == null || this._id < 0)
            return false;

        boolean res = false;

        StringBuilder query = new StringBuilder();
        query.append("SELECT ")
                .append("a._id, case when o.amount IS NULL then 0 else ")
                .append("case when o.idCurrency=a.idCurrency then sum(o.amount) ")
                .append("else sum(o.amount/o.currencyValue) end) end as balance ")
                .append("from account a ")
                .append("LEFT JOIN operation o ON a._id=o.idAccount ")
                .append("WHERE a._id=? ")
                .append("GROUP BY o.idAccount;");


        try {
            TmhApplication.getDatabaseHelper().getDb().beginTransaction();
            Cursor c = TmhApplication.getDatabaseHelper().getDb()
                    .rawQuery(query.toString(), new String[]{String.valueOf(this._id)});

            if (c == null || c.getCount() == 0)
                return false;

            c.moveToFirst();

            // Getting computed balance and update account balance
            int idxBalance = c.getColumnIndexOrThrow("balance");
            Double bal = c.getDouble(idxBalance);

            // Refetch entire object if only id is filled
            //c = this.fetch(this._id);
            //this.toDTO(c);

            this.setBalance(bal);
            this.update();

            TmhApplication.getDatabaseHelper().getDb().setTransactionSuccessful();
            res = true;
        } finally {
            TmhApplication.getDatabaseHelper().getDb().endTransaction();
        }

        return res;
    }


    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void reset() {
        super._id = null;
        this.name = null;
        this.iconBytes = null;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public String getDefaultOrderColumn() {
        return AccountData.KEY_NAME;
    }

    @Override
    public int compareTo(Object another) {
        Account a = (Account) another;
        return name.compareTo(a.getName());
    }
}
