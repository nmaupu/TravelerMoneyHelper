package org.maupu.android.tmh.database.object;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.StaticPrefsData;
import org.maupu.android.tmh.ui.StaticData;

import java.util.Date;

public class StaticPrefs extends BaseObject {
    private static final long serialVersionUID = 1L;
    private Category withdrawalCategory;
    private Category currentSelectedCategory;
    private Date statsDateBeg;
    private Date statsDateEnd;

    public Category getWithdrawalCategory() {
        return withdrawalCategory;
    }
    public Category getCurrentSelectedCategory() {
        return currentSelectedCategory;
    }
    public Date getStatsDateBeg() {
        return statsDateBeg;
    }
    public Date getStatsDateEnd() {
        return statsDateEnd;
    }

    public void setWithdrawalCategory(Category withdrawalCategory) {
        this.withdrawalCategory = withdrawalCategory;
    }
    public void setCurrentSelectedCategory(Category currentSelectedCategory) {
        this.currentSelectedCategory = currentSelectedCategory;
    }
    public void setStatsDateBeg(Date statsDateBeg) {
        this.statsDateBeg = statsDateBeg;
    }
    public void setStatsDateEnd(Date statsDateEnd) {
        this.statsDateEnd = statsDateEnd;
    }

    @Override
    public BaseObject copy() {
        StaticPrefs sp = new StaticPrefs();
        sp._id = super.getId();
        sp.setCurrentSelectedCategory((Category)currentSelectedCategory.copy());
        sp.setWithdrawalCategory((Category)withdrawalCategory.copy());
        sp.setStatsDateBeg((Date)statsDateBeg.clone());
        sp.setStatsDateEnd((Date)statsDateEnd.clone());
        return sp;
    }

    @Override
    public ContentValues createContentValues() {
        ContentValues cv = new ContentValues();

        if(this.getWithdrawalCategory() != null) {
            cv.put(StaticPrefsData.KEY_WITHDRAWAL_CATEGORY, this.getWithdrawalCategory().getId());
        }
        if(this.getCurrentSelectedCategory() != null) {
            cv.put(StaticPrefsData.KEY_WITHDRAWAL_CATEGORY, this.getCurrentSelectedCategory().getId());
        }
        if(this.getStatsDateBeg() != null) {
            cv.put(StaticPrefsData.KEY_STATS_DATE_BEG, DatabaseHelper.formatDateForSQL(this.getStatsDateBeg()));
        }
        if(this.getStatsDateEnd() != null) {
            cv.put(StaticPrefsData.KEY_STATS_DATE_BEG, DatabaseHelper.formatDateForSQL(this.getStatsDateEnd()));
        }

        return cv;
    }

    @Override
    public String getTableName() { return StaticPrefsData.TABLE_NAME; }

    @Override
    public BaseObject toDTOWithDb(SQLiteDatabase db, Cursor cursor) throws IllegalArgumentException {
        this.reset();
        int idxId = cursor.getColumnIndexOrThrow(StaticPrefsData.KEY_ID);
        int idxWithdrawalCategory = cursor.getColumnIndexOrThrow(StaticPrefsData.KEY_WITHDRAWAL_CATEGORY);
        int idxCurrentSelectedCategory = cursor.getColumnIndexOrThrow(StaticPrefsData.KEY_CURRENT_SELECTED_CATEGORY);
        int idxStatsDateBeg = cursor.getColumnIndexOrThrow(StaticPrefsData.KEY_STATS_DATE_BEG);
        int idxStatsDateEnd = cursor.getColumnIndexOrThrow(StaticPrefsData.KEY_STATS_DATE_END);

        if (!cursor.isClosed() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
            this._id = cursor.getInt(idxId);

            int idWithdrawalCat;
            try {
                idWithdrawalCat = cursor.getInt(idxWithdrawalCategory);
                Category withdrawalCat = new Category();
                Cursor cursorWithdrawalCat = withdrawalCat.fetchWithDB(db, idWithdrawalCat);
                withdrawalCat.toDTOWithDb(db, cursorWithdrawalCat);
                cursorWithdrawalCat.close();
                this.setWithdrawalCategory(withdrawalCat);
            } catch (Exception e){
                this.setWithdrawalCategory(null);
            }


            int idCurrentSelectedCat;
            try {
                idCurrentSelectedCat = cursor.getInt(idxCurrentSelectedCategory);
                Category currentSelectedCat = new Category();
                Cursor cursorCurrentSelectedCat = currentSelectedCat.fetchWithDB(db, idCurrentSelectedCat);
                currentSelectedCat.toDTOWithDb(db, cursorCurrentSelectedCat);
                cursorCurrentSelectedCat.close();
                this.setCurrentSelectedCategory(currentSelectedCat);
            } catch (Exception e) {
                this.setCurrentSelectedCategory(null);
            }

            String sDateBeg = cursor.getString(idxStatsDateBeg);
            if(sDateBeg != null){
                this.setStatsDateBeg(DatabaseHelper.toDate(sDateBeg));
            }

            String sDateEnd = cursor.getString(idxStatsDateEnd);
            if(sDateEnd != null) {
                this.setStatsDateEnd(DatabaseHelper.toDate(sDateEnd));
            }
        }

        return super.getFromCache();
    }

    @Override
    public boolean validate() { return true; }

    @Override
    public void reset() {
        super._id = null;
        this.withdrawalCategory = null;
        this.currentSelectedCategory = null;
        this.statsDateBeg = null;
        this.statsDateEnd = null;
    }

    @Override
    public String toString() {
        return "StaticPrefs{" +
                "withdrawalCategory=" + withdrawalCategory +
                ", currentSelectedCategory=" + currentSelectedCategory +
                ", statsDateBeg=" + statsDateBeg +
                ", statsDateEnd=" + statsDateEnd +
                '}';
    }

    @Override
    public String getDefaultOrderColumn() { return StaticPrefsData.KEY_ID; }

    @Override
    public int compareTo(Object another) {
        return 0;
    }

    public static StaticPrefs fromStaticData() {
        StaticPrefs sp = new StaticPrefs();
        sp._id = 1;
        sp.withdrawalCategory = StaticData.getWithdrawalCategory();
        sp.currentSelectedCategory = StaticData.getCurrentSelectedCategory();
        sp.statsDateBeg = StaticData.getDateField(StaticData.PREF_STATS_DATE_BEG);
        sp.statsDateEnd = StaticData.getDateField(StaticData.PREF_STATS_DATE_END);
        return sp;
    }

    public static void toStaticData(StaticPrefs sp) {
        StaticPrefs spToSet = sp;
        if (spToSet == null) {
            spToSet = new StaticPrefs();
        }
        StaticData.setWithdrawalCategory(spToSet.getWithdrawalCategory());
        StaticData.setCurrentSelectedCategory(spToSet.getCurrentSelectedCategory());
        StaticData.setDateField(StaticData.PREF_STATS_DATE_BEG, spToSet.getStatsDateBeg());
        StaticData.setDateField(StaticData.PREF_STATS_DATE_END, spToSet.getStatsDateEnd());
    }

    public static StaticPrefs loadCurrentStaticPrefs() {
        StaticPrefs ret = new StaticPrefs();
        Cursor c = ret.fetch(1);
        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();
        ret.toDTO(c);
        return ret;
    }

    public static boolean saveCurrentStaticPrefs() {
        return fromStaticData().insertOrUpdate();
    }
}
