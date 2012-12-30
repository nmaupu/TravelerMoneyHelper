package org.maupu.android.tmh;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBar.Type;
import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

import org.maupu.android.tmh.core.TmhApplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ListView;

public abstract class TmhActivity extends GDActivity {
	public TmhActivity() {
		super(Type.Normal);
	}
	
	public TmhActivity(Type type) {
		super(type);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TmhApplication.getDatabaseHelper().createSampleData();
	}

	@Override
	protected void onDestroy() {
		TmhApplication.getDatabaseHelper().close();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshDisplay();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.item_refresh:
			refreshDisplay();
			break;
		case R.id.item_preferences:
			startActivityFromMenu(PreferencesActivity.class);
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	protected QuickActionGrid createQuickActionGridEdition() {
		QuickActionGrid quickActionGrid = new QuickActionGrid(this);
		quickActionGrid.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_group, R.string.accounts));
		quickActionGrid.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_list, R.string.categories));
		quickActionGrid.addQuickAction(new MyQuickAction(this, R.drawable.gd_action_bar_star, R.string.currencies));

		quickActionGrid.setOnQuickActionClickListener(new OnQuickActionClickListener() {
			@Override
			public void onQuickActionClicked(QuickActionWidget widget, int position) {
				switch(position) {
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
	}

	protected void startActivityFromMenu(Class<?> cls) {
		startActivity(new Intent(this, cls));
	}

	/**
	 * Called when click refresh button on menu
	 */
	public abstract void refreshDisplay();
	
	@SuppressLint("NewApi")
	public void setActionBarHomeDrawable(int drawable) {
		ImageButton ib = (ImageButton)getActionBar().findViewById(R.id.gd_action_bar_home_item);
		if(ib != null) {
			ib.setImageResource(drawable);
		}
	}

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
}
