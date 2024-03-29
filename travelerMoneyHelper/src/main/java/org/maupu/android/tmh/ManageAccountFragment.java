package org.maupu.android.tmh;

import android.database.Cursor;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.ui.ApplicationDrawer;
import org.maupu.android.tmh.ui.async.AbstractAsyncTask;
import org.maupu.android.tmh.ui.widget.IconCheckableCursorAdapter;

import java.util.HashMap;
import java.util.Map;

public class ManageAccountFragment extends ManageableObjectFragment<Account> {
    private static final String TAG = ManageAccountFragment.class.getName();
    private IconCheckableCursorAdapter iconCheckableCursorAdapter = null;

    public ManageAccountFragment() {
        super(R.string.fragment_title_manage_account, AddOrEditAccountFragment.class, new Account(), true);
    }

    @Override
    protected boolean validateConstraintsForDeletion(Account obj) {
        int nb = TmhApplication.getDatabaseHelper().getDb().query(OperationData.TABLE_NAME,
                new String[]{OperationData.KEY_ID},
                OperationData.KEY_ID_ACCOUNT + "=" + obj.getId(), null, null, null, null).getCount();
        return nb == 0;
    }

    @Override
    public void onDestroy() {
        closeIconCheckableCursorAdapterIfNeeded();
        super.onDestroy();
    }

    @Override
    protected void onClickUpdate(Integer[] objs) {
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground(AbstractAsyncTask asyncTask) {
        Account account = new Account();
        Cursor c = account.fetchAll();

        Map<Integer, Object> results = new HashMap<>();
        results.put(0, c);

        return results;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        Cursor c = (Cursor) results.get(0);

        closeIconCheckableCursorAdapterIfNeeded();

        // custom custom cursor adapter lol :D
        iconCheckableCursorAdapter = new IconCheckableCursorAdapter(
                requireContext(),
                R.layout.icon_name_item,
                c,
                new String[]{AccountData.KEY_ICON_BYTES, AccountData.KEY_NAME},
                new int[]{R.id.icon, R.id.name});
        super.setAdapter(iconCheckableCursorAdapter);
    }

    private void closeIconCheckableCursorAdapterIfNeeded() {
        try {
            iconCheckableCursorAdapter.getCursor().close();
        } catch (NullPointerException npe) {
            // nothing to be done
        }
    }

    @Override
    protected void onItemDelete() {
        super.onItemDelete();
        ApplicationDrawer.getInstance().refreshAccountsProfile();
    }
}
