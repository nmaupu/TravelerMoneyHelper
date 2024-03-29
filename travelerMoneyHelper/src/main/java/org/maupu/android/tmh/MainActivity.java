package org.maupu.android.tmh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.databinding.ActivityMainBinding;
import org.maupu.android.tmh.ui.ApplicationDrawer;
import org.maupu.android.tmh.ui.DialogHelper;

public class MainActivity extends TmhActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);

        if (!TmhApplication.checkDatabaseInitialized()) {
            TmhApplication.initDatabase(this);
        }

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Initialize the drawer singleton
        ApplicationDrawer.resetDrawer(this);
        ApplicationDrawer.getInstance().initDrawer(this);
        ApplicationDrawer.getDrawer().setOnDrawerItemClickListener((view, position, drawerItem) -> {
            // When returning true, drawer doesn't close itself
            // whereas when returning false, drawer closes
            if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_OPERATIONS) {
                changeFragment(ViewPagerOperationFragment.class, false);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_CONVERTER) {
                changeFragment(ConverterFragment.class, false);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_STATS) {
                changeFragment(StatsFragment.class, false);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_ACCOUNTS) {
                changeFragment(ManageAccountFragment.class, false);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_CATEGORIES) {
                changeFragment(ManageCategoryFragment.class, false);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_CURRENCIES) {
                changeFragment(ManageCurrencyFragment.class, false);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_PARAMETERS) {
                Intent intent = new Intent(this, PreferencesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_ABOUT) {
                DialogHelper.popupDialogAbout(this);
            }

            return false;
        });

        // Display initial fragment
        if (savedInstanceState == null) {
            changeFragment(ViewPagerOperationFragment.class, false, null);
        }
    }

    // When clicking on the "home" button which can be hamburger or back arrow
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (ApplicationDrawer.getInstance().isDrawerEnabled()) {
                    if (ApplicationDrawer.getDrawer().isDrawerOpen())
                        ApplicationDrawer.getDrawer().closeDrawer();
                    else
                        ApplicationDrawer.getDrawer().openDrawer();
                } else {
                    onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
