package org.maupu.android.tmh.ui;

import android.graphics.Color;
import android.graphics.Typeface;

public class NavigationDrawerIconItem {
    public static final int SMALL_TEXT_SIZE = 14;
    public static final int SMALL_TEXT_COLOR = Color.GRAY;
    public static final int SMALL_HEIGHT = 125;
    public static final Typeface SMALL_TYPEFACE = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
    public static final int NORMAL_TEXT_SIZE = 16;
    public static final int NORMAL_TEXT_COLOR = Color.BLACK;
    public static final int NORMAL_HEIGHT = 200;
    public static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
    public static final int SEPARATOR_HEIGHT = 8;
    public static final int SEPARATOR_COLOR = Color.BLACK;
    public static final Typeface SELECTED_TYPEFACE = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
    public static final int SELECT_BG_COLOR = Color.LTGRAY;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    public enum ItemType {
        SMALL, NORMAL
    }

    Object tag;
    int iconResource;
    String text;
    INavigationDrawerCallback callback;
    boolean isSelectable = true;
    float textSize;
    int textColor;
    int height;
    Typeface tf;
    boolean separator = false;

    public NavigationDrawerIconItem() {
        this.separator = true;
        this.height = SEPARATOR_HEIGHT;
        this.textColor = SEPARATOR_COLOR;
    }

    public NavigationDrawerIconItem(Object tag, int iconRes, String text, INavigationDrawerCallback callback) {
        this(tag, iconRes, text, callback, ItemType.NORMAL);
    }

    public NavigationDrawerIconItem(Object tag, int iconRes, String text, INavigationDrawerCallback callback, ItemType itemType) {
        this(tag, iconRes, text, callback, itemType, null);
    }

    public NavigationDrawerIconItem(Object tag, int iconRes, String text, INavigationDrawerCallback callback, ItemType itemType, Boolean isSelectable) {
        this.tag = tag;
        this.iconResource = iconRes;
        this.text = text;
        this.callback = callback;
        boolean isSel = true;

        switch(itemType) {
            case NORMAL:
                textSize = NORMAL_TEXT_SIZE;
                textColor = NORMAL_TEXT_COLOR;
                height = NORMAL_HEIGHT;
                tf = NORMAL_TYPEFACE;
                isSel = true;
                break;
            case SMALL:
                textSize = SMALL_TEXT_SIZE;
                textColor = SMALL_TEXT_COLOR;
                height = SMALL_HEIGHT;
                tf = SMALL_TYPEFACE;
                isSel = false;
                break;
        }

        if(isSelectable == null)
            setSelectable(isSel);
        else
            setSelectable(true);
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
        if(getCallback() != null)
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

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    /**
     * Create a NavigationDrawerIconItem so that it is a separator with default size and color
     * @return a new NavigationDrawerIconItem which is a default separator
     */
    public static NavigationDrawerIconItem separator() {
        return new NavigationDrawerIconItem();
    }
}
