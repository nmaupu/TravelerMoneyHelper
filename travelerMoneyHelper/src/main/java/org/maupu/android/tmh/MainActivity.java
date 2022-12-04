package org.maupu.android.tmh;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.databinding.ActivityMainBinding;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final Class TAG = MainActivity.class;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    // drawer
    public AccountHeader accountHeader;
    public Drawer navigationDrawer;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.tmhToolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        initNavigationDrawer();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    private void initNavigationDrawer() {
        if (accountHeader == null) {
            /** Header **/
            accountHeader = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.navigation_drawer_header)
                    .withOnAccountHeaderListener((view, profile, current) -> {
                        Account a = (Account) ((ProfileDrawerItem) profile).getTag();
                        StaticData.setCurrentAccount(a);
                        refreshAfterCurrentAccountChanged();
                        return false;
                    }).build();

            Cursor cursor = new Account().fetchAll();
            cursor.moveToFirst();
            int currentAccountId = -1;
            if (StaticData.getCurrentAccount() != null && StaticData.getCurrentAccount().getId() != null)
                currentAccountId = StaticData.getCurrentAccount().getId();
            IProfile profileToActivate = null;
            for (int i = 0; i < cursor.getCount(); i++) {
                int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
                int idxName = cursor.getColumnIndexOrThrow(AccountData.KEY_NAME);
                int idxIcon = cursor.getColumnIndexOrThrow(AccountData.KEY_ICON);
                Bitmap icon = ImageViewHelper.getBitmapIcon(this, cursor.getString(idxIcon));
                Account a = new Account();
                a.toDTO(cursor);
                IProfile profile = new ProfileDrawerItem()
                        .withTag(a)
                        .withName(cursor.getString(idxName))
                        .withIcon(icon);
                // Select current account on nav drawer header
                if (cursor.getInt(idxId) == currentAccountId)
                    profileToActivate = profile;
                accountHeader.addProfile(profile, i);
                cursor.moveToNext();
            }
            cursor.close();

            if (profileToActivate != null)
                accountHeader.setActiveProfile(profileToActivate);
        }

        if (navigationDrawer == null) {
            /** Items **/
            List<IDrawerItem> items = new ArrayList<>();
            items.add(createPrimaryDrawerItem(DRAWER_ITEM_OPERATIONS, R.drawable.ic_account_balance_black, R.string.dashboard_operation));
            items.add(createPrimaryDrawerItem(DRAWER_ITEM_STATS, R.drawable.ic_equalizer_black, R.string.dashboard_stats));
            items.add(createPrimaryDrawerItem(DRAWER_ITEM_CONVERTER, R.drawable.ic_converter_black, R.string.converter));
            items.add(new DividerDrawerItem());
            items.add(createPrimaryDrawerItem(DRAWER_ITEM_CURRENCIES, R.drawable.ic_currency_black, R.string.currencies));
            items.add(createPrimaryDrawerItem(DRAWER_ITEM_CATEGORIES, R.drawable.ic_folder_empty_black, R.string.categories));
            items.add(createPrimaryDrawerItem(DRAWER_ITEM_ACCOUNTS, R.drawable.ic_account_black, R.string.accounts));

            /** Custom items **/
            //IDrawerItem[] customItems = buildNavigationDrawer();
            IDrawerItem[] customItems = null;
            if (customItems != null) {
                items.add(new DividerDrawerItem());
                items.addAll(Arrays.asList(customItems));
            }

            items.add(new DividerDrawerItem());

            /** Last items : refresh, parameters, etc ... **/
            items.add(createSecondaryDrawerItem(DRAWER_ITEM_PARAMETERS, R.drawable.ic_settings_black, R.string.parameters));
            items.add(createSecondaryDrawerItem(DRAWER_ITEM_REFRESH, R.drawable.ic_refresh_black, R.string.refresh));
            items.add(createSecondaryDrawerItem(DRAWER_ITEM_ABOUT, R.drawable.ic_info_black, R.string.about_title));

            /** Navigation drawer itself **/
            FragmentActivity activity = this;
            navigationDrawer = new DrawerBuilder()
                    .withActivity(this)
                    .addDrawerItems(items.toArray(new IDrawerItem[0]))
                    .withAccountHeader(accountHeader)
                    .withToolbar(binding.tmhToolbar)
                    .withActionBarDrawerToggle(true)
                    .withOnDrawerListener(new Drawer.OnDrawerListener() {
                        @Override
                        public void onDrawerOpened(View drawerView) {
                            SoftKeyboardHelper.hide(activity);
                        }

                        @Override
                        public void onDrawerClosed(View drawerView) {
                        }

                        @Override
                        public void onDrawerSlide(View drawerView, float slideOffset) {
                            SoftKeyboardHelper.hide(activity);
                        }
                    })
                    //.withOnDrawerItemClickListener(this)
                    .build();

            // Update all badges in the drawer
            updateDrawerBadges();

            if (binding.tmhToolbar != null) {
                // Order and boolean are important to have custom icon as home up indicator
                navigationDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    public void refreshAfterCurrentAccountChanged() {
        List<IProfile> profiles = accountHeader.getProfiles();
        Iterator<IProfile> it = profiles.iterator();
        while (it.hasNext()) {
            ProfileDrawerItem p = (ProfileDrawerItem) it.next();
            Account a = (Account) p.getTag();
            if (StaticData.getCurrentAccount() != null && a.getId() == StaticData.getCurrentAccount().getId())
                accountHeader.setActiveProfile(p);
        }
    }

    public IDrawerItem createPrimaryDrawerItem(int identifier, int iconRes, int textRes) {
        TmhLogger.d(TAG, "Creating primary drawer item (" + getResources().getString(textRes) + ") with identifier=" + identifier);
        return new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.blue))
                .withIcon(iconRes)
                .withName(textRes);
    }

    public IDrawerItem createSecondaryDrawerItem(int identifier, int iconRes, int textRes) {
        TmhLogger.d(TAG, "Creating secondary drawer item (" + getResources().getString(textRes) + ") with identifier=" + identifier);
        return new SecondaryDrawerItem()
                .withIdentifier(identifier)
                .withIcon(iconRes)
                .withName(textRes)
                .withSelectable(false);
    }

    public void updateDrawerBadges() {
        int nbCurrencies = new Currency().getCount();
        int nbCategories = new Category().getCount();
        int nbAccounts = new Account().getCount();

        navigationDrawer.updateBadge(DRAWER_ITEM_CURRENCIES, new StringHolder(String.valueOf(nbCurrencies)));
        navigationDrawer.updateBadge(DRAWER_ITEM_CATEGORIES, new StringHolder(String.valueOf(nbCategories)));
        navigationDrawer.updateBadge(DRAWER_ITEM_ACCOUNTS, new StringHolder(String.valueOf(nbAccounts)));
    }

    protected void selectDrawerItem(int identifier) {
        navigationDrawer.setSelection(identifier, false);
    }
}
