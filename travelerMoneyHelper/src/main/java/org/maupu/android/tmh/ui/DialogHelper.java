package org.maupu.android.tmh.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import androidx.fragment.app.FragmentActivity;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.TmhFragment;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.filter.OperationFilter;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.widget.CheckableCursorAdapter;
import org.maupu.android.tmh.ui.widget.IconCursorAdapter;
import org.maupu.android.tmh.ui.widget.OperationCursorAdapter;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class DialogHelper {
    private static final Class TAG = DialogHelper.class;
    private static CheckableCursorAdapter categoryChooserAdapter = null;

    public static boolean isCheckableCursorAdapterInit() {
        return categoryChooserAdapter != null;
    }

    public static Dialog popupDialogAccountChooser(final TmhFragment fragment) {
        if (fragment == null || fragment.getContext() == null)
            return null;

        final Dialog dialog = new Dialog(fragment.getContext());

        dialog.setContentView(R.layout.dialog_listview);
        dialog.setTitle(fragment.getString(R.string.pick_account));

        ListView listAccount = dialog.findViewById(R.id.list);
        Account dummyAccount = new Account();
        final Cursor cursorAllAccounts = dummyAccount.fetchAll();

        final IconCursorAdapter adapter = new IconCursorAdapter(
                fragment.getContext(),
                R.layout.icon_name_item_no_checkbox,
                cursorAllAccounts,
                new String[]{AccountData.KEY_ICON, AccountData.KEY_NAME},
                new int[]{R.id.icon, R.id.name}, new ICallback<View>() {
            @Override
            public View callback(Object item) {
                TmhLogger.d(TAG, "popupDialogAccountChooser : callback called");

                int position = (Integer) ((View) item).getTag();
                int oldPosition = cursorAllAccounts.getPosition();
                cursorAllAccounts.moveToPosition(position);

                Account account = new Account();
                account.toDTO(cursorAllAccounts);

                cursorAllAccounts.moveToPosition(oldPosition);

                /** Replacing preferences account (excepted categories are also reset to auto) **/
                /** Setting some specific stuff as well **/
                StaticData.setCurrentAccount(account);
                // TODO refresh if account changes
                //fragment.refreshAfterCurrentAccountChanged();
                fragment.refreshDisplay();
                dialog.dismiss();

                return (View) item;
            }
        });
        listAccount.setAdapter(adapter);
        dialog.show();

        return dialog;
    }

    /**
     * Get position of category in a cursor. Useful to know position on a list (cursor adapter)
     *
     * @param cursor
     * @param category
     * @return the position of the category inside cursor
     */
    private static Integer getCategoryPositionInCursor(Cursor cursor, Category category) {
        int currentCursorPos = cursor.getPosition();
        int pos = 0;
        Integer resPos = null;

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int idxId = cursor.getColumnIndexOrThrow(CategoryData.KEY_ID);
            int id = cursor.getInt(idxId);
            if (id == category.getId()) {
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
     *
     * @param cursor
     * @return List of all category position (in a cursor) to except. Those categories are taken in db automatically.
     */
    private static Integer[] getAutoExceptCategoriesPositions(Cursor cursor) {
        final List<Integer> posToCheck = new ArrayList<Integer>();
        Operation o = new Operation();
        Integer[] cats = o.getExceptCategoriesAuto(StaticData.getCurrentAccount());
        for (int i = 0; i < cats.length; i++) {
            Category ca = new Category();
            Cursor cu = ca.fetch(cats[i]);
            ca.toDTO(cu);
            cu.close();
            posToCheck.add(getCategoryPositionInCursor(cursor, ca));
            TmhLogger.d(TAG, "  - Auto checked category : " + cats[i] + " (" + ca.getName() + ")");
        }

        return posToCheck.toArray(new Integer[0]);
    }

    public static Dialog popupDialogStatsDetails(Context context, Date beg, Date end, Integer[] categories, Integer[] exceptCategories) {
        if (context == null)
            return null;

        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialog_listview);
        final ListView list = (ListView) dialog.findViewById(R.id.list);

        // Adapter
        Operation dummyOp = new Operation();
        Account currentAccount = StaticData.getCurrentAccount();
        dummyOp.getFilter().addFilter(OperationFilter.FUNCTION_EQUAL, OperationData.KEY_ID_ACCOUNT, String.valueOf(currentAccount.getId()));

        // Filtering category if needed
        if (categories != null && categories.length > 0)
            dummyOp.getFilter().addFilter(OperationFilter.FUNCTION_IN, OperationData.KEY_ID_CATEGORY, TextUtils.join(",", categories));
        if (exceptCategories != null && exceptCategories.length > 0)
            dummyOp.getFilter().addFilter(OperationFilter.FUNCTION_NOTIN, OperationData.KEY_ID_CATEGORY, TextUtils.join(",", exceptCategories));

        // Getting cursor
        Cursor cursor = dummyOp.fetchByPeriod(beg, end, "o." + OperationData.KEY_AMOUNT + " ASC", -1);
        if (cursor != null && cursor.getCount() > 0) {
            OperationCursorAdapter sca = new OperationCursorAdapter(context,
                    R.layout.operation_item_nocheckbox,
                    cursor,
                    new String[]{"dateStringHours", "category", "amountString", "convertedAmount"},
                    new int[]{R.id.date, R.id.category, R.id.amount, R.id.convAmount});
            list.setAdapter(sca);

            dialog.show();
            return dialog;
        } else {
            return null;
        }
    }

    public static void popupDialogAbout(final FragmentActivity tmhFragment) {
        // Popup about dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(tmhFragment);

        PackageInfo pInfo = null;
        try {
            pInfo = TmhApplication.getAppContext().getPackageManager().getPackageInfo(tmhFragment.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            nnfe.printStackTrace();
        }

        String appVersion = pInfo.versionName;
        int appCode = pInfo.versionCode;
        StringBuilder sb = new StringBuilder();
        sb.append(tmhFragment.getString(R.string.about))
                .append("\n")
                .append("App ver: ").append(appVersion)
                .append("\n")
                .append("Code ver: ").append(appCode)
                .append("\n")
                .append("DB ver: ").append(DatabaseHelper.DATABASE_VERSION)
                .append("\n")
                .append(tmhFragment.getString(R.string.about_flags_copyright)).append(" ").append("www.icondrawer.com");

        builder.setMessage(sb.toString())
                .setTitle(tmhFragment.getString(R.string.about_title))
                .setIcon(R.drawable.tmh_icon_48)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }
}
