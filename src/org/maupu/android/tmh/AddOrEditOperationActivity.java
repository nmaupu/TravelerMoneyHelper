package org.maupu.android.tmh;

import java.util.Calendar;
import java.util.Date;

import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.widget.SpinnerManager;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class AddOrEditOperationActivity extends AddOrEditActivity<Operation> implements OnCheckedChangeListener {
	private DatePicker datePicker;
	private SpinnerManager smAccount;
	private SpinnerManager smCategory;
	private SpinnerManager smCurrency;
	private EditText amount;
	private RadioButton radioButtonDebit;
	private RadioButton radioButtonCredit;
	private TextView textViewSign;
	private static final String PLUS="+";
	private static final String MINUS="-";

	public AddOrEditOperationActivity() {
		super(R.string.activity_title_edition_operation, R.drawable.ic_stat_categories, R.layout.add_or_edit_operation, new Operation());
	}

	@Override
	protected void initResources() {
		datePicker = (DatePicker)findViewById(R.id.date);

		Calendar cal = Calendar.getInstance();
		int maxYear = cal.get(Calendar.YEAR);
		int maxMonth = cal.get(Calendar.MONTH);
		int maxDay = cal.get(Calendar.DAY_OF_MONTH);
		datePicker.init(maxYear, maxMonth, maxDay, null);

		smAccount = new SpinnerManager(this, (Spinner)findViewById(R.id.account));
		smCategory = new SpinnerManager(this, (Spinner)findViewById(R.id.category));
		amount = (EditText)findViewById(R.id.amount);
		smCurrency = new SpinnerManager(this, (Spinner)findViewById(R.id.currency));

		radioButtonCredit = (RadioButton)findViewById(R.id.credit);
		radioButtonDebit = (RadioButton)findViewById(R.id.debit);
		radioButtonCredit.setOnCheckedChangeListener(this);
		radioButtonDebit.setOnCheckedChangeListener(this);
		textViewSign = (TextView)findViewById(R.id.sign);

		// Set spinners content
		Cursor c = null;

		Account dummyAccount = new Account();
		c = dummyAccount.fetchAll(super.dbHelper);
		smAccount.setAdapter(c, AccountData.KEY_NAME);
		
		// Set spinner to current account given by extras
		Bundle b = this.getIntent().getExtras();
		Account currentAccount = (Account)b.get("account");
		if(currentAccount != null)
			smAccount.setSpinnerPositionCursor(dbHelper, currentAccount.toString(), new Account());

		Category dummyCategory = new Category();
		c = dummyCategory.fetchAll(super.dbHelper);
		smCategory.setAdapter(c, CategoryData.KEY_NAME);

		Currency dummyCurrency = new Currency();
		c = dummyCurrency.fetchAll(super.dbHelper);
		smCurrency.setAdapter(c, CurrencyData.KEY_LONG_NAME);
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
				smAccount.setSpinnerPositionCursor(dbHelper, obj.getAccount().getName(), new Account());
			if(obj.getCategory() != null)
				smCategory.setSpinnerPositionCursor(dbHelper, obj.getCategory().getName(), new Category());
			if(obj.getCurrency() != null)
				smCurrency.setSpinnerPositionCursor(dbHelper, obj.getCurrency().getLongName(), new Currency());
			if(obj.getAmount() != null) {
				amount.setText(""+Math.abs(obj.getAmount()));
				if(obj.getAmount() >= 0.0f)
					radioButtonCredit.setChecked(true);
				else
					radioButtonDebit.setChecked(true);
			} else {
				amount.setText("");
			}
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

			c = smAccount.getSelectedItem();
			Account u = new Account();
			u.toDTO(super.dbHelper, c);
			obj.setAccount(u);

			c = smCategory.getSelectedItem();
			Category cat = new Category();
			cat.toDTO(super.dbHelper, c);
			obj.setCategory(cat);

			c = smCurrency.getSelectedItem();
			Currency cur = new Currency();
			cur.toDTO(super.dbHelper, c);
			obj.setCurrency(cur);

			obj.setAmount(Float.valueOf(amount.getText().toString().trim()));
			if(radioButtonDebit.isChecked())
				obj.setAmount(obj.getAmount()*-1);

			// Store currency value for this addition
			obj.setCurrencyValueOnCreated(cur.getTauxEuro());
		}
	}

	private void initDatePicker(Date d) {
		Calendar cal = Calendar.getInstance();
		if(d != null)
			cal.setTime(d);

		// init instead of update seems to work as expected ...
		datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Log.d("AddOrEditOperationActivity", "RadioButton check changed");
		if(radioButtonCredit.isChecked()) {
			textViewSign.setText(PLUS);
			textViewSign.setTextColor(Operation.COLOR_POSITIVE_AMOUNT);
		} else {
			textViewSign.setText(MINUS);
			textViewSign.setTextColor(Operation.COLOR_NEGATIVE_AMOUNT);
		}
	}
}