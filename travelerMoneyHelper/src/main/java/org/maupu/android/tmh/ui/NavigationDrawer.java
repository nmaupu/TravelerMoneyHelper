package org.maupu.android.tmh.ui;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;

import org.maupu.android.tmh.core.TmhApplication;

public class NavigationDrawer {
    private AccountHeader accountHeader;
    private Drawer navigationDrawer;

    /**
     * Navigation drawer items
     **/
    protected static final int DRAWER_ITEM_OPERATIONS = TmhApplication.getIdentifier("DRAWER_ITEM_OPERATIONS");
    protected static final int DRAWER_ITEM_STATS = TmhApplication.getIdentifier("DRAWER_ITEM_STATS");
    protected static final int DRAWER_ITEM_CONVERTER = TmhApplication.getIdentifier("DRAWER_ITEM_CONVERTER");
    protected static final int DRAWER_ITEM_ACCOUNTS = TmhApplication.getIdentifier("DRAWER_ITEM_ACCOUNTS");
    protected static final int DRAWER_ITEM_CATEGORIES = TmhApplication.getIdentifier("DRAWER_ITEM_CATEGORIES");
    protected static final int DRAWER_ITEM_CURRENCIES = TmhApplication.getIdentifier("DRAWER_ITEM_CURRENCIES");
    protected static final int DRAWER_ITEM_PARAMETERS = TmhApplication.getIdentifier("DRAWER_ITEM_PARAMETERS");
    protected static final int DRAWER_ITEM_REFRESH = TmhApplication.getIdentifier("DRAWER_ITEM_REFRESH");
    protected static final int DRAWER_ITEM_ABOUT = TmhApplication.getIdentifier("DRAWER_ITEM_ABOUT");

    public NavigationDrawer() {
    }
}
