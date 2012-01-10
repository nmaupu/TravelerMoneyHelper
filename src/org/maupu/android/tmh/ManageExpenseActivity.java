package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Expense;
import org.maupu.android.tmh.ui.widget.UserIconCheckableCursorAdapter;

import android.database.Cursor;

public class ManageExpenseActivity extends ManageableObjectActivity<Expense> {
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
		Cursor c = dummyExpense.fetchAll(dbHelper);

		UserIconCheckableCursorAdapter adapter = new UserIconCheckableCursorAdapter(this, 
				R.layout.expense_item,
				c,
				new String[]{"icon", "user", "category", "dateString", "amountString", "tauxEuro"},
				new int[]{R.id.icon, R.id.username, R.id.category, R.id.date, R.id.amount, R.id.euroAmount});
		super.setAdapter(adapter);
	}
}
