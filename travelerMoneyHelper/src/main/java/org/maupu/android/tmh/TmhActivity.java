package org.maupu.android.tmh;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import org.maupu.android.tmh.ui.DialogHelper;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public abstract class TmhActivity extends AppCompatActivity implements IAsyncActivityRefresher, Drawer.OnDrawerItemClickListener {
    private static final Class TAG = TmhActivity.class;

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

    protected Integer contentView;
    protected Integer title;
    protected Toolbar toolbar;

    public TmhActivity() {
        super();
    }

    public TmhActivity(int contentView, int title) {
        this.contentView = contentView;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.contentView != null)
            setContentView(this.contentView);
        if (this.title != null)
            setTitle(this.title);

        toolbar = findViewById(R.id.tmh_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
                getSupportActionBar().setHomeButtonEnabled(false);
            }
        }

        initNavigationDrawer();
        //TmhApplication.getDatabaseHelper().createSampleData();
    }

    public static LayoutInflater getInflater(Context ctx) {
        return (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, ViewPagerOperationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
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
            IDrawerItem[] customItems = buildNavigationDrawer();
            if (customItems != null) {
                items.add(new DividerDrawerItem());
                items.addAll(Arrays.asList(customItems));
            }

            items.add(new DividerDrawerItem());

            /** Last items : refresh, parameters, etc ... **/
            items.add(createSecondaryDrawerItem(DRAWER_ITEM_PARAMETERS, R.drawable.ic_settings_black, R.string.parameters));
            items.add(createSecondaryDrawerItem(DRAWER_ITEM_REFRESH, R.drawable.ic_refresh_black, R.string.refresh));
            items.add(createSecondaryDrawerItem(DRAWER_ITEM_ABOUT, R.drawable.ic_info_black, R.string.about_title));

            final TmhActivity thisActivity = this;
            /** Navigation drawer itself **/
            navigationDrawer = new DrawerBuilder()
                    .withActivity(this)
                    .addDrawerItems(items.toArray(new IDrawerItem[0]))
                    .withAccountHeader(accountHeader)
                    .withToolbar(toolbar)
                    .withActionBarDrawerToggle(true)
                    .withOnDrawerListener(new Drawer.OnDrawerListener() {
                        @Override
                        public void onDrawerOpened(View drawerView) {
                            SoftKeyboardHelper.hide(thisActivity);
                        }

                        @Override
                        public void onDrawerClosed(View drawerView) {
                        }

                        @Override
                        public void onDrawerSlide(View drawerView, float slideOffset) {
                            SoftKeyboardHelper.hide(thisActivity);
                        }
                    })
                    //.withOnDrawerItemClickListener(this)
                    .build();

            // Update all badges in the drawer
            updateDrawerBadges();

            if (toolbar != null) {
                // Order and boolean are important to have custom icon as home up indicator
                navigationDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        /** Select good item in nav drawer **/
        this.selectDrawerItem(whatIsMyDrawerIdentifier());
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

    /**
     * Called when navigation drawer's item is clicked
     *
     * @param view
     * @param position
     * @param item
     * @return false to stop broadcasting event, true otherwise.
     */
    @Override
    public boolean onItemClick(View view, int position, IDrawerItem item) {
        Intent intent = null;
        boolean killCurrentActivity = true;

        /** Determine what item has been clicked **/
        if (item.getIdentifier() == DRAWER_ITEM_REFRESH) {
            refreshDisplay();
        } else if (item.getIdentifier() == DRAWER_ITEM_PARAMETERS) {
            intent = new Intent(this, PreferencesActivity.class);
            killCurrentActivity = false;
        } else if (item.getIdentifier() == DRAWER_ITEM_OPERATIONS) {
            intent = new Intent(this, ViewPagerOperationActivity.class);
        } else if (item.getIdentifier() == DRAWER_ITEM_STATS) {
            intent = new Intent(this, StatsActivity.class);
        } else if (item.getIdentifier() == DRAWER_ITEM_CONVERTER) {
            intent = new Intent(this, ConverterActivity.class);
        } else if (item.getIdentifier() == DRAWER_ITEM_ACCOUNTS) {
            intent = new Intent(this, ManageAccountActivity.class);
        } else if (item.getIdentifier() == DRAWER_ITEM_CATEGORIES) {
            intent = new Intent(this, ManageCategoryActivity.class);
        } else if (item.getIdentifier() == DRAWER_ITEM_CURRENCIES) {
            intent = new Intent(this, ManageCurrencyActivity.class);
        } else if (item.getIdentifier() == DRAWER_ITEM_ABOUT) {
            DialogHelper.popupDialogAbout(this);
        }

        /** Launch corresponding activity if recognized **/
        if (intent != null) {
            if (killCurrentActivity)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);

            if (killCurrentActivity)
                finish();
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDisplay();
    }

    /**
     * Called when click refresh button on menu
     */
    public void refreshDisplay() {
        AsyncActivityRefresher refresher = new AsyncActivityRefresher(this, this, false);

        try {
            // Execute background task implemented by client class
            refresher.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateDrawerBadges();
    }

    public static void setListViewAnimation(ListView listView) {
        // Setting animation
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        listView.setLayoutAnimation(controller);
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground() {
        return null;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
    }

    protected void selectDrawerItem(int identifier) {
        navigationDrawer.setSelection(identifier, false);
    }

    public abstract int whatIsMyDrawerIdentifier();

    /**
     * Called when navigation drawer is created. To customize, override this and return
     * an array. Separators are already included.
     *
     * @return an array of IDrawerItem corresponding to custom items
     */
    public IDrawerItem[] buildNavigationDrawer() {
        return null;
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
}
