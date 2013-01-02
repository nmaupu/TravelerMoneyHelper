package org.maupu.android.tmh;

import java.util.HashMap;
import java.util.Map;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.ui.widget.IconCheckableCursorAdapter;

import android.database.Cursor;

public class ManageAccountActivity extends ManageableObjectActivity<Account>{
	public ManageAccountActivity() {
		super(R.string.activity_title_manage_account, AddOrEditAccountActivity.class, new Account(), true);
	}

	@Override
	protected boolean validateConstraintsForDeletion(Account obj) {
		int nb = TmhApplication.getDatabaseHelper().getDb().query(OperationData.TABLE_NAME, 
				new String[]{OperationData.KEY_ID},
				OperationData.KEY_ID_ACCOUNT+"="+obj.getId(), null, null, null, null).getCount();
		return nb == 0;
	}

	@Override
	protected void onClickUpdate(Integer[] objs) {}

	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		Account account = new Account();
		Cursor c = account.fetchAll();

		Map<Integer, Object> results = new HashMap<Integer, Object>();
		results.put(0, c);

		return results;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> results) {
		Cursor c = (Cursor)results.get(0);

		// custom custom cursor adapter lol :D
		IconCheckableCursorAdapter adapter = new IconCheckableCursorAdapter(
				this, R.layout.icon_name_item,
				c, 
				new String[]{AccountData.KEY_ICON, AccountData.KEY_NAME}, 
				new int[]{R.id.icon, R.id.name});
		super.setAdapter(adapter);
	}
}
