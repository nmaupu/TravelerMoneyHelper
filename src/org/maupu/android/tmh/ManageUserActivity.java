package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.ExpenseData;
import org.maupu.android.tmh.database.UserData;
import org.maupu.android.tmh.database.object.User;
import org.maupu.android.tmh.ui.widget.UserCheckableCursorAdapter;

import android.database.Cursor;

public class ManageUserActivity extends ManageableObjectActivity<User>{
	public ManageUserActivity() {
		super("Users", R.drawable.ic_stat_categories, AddOrEditUserActivity.class, new User());
	}
	
	@Override
	protected boolean validateConstraintsForDeletion(DatabaseHelper dbHelper, User obj) {
		int nb = dbHelper.getDb().query(ExpenseData.TABLE_NAME, 
				new String[]{ExpenseData.KEY_ID},
				ExpenseData.KEY_ID_USER+"="+obj.getId(), null, null, null, null).getCount();
		return nb == 0;
	}

	@Override
	protected void refreshListView(DatabaseHelper dbHelper) {
		User user = new User();
		Cursor cursor = user.fetchAll(dbHelper);
		
		// custom custom cursor adapter lol :D
		UserCheckableCursorAdapter adapter = new UserCheckableCursorAdapter(this, R.layout.user_item, cursor, 
				new String[]{UserData.KEY_ICON, UserData.KEY_NAME}, 
				new int[]{R.id.icon, R.id.name});
		super.setAdapter(adapter);
	}
}
