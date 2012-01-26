package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.ui.widget.IconCheckableCursorAdapter;

import android.database.Cursor;

public class ManageAccountActivity extends ManageableObjectActivity<Account>{
	public ManageAccountActivity() {
		super(R.string.activity_title_manage_account, R.drawable.ic_stat_categories, AddOrEditAccountActivity.class, new Account(), true);
	}
	
	@Override
	protected boolean validateConstraintsForDeletion(DatabaseHelper dbHelper, Account obj) {
		int nb = dbHelper.getDb().query(OperationData.TABLE_NAME, 
				new String[]{OperationData.KEY_ID},
				OperationData.KEY_ID_ACCOUNT+"="+obj.getId(), null, null, null, null).getCount();
		return nb == 0;
	}

	@Override
	public void refreshDisplay(DatabaseHelper dbHelper) {
		Account account = new Account();
		Cursor cursor = account.fetchAll(dbHelper);
		
		// custom custom cursor adapter lol :D
		IconCheckableCursorAdapter adapter = new IconCheckableCursorAdapter(this, R.layout.account_item, cursor, 
				new String[]{AccountData.KEY_ICON, AccountData.KEY_NAME}, 
				new int[]{R.id.icon, R.id.name});
		super.setAdapter(adapter);
	}
}
