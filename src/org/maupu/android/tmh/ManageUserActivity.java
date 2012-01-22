package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.ExpenseData;
import org.maupu.android.tmh.database.UserData;
import org.maupu.android.tmh.database.object.User;
import org.maupu.android.tmh.ui.widget.UserIconCheckableCursorAdapter;

import android.database.Cursor;

public class ManageUserActivity extends ManageableObjectActivity<User>{
	public ManageUserActivity() {
		super(R.string.activity_title_manage_user, R.drawable.ic_stat_categories, AddOrEditUserActivity.class, new User(), true);
	}
	
	@Override
	protected boolean validateConstraintsForDeletion(DatabaseHelper dbHelper, User obj) {
		int nb = dbHelper.getDb().query(ExpenseData.TABLE_NAME, 
				new String[]{ExpenseData.KEY_ID},
				ExpenseData.KEY_ID_USER+"="+obj.getId(), null, null, null, null).getCount();
		return nb == 0;
	}

	@Override
	public void refreshDisplay(DatabaseHelper dbHelper) {
		User user = new User();
		Cursor cursor = user.fetchAll(dbHelper);
		
		// custom custom cursor adapter lol :D
		UserIconCheckableCursorAdapter adapter = new UserIconCheckableCursorAdapter(this, R.layout.user_item, cursor, 
				new String[]{UserData.KEY_ICON, UserData.KEY_NAME}, 
				new int[]{R.id.icon, R.id.name});
		super.setAdapter(adapter);
	}
}
