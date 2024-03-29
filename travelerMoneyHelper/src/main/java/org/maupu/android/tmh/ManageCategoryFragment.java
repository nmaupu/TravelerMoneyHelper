package org.maupu.android.tmh;

import android.database.Cursor;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AbstractAsyncTask;

import java.util.HashMap;
import java.util.Map;

public class ManageCategoryFragment extends ManageableObjectFragment<Category> {
    public ManageCategoryFragment() {
        super(R.string.fragment_title_manage_category, AddOrEditCategoryFragment.class, new Category(), true);
    }

    @Override
    protected boolean validateConstraintsForDeletion(Category obj) {
        if (obj == null || obj.getId() == null)
            return true;

        int nb = TmhApplication.getDatabaseHelper().getDb().query(OperationData.TABLE_NAME,
                new String[]{OperationData.KEY_ID},
                OperationData.KEY_ID_CATEGORY + "=" + obj.getId(), null, null, null, null).getCount();

        Category withdrawalCat = StaticData.getWithdrawalCategory();
        if (withdrawalCat != null && withdrawalCat.getId() != null && withdrawalCat.getId() == obj.getId()) {
            // Unset withdrawal cat because we are deleting it
            StaticData.setWithdrawalCategory(null);
        }

        return nb == 0;
    }

    @Override
    public void onDestroy() {
        closeCursorIfNeeded();
        super.onDestroy();
    }

    @Override
    protected void onClickUpdate(Integer[] objs) {
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground(AbstractAsyncTask asyncTask) {
        Category category = new Category();
        Cursor c = category.fetchAll();

        Map<Integer, Object> results = new HashMap<Integer, Object>();
        results.put(0, c);

        return results;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        Cursor c = (Cursor) results.get(0);

        closeCursorIfNeeded();

        super.setAdapter(R.layout.category_item,
                c,
                new String[]{CategoryData.KEY_NAME},
                new int[]{R.id.name});
    }

    private void closeCursorIfNeeded() {
        try {
            super.getAdapter().getCursor().close();
        } catch (NullPointerException npe) {
            // Nothing to be done
        }
    }
}
