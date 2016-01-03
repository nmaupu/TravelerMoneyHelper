package org.maupu.android.tmh.stats;

import android.support.annotation.NonNull;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
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
    //private Map<String, Float> values = new HashMap<>();
    private Set<Operation> operations = new HashSet<>();
    private Date dateBegin;
    private Date dateEnd;
    private int color = ColorTemplate.COLOR_NONE;
    private Currency currency;

    public StatsCategoryValues(Category category, Date dateBegin, Date dateEnd, Currency currency) {
        categories.add(category);
        name = category.getName();
        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
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

    public void addOperation(Operation operation) {
        operations.add(operation);
    }

    public Set<Operation> getOperations() {
        return operations;
    }

    public void addCategory(Category category) {
        if(category != null && category.getId() != null)
            categories.add(category);
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public Category getFirstCategory() {
        Iterator<Category> catIt = categories.iterator();
        return catIt.next();
    }

    public boolean contains(Integer catId) {
        if(catId == null || catId < 0 || categories == null || categories.size() == 0)
            return false;

        Iterator<Category> it = categories.iterator();
        while(it.hasNext()) {
            Category c = it.next();
            if(c.getId() == catId)
                return true;
        }

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
            Double v = summarize(d);
            entries.add((T)new Entry(v.floatValue(), x));
            d = DateUtil.addDays(d, 1);
        }

        return entries;
    }

    @NonNull
    public Double summarize(Date day, boolean convert) {
        double sum = 0d;
        for(Operation o : operations) {
            if(day != null) {
                Date opDate = o.getDate();

                Calendar calOp = Calendar.getInstance();
                calOp.setTime(opDate);

                Calendar calDate = Calendar.getInstance();
                calDate.setTime(day);

                if (calOp.get(Calendar.YEAR) == calDate.get(Calendar.YEAR) &&
                        calOp.get(Calendar.MONTH) == calDate.get(Calendar.MONTH) &&
                        calOp.get(Calendar.DAY_OF_MONTH) == calDate.get(Calendar.DAY_OF_MONTH)) {
                    if(convert)
                        sum += o.getAmount() / o.getCurrencyValueOnCreated();
                    else
                        sum += o.getAmount();
                }
            } else {
                if(convert)
                    sum += o.getAmount() / o.getCurrencyValueOnCreated();
                else
                    sum += o.getAmount();
            }
        }

        return sum;
    }

    @NonNull
    public Double summarize(boolean convert) {
        return summarize(null, convert);
    }

    @NonNull
    public Double summarize() {
        return summarize(false);
    }

    @NonNull
    public Double summarize(Date day) {
        return summarize(day, false);
    }

    @NonNull
    public Double average(boolean convert) {
        int nbDays = DateUtil.getNumberOfDaysBetweenDates(dateBegin, dateEnd);
        if(convert)
            return summarize(true) / nbDays;
        else
            return nbDays == 0 ? 0d : summarize() / nbDays;
    }

    @NonNull
    public Double average() {
        return average(false);
    }

    public void fusionWith(final StatsCategoryValues scv) {
        categories.addAll(scv.getCategories());
        Date db = scv.dateBegin;
        Date de = scv.dateEnd;

        if(db.before(dateBegin))
            dateBegin = db;
        if(de.after(dateEnd))
            dateEnd = de;

        operations.addAll(scv.getOperations());
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
