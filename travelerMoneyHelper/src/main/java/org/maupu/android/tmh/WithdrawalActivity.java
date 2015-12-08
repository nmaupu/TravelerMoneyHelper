package org.maupu.android.tmh;

import greendroid.widget.ActionBarItem;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.CustomActionBarItem;
import org.maupu.android.tmh.ui.CustomActionBarItem.CustomType;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.NumberEditText;
import org.maupu.android.tmh.ui.widget.SpinnerManager;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.NumberUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class WithdrawalActivity extends TmhActivity implements OnItemSelectedListener, OnClickListener, OnDateSetListener, OnTimeSetListener, TextWatcher {
	private static final int DATE_DIALOG_ID = 0;
	private static final int TIME_DIALOG_ID = 1;
	
	private Spinner spinnerFrom;
	private Spinner spinnerTo;
	private Spinner spinnerCurrency;
	private Spinner spinnerCategory;
	private SpinnerManager spinnerManagerFrom;
	private SpinnerManager spinnerManagerTo;
	private SpinnerManager spinnerManagerCurrency;
	private SpinnerManager spinnerManagerCategory;
	private NumberEditText amount;
    private TextView textViewAmount;
	private TextView textViewConvertedAmount;
	private TextView textViewDate;
	private TextView textViewTime;
    private Button buttonToday;
	private int mYear, mMonth, mDay;
	private int mHours = 0;
    private int mMinutes = 0;
    private int mSeconds = 0;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.setContentView(R.layout.withdrawal);
        super.onCreate(savedInstanceState);
		setTitle(R.string.activity_title_edition_withdrawal);

        /*
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Save), TmhApplication.ACTION_BAR_SAVE);
		*/

		// Set current time
		Calendar cal = Calendar.getInstance();
		mHours = cal.get(Calendar.HOUR_OF_DAY);
		mMinutes = cal.get(Calendar.MINUTE);
		mSeconds = cal.get(Calendar.SECOND);
		
		spinnerFrom = (Spinner)findViewById(R.id.spinner_from);
		spinnerTo = (Spinner)findViewById(R.id.spinner_to);
		spinnerCurrency = (Spinner)findViewById(R.id.spinner_currency);
		spinnerCategory = (Spinner)findViewById(R.id.spinner_category);
		amount = (NumberEditText)findViewById(R.id.amount);
        amount.addTextChangedListener(this);
        textViewAmount = (TextView)findViewById(R.id.text_amount);
		textViewConvertedAmount = (TextView)findViewById(R.id.converted_amount);
		textViewDate = (TextView)findViewById(R.id.date);
		textViewDate.setOnClickListener(this);
		textViewTime = (TextView)findViewById(R.id.time);
		textViewTime.setOnClickListener(this);
		buttonToday = (Button)findViewById(R.id.button_today);
		buttonToday.setOnClickListener(this);

		spinnerTo.setOnItemSelectedListener(this);
		
		initSpinnerManagers();
		initDatePickerTextView(Calendar.getInstance().getTime());

        // Force edit text to get focus on startup
        amount.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}
	
	@Override
	protected void onDestroy() {
		spinnerManagerFrom.closeAdapterCursor();
		spinnerManagerTo.closeAdapterCursor();
		spinnerManagerCurrency.closeAdapterCursor();
		spinnerManagerCategory.closeAdapterCursor();
		
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.date) {
			showDialog(DATE_DIALOG_ID);
		} else if(v.getId() == R.id.time) {
			showDialog(TIME_DIALOG_ID);
		} else if(v.getId() == R.id.button_today) {
			Date now = Calendar.getInstance().getTime();
			setDateTimeFields(now);
			initDatePickerTextView(now);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, this, mYear, mMonth, mDay);
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, this, mHours, mMinutes, true);
		}
		
		return null;
	}
	
	private void initDatePickerTextView(Date d) {
		Date previousDate = StaticData.getCurrentOperationDatePickerDate();
		Date dateToSet = d != null ? d : previousDate;
		
		setDateTimeFields(dateToSet);
		updateDatePickerTextView();
	}
	
	public void updateDatePickerTextView() {
		GregorianCalendar cal = new GregorianCalendar(mYear, mMonth, mDay, mHours, mMinutes, mSeconds);
		textViewDate.setText(DateUtil.dateToStringNoTime(cal.getTime()));
		textViewTime.setText(DateUtil.dateToStringOnlyTime(cal.getTime()));
	}
	
	private void setDateTimeFields(Date d) {
		Calendar cal = Calendar.getInstance();
		if (d != null)
			cal.setTime(d);
		
		mYear = cal.get(Calendar.YEAR);
		mMonth = cal.get(Calendar.MONTH);
		mDay = cal.get(Calendar.DAY_OF_MONTH);
		mHours = cal.get(Calendar.HOUR_OF_DAY);
		mMinutes = cal.get(Calendar.MINUTE);
		mSeconds = cal.get(Calendar.SECOND);
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		mYear = year;
		mMonth = monthOfYear;
		mDay = dayOfMonth;
		updateDatePickerTextView();
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
		spinnerCategory.setEnabled(false);

		// Getting default category for withdrawal from preferences
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		//int idCategory = Integer.parseInt(prefs.getString("category", null));
		category = StaticData.getWithdrawalCategory();
		if(category != null) {
			spinnerManagerCategory.setSpinnerPositionCursor(category.getName(), new Category());
		} else {
			final Activity activity = this;
			SimpleDialog.errorDialog(
					this, 
					getString(R.string.warning), 
					getString(R.string.default_category_warning),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(activity, PreferencesActivity.class));
							activity.finish();
						}
					}).show();
		}

		// To
		Account current = StaticData.getCurrentAccount();
		spinnerManagerTo.setSpinnerPositionCursor(current.getName(), new Account());
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
			c.close();

			spinnerManagerCurrency.setSpinnerPositionCursor(currency.getLongName(), new Currency());
			updateConvertedAmount();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

    /*
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch(item.getItemId()) {
		case TmhApplication.ACTION_BAR_SAVE:
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

				Double amountd = Double.valueOf(amount.getStringText().toString().trim());

				Currency currency = new Currency();
				cursor = spinnerManagerCurrency.getSelectedItem();
				currency.toDTO(cursor);

				Date date = new GregorianCalendar(mYear, mMonth, mDay, mHours, mMinutes, mSeconds).getTime();
				
				// First, we debit from account
				Operation operationFrom = new Operation();
				operationFrom.setAccount(accountFrom);
				operationFrom.setAmount(-1d * amountd);
				operationFrom.setCategory(category);
				operationFrom.setCurrency(currency);
				operationFrom.setDate(date);
				operationFrom.setCurrencyValueOnCreated(currency.getRateCurrencyLinked());
				operationFrom.insert();

				// Second, we credit 'to' account
				Operation operationTo = new Operation();
				operationTo.setAccount(accountTo);
				operationTo.setAmount(amountd);
				operationTo.setCategory(category);
				operationTo.setCurrency(currency);
				operationTo.setDate(date);
				operationTo.setCurrencyValueOnCreated(currency.getRateCurrencyLinked());
				operationTo.insert();

				Operation.linkTwoOperations(operationFrom, operationTo);

				// Dispose activity
				finish();
			}
			break;
		default:
			return super.onHandleActionBarItemClick(item, position);		
		}

		return true;
	}
    */

	protected boolean validate() {
		Date d = new GregorianCalendar(mYear, mMonth, mDay).getTime();
		StaticData.setCurrentOperationDatePickerDate(d);
		
		return amount.getText() != null && ! "".equals(amount.getText().toString().trim());
	}
	
	@Override
	public void onTimeSet(TimePicker ctx, int hours, int minutes) {
		mHours = hours;
		mMinutes = minutes;
		updateDatePickerTextView();
	}
	
	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		return null;
	}
	
	@Override
	public void handleRefreshEnding(Map<Integer, Object> results) {
	}

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void afterTextChanged(Editable editable) {
        updateConvertedAmount();
    }

    private void updateConvertedAmount() {
        try {
            Cursor c = spinnerManagerCurrency.getSelectedItem();
            Currency dummyCur = new Currency();
            dummyCur.toDTO(c);

            String a = amount.getStringText();
            Log.d(AddOrEditOperationActivity.class.getName(), "Current amount to convert = " + a);
            a = a != null ? a.trim() : a;

            Double currentAmount = 0d;
            if (a != null && ! "".equals(a))
                currentAmount = Math.abs(Double.parseDouble(a));

            Double rate = dummyCur.getRateCurrencyLinked();
            textViewConvertedAmount.setText(""+NumberUtil.formatDecimal(currentAmount / rate)+" "+StaticData.getMainCurrency().getShortName());
            textViewAmount.setText("" + NumberUtil.formatDecimal(currentAmount) + " " + dummyCur.getShortName());
        } catch (NumberFormatException nfe) {
            // No conversion
            Log.d(AddOrEditOperationActivity.class.getName(), "NumberFormatException occured, no conversion is done");
        }
    }
}
