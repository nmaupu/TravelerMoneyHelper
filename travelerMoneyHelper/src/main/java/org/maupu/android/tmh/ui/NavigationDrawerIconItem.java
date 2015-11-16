package org.maupu.android.tmh.ui;

public class NavigationDrawerIconItem {
    Object flag;
    int iconResource;
    String text;
    INavigationDrawerCallback callback;

    public NavigationDrawerIconItem(Object flag, int iconRes, String text, INavigationDrawerCallback callback) {
        this.flag = flag;
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

    public Object getFlag() {
        return flag;
    }

    public INavigationDrawerCallback getCallback() {
        return callback;
    }

    public void generateClickEvent() {
        getCallback().onNavigationDrawerClick(this);
    }
}
