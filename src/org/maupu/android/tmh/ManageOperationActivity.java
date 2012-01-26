package org.maupu.android.tmh;

import java.util.Date;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.widget.IconCheckableCursorAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class ManageOperationActivity extends ManageableObjectActivity<Operation> {
	public static final String EXTRA_OPERATION_TYPE="type";
	private static Operation dummyOperation = new Operation();

	public ManageOperationActivity() {
		// No custom title bar because activity used in a TabHost
		super(R.string.activity_title_manage_operation, R.drawable.ic_stat_categories, AddOrEditOperationActivity.class, new Operation(), false);
	}

	@Override
	protected boolean validateConstraintsForDeletion(DatabaseHelper dbHelper, Operation obj) {
		return true;
	}

	@Override
	public void refreshDisplay(DatabaseHelper dbHelper) {
		String[] types = new String[]{OperationData.OPERATION_TYPE_DEFAULT};

		try {
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
			types = (String[]) bundle.get(ManageOperationActivity.EXTRA_OPERATION_TYPE);
		} catch (NullPointerException npe) {
			// No extras, keep default value
		}

		Cursor c = dummyOperation.fetchByMonth(dbHelper, new Date(), types);

		IconCheckableCursorAdapter adapter = new IconCheckableCursorAdapter(this, 
				R.layout.operation_item,
				c,
				new String[]{"icon", "account", "category", "dateString", "amountString", "euroAmount"},
				new int[]{R.id.icon, R.id.account, R.id.category, R.id.date, R.id.amount, R.id.euroAmount});
		super.setAdapter(adapter);
	}
}
