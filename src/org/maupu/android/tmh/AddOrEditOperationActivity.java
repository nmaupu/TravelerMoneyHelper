package org.maupu.android.tmh;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;

import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

public class AddOrEditOperationActivity extends AddOrEditActivity<Operation> {
	private DatePicker datePicker;
	private Spinner who;
	private Spinner category;
	private EditText amount;
	private Spinner currency;
	private Spinner operationType;

	public AddOrEditOperationActivity() {
		super(R.string.activity_title_edition_operation, R.drawable.ic_stat_categories, R.layout.add_or_edit_operation, new Operation());
	}
	
	@Override
	protected void initResources() {
		operationType = (Spinner)findViewById(R.id.operation_type);
		List<String> objs = new ArrayList<String>();
		objs.add(OperationData.OPERATION_TYPE_CASH);
		objs.add(OperationData.OPERATION_TYPE_WITHDRAWAL);
		objs.add(OperationData.OPERATION_TYPE_CREDITCARD);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, objs);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		operationType.setAdapter(adapter);

		datePicker = (DatePicker)findViewById(R.id.date);
		
		Calendar cal = Calendar.getInstance();
		int maxYear = cal.get(Calendar.YEAR);
		int maxMonth = cal.get(Calendar.MONTH);
		int maxDay = cal.get(Calendar.DAY_OF_MONTH);
		datePicker.init(maxYear, maxMonth, maxDay, null);

		who = (Spinner)findViewById(R.id.account);
		category = (Spinner)findViewById(R.id.category);
		amount = (EditText)findViewById(R.id.amount);
		currency = (Spinner)findViewById(R.id.currency);

		// Set spinners content
		Cursor c = null;

		Account dummyAccount = new Account();
		c = dummyAccount.fetchAll(super.dbHelper);
		who.setAdapter(getDefaultSpinnerCursorAdapter(c, AccountData.KEY_NAME));

		Category dummyCategory = new Category();
		c = dummyCategory.fetchAll(super.dbHelper);
		category.setAdapter(getDefaultSpinnerCursorAdapter(c, CategoryData.KEY_NAME));

		Currency dummyCurrency = new Currency();
		c = dummyCurrency.fetchAll(super.dbHelper);
		currency.setAdapter(getDefaultSpinnerCursorAdapter(c, CurrencyData.KEY_LONG_NAME));
	}

	@Override
	protected boolean validate() {
		return amount != null && amount.getText() != null && !"".equals(amount.getText().toString().trim());
	}

	@Override
	protected void baseObjectToFields(Operation obj) {
		if(obj != null) {
			initDatePicker(obj.getDate());
			if(obj.getAccount() !=null)
				setSpinnerPositionCursor(who, obj.getAccount().getName(), new Account());
			if(obj.getCategory() != null)
				setSpinnerPositionCursor(category, obj.getCategory().getName(), new Category());
			if(obj.getCurrency() != null)
				setSpinnerPositionCursor(currency, obj.getCurrency().getLongName(), new Currency());
			if(obj.getAmount() != null)
				amount.setText(""+obj.getAmount());
			else
				amount.setText("");
			if(obj.getType() != null)
				setSpinnerPositionString(operationType, obj.getType());
		} else {
			// Reset all fields
			initDatePicker(null);
			amount.setText("");
		}
	}

	@Override
	protected void fieldsToBaseObject(Operation obj) {
		if(obj != null) {
			int year = datePicker.getYear()-1900;
			int month = datePicker.getMonth();
			int day = datePicker.getDayOfMonth();
			obj.setDate(new Date(year, month, day));

			Cursor c = null;

			c = (Cursor)who.getSelectedItem();
			Account u = new Account();
			u.toDTO(super.dbHelper, c);
			obj.setAccount(u);

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
			
			obj.setType(operationType.getSelectedItem().toString());
		}
	}

	private void initDatePicker(Date d) {
		Calendar cal = Calendar.getInstance();
		if(d != null)
			cal.setTime(d);

		// init instead of update seems to work as expected ...
		datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
	}
}