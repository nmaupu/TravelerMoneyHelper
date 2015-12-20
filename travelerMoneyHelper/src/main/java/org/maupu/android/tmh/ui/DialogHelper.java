package org.maupu.android.tmh.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.TmhActivity;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.filter.OperationFilter;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.widget.CheckableCursorAdapter;
import org.maupu.android.tmh.ui.widget.IconCursorAdapter;
import org.maupu.android.tmh.ui.widget.OperationCursorAdapter;

import android.app.Dialog;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

public abstract class DialogHelper {
	private static CheckableCursorAdapter categoryChooserAdapter = null;
	
	public static boolean isCheckableCursorAdapterInit() {
		return categoryChooserAdapter != null;
	}
	
	public static Dialog popupDialogAccountChooser(final TmhActivity tmhActivity) {
		if(tmhActivity == null)
			return null;
		
		final Dialog dialog = new Dialog(tmhActivity);

		dialog.setContentView(R.layout.dialog_listview);
		dialog.setTitle(tmhActivity.getString(R.string.pick_account));

		ListView listAccount = (ListView)dialog.findViewById(R.id.list);
		Account dummyAccount = new Account();
		final Cursor cursorAllAccounts = dummyAccount.fetchAll();

		final IconCursorAdapter adapter = new IconCursorAdapter(
                tmhActivity,
                R.layout.icon_name_item_no_checkbox,
                cursorAllAccounts,
                new String[]{AccountData.KEY_ICON, AccountData.KEY_NAME},
                new int[]{R.id.icon, R.id.name}, new ICallback<View>() {
                    @Override
                    public View callback(Object item) {
                        Log.d(DialogHelper.class.getName(), "popupDialogAccountChooser : callback called");

                        int position = (Integer)((View)item).getTag();
                        int oldPosition = cursorAllAccounts.getPosition();
                        cursorAllAccounts.moveToPosition(position);

                        Account account = new Account();
                        account.toDTO(cursorAllAccounts);

                        cursorAllAccounts.moveToPosition(oldPosition);

                        /** Replacing preferences account (excepted categories are also reset to auto) **/
                        /** Setting some specific stuff as well **/
                        StaticData.setCurrentAccount(account);
                        tmhActivity.refreshAfterCurrentAccountChanged();
                        tmhActivity.refreshDisplay();
                        dialog.dismiss();

                        return (View)item;
                    }
        });
		listAccount.setAdapter(adapter);
		dialog.show();

		return dialog;
	}
	
	public static Dialog popupDialogCategoryChooser(final TmhActivity tmhActivity, boolean resetPopup, boolean hideUnusedCat) {
		if(tmhActivity == null)
			return null;
		
		final Dialog dialog = new Dialog(tmhActivity);
		dialog.setCancelable(true);
		dialog.setTitle(R.string.stats_filter_cat_title);
		dialog.setContentView(R.layout.category_chooser_activity);

        Button btnValidate = (Button)dialog.findViewById(R.id.btn_validate);
        Button btnAutosel = (Button)dialog.findViewById(R.id.btn_autosel);
		final ListView list = (ListView)dialog.findViewById(R.id.list);

        /**
         * Adapter for category chooser
         */
        Category cat = new Category();
        Account currentAccount = StaticData.getCurrentAccount();
        final Cursor cursor;
        if(hideUnusedCat)
            cursor = cat.fetchAllCategoriesUsedByAccountOperations(currentAccount.getId());
        else
            cursor = cat.fetchAll();
        cursor.moveToFirst();

        /**
         * Buttons' listener
         */
		Button.OnClickListener listener = new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.btn_validate) {
					StaticData.getStatsExceptedCategories().clear();
					CheckableCursorAdapter adapter = (CheckableCursorAdapter)list.getAdapter(); 
					Integer[] ints = adapter.getCheckedPositions();
					for(int i : ints) {
						Cursor c = (Cursor)adapter.getItem(i);
						Category cat = new Category();
						cat.toDTO(c);
						StaticData.getStatsExceptedCategories().add(cat.getId());
					}
                    dialog.dismiss();
					tmhActivity.refreshDisplay();
				} else if (v.getId() == R.id.btn_autosel) {
                    categoryChooserAdapter.setToCheck(getAutoExceptCategoriesPositions(cursor));
                    categoryChooserAdapter.notifyDataSetChanged();
                }
			}
		};
		btnValidate.setOnClickListener(listener);
        btnAutosel.setOnClickListener(listener);

		if(categoryChooserAdapter == null || resetPopup) {
            Log.d(DialogHelper.class.getName(), "categoryChooserAdapter creation from scratch");
			categoryChooserAdapter = new CheckableCursorAdapter(
					tmhActivity, 
					R.layout.category_item,
					cursor,
					new String[]{CategoryData.KEY_NAME}, 
					new int[]{R.id.name},
                    getAutoExceptCategoriesPositions(cursor));
		} else {
            Log.d(DialogHelper.class.getName(), "categoryChooserAdapter reused but cursor changed");
			categoryChooserAdapter.changeCursor(cursor);
		}

		list.setAdapter(categoryChooserAdapter);
		dialog.show();

		return dialog;
	}

    /**
     * Get position of category in a cursor. Useful to know position on a list (cursor adapter)
     * @param cursor
     * @param category
     * @return the position of the category inside cursor
     */
    private static Integer getCategoryPositionInCursor(Cursor cursor, Category category) {
        int currentCursorPos = cursor.getPosition();
        int pos = 0;
        Integer resPos = null;

        cursor.moveToFirst();
        while(! cursor.isAfterLast()) {
            int idxId = cursor.getColumnIndexOrThrow(CategoryData.KEY_ID);
            int id = cursor.getInt(idxId);
            if(id == category.getId()) {
                resPos = new Integer(pos);
                break;
            }

            cursor.moveToNext();
            pos++;
        }

        cursor.move(currentCursorPos);
        return resPos;
    }

    /**
     * Get all categories to except automatically (categories that have credit)
     * @param cursor
     * @return List of all category position (in a cursor) to except. Those categories are taken in db automatically.
     */
    private static Integer[] getAutoExceptCategoriesPositions(Cursor cursor) {
        final List<Integer> posToCheck = new ArrayList<Integer>();
        Operation o = new Operation();
        Integer[] cats = o.getExceptCategoriesAuto(StaticData.getCurrentAccount());
        for(int i=0; i<cats.length; i++) {
            Category ca = new Category();
            Cursor cu = ca.fetch(cats[i]);
            ca.toDTO(cu);
            cu.close();
            posToCheck.add(getCategoryPositionInCursor(cursor, ca));
            Log.d(DialogHelper.class.getName(), "  - Auto checked category : "+cats[i]+" ("+ca.getName()+")");
        }

        return posToCheck.toArray(new Integer[0]);
    }

	public static Dialog popupDialogStatsDetails(final TmhActivity tmhActivity, Date beg, Date end, String catName, Integer[] exceptCategories) {
		if(tmhActivity == null)
			return null;
		
		final Dialog dialog = new Dialog(tmhActivity);
		dialog.setCancelable(true);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(R.layout.dialog_listview);
		final ListView list = (ListView)dialog.findViewById(R.id.list);
		
		// Adapter
		Operation dummyOp = new Operation();
		Account currentAccount = StaticData.getCurrentAccount();
		dummyOp.getFilter().addFilter(OperationFilter.FUNCTION_EQUAL, OperationData.KEY_ID_ACCOUNT, String.valueOf(currentAccount.getId()));
		
		// Fitering category if needed
		Category dummyCat = new Category();
		if(catName != null && !"".equals(catName)) {
			Cursor cursorCat = dummyCat.fetchByName(catName);
			dummyCat.toDTO(cursorCat);
            cursorCat.close();
		}
		
		if(dummyCat.getId() != null)
			dummyOp.getFilter().addFilter(OperationFilter.FUNCTION_EQUAL, OperationData.KEY_ID_CATEGORY, String.valueOf(dummyCat.getId()));
		
		StringBuilder b = new StringBuilder("");
		for(int i=0; i<exceptCategories.length; i++) {
			int catId = exceptCategories[i];
			b.append(catId);
			if(i<exceptCategories.length-1)
				b.append(",");
		}
		
		if(exceptCategories.length > 0)
			dummyOp.getFilter().addFilter(OperationFilter.FUNCTION_NOTIN, OperationData.KEY_ID_CATEGORY, b.toString());
		
		// Getting cursor
		Cursor cursor = dummyOp.fetchByPeriod(beg, end, "o."+OperationData.KEY_AMOUNT+" ASC", -1);
		
		OperationCursorAdapter sca = new OperationCursorAdapter(tmhActivity, 
				R.layout.operation_item_nocheckbox, 
				cursor, 
				new String[]{"dateStringHours", "category", "amountString", "convertedAmount"}, 
				new int[]{R.id.date, R.id.category, R.id.amount, R.id.convAmount});
		list.setAdapter(sca);
		
		dialog.show();
		return dialog;
	}
}
