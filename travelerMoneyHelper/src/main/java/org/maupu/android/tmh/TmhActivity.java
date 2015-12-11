package org.maupu.android.tmh;

import org.maupu.android.tmh.ui.INavigationDrawerCallback;
import org.maupu.android.tmh.ui.NavigationDrawerIconItem;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.TmhNavigationDrawerClickListener;
import org.maupu.android.tmh.ui.async.AsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;
import org.maupu.android.tmh.ui.widget.IconArrayAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Adapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public abstract class TmhActivity extends AppCompatActivity implements IAsyncActivityRefresher, INavigationDrawerCallback {
    private static final String TAG = TmhActivity.class.getName();
	protected DrawerLayout drawerLayout;
	protected ListView drawerList;

    /** Navigation drawer items **/
    protected static final String DRAWER_ITEM_OPERATIONS = UUID.randomUUID().toString();
    protected static final String DRAWER_ITEM_STATS = UUID.randomUUID().toString();
    protected static final String DRAWER_ITEM_CONVERTER = UUID.randomUUID().toString();
    protected static final String DRAWER_ITEM_ACCOUNTS = UUID.randomUUID().toString();
    protected static final String DRAWER_ITEM_CATEGORIES = UUID.randomUUID().toString();
    protected static final String DRAWER_ITEM_CURRENCIES = UUID.randomUUID().toString();
    protected static final String DRAWER_ITEM_PARAMETERS = UUID.randomUUID().toString();
    protected static final String DRAWER_ITEM_REFRESH = UUID.randomUUID().toString();

    protected Integer menuResId;
    protected Integer contentView;
    protected Integer title;

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
        if(this.contentView != null)
            setContentView(this.contentView);
        if(this.title != null)
            setTitle(this.title);

        Toolbar toolbar = (Toolbar)findViewById(R.id.tmh_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.tmh_icon_verysmall);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        initNavigationDrawer();
        //TmhApplication.getDatabaseHelper().createSampleData();
	}

    public static LayoutInflater getInflater(Context ctx) {
        return (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, ViewPagerOperationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initNavigationDrawer() {
		try {
			drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			drawerList = (ListView) findViewById(R.id.left_drawer);

            /** Main items **/
			List<NavigationDrawerIconItem> items = new ArrayList<NavigationDrawerIconItem>();
            items.add(new NavigationDrawerIconItem(DRAWER_ITEM_OPERATIONS, R.drawable.ic_account_balance_black, getResources().getString(R.string.dashboard_operation), this));
            items.add(new NavigationDrawerIconItem(DRAWER_ITEM_STATS, R.drawable.ic_equalizer_black, getResources().getString(R.string.dashboard_stats), this));
            items.add(new NavigationDrawerIconItem(DRAWER_ITEM_CONVERTER, R.drawable.ic_converter_black, getResources().getString(R.string.converter), this));
            items.add(NavigationDrawerIconItem.separator());
            items.add(new NavigationDrawerIconItem(DRAWER_ITEM_CURRENCIES, R.drawable.ic_currency_black, getResources().getString(R.string.currencies), this));
            items.add(new NavigationDrawerIconItem(DRAWER_ITEM_CATEGORIES, R.drawable.ic_folder_empty_black, getResources().getString(R.string.categories), this));
            items.add(new NavigationDrawerIconItem(DRAWER_ITEM_ACCOUNTS, R.drawable.ic_account_black, getResources().getString(R.string.accounts), this));

            /** Custom items **/
            NavigationDrawerIconItem[] customItems = buildNavigationDrawer();
            if(customItems != null) {
                items.add(NavigationDrawerIconItem.separator());
                items.addAll(Arrays.asList(customItems));
            }

            /** sepatator **/
            items.add(NavigationDrawerIconItem.separator());

            /** Last items : refresh, parameters, etc ... **/
            items.add(new NavigationDrawerIconItem(DRAWER_ITEM_PARAMETERS, R.drawable.ic_settings_black, getResources().getString(R.string.parameters), this, NavigationDrawerIconItem.ItemType.SMALL));
            items.add(new NavigationDrawerIconItem(DRAWER_ITEM_REFRESH, R.drawable.ic_refresh_black, getResources().getString(R.string.refresh), this, NavigationDrawerIconItem.ItemType.SMALL));

            /** Add items to adapter **/
			drawerList.setAdapter(new IconArrayAdapter(this, R.layout.drawer_list_item, items));
			((IconArrayAdapter)drawerList.getAdapter()).selectItem(StaticData.navigationDrawerItemSelected);

            /** Listener **/
			drawerList.setOnItemClickListener(new TmhNavigationDrawerClickListener(drawerLayout, drawerList));

            /** Change home icon and open / close drawer on click **/
            /*
            setActionBarHomeDrawable(R.drawable.ic_menu_white);
            setActionBarHomeOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Drawer button clicked");
                    if (drawerLayout.isDrawerOpen(drawerList))
                        drawerLayout.closeDrawer(drawerList);
                    else
                        drawerLayout.openDrawer(drawerList);
                }
            });
            */
		} catch(NullPointerException npe) {
			// No drawer available in XML file
			Log.e(TAG, "No drawer_layout and/or no left_drawer available in XML resource");
		}
	}

	@Override
	public void onNavigationDrawerClick(NavigationDrawerIconItem item) {
		Intent intent = null;
        boolean killCurrentActivity = true;

        if(item.getTag() instanceof String) {
            /** Determine what item has been clicked **/
            if (item.getTag() == DRAWER_ITEM_REFRESH) {
                refreshDisplay();
            } else if (item.getTag() == DRAWER_ITEM_PARAMETERS) {
                intent = new Intent(this, PreferencesActivity.class);
                killCurrentActivity = false;
            } else if (item.getTag() == DRAWER_ITEM_OPERATIONS) {
                intent = new Intent(this, ViewPagerOperationActivity.class);
            } else if (item.getTag() == DRAWER_ITEM_STATS) {
                intent = new Intent(this, StatsActivity.class);
            } else if(item.getTag() == DRAWER_ITEM_CONVERTER) {
                intent = new Intent(this, ConverterActivity.class);
            } else if (item.getTag() == DRAWER_ITEM_ACCOUNTS) {
                intent = new Intent(this, ManageAccountActivity.class);
            } else if (item.getTag() == DRAWER_ITEM_CATEGORIES) {
                intent = new Intent(this, ManageCategoryActivity.class);
            } else if (item.getTag() == DRAWER_ITEM_CURRENCIES) {
                intent = new Intent(this, ManageCurrencyActivity.class);
            }

            /** Launch corresponding activity if recognized **/
            if(intent != null) {
                if(killCurrentActivity)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);

                if(killCurrentActivity)
                    finish();
            }
        }
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

    /*
	protected QuickActionGrid createQuickActionGridEdition() {
		QuickActionGrid quickActionGrid = new QuickActionGrid(this);
		quickActionGrid.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_group, R.string.accounts));
		quickActionGrid.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_list, R.string.categories));
		quickActionGrid.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_star, R.string.currencies));

		quickActionGrid.setOnQuickActionClickListener(new OnQuickActionClickListener() {
            @Override
            public void onQuickActionClicked(QuickActionWidget widget, int position) {
                switch (position) {
                    case 0:
                        startActivityFromMenu(ManageAccountActivity.class);
                        break;
                    case 1:
                        startActivityFromMenu(ManageCategoryActivity.class);
                        break;
                    case 2:
                        startActivityFromMenu(ManageCurrencyActivity.class);
                        break;
                }
            }
        });

		return quickActionGrid;
	}*/

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
	}

    /*
	protected void setActionBarHomeDrawable(int drawable) {
		ImageButton ib = (ImageButton)getGDActionBar().findViewById(R.id.gd_action_bar_home_item);
		if(ib != null) {
			ib.setImageResource(drawable);
		}
	}

    protected void setActionBarHomeOnClickListener(View.OnClickListener listener) {
        ImageButton ib = (ImageButton)getGDActionBar().findViewById(R.id.gd_action_bar_home_item);
        if(ib != null) {
            ib.setOnClickListener(listener);
        }
    }
    */

	public static void setListViewAnimation(ListView listView) {
		// Setting animation
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(50);
		set.addAnimation(animation);

		animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
				);
		animation.setDuration(100);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);        
		listView.setLayoutAnimation(controller);
	}

    /*
	protected static class MyQuickAction extends QuickAction {
		private static final ColorFilter BLACK_CF = new LightingColorFilter(Color.BLACK, Color.BLACK);

		public MyQuickAction(Context ctx, int drawableId, int titleId) {
			super(ctx, buildDrawable(ctx, drawableId), titleId);
		}

		private static Drawable buildDrawable(Context ctx, int drawableId) {
			Drawable d = ctx.getResources().getDrawable(drawableId);
			d.setColorFilter(BLACK_CF);
			return d;
		}
	}
	*/

    protected Integer getPositionNavigationDrawerItem(Object tag) {
        if(drawerList == null || drawerList.getAdapter() == null)
            return null;

        Adapter adapter = drawerList.getAdapter();
        int nb = adapter.getCount();
        for(int i=0; i<nb; i++) {
            if(((NavigationDrawerIconItem)adapter.getItem(i)).getTag() == tag)
                return i;
        }

        return null;
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground() {
        return null;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {}

    /**
     * Called when navigation drawer is created. To customize, override this and return
     * an array. Separators are already included.
     * @return an array of NavigationDrawerIconItem corresponding to custom items
     */
    public NavigationDrawerIconItem[] buildNavigationDrawer() {
        return null;
    }

    public NavigationDrawerIconItem createSmallNavigationDrawerItem(Object tag, int iconRes, int textRes) {
        return new NavigationDrawerIconItem(
                tag, iconRes, getResources().getString(textRes), this, NavigationDrawerIconItem.ItemType.SMALL);
    }
}
