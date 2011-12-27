package org.maupu.android.tmh;

import org.maupu.android.R;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.ExpenseData;
import org.maupu.android.tmh.database.object.Category;

import android.database.Cursor;
import android.os.Bundle;

public class ManageCategoryActivity extends ManageableObjectActivity {
	private DatabaseHelper dbHelper = new DatabaseHelper(this);

	public ManageCategoryActivity() {
		super("Categories", R.drawable.ic_stat_categories, AddOrEditCategoryActivity.class);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper.openWritable();
		refreshListView();
	}
	
	@Override
	protected boolean delete(int itemId) {
		Category category = new Category();
		Cursor cursor = category.fetch(dbHelper, itemId);
		category.toDTO(dbHelper, cursor);
		
		int nb = dbHelper.getDb().query(ExpenseData.TABLE_NAME,
				new String[]{ExpenseData.KEY_ID}, 
				ExpenseData.KEY_ID_CATEGORY+"="+itemId, null, null, null, null).getCount();
		
		if(nb == 0)
			return category.delete(dbHelper);
		else
			return false;

	}

	@Override
	protected void refreshListView() {
		Category category = new Category();
		Cursor cursor = category.fetchAll(dbHelper);
		
		super.setAdapter(R.layout.category_item, 
				cursor, 
				new String[]{CategoryData.KEY_NAME}, 
				new int[]{R.id.name});
	}

}
