package org.maupu.android.tmh.ui;

import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.maupu.android.tmh.ui.widget.IconArrayAdapter;

public class TmhNavigationDrawerClickListener implements AdapterView.OnItemClickListener {
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    public TmhNavigationDrawerClickListener(DrawerLayout drawerLayout, ListView drawerList) {
        this.drawerLayout = drawerLayout;
        this.drawerList = drawerList;
    }

    @Override
    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        Log.d(TmhNavigationDrawerClickListener.class.getName(), "onItemClicked : "+adapterView.getAdapter().getItem(position));
        ((IconArrayAdapter)adapterView.getAdapter()).selectItem(position);
        this.drawerLayout.closeDrawer(drawerList);
    }
}
