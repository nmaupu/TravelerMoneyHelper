package org.maupu.android.tmh.stats;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatsCategoryValues<T extends Entry> implements Comparable<StatsCategoryValues> {
    private static final String TAG = StatsCategoryValues.class.getName();
    private Set<Category> categories = new HashSet<>();
    private String name = null;
    private Map<String, Float> values = new HashMap<>();
    private Map<String, Float> valuesConv = new HashMap<>();
    private Date dateBegin;
    private Date dateEnd;
    private int color = ColorTemplate.COLOR_NONE;
    private Float sum, avg, sumConv, avgConv;
    private Double rateAvg = 1d;
    private Currency currency;

    public StatsCategoryValues(Category category, Date dateBegin, Date dateEnd, Double rateAvg, Currency currency) {
        categories.add(category);
        name = category.getName();
        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
        this.rateAvg = rateAvg;
        this.currency = currency;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name != null ? name : getFirstCategory().getName();
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Date getDateBegin() {
        return dateBegin;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    private void invalidateCache() {
        avg = sum = avgConv = sumConv = null;
    }

    public void addValue(String key, Float value) {
        if(value == null)
            return;

        invalidateCache();
        Float curVal = values.get(key);
        if(curVal == null)
            values.put(key, value);
        else
            values.put(key, curVal + value);
    }

    public void addValueConv(String key, Float value) {
        if(valuesConv == null)
            return;

        invalidateCache();
        Float curVal = valuesConv.get(key);
        if(curVal == null)
            valuesConv.put(key, value);
        else
            valuesConv.put(key, curVal + value);
    }

    public Map<String, Float> getValues() {
        return values;
    }

    public Map<String, Float> getValuesConv() {
        return valuesConv;
    }

    public void addCategory(Category category) {
        if(category != null && category.getId() != null)
            categories.add(category);
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public Double getRateAvg() {
        return rateAvg;
    }

    public void setRateAvg(Double rateAvg) {
        this.rateAvg = rateAvg;
    }

    public Category getFirstCategory() {
        Iterator<Category> catIt = categories.iterator();
        return catIt.next();
    }

    public boolean contains(Integer catId) {
        if(catId == null || catId < 0 || categories == null || categories.size() == 0)
            return false;

        for(Category c : categories)
            if(c.getId() == catId)
                return true;

        return false;
    }

    public static List<String> buildXEntries(Date dateBegin, Date dateEnd) {
        if(dateBegin == null || dateEnd == null)
            return null;

        List<String> xEntries = new ArrayList<>();
        Date d = (Date)dateBegin.clone();
        int nbDays = DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd);
        Log.d(TAG, "Number of days between "+dateBegin+" and "+dateEnd+" = "+nbDays);
        for(int x=0; x<nbDays+1; x++) {
            xEntries.add(DateUtil.dateToStringNoTime(d));
            d = DateUtil.addDays(d, 1);
        }

        return xEntries;
    }

    public List<T> getYEntries() {
        if(dateBegin == null || dateEnd == null)
            return null;

        List<T> entries = new ArrayList<>();

        Date d = (Date)dateBegin.clone();
        int nbDays = DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd);

        for(int x=0; x<nbDays+1; x++) {
            String dateString = DateUtil.dateToStringNoTime(d);
            Float v = values.get(dateString) == null ? 0f : values.get(dateString);
            entries.add((T)new Entry(v, x));
            d = DateUtil.addDays(d, 1);
        }

        return entries;
    }

    public Float summarize() {
        if(values == null || values.size() == 0)
            return 0f;

        if(sum != null)
            return sum;

        sum = 0f;
        Iterator<Float> it = values.values().iterator();
        while(it.hasNext()) {
            sum += it.next();
        }

        return sum;
    }

    public Float summarizeConv() {
        if(valuesConv == null || valuesConv.size() == 0)
            return 0f;

        if(sumConv != null)
            return sumConv;

        sumConv = 0f;
        Iterator<Float> it = valuesConv.values().iterator();
        while(it.hasNext()) {
            sumConv += it.next();
        }

        return sumConv;
    }

    public Float average() {
        if(values == null || values.size() == 0)
            return 0f;

        if(avg != null)
            return avg;

        int nbDays = DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd);
        avg = nbDays == 0 ? 0f : summarize()/nbDays;
        return avg;
    }

    public Float averageConv() {
        if(valuesConv == null || valuesConv.size() == 0)
            return 0f;

        if(avgConv != null)
            return avgConv;

        int nbDays = DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd);
        avgConv = nbDays == 0 ? 0f : summarizeConv()/nbDays;
        return avgConv;
    }

    public void fusionWith(final StatsCategoryValues scv) {
        categories.addAll(scv.getCategories());
        Date db = scv.dateBegin;
        Date de = scv.dateEnd;

        if(db.before(dateBegin))
            dateBegin = db;
        if(de.after(dateEnd))
            dateEnd = de;

        if(scv.values == null || scv.values.size() == 0)
            return;

        Iterator<String> it = scv.values.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            Float value = (Float)scv.getValues().get(key);
            if(value != null) {
                Float curVal = values.get(key) == null ? 0f : values.get(key);
                values.put(key, curVal+value);
            }
        }

        it = scv.valuesConv.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            Float value = (Float)scv.getValuesConv().get(key);
            if(value != null) {
                Float curVal = valuesConv.get(key) == null ? 0f : valuesConv.get(key);
                valuesConv.put(key, curVal+value);
            }
        }
    }

    @Override
    public int compareTo(StatsCategoryValues another) {
        // Order by biggest to smallest
        return (int)(another.summarize() - summarize());
    }

    public Integer[] getCategoryIds() {
        if(getCategories() == null || getCategories().size() == 0)
            return null;

        Integer[] cats = new Integer[getCategories().size()];
        int i = 0;
        for(Category cat : getCategories()) {
            cats[i++] = cat.getId();
        }
        return cats;
    }
}
