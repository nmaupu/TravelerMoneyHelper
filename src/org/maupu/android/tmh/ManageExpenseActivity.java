package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Expense;
import org.maupu.android.tmh.ui.widget.UserIconCheckableCursorAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ManageExpenseActivity extends ManageableObjectActivity<Expense> {
	private static Expense dummyExpense = new Expense();
	
	public ManageExpenseActivity() {
		super("Expenses", R.drawable.ic_stat_categories, AddOrEditExpenseActivity.class, new Expense());
	}

	@Override
	protected boolean validateConstraintsForDeletion(DatabaseHelper dbHelper, Expense obj) {
		return true;
	}

	@Override
	protected void refreshListView(DatabaseHelper dbHelper) {
		Cursor c = dummyExpense.fetchAll(dbHelper);

		UserIconCheckableCursorAdapter adapter = new UserIconCheckableCursorAdapter(this, 
				R.layout.expense_item,
				c,
				new String[]{"icon", "user", "category", "dateString", "amountString", "tauxEuro"},
				new int[]{R.id.icon, R.id.username, R.id.category, R.id.date, R.id.amount, R.id.euroAmount});
		super.setAdapter(adapter);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.item_categories:
			startActivityFromMenu(ManageCategoryActivity.class);
			break;
		case R.id.item_currencies:
			startActivityFromMenu(ManageCurrencyActivity.class);
			break;
		case R.id.item_user:
			startActivityFromMenu(ManageUserActivity.class);
			break;
		case R.id.item_refresh:
			refreshListView(super.dbHelper);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
	
	private void startActivityFromMenu(Class<?> cls) {
		startActivity(new Intent(this, cls));
	}
}
