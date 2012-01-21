package org.maupu.android.tmh;

import java.util.Date;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.ExpenseData;
import org.maupu.android.tmh.database.object.Expense;
import org.maupu.android.tmh.ui.widget.UserIconCheckableCursorAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class ManageExpenseActivity extends ManageableObjectActivity<Expense> {
	public static final String EXTRA_EXPENSE_TYPE="type";
	private static Expense dummyExpense = new Expense();

	public ManageExpenseActivity() {
		// No custom title bar because activity used in a TabHost
		super("Expenses", R.drawable.ic_stat_categories, AddOrEditExpenseActivity.class, new Expense(), false);
	}

	@Override
	protected boolean validateConstraintsForDeletion(DatabaseHelper dbHelper, Expense obj) {
		return true;
	}

	@Override
	public void refreshDisplay(DatabaseHelper dbHelper) {
		String[] types = new String[]{ExpenseData.EXPENSE_TYPE_DEFAULT};

		try {
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
			types = (String[]) bundle.get(ManageExpenseActivity.EXTRA_EXPENSE_TYPE);
		} catch (NullPointerException npe) {
			// No extras, keep default value
		}

		Cursor c = dummyExpense.fetchByMonth(dbHelper, new Date(), types);

		UserIconCheckableCursorAdapter adapter = new UserIconCheckableCursorAdapter(this, 
				R.layout.expense_item,
				c,
				new String[]{"icon", "user", "category", "dateString", "amountString", "euroAmount"},
				new int[]{R.id.icon, R.id.username, R.id.category, R.id.date, R.id.amount, R.id.euroAmount});
		super.setAdapter(adapter);
	}
}
