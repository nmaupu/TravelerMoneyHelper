package org.maupu.android.tmh.ui;

import greendroid.graphics.drawable.ActionBarDrawable;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBarItem;
import greendroid.widget.NormalActionBarItem;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.cyrilmottier.android.greendroid.R;

public abstract class CustomActionBarItem extends ActionBarItem {
	public enum CustomType {
		Save,
		SaveAndAdd,
		Add,
		Edit,
		Delete,
		Withdrawal
	}

	public static ActionBarItem createActionBarItemFromType(ActionBar actionBar, CustomType type) {

		int drawableId = 0;
		int descriptionId = 0;

		switch (type) {
		case Save:
			drawableId = R.drawable.gd_action_bar_compose;
			descriptionId = R.string.save;
			break;
		case SaveAndAdd:
			drawableId = R.drawable.gd_action_bar_add;
			descriptionId = R.string.save_and_add;
			break;
		case Add:
			drawableId = R.drawable.gd_action_bar_add;
			descriptionId = R.string.add;
			break;
		case Edit:
			drawableId = R.drawable.gd_action_bar_edit;
			descriptionId = R.string.edit;
			break;
		case Withdrawal:
			drawableId = R.drawable.gd_action_bar_export;
			descriptionId = R.string.withdrawal;
			break;
		default:
			return null;
		}
		
		final Drawable d = new ActionBarDrawable(actionBar.getContext(), drawableId);
		ActionBarItem item = actionBar.newActionBarItem(NormalActionBarItem.class).setDrawable(d).setContentDescription(descriptionId);
		return item;
	}
	
	public static void setEnableItem(boolean isEnabled, ActionBarItem item) {
		if(! isEnabled) {
			item.getItemView().setBackgroundColor(Color.parseColor("#87C8FF"));
		} else {
			item.getItemView().setBackgroundColor(Color.parseColor("#0066bb"));
		}
		
		item.getItemView().setEnabled(isEnabled);
	}
}
