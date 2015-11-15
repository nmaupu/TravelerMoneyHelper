package org.maupu.android.tmh.ui;

/**
 * Created by bicnic on 15/11/15.
 */
public class SimpleIconItem {
    int iconResource;
    String text;

    public SimpleIconItem(int iconRes, String text) {
        this.iconResource = iconRes;
        this.text = text;
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
}
