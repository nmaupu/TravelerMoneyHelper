package org.maupu.android.tmh;

import java.util.Date;
import java.util.GregorianCalendar;

import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.SpinnerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class WithdrawalActivity extends TmhActivity implements OnItemSelectedListener, OnClickListener {
	private Spinner spinnerFrom;
	private Spinner spinnerTo;
	private Spinner spinnerCurrency;
	private Spinner spinnerCategory;
	private SpinnerManager spinnerManagerFrom;
	private SpinnerManager spinnerManagerTo;
	private SpinnerManager spinnerManagerCurrency;
	private SpinnerManager spinnerManagerCategory;
	private Button buttonValidate;
	private EditText amountEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.withdrawal);

		spinnerFrom = (Spinner)findViewById(R.id.spinner_from);
		spinnerTo = (Spinner)findViewById(R.id.spinner_to);
		spinnerCurrency = (Spinner)findViewById(R.id.spinner_currency);
		spinnerCategory = (Spinner)findViewById(R.id.spinner_category);
		buttonValidate = (Button)findViewById(R.id.button_validate);
		amountEditText = (EditText)findViewById(R.id.amount);

		spinnerTo.setOnItemSelectedListener(this);
		buttonValidate.setOnClickListener(this);

		initSpinnerManagers();
	}

	private void initSpinnerManagers() {
		spinnerManagerFrom = new SpinnerManager(this, spinnerFrom);
		spinnerManagerTo = new SpinnerManager(this, spinnerTo);
		spinnerManagerCurrency = new SpinnerManager(this, spinnerCurrency);
		spinnerManagerCategory = new SpinnerManager(this, spinnerCategory);

		Account account = new Account();
		Cursor cursor = account.fetchAll();
		spinnerManagerFrom.setAdapter(cursor, AccountData.KEY_NAME);

		cursor = account.fetchAll();
		spinnerManagerTo.setAdapter(cursor, AccountData.KEY_NAME);

		Currency currency = new Currency();
		cursor = currency.fetchAll();
		spinnerManagerCurrency.setAdapter(cursor, CurrencyData.KEY_LONG_NAME);

		Category category = new Category();
		cursor = category.fetchAll();
		spinnerManagerCategory.setAdapter(cursor, CategoryData.KEY_NAME);

		// Getting default category for withdrawal from preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		try {
			int idCategory = Integer.parseInt(prefs.getString("category", null));
			if(idCategory != -1) {
				Cursor c = category.fetch(idCategory);
				category.toDTO(c);
				spinnerManagerCategory.setSpinnerPositionCursor(category.getName(), new Category());
			}
		} catch (NumberFormatException nfe) {
			Log.e("WithdrawalActivity", "Problem parsing preferences category for withdrawal, not set ?");
		}


		// From
		Account current = StaticData.getCurrentAccount();
		spinnerManagerFrom.setSpinnerPositionCursor(current.getName(), new Account());
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Disable add button for this activity
		MenuItem mi = menu.findItem(R.id.item_add);
		if(mi != null)
			mi.setEnabled(false);
		return true;
	}

	@Override
	public void refreshDisplay() {}

	@Override
	protected Intent onAddClicked() {
		return null;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if(parent.getId() == R.id.spinner_to) {
			// Select corresponding currency to 'to' account

			Cursor item = spinnerManagerTo.getSelectedItem();
			int idxCurrencyId = item.getColumnIndexOrThrow(AccountData.KEY_ID_CURRENCY);
			int currencyId = item.getInt(idxCurrencyId);

			Currency currency = new Currency();
			Cursor c = currency.fetch(currencyId);
			currency.toDTO(c);

			spinnerManagerCurrency.setSpinnerPositionCursor(currency.getLongName(), new Currency());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public void onClick(View v) {
		if(v.getId() == buttonValidate.getId()) {
			if(!validate()) {
				SimpleDialog.errorDialog(this, getString(R.string.error), getString(R.string.error_add_object)).show();
			} else {
				Account accountFrom = new Account();
				Cursor cursor = spinnerManagerFrom.getSelectedItem();
				accountFrom.toDTO(cursor);

				Account accountTo = new Account();
				cursor = spinnerManagerTo.getSelectedItem();
				accountTo.toDTO(cursor);

				Category category = new Category();
				cursor = spinnerManagerCategory.getSelectedItem();
				category.toDTO(cursor);

				Float amount = Float.valueOf(amountEditText.getText().toString().trim());

				Currency currency = new Currency();
				cursor = spinnerManagerCurrency.getSelectedItem();
				currency.toDTO(cursor);

				Date now = new GregorianCalendar().getTime();
				// First, we debit from account
				Operation operationFrom = new Operation();
				operationFrom.setAccount(accountFrom);
				operationFrom.setAmount(-1f * amount);
				operationFrom.setCategory(category);
				operationFrom.setCurrency(currency);
				operationFrom.setDate(now);
				operationFrom.setCurrencyValueOnCreated(currency.getTauxEuro());
				operationFrom.insert();

				// Second, we credit 'to' account
				Operation operationTo = new Operation();
				operationTo.setAccount(accountTo);
				operationTo.setAmount(amount);
				operationTo.setCategory(category);
				operationTo.setCurrency(currency);
				operationTo.setDate(now);
				operationTo.setCurrencyValueOnCreated(currency.getTauxEuro());
				operationTo.insert();
				
				Operation.linkTwoOperations(operationFrom, operationTo);

				// Dispose activity
				finish();
			}
		}
	}
	
	protected boolean validate() {
		return amountEditText.getText() != null && ! "".equals(amountEditText.getText().toString().trim());
	}
}
