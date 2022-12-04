package org.maupu.android.tmh.ui;

import com.mikepenz.materialdrawer.Drawer;

import java.io.Serializable;

public class SerializableNavigationDrawer implements Serializable {
    private Drawer drawer;

    public SerializableNavigationDrawer(Drawer drawer) {
        this.drawer = drawer;
    }

    public Drawer getDrawer() {
        return drawer;
    }
}
