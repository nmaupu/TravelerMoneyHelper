package org.maupu.android.tmh;

import org.maupu.android.R;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Expense;
import org.maupu.android.tmh.ui.CustomTitleBar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TravelerMoneyHelperActivity extends Activity {
	private DatabaseHelper dbHelper = new DatabaseHelper(this);
	private ListView listView;
	private TextView tvEmpty;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(R.layout.expense_activity);
		customTB.setName("Expense");
		customTB.setIcon(R.drawable.ic_stat_categories);
		
		// Get widgets
		this.tvEmpty = (TextView)findViewById(R.id.expense_empty);
		tvEmpty.setText("");
		this.listView = (ListView)findViewById(R.id.expense_list);

		// Open db
		dbHelper.openWritable();


		// Create test data
		/*
		User user = new User();
		user.setName("Nicolas");
		user.insert(dbHelper);

		user.setName("Marianne");
		user.insert(dbHelper);

		Category category = new Category();
		category.setName("Course");
		category.insert(dbHelper);

		category.setName("Loisir");
		category.insert(dbHelper);

		Currency currency = new Currency();
		currency.setLongName("Euro");
		currency.setShortName("e");
		currency.setTauxEuro(1.0f);
		currency.insert(dbHelper);

		Cursor c;
		c= category.fetchAll(dbHelper); c.moveToFirst();
		category.toDTO(dbHelper, c);
		c = currency.fetchAll(dbHelper); c.moveToFirst();
		currency.toDTO(dbHelper, c);
		c = user.fetchAll(dbHelper); c.moveToFirst();
		user.toDTO(dbHelper, c);
		Expense expense = new Expense();
		expense.setCategory(category);
		expense.setUser(user);
		expense.setCurrency(currency);
		expense.setAmount(12.0f);
		expense.setDate(new Date());
		expense.insert(dbHelper);

		expense.setAmount(12.50f);
		expense.insert(dbHelper);

		expense.setAmount(10.0f);
		expense.insert(dbHelper);
		//
		*/


		fillData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		
		switch(item.getItemId()) {
		case R.id.item_categories:
			intent = new Intent(this, ManageCategoryActivity.class);
	        startActivity(intent);
			break;
		case R.id.item_currencies:
			intent = new Intent(this, ManageCurrencyActivity.class);
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private void fillData() {
		Expense dummyExpense = new Expense();
		Cursor data = dummyExpense.fetchAll(dbHelper);
		startManagingCursor(data);
		
		if(data.getCount() == 0) {
			tvEmpty.setText("No expense to display");
			listView.setAdapter(null);
		} else {
			// Create list adapter
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.expense_item, data,
					new String[]{"user", "category", "date", "amount", "tauxEuro"},
					new int[]{R.id.icon, R.id.category, R.id.date, R.id.amount, R.id.euroAmount});
			listView.setAdapter(adapter);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbHelper != null) {
			dbHelper.close();
		}
	}
}