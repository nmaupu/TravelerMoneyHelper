package org.maupu.android.tmh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import org.maupu.android.tmh.databinding.ActivityMainBinding;
import org.maupu.android.tmh.ui.ApplicationDrawer;
import org.maupu.android.tmh.ui.DialogHelper;

public class MainActivity extends AppCompatActivity {
    private static final Class<MainActivity> TAG = MainActivity.class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Initialize the drawer singleton
        ApplicationDrawer.getInstance().initDrawer(this);
        ApplicationDrawer.getDrawer().setOnDrawerItemClickListener((view, position, drawerItem) -> {
            // When returning true, drawer doesn't close itself
            // whereas when returning false, drawer closes
            if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_ABOUT) {
                DialogHelper.popupDialogAbout(this);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_CONVERTER) {
                changeFragment(ConverterFragment.class, false, null);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_OPERATIONS) {
                changeFragment(ViewPagerOperationFragment.class, false, null);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_PARAMETERS) {
                Intent intent = new Intent(this, PreferencesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (drawerItem.getIdentifier() == ApplicationDrawer.DRAWER_ITEM_ACCOUNTS) {
                changeFragment(ManageAccountFragment.class, false, null);
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

    public void changeFragment(Class fragment, boolean addToBackStack, Bundle args) {
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();

        if (addToBackStack) {
            transaction
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.fragment_content_main, fragment, args)
                    .setReorderingAllowed(true)
                    .addToBackStack(null);

            ApplicationDrawer.getInstance().setNavigationBarDrawerIconToBackArrow();
        } else {
            transaction
                    .replace(R.id.fragment_content_main, fragment, args)
                    .setReorderingAllowed(true);
        }

        transaction.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public void onBackPressed() {
        if (ApplicationDrawer.getDrawer().isDrawerOpen()) {
            ApplicationDrawer.getDrawer().closeDrawer();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            ApplicationDrawer.getInstance().setNavigationBarDrawerIconToHamburger();
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
