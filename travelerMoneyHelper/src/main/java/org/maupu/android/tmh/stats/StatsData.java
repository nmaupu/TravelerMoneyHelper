package org.maupu.android.tmh.stats;

import android.database.Cursor;

import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StatsData extends HashMap<Integer, StatsCategoryValues> {
    private List<IStatsDataChangedListener> listeners;
    private Category biggestCategory;
    private int[] colors;
    private StatsCategoryValues miscCategory;
    private Category catToHighlight;
    private boolean chartAnim;
    private Date dateBegin, dateEnd;

    public StatsData(int[] colors) {
        this.colors = colors.clone();
        this.chartAnim = true;
        this.listeners = new ArrayList<>();
    }

    public Category getBiggestCategory() {
        return biggestCategory;
    }

    public Category getCatToHighlight() {
        return catToHighlight;
    }

    public void setCatToHighlight(Category catToHighlight) {
        this.catToHighlight = catToHighlight;
    }

    public boolean isChartAnim() {
        return chartAnim;
    }

    public void disableChartAnim() {
        setChartAnim(false);
    }

    public void enableChartAnim() {
        setChartAnim(true);
    }

    public void setChartAnim(boolean chartAnimation) {
        this.chartAnim = chartAnimation;
    }

    public Date getDateBegin() {
        return dateBegin;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void addOnStatsDataChangedListener(IStatsDataChangedListener listener) {
        listeners.add(listener);
    }

    protected void notifyStatsDataChanged() {
        for (IStatsDataChangedListener l: listeners) {
            l.onStatsDataChanged(this);
        }
    }

    /**
     * Rebuild charts data and send events to all listeners that data have been changed.
     * @param exceptedCategories Categories to except
     * @param dateBegin Date from when we are loading data
     * @param dateEnd Date until when we are loading data
     * @param limitBeforeAggregation Number of categories before aggregating them together
     * @param aggregationName Name of the aggregated category
     * @param forwardEvent Specify whether event are forwarded to listeners or not
     */
    public void rebuildChartsData(final Set<Integer> exceptedCategories,
                                  final Date dateBegin, final Date dateEnd,
                                  int limitBeforeAggregation, String aggregationName,
                                  boolean forwardEvent) {
        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
        Integer[] exceptedCats = null;
        if(exceptedCategories != null)
            exceptedCats = exceptedCategories.toArray(new Integer[exceptedCategories.size()]);

        Cursor c = new Operation().sumOperationsGroupByDayOrderDateAsc(
                StaticData.getCurrentAccount(),
                dateBegin,
                dateEnd,
                exceptedCats
        );
        if(c == null)
            return;
        if(c.getCount() == 0) {
            c.close();
            return;
        }

        c.moveToFirst();

        this.clear();
        do {
            int idxAmount = c.getColumnIndexOrThrow("amountString");
            int idxAmountConv = c.getColumnIndexOrThrow("amountConv");
            int idxDate = c.getColumnIndexOrThrow("dateString");
            int idxCatId = c.getColumnIndexOrThrow(OperationData.KEY_ID_CATEGORY);
            int idxRate = c.getColumnIndexOrThrow("rateAvg");
            int idxCurrencyId = c.getColumnIndexOrThrow(CurrencyData.KEY_ID);
            Double amount = c.getDouble(idxAmount);
            Double amountConv = c.getDouble(idxAmountConv);
            String dateString = c.getString(idxDate);
            int catId = c.getInt(idxCatId);
            double rate = c.getDouble(idxRate);
            int curId = c.getInt(idxCurrencyId);

            Category cat = new Category();
            Cursor cursorCat = cat.fetch(catId);
            cat.toDTO(cursorCat);
            cursorCat.close();

            Currency currency = new Currency();
            Cursor cursorCur = currency.fetch(curId);
            currency.toDTO(cursorCur);
            cursorCur.close();

            try {
                StatsCategoryValues scv = this.get(catId);
                if(scv == null) {
                    scv = new StatsCategoryValues(cat, dateBegin, dateEnd, rate, currency);
                    this.put(catId, scv);
                }

                scv.addValue(dateString, Math.abs(amount.floatValue()));
                scv.addValueConv(dateString, Math.abs(amountConv.floatValue()));
            } catch(NullPointerException | NumberFormatException ex) {}
        } while(c.moveToNext());

        c.close();

        List<StatsCategoryValues> chartsDataList = new ArrayList<>(this.values());
        Collections.sort(chartsDataList);
        int nbElts = chartsDataList.size();

        // Biggest category is the first one after sorting the list
        if(chartsDataList.size() > 0)
            biggestCategory = chartsDataList.get(0).getFirstCategory();

        // Put color from an array on each stats category value
        int x = 0;
        for (StatsCategoryValues s: chartsDataList) {
            s.setColor(colors[x % colors.length]);
            x++;
        }

        // If max nb of displayed slices is reach, gather remaining categories together
        if(nbElts > limitBeforeAggregation) {
            miscCategory = chartsDataList.get(limitBeforeAggregation);
            miscCategory.setName(aggregationName);
            // Remove from charts data, fusion everything and reintegrate it
            this.remove(miscCategory.getFirstCategory().getId());
            for(int i=limitBeforeAggregation+1; i<nbElts; i++) {
                StatsCategoryValues curScv = chartsDataList.get(i);
                miscCategory.fusionWith(curScv);
                this.remove(curScv.getFirstCategory().getId());
            }
            this.put(miscCategory.getFirstCategory().getId(), miscCategory);
        } else {
            miscCategory = null;
        }

        if(forwardEvent)
            notifyStatsDataChanged();
    }

    public String getMiscCategoryText() {
        if(miscCategory == null || miscCategory.getCategories() == null || miscCategory.getCategories().size() == 0)
            return null;
        else if(miscCategory.getCategories().size() == 1)
            return miscCategory.getFirstCategory().getName();

        StringBuilder sb = null;

        List<Category> miscCats = new ArrayList<>(miscCategory.getCategories());
        Collections.sort(miscCats);
        for (Category cat: miscCats) {
            if (sb == null) {
                sb = new StringBuilder();
                sb.append(cat.getName());
            } else {
                sb.append("\n").append(cat.getName());
            }
        }

        return sb.toString();
    }

    public StatsCategoryValues sumForDate(String dateString) {
        if(size() == 0)
            return null;

        Category dummyCat = new Category();
        dummyCat.setName(dateString);

        int firstKey = keySet().iterator().next();
        StatsCategoryValues firstScv = get(firstKey);
        Double rate = firstScv.getRateAvg();
        Currency currency = firstScv.getCurrency();

        StatsCategoryValues ret = new StatsCategoryValues(dummyCat, null, null, rate, currency);

        for(int k : keySet()) {
            StatsCategoryValues scv = get(k);
            ret.addValue(dateString, (Float) scv.getValues().get(dateString));
        }

        return ret;
    }
}
