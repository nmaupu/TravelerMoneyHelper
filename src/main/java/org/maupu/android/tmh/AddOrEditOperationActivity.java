package org.maupu.android.tmh;

import java.util.Calendar;
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
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.NumberUtil;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class AddOrEditOperationActivity extends AddOrEditActivity<Operation> implements OnCheckedChangeListener, OnClickListener, OnDateSetListener {
	private static final int DATE_DIALOG_ID = 0;
	//private DatePicker datePicker;
	private int mYear;
    private int mMonth;
    private int mDay;

	private SpinnerManager smAccount;
	private SpinnerManager smCategory;
	private SpinnerManager smCurrency;
	private EditText amount;
	private RadioButton radioButtonDebit;
	private RadioButton radioButtonCredit;
	private CheckBox checkboxUpdateRate;
	private LinearLayout linearLayoutRateUpdater;
	private TextView textViewSign;
	private TextView textViewDate;
	private Button buttonToday;
	private static final String PLUS="+";
	private static final String MINUS="-";

	public AddOrEditOperationActivity() {
		super(R.string.activity_title_edition_operation, R.layout.add_or_edit_operation, new Operation());
	}

	@Override
	protected void initResources() {
		smAccount = new SpinnerManager(this, (Spinner)findViewById(R.id.account));
		smCategory = new SpinnerManager(this, (Spinner)findViewById(R.id.category));
		amount = (EditText)findViewById(R.id.amount);
		linearLayoutRateUpdater = (LinearLayout)findViewById(R.id.ll_exchange_rate);
		checkboxUpdateRate = (CheckBox)findViewById(R.id.checkbox_update_rate);
		smCurrency = new SpinnerManager(this, (Spinner)findViewById(R.id.currency));

		radioButtonCredit = (RadioButton)findViewById(R.id.credit);
		radioButtonDebit = (RadioButton)findViewById(R.id.debit);
		radioButtonCredit.setOnCheckedChangeListener(this);
		radioButtonDebit.setOnCheckedChangeListener(this);
		textViewSign = (TextView)findViewById(R.id.sign);
		textViewDate = (TextView)findViewById(R.id.date);
		textViewDate.setOnClickListener(this);
		buttonToday = (Button)findViewById(R.id.button_today);
		buttonToday.setOnClickListener(this);
		

		// Set spinners content
		Cursor c = null;

		Account dummyAccount = new Account();
		c = dummyAccount.fetchAll();
		smAccount.setAdapter(c, AccountData.KEY_NAME);
		
		Account currentAccount = StaticData.getCurrentAccount();
		if(currentAccount != null)
			smAccount.setSpinnerPositionCursor(currentAccount.toString(), new Account());

		Category dummyCategory = new Category();
		Category withdrawalCat = StaticData.getWithdrawalCategory();
		if(withdrawalCat == null) {
			c = dummyCategory.fetchAll();
		} else if(getObj() != null && getObj().getCategory() != null && getObj().getCategory().getId() != null) {
			// We are currently editing ...
			if(withdrawalCat.getId().equals(super.getObj().getCategory().getId())) {
				c = dummyCategory.fetchAll(); // ... a withdrawal
				smCategory.getSpinner().setEnabled(false);
			} else {
				c = dummyCategory.fetchAllExcept(new Integer[]{withdrawalCat.getId()}); // ... non withdrawal operation
			}
		} else {
			// Adding an operation
			c = dummyCategory.fetchAllExcept(new Integer[]{withdrawalCat.getId()});
			final Activity activity = this;
			if(c.getCount() == 0) {
				// No category created yet, cannot continue !
				SimpleDialog.errorDialog(this, getString(R.string.error), getString(R.string.error_no_category), new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(activity, AddOrEditCategoryActivity.class));
						activity.finish();
					}
				}).show();
			}
		}
			
		smCategory.setAdapter(c, CategoryData.KEY_NAME);
		// Set spinner category to current selected one if exists
		Category cat = StaticData.getCurrentSelectedCategory();
		if(cat != null && cat.getName() != null)
			smCategory.setSpinnerPositionCursor(cat.getName(), new Category());

		Currency dummyCurrency = new Currency();
		c = dummyCurrency.fetchAll();
		smCurrency.setAdapter(c, CurrencyData.KEY_LONG_NAME);
		if(currentAccount != null && currentAccount.getCurrency() != null)
			smCurrency.setSpinnerPositionCursor(currentAccount.getCurrency().toString(), new Currency());
	}

	@Override
	protected boolean validate() {
		// Called when persisting data, we store current category for next insertion before validating data
		Category cat = new Category();
		Cursor c = smCategory.getSelectedItem();
		if(c != null) {
			cat.toDTO(c);
			StaticData.setCurrentSelectedCategory(cat);
		} else {
			return false;
		}
		
		if(! isEditing()) {
			Date d = new GregorianCalendar(mYear, mMonth, mDay).getTime();
			StaticData.setCurrentOperationDatePickerDate(d);
		}
		
		boolean res = amount != null && 
				amount.getText() != null && 
				!"".equals(amount.getText().toString().trim()) &&
				NumberUtil.parseDecimal(amount.getText().toString()) != null;
		
		return res;
	}

	@Override
	protected void baseObjectToFields(Operation obj) {
		if(obj != null) {
			
			if(obj.getId() != null) {
				// Updating
				linearLayoutRateUpdater.setVisibility(View.VISIBLE);
				checkboxUpdateRate.setChecked(false);
			}
			
			initDatePickerTextView(obj.getDate());
			if(obj.getAccount() !=null)
				smAccount.setSpinnerPositionCursor(obj.getAccount().getName(), new Account());
			if(obj.getCategory() != null)
				smCategory.setSpinnerPositionCursor(obj.getCategory().getName(), new Category());
			if(obj.getCurrency() != null)
				smCurrency.setSpinnerPositionCursor(obj.getCurrency().getLongName(), new Currency());
			if(obj.getAmount() != null) {
				amount.setText(""+NumberUtil.formatDecimal(Math.abs(obj.getAmount())));
				if(obj.getAmount() >= 0.0f)
					radioButtonCredit.setChecked(true);
				else
					radioButtonDebit.setChecked(true);
			} else {
				amount.setText("");
			}
		} else {
			// Reset all fields
			initDatePickerTextView(null);
			amount.setText("");
			// Set spinner category to current selected one if exists
			Category cat = StaticData.getCurrentSelectedCategory();
			smCategory.setSpinnerPositionCursor(cat.getName(), new Category());
		}
	}

	@Override
	protected void fieldsToBaseObject(Operation obj) {
		if(obj != null) {
			obj.setDate(new GregorianCalendar(mYear, mMonth, mDay).getTime());

			Cursor c = null;

			c = smAccount.getSelectedItem();
			Account u = new Account();
			u.toDTO(c);
			obj.setAccount(u);

			c = smCategory.getSelectedItem();
			Category cat = new Category();
			cat.toDTO(c);
			obj.setCategory(cat);

			c = smCurrency.getSelectedItem();
			Currency cur = new Currency();
			cur.toDTO(c);
			obj.setCurrency(cur);

			obj.setAmount(NumberUtil.parseDecimal(amount.getText().toString()));
			if(radioButtonDebit.isChecked())
				obj.setAmount(obj.getAmount()*-1);

			// Store currency value for this addition if exchange rate checkbox is selected
			if(linearLayoutRateUpdater.getVisibility() == View.GONE || checkboxUpdateRate.isChecked())
				obj.setCurrencyValueOnCreated(cur.getRateCurrencyLinked());
		}
	}

	private void initDatePickerTextView(Date d) {
		Date previousDate = StaticData.getCurrentOperationDatePickerDate();
		Calendar cal = Calendar.getInstance();
		
		if(d != null)
			cal.setTime(d);
		else if(previousDate != null)
			cal.setTime(previousDate);
		
		mYear = cal.get(Calendar.YEAR);
		mMonth = cal.get(Calendar.MONTH);
		mDay = cal.get(Calendar.DAY_OF_MONTH);
		
		updateDatePickerTextView();
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
	
	@Override
	public void onClick(View v) {
		//super.onClick(v);
		
		if(v.getId() == R.id.date) {
			showDialog(DATE_DIALOG_ID);
		} else if(v.getId() == R.id.button_today) {
			initDatePickerTextView(Calendar.getInstance().getTime());
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, this, mYear, mMonth, mDay);
		}
		
		return null;
	}
	
	public void updateDatePickerTextView() {
		textViewDate.setText(
				DateUtil.dateToStringNoHour(new GregorianCalendar(mYear, mMonth, mDay).getTime()));
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		mYear = year;
		mMonth = monthOfYear;
		mDay = dayOfMonth;
		updateDatePickerTextView();
	}
}