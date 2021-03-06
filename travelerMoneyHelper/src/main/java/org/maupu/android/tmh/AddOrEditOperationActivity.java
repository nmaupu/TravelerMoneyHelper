package org.maupu.android.tmh;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.CustomDatePickerDialog;
import org.maupu.android.tmh.ui.widget.NumberEditText;
import org.maupu.android.tmh.ui.widget.SpinnerManager;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.NumberUtil;
import org.maupu.android.tmh.util.TmhLogger;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class AddOrEditOperationActivity extends AddOrEditActivity<Operation> implements OnCheckedChangeListener, OnClickListener, OnDateSetListener, OnTimeSetListener, TextWatcher, OnItemSelectedListener {
	private static final int DATE_DIALOG_ID = 0;
	private static final int TIME_DIALOG_ID = 1;
	//private DatePicker datePicker;
	private int mYear;
    private int mMonth;
    private int mDay;
    private int mHours = 0;
    private int mMinutes = 0;
    private int mSeconds = 0;

    private CustomDatePickerDialog customDatePickerDialog = null;
	//private SpinnerManager smAccount;
    private Account account;
    private ImageView accountIcon;
    private TextView accountName;
	private SpinnerManager smCategory;
	private SpinnerManager smCurrency;
	private NumberEditText amount;
	private RadioButton radioButtonDebit;
	private RadioButton radioButtonCredit;
	private CheckBox checkboxUpdateRate;
	private LinearLayout linearLayoutRateUpdater;
	private TextView textViewSign;
	private TextView textViewDate;
	private TextView textViewTime;
	private TextView textViewConvertedAmount;
    private TextView textViewAmount;
	private Button buttonToday;
	private static final String PLUS="+";
	private static final String MINUS="-";

	public AddOrEditOperationActivity() {
		super(R.string.activity_title_edition_operation, R.layout.add_or_edit_operation, new Operation());
	}

    @Override
    public int whatIsMyDrawerIdentifier() {
        return super.DRAWER_ITEM_OPERATIONS;
    }

	@Override
	protected View initResources() {
		// Set current time
		Calendar cal = Calendar.getInstance();
		mHours = cal.get(Calendar.HOUR_OF_DAY);
		mMinutes = cal.get(Calendar.MINUTE);
		mSeconds = cal.get(Calendar.SECOND);
		
		//smAccount = new SpinnerManager(this, (Spinner)findViewById(R.id.account));
        accountIcon = (ImageView)findViewById(R.id.account_icon);
        accountName = (TextView)findViewById(R.id.account_name);
		smCategory = new SpinnerManager(this, (Spinner)findViewById(R.id.category));
		amount = (NumberEditText)findViewById(R.id.amount);
		amount.addTextChangedListener(this);
		linearLayoutRateUpdater = (LinearLayout)findViewById(R.id.ll_exchange_rate);
		checkboxUpdateRate = (CheckBox)findViewById(R.id.checkbox_update_rate);
        checkboxUpdateRate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateConvertedAmount();
            }
        });
		smCurrency = new SpinnerManager(this, (Spinner)findViewById(R.id.currency));
		smCurrency.getSpinner().setOnItemSelectedListener(this);

		radioButtonCredit = (RadioButton)findViewById(R.id.credit);
		radioButtonDebit = (RadioButton)findViewById(R.id.debit);
		radioButtonCredit.setOnCheckedChangeListener(this);
		radioButtonDebit.setOnCheckedChangeListener(this);
		textViewSign = (TextView)findViewById(R.id.sign);
		textViewDate = (TextView)findViewById(R.id.date);
		textViewDate.setOnClickListener(this);
		textViewTime = (TextView)findViewById(R.id.time);
		textViewTime.setOnClickListener(this);
		buttonToday = (Button)findViewById(R.id.button_today);
		buttonToday.setOnClickListener(this);
		textViewConvertedAmount = (TextView)findViewById(R.id.converted_amount);
        textViewAmount = (TextView)findViewById(R.id.text_amount);

		// Set spinners content
		Cursor c;

        // Init on current account
		account = StaticData.getCurrentAccount();
        accountName.setText(account.getName());
        ImageViewHelper.setIcon(this, accountIcon, account.getIcon());

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
		if(account != null && account.getCurrency() != null)
			smCurrency.setSpinnerPositionCursor(account.getCurrency().getLongName(), new Currency());

        // Force edit text to get focus on startup
        return amount;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
	protected void onDestroy() {
		smCategory.closeAdapterCursor();
		smCurrency.closeAdapterCursor();
		
		super.onDestroy();
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
			Date d = new GregorianCalendar(mYear, mMonth, mDay, mHours, mMinutes, mSeconds).getTime();
			StaticData.setCurrentOperationDatePickerDate(d);
		}
		
		return amount != null && 
				amount.getStringText() != null && 
				!"".equals(amount.getStringText().trim());
	}

	@Override
	protected void baseObjectToFields(Operation obj) {
		if(obj != null) {
			Date d;
			
			if(obj.getId() != null) {
				// Updating
				linearLayoutRateUpdater.setVisibility(View.VISIBLE);
				checkboxUpdateRate.setChecked(false);
				d = obj.getDate();
			} else {
				// New operation - set date and time to now by default
				d = Calendar.getInstance().getTime();
			}
			
			initDatePickerTextView(d);
			if(obj.getAccount() !=null) {
                account = obj.getAccount();
                accountName.setText(account.getName());
                ImageViewHelper.setIcon(this, accountIcon, account.getIcon());
            }
			if(obj.getCategory() != null)
				smCategory.setSpinnerPositionCursor(obj.getCategory().getName(), new Category());
			if(obj.getCurrency() != null)
				smCurrency.setSpinnerPositionCursor(obj.getCurrency().getLongName(), new Currency());
			if(obj.getAmount() != null) {
				//amount.setText(""+NumberUtil.formatDecimal(Math.abs(obj.getAmount())));
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
			obj.setDate(new GregorianCalendar(mYear, mMonth, mDay, mHours, mMinutes, mSeconds).getTime());

			Cursor c;

			obj.setAccount((Account)account.copy());

			c = smCategory.getSelectedItem();
			Category cat = new Category();
			cat.toDTO(c);
			obj.setCategory(cat);

			c = smCurrency.getSelectedItem();
			Currency cur = new Currency();
			cur.toDTO(c);
			obj.setCurrency(cur);

			obj.setAmount(NumberUtil.parseDecimal(amount.getStringText()));
			if(radioButtonDebit.isChecked())
				obj.setAmount(obj.getAmount()*-1);

			// Store currency value for this addition if exchange rate checkbox is selected
			if(linearLayoutRateUpdater.getVisibility() == View.GONE || checkboxUpdateRate.isChecked())
				obj.setCurrencyValueOnCreated(cur.getRateCurrencyLinked());
		}
	}

	private void initDatePickerTextView(Date d) {
		Date previousDate = StaticData.getCurrentOperationDatePickerDate();
		Date dateToSet = d != null ? d : previousDate;
		
		setDateTimeFields(dateToSet);
		updateDatePickerTextView();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		TmhLogger.d(AddOrEditOperationActivity.class, "RadioButton check changed");
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
		} else if(v.getId() == R.id.time) {
			showDialog(TIME_DIALOG_ID);
		} else if(v.getId() == R.id.button_today) {
			Date now = Calendar.getInstance().getTime();
			setDateTimeFields(now);
			
			//customDatePickerDialog.updateDate(mYear, mMonth, mDay);
			initDatePickerTextView(now);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DATE_DIALOG_ID:
			customDatePickerDialog = new CustomDatePickerDialog(this, this, mYear, mMonth, mDay);
			return customDatePickerDialog;
		case TIME_DIALOG_ID:
			TimePickerDialog tpd = new TimePickerDialog(this, this, mHours, mMinutes, true);
			return tpd;
		}
		
		return null;
	}
	
	public void updateDatePickerTextView() {
		GregorianCalendar gc = new GregorianCalendar(mYear, mMonth, mDay, mHours, mMinutes, mSeconds);
		
		textViewDate.setText(DateUtil.dateToStringNoTime(gc.getTime()));
		textViewTime.setText(DateUtil.dateToStringOnlyTime(gc.getTime()));
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		mYear = year;
		mMonth = monthOfYear;
		mDay = dayOfMonth;
		updateDatePickerTextView();
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
	public void onTimeSet(TimePicker ctx, int hours, int minutes) {
		mHours = hours;
		mMinutes = minutes;
		updateDatePickerTextView();
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
			Cursor c = smCurrency.getSelectedItem();
			Currency dummyCur = new Currency();
			dummyCur.toDTO(c);

			String a = amount.getStringText();
			TmhLogger.d(AddOrEditOperationActivity.class, "Current amount to convert = "+a);
			a = a != null ? a.trim() : a;
			
			Double currentAmount = 0d;
			if (a != null && ! "".equals(a))
				currentAmount = Math.abs(Double.parseDouble(a));

            Double rate;
            if(checkboxUpdateRate.isChecked())
                rate = dummyCur.getRateCurrencyLinked();
            else
                rate = getObj().getCurrencyValueOnCreated();

			textViewConvertedAmount.setText(""+NumberUtil.formatDecimal(currentAmount / rate)+" "+StaticData.getMainCurrency().getShortName());
            textViewAmount.setText(""+NumberUtil.formatDecimal(currentAmount)+" "+dummyCur.getShortName());
		} catch (NumberFormatException nfe) {
			// No conversion
			TmhLogger.d(AddOrEditOperationActivity.class, "NumberFormatException occured, no conversion is done");
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View selectedItemView, int position, long id) {
		// smCurrrency changed, update converted amount text view
		updateConvertedAmount();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// smCurrency changed and nothing selected
	}
}