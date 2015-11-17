package org.maupu.android.tmh.ui;

public class NavigationDrawerIconItem {
    Object tag;
    int iconResource;
    String text;
    INavigationDrawerCallback callback;
    boolean isSelectable = true;

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
}
