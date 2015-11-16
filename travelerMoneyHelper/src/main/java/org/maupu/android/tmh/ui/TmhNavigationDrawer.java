package org.maupu.android.tmh.ui;

import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;

public class TmhNavigationDrawer {
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private TmhNavigationDrawerClickListener listener;

    public TmhNavigationDrawer(TmhNavigationDrawerClickListener listener, DrawerLayout drawerLayout, ListView drawerList) {
        this.drawerLayout = drawerLayout;
        this.listener = listener;
        this.drawerList = drawerList;
    }

    public TmhNavigationDrawer() {}

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public ListView getDrawerList() {
        return drawerList;
    }

    public TmhNavigationDrawerClickListener getListener() {
        return listener;
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    public void setDrawerList(ListView drawerList) {
        this.drawerList = drawerList;
    }

    public void setListener(TmhNavigationDrawerClickListener listener) {
        this.listener = listener;
    }
}
