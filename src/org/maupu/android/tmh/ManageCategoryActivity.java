package org.maupu.android.tmh;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Category;

import android.database.Cursor;

public class ManageCategoryActivity extends ManageableObjectActivity<Category> {
	public ManageCategoryActivity() {
		super(R.string.activity_title_manage_category, R.drawable.ic_stat_categories, AddOrEditCategoryActivity.class, new Category(), true);
	}
	
	@Override
	protected boolean validateConstraintsForDeletion(DatabaseHelper dbHelper, Category obj) {
		int nb = dbHelper.getDb().query(OperationData.TABLE_NAME,
				new String[]{OperationData.KEY_ID}, 
				OperationData.KEY_ID_CATEGORY+"="+obj.getId(), null, null, null, null).getCount();
		
		return nb == 0;
	}

	@Override
	public void refreshDisplay(DatabaseHelper dbHelper) {
		Category category = new Category();
		Cursor cursor = category.fetchAll();
		
		super.setAdapter(R.layout.category_item, 
				cursor, 
				new String[]{CategoryData.KEY_NAME}, 
				new int[]{R.id.name});
	}
}
