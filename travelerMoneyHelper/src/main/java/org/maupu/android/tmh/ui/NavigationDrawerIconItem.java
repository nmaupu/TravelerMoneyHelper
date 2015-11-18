package org.maupu.android.tmh.ui;

import android.graphics.Color;
import android.graphics.Typeface;

public class NavigationDrawerIconItem {
    Object tag;
    int iconResource;
    String text;
    INavigationDrawerCallback callback;
    boolean isSelectable = true;
    float textSize = 16;
    int textColor = Color.BLACK;
    int height = 200;
    Typeface tf = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);

    public NavigationDrawerIconItem(Object tag, int iconRes, String text, INavigationDrawerCallback callback) {
        this.tag = tag;
        this.iconResource = iconRes;
        this.text = text;
        this.callback = callback;
    }

    public String getText() {
        return this.text;
    }

    public int getIconResource() {
        return this.iconResource;
    }

    public String toString() {
        return getText();
    }

    public Object getTag() {
        return tag;
    }

    public INavigationDrawerCallback getCallback() {
        return callback;
    }

    public void generateClickEvent() {
        getCallback().onNavigationDrawerClick(this);
    }

    public void setSelectable(boolean b) {
        this.isSelectable = b;
    }

    public boolean isSelectable() {
        return this.isSelectable;
    }

    public float getTextSize() {
        return textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Typeface getTypeface() {
        return tf;
    }

    public void setTypeface(Typeface tf) {
        this.tf = tf;
    }
}
