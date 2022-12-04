package org.maupu.android.tmh;

import android.os.Bundle;

public class MainActivity extends TmhActivity {
    public MainActivity() {
        super(R.layout.main_activity, R.string.activity_title_main);
    }

    @Override
    public int whatIsMyDrawerIdentifier() {
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Bundle bundle = new Bundle();
        bundle.putSerializable(MainFragment.BUNDLE_NAVIGATION_DRAWER_KEY, new SerializableNavigationDrawer(super.navigationDrawer));*/

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.nav_host_fragment, MainFragment.class, null)
                    .commit();
        }
    }
}
