package org.maupu.android.tmh.util.stats;

import java.util.ArrayList;
import java.util.List;

public class StatsData<T> implements Comparable {
    Float statValue;
    T obj;
    List<String> names;

    public StatsData(Float statValue, T obj, String name) {
        this.statValue = statValue;
        this.obj = obj;
        addName(name);
    }

    public void addName(String name) {
        if(names == null)
            names = new ArrayList<>();
        names.add(name);
    }

    public List<String> getNames() {
        return names;
    }

    public String getName(int location) {
        return names.get(location);
    }

    public float getStatValue() {
        return statValue;
    }

    public void setStatValue(Float statValue) {
        this.statValue = statValue;
    }

    public void addStatValue(Float statValue) {
        this.statValue += statValue;
    }

    public T getObj() {
        return obj;
    }

    @Override
    public int compareTo(Object another) {
        StatsData a = (StatsData) another;
        return (int)(a.getStatValue() - getStatValue());
    }
}
