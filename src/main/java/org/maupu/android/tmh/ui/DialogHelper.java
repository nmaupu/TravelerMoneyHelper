package org.maupu.android.tmh.ui;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.TmhActivity;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.ui.widget.CheckableCursorAdapter;
import org.maupu.android.tmh.ui.widget.IconCursorAdapter;

import android.app.Dialog;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public abstract class DialogHelper {
	private static CheckableCursorAdapter categoryChooserAdapter = null;
	
	public static Dialog popupDialogAccountChooser(final TmhActivity tmhActivity) {
		if(tmhActivity == null)
			return null;
		
		final Dialog dialog = new Dialog(tmhActivity);

		dialog.setContentView(R.layout.dialog_choose_account);
		dialog.setTitle(tmhActivity.getString(R.string.pick_account));

		ListView listAccount = (ListView)dialog.findViewById(R.id.list);
		Account dummyAccount = new Account();
		final Cursor cursorAllAccounts = dummyAccount.fetchAll();

		final IconCursorAdapter adapter = new IconCursorAdapter(tmhActivity, 
				R.layout.icon_name_item_no_checkbox, 
				cursorAllAccounts,
				new String[]{AccountData.KEY_ICON, AccountData.KEY_NAME}, 
				new int[]{R.id.icon, R.id.name}, new ICallback<View>() {
			@Override
			public View callback(Object item) {
				int position = (Integer)((View)item).getTag();
				int oldPosition = cursorAllAccounts.getPosition();
				cursorAllAccounts.moveToPosition(position);

				Account account = new Account();
				account.toDTO(cursorAllAccounts);

				cursorAllAccounts.moveToPosition(oldPosition);


				// Replacing preferences account
				StaticData.setCurrentAccount(account);
				tmhActivity.refreshDisplay();
				Log.d("OperationPagerItem", "Callback called");
				dialog.dismiss();

				return (View)item;
			}
		});
		listAccount.setAdapter(adapter);
		dialog.show();

		return dialog;
	}
	
	public static Dialog popupDialogCategoryChooser(final TmhActivity tmhActivity, boolean resetPopup) {
		if(tmhActivity == null)
			return null;
		
		final Dialog dialog = new Dialog(tmhActivity);
		dialog.setCancelable(false);
		dialog.setTitle(R.string.stats_filter_cat_title);
		dialog.setContentView(R.layout.category_chooser_activity);
		dialog.findViewById(R.id.list);
		
		
		Button btnValidate = (Button)dialog.findViewById(R.id.btn_validate);
		final ListView list = (ListView)dialog.findViewById(R.id.list);
		
		Button.OnClickListener listener = new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.btn_validate) {
					StaticData.getStatsExpectedCategories().clear();
					CheckableCursorAdapter adapter = (CheckableCursorAdapter)list.getAdapter(); 
					Integer[] ints = adapter.getCheckedPositions();
					for(int i : ints) {
						Cursor c = (Cursor)adapter.getItem(i);
						Category cat = new Category();
						cat.toDTO(c);
						StaticData.getStatsExpectedCategories().add(cat.getId());
						//Log.d(StatsActivity.class.getName(), "category "+cat+" is checked");
					}
					tmhActivity.refreshDisplay();
					dialog.dismiss();
				}
			}
		};
		btnValidate.setOnClickListener(listener);
		
		// Adapter for category chooser
		Category cat = new Category();
		Account currentAccount = StaticData.getCurrentAccount();
		Cursor cursor = cat.fetchAllCategoiesUsedByAccountOperations(currentAccount.getId());
		
		if(categoryChooserAdapter == null || resetPopup) {
			categoryChooserAdapter = new CheckableCursorAdapter(
					tmhActivity, 
					R.layout.category_item,
					cursor,
					new String[]{CategoryData.KEY_NAME}, 
					new int[]{R.id.name});
		} else {
			categoryChooserAdapter.changeCursor(cursor);
		}
		
		list.setAdapter(categoryChooserAdapter);
		dialog.show();

		return dialog;
	}
}
