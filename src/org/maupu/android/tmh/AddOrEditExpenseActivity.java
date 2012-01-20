package org.maupu.android.tmh;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.ExpenseData;
import org.maupu.android.tmh.database.UserData;
import org.maupu.android.tmh.database.object.BaseObject;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Expense;
import org.maupu.android.tmh.database.object.User;

import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class AddOrEditExpenseActivity extends AddOrEditActivity<Expense> {
	private DatePicker datePicker;
	private Spinner who;
	private Spinner category;
	private EditText amount;
	private Spinner currency;
	private Spinner expenseType;

	public AddOrEditExpenseActivity() {
		super("Expense edition", R.drawable.ic_stat_categories, R.layout.add_or_edit_expense, new Expense());
	}
	
	@Override
	protected void initResources() {
		expenseType = (Spinner)findViewById(R.id.expense_type);
		List<String> objs = new ArrayList<String>();
		objs.add(ExpenseData.EXPENSE_TYPE_CASH);
		objs.add(ExpenseData.EXPENSE_TYPE_WITHDRAWAL);
		objs.add(ExpenseData.EXPENSE_TYPE_CREDITCARD);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, objs);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		expenseType.setAdapter(adapter);

		datePicker = (DatePicker)findViewById(R.id.date);
		Calendar cal = Calendar.getInstance();
		int maxYear = cal.get(Calendar.YEAR);
		int maxMonth = cal.get(Calendar.MONTH);
		int maxDay = cal.get(Calendar.DAY_OF_MONTH);
		datePicker.init(maxYear, maxMonth, maxDay, null);

		who = (Spinner)findViewById(R.id.user);
		category = (Spinner)findViewById(R.id.category);
		amount = (EditText)findViewById(R.id.amount);
		currency = (Spinner)findViewById(R.id.currency);

		// Set spinners content
		Cursor c = null;

		User dummyUser = new User();
		c = dummyUser.fetchAll(super.dbHelper);
		who.setAdapter(getDefaultSpinnerCursorAdapter(c, UserData.KEY_NAME));

		Category dummyCategory = new Category();
		c = dummyCategory.fetchAll(super.dbHelper);
		category.setAdapter(getDefaultSpinnerCursorAdapter(c, CategoryData.KEY_NAME));

		Currency dummyCurrency = new Currency();
		c = dummyCurrency.fetchAll(super.dbHelper);
		currency.setAdapter(getDefaultSpinnerCursorAdapter(c, CurrencyData.KEY_LONG_NAME));
	}

	private SpinnerAdapter getDefaultSpinnerCursorAdapter(Cursor c, String from) {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				c, new String[]{from}, new int[]{android.R.id.text1});
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	@Override
	protected boolean validate() {
		return amount != null && amount.getText() != null && !"".equals(amount.getText().toString().trim());
	}

	@Override
	protected void baseObjectToFields(Expense obj) {
		if(obj != null) {
			initDatePicker(obj.getDate());
			if(obj.getUser() !=null)
				setSpinnerPositionCursor(who, obj.getUser().getName(), new User());
			if(obj.getCategory() != null)
				setSpinnerPositionCursor(category, obj.getCategory().getName(), new Category());
			if(obj.getCurrency() != null)
				setSpinnerPositionCursor(currency, obj.getCurrency().getLongName(), new Currency());
			if(obj.getAmount() != null)
				amount.setText(""+obj.getAmount());
			else
				amount.setText("");
			if(obj.getType() != null)
				setSpinnerPositionString(expenseType, obj.getType());
		} else {
			// Reset all fields
			initDatePicker(null);
			amount.setText("");
		}
	}

	@Override
	protected void fieldsToBaseObject(Expense obj) {
		if(obj != null) {
			int year = datePicker.getYear()-1900;
			int month = datePicker.getMonth();
			int day = datePicker.getDayOfMonth();
			obj.setDate(new Date(year, month, day));

			Cursor c = null;

			c = (Cursor)who.getSelectedItem();
			User u = new User();
			u.toDTO(super.dbHelper, c);
			obj.setUser(u);

			c = (Cursor)category.getSelectedItem();
			Category cat = new Category();
			cat.toDTO(super.dbHelper, c);
			obj.setCategory(cat);

			c = (Cursor)currency.getSelectedItem();
			Currency cur = new Currency();
			cur.toDTO(super.dbHelper, c);
			obj.setCurrency(cur);

			obj.setAmount(Float.valueOf(amount.getText().toString().trim()));
			// Store currency value for this addition
			obj.setCurrencyValueOnCreated(cur.getTauxEuro());
			
			obj.setType(expenseType.getSelectedItem().toString());
		}
	}

	private void initDatePicker(Date d) {
		Calendar cal = Calendar.getInstance();
		if(d != null)
			cal.setTime(d);

		// TODO bug : cannot set datePicker date
		datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * Set spinner to position corresponding to value
	 * @param spinner
	 * @param value
	 */
	private void setSpinnerPositionCursor(Spinner spinner, String value, BaseObject dummy) {
		// Finding value's position
		int count = spinner.getCount();
		SimpleCursorAdapter adapter = (SimpleCursorAdapter)spinner.getAdapter();

		for(int i=0; i<count; i++) {
			Cursor c = (Cursor)adapter.getItem(i);
			dummy.toDTO(super.dbHelper, c);
			if(isSpinnerValueEqualsToBaseObject(dummy, value)) {
				spinner.setSelection(i, true);
				break;
			}
		}
	}
	
	private void setSpinnerPositionString(Spinner spinner, String value) {
		int count = spinner.getCount();
		SpinnerAdapter adapter = spinner.getAdapter();
		
		for(int i=0; i<count; i++) {
			String val = (String)adapter.getItem(i);
			if(val.equals(value))
				spinner.setSelection(i, true);
		}
	}

	private boolean isSpinnerValueEqualsToBaseObject(BaseObject bo, String value) {
		if(bo instanceof User) {
			return value.equals(((User)bo).getName());
		} else if (bo instanceof Category) {
			return value.equals(((Category)bo).getName());
		} else if (bo instanceof Currency) {
			return value.equals(((Currency)bo).getLongName());
		}

		return false;
	}
}