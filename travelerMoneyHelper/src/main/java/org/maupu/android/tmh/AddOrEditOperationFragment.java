package org.maupu.android.tmh;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.polyak.iconswitch.IconSwitch;

import org.maupu.android.tmh.database.CategoryData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.dialog.DatePickerDialogFragment;
import org.maupu.android.tmh.dialog.TimePickerDialogFragment;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.NumberEditText;
import org.maupu.android.tmh.ui.widget.SpinnerManager;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.ImageUtil;
import org.maupu.android.tmh.util.NumberUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AddOrEditOperationFragment extends AddOrEditFragment<Operation> implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, TextWatcher, AdapterView.OnItemSelectedListener {
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHours = 0;
    private int mMinutes = 0;
    private int mSeconds = 0;

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
    private IconSwitch switchCreditDebitCard;
    private TextView switchCreditDebitCardTextView;
    private static final String PLUS = "+";
    private static final String MINUS = "-";

    public AddOrEditOperationFragment() {
        super(R.string.fragment_title_edition_operation, R.layout.add_or_edit_operation, new Operation());
    }

    @Override
    protected View initResources(View view) {
        // Set current time
        Calendar cal = Calendar.getInstance();
        mHours = cal.get(Calendar.HOUR_OF_DAY);
        mMinutes = cal.get(Calendar.MINUTE);
        mSeconds = cal.get(Calendar.SECOND);

        //smAccount = new SpinnerManager(this, (Spinner)findViewById(R.id.account));
        accountIcon = view.findViewById(R.id.account_icon);
        accountName = view.findViewById(R.id.account_name);
        smCategory = new SpinnerManager(getContext(), view.findViewById(R.id.category));
        amount = view.findViewById(R.id.amount);
        amount.addTextChangedListener(this);
        linearLayoutRateUpdater = view.findViewById(R.id.ll_exchange_rate);
        checkboxUpdateRate = view.findViewById(R.id.checkbox_update_rate);
        checkboxUpdateRate.setOnCheckedChangeListener((buttonView, isChecked) -> updateConvertedAmount());
        smCurrency = new SpinnerManager(getContext(), view.findViewById(R.id.currency));
        smCurrency.getSpinner().setOnItemSelectedListener(this);

        radioButtonCredit = view.findViewById(R.id.credit);
        radioButtonDebit = view.findViewById(R.id.debit);
        radioButtonCredit.setOnCheckedChangeListener(this);
        radioButtonDebit.setOnCheckedChangeListener(this);
        textViewSign = view.findViewById(R.id.sign);
        textViewDate = view.findViewById(R.id.date);
        textViewDate.setOnClickListener(this);
        textViewTime = view.findViewById(R.id.time);
        textViewTime.setOnClickListener(this);
        buttonToday = view.findViewById(R.id.button_today);
        buttonToday.setOnClickListener(this);
        textViewConvertedAmount = view.findViewById(R.id.converted_amount);
        textViewAmount = view.findViewById(R.id.text_amount);
        switchCreditDebitCardTextView = view.findViewById(R.id.switch_credit_debit_card_text);
        switchCreditDebitCard = view.findViewById(R.id.switch_credit_debit_card);
        switchCreditDebitCard.setCheckedChangeListener(current -> {
            // toggle text
            if (switchCreditDebitCard.getChecked() == IconSwitch.Checked.LEFT)
                switchCreditDebitCardTextView.setText(R.string.add_or_edit_operation_use_credit_debit_card_cash);
            else
                switchCreditDebitCardTextView.setText(R.string.add_or_edit_operation_use_credit_debit_card_card);
        });


        // Set spinners content
        Cursor c;

        // Init on current account
        account = StaticData.getCurrentAccount();
        accountName.setText(account.getName());
        ImageUtil.setIcon(accountIcon, account.getIcon());

        Category dummyCategory = new Category();
        Category withdrawalCat = StaticData.getWithdrawalCategory();
        if (withdrawalCat == null) {
            c = dummyCategory.fetchAll();
        } else if (getObj() != null && getObj().getCategory() != null && getObj().getCategory().getId() != null) {
            // We are currently editing ...
            if (withdrawalCat.getId().equals(super.getObj().getCategory().getId())) {
                c = dummyCategory.fetchAll(); // ... a withdrawal
                smCategory.getSpinner().setEnabled(false);
            } else {
                c = dummyCategory.fetchAllExcept(new Integer[]{withdrawalCat.getId()}); // ... non withdrawal operation
            }
        } else {
            // Adding an operation
            c = dummyCategory.fetchAllExcept(new Integer[]{withdrawalCat.getId()});
            if (c.getCount() == 0) {
                // No category created yet, cannot continue !
                SimpleDialog.errorDialogWithCancel(
                        getContext(),
                        getString(R.string.error),
                        getString(R.string.error_no_category),
                        (dialog, which) -> {
                            ((MainActivity) requireActivity()).changeFragment(
                                    AddOrEditCategoryFragment.class,
                                    true,
                                    null);
                        },
                        (dialog, which) -> {
                            SoftKeyboardHelper.hide(requireContext(), getView().getRootView());
                            ((MainActivity) requireActivity()).changeFragment(
                                    ViewPagerOperationFragment.class,
                                    false,
                                    null);
                        }).show();
            }
        }

        smCategory.setAdapter(c, CategoryData.KEY_NAME);
        // Set spinner category to current selected one if exists
        Category cat = StaticData.getCurrentSelectedCategory();
        if (cat != null && cat.getName() != null)
            smCategory.setSpinnerPositionCursor(cat.getName(), new Category());

        Currency dummyCurrency = new Currency();
        c = dummyCurrency.fetchAll();
        smCurrency.setAdapter(c, CurrencyData.KEY_LONG_NAME);
        if (account != null && account.getCurrency() != null)
            smCurrency.setSpinnerPositionCursor(account.getCurrency().getLongName(), new Currency());

        // Force edit text to get focus on startup
        return amount;
    }

    @Override
    public void onDestroy() {
        smCategory.closeAdapterCursor();
        smCurrency.closeAdapterCursor();

        super.onDestroy();
    }

    @Override
    protected boolean validate() {
        // Called when persisting data, we store current category for next insertion before validating data
        Category cat = new Category();
        Cursor c = smCategory.getSelectedItem();
        if (c != null) {
            cat.toDTO(c);
            StaticData.setCurrentSelectedCategory(cat);
        } else {
            return false;
        }

        if (!isEditing()) {
            Date d = new GregorianCalendar(mYear, mMonth, mDay, mHours, mMinutes, mSeconds).getTime();
            StaticData.setCurrentOperationDatePickerDate(d);
        }

        return amount != null &&
                amount.getStringText() != null &&
                !"".equals(amount.getStringText().trim());
    }

    @Override
    protected void baseObjectToFields(Operation obj) {
        if (obj != null) {
            Date d;

            if (obj.getId() != null) {
                // Updating
                linearLayoutRateUpdater.setVisibility(View.VISIBLE);
                checkboxUpdateRate.setChecked(false);
                d = obj.getDate();
            } else {
                // New operation - set date and time to now by default
                d = Calendar.getInstance().getTime();
            }

            initDatePickerTextView(d);
            if (obj.getAccount() != null) {
                account = obj.getAccount();
                accountName.setText(account.getName());
                ImageUtil.setIcon(accountIcon, account.getIcon());
            }
            if (obj.getCategory() != null)
                smCategory.setSpinnerPositionCursor(obj.getCategory().getName(), new Category());
            if (obj.getCurrency() != null)
                smCurrency.setSpinnerPositionCursor(obj.getCurrency().getLongName(), new Currency());
            if (obj.getAmount() != null) {
                //amount.setText(""+NumberUtil.formatDecimal(Math.abs(obj.getAmount())));
                amount.setText("" + Math.abs(obj.getAmount()));
                if (obj.getAmount() >= 0.0f)
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
        if (obj != null) {
            obj.setDate(new GregorianCalendar(mYear, mMonth, mDay, mHours, mMinutes, mSeconds).getTime());

            Cursor c;

            obj.setAccount((Account) account.copy());

            c = smCategory.getSelectedItem();
            Category cat = new Category();
            cat.toDTO(c);
            obj.setCategory(cat);

            c = smCurrency.getSelectedItem();
            Currency cur = new Currency();
            cur.toDTO(c);
            obj.setCurrency(cur);

            obj.setAmount(NumberUtil.parseDecimal(amount.getStringText()));
            if (radioButtonDebit.isChecked())
                obj.setAmount(obj.getAmount() * -1);

            // Store currency value for this addition if exchange rate checkbox is selected
            if (linearLayoutRateUpdater.getVisibility() == View.GONE || checkboxUpdateRate.isChecked())
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
        TmhLogger.d(AddOrEditOperationFragment.class, "RadioButton check changed");
        if (radioButtonCredit.isChecked()) {
            textViewSign.setText(PLUS);
            textViewSign.setTextColor(Operation.COLOR_POSITIVE_AMOUNT);
        } else {
            textViewSign.setText(MINUS);
            textViewSign.setTextColor(Operation.COLOR_NEGATIVE_AMOUNT);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.date) {
            new DatePickerDialogFragment(mYear, mMonth, mDay, (view, year, month, dayOfMonth) -> {
                mYear = year;
                mMonth = month;
                mDay = dayOfMonth;
                updateDatePickerTextView();
            }).show(getChildFragmentManager(), DatePickerDialogFragment.TAG);
        } else if (v.getId() == R.id.time) {
            new TimePickerDialogFragment(mHours, mMinutes, true, (view, hourOfDay, minute) -> {
                mHours = hourOfDay;
                mMinutes = minute;
                updateDatePickerTextView();
            }).show(getChildFragmentManager(), TimePickerDialogFragment.TAG);
        } else if (v.getId() == R.id.button_today) {
            Date now = Calendar.getInstance().getTime();
            setDateTimeFields(now);
            initDatePickerTextView(now);
        }
    }

    public void updateDatePickerTextView() {
        GregorianCalendar gc = new GregorianCalendar(mYear, mMonth, mDay, mHours, mMinutes, mSeconds);

        textViewDate.setText(DateUtil.dateToStringNoTime(gc.getTime()));
        textViewTime.setText(DateUtil.dateToStringOnlyTime(gc.getTime()));
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
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        updateConvertedAmount();
    }

    @SuppressLint("SetTextI18n")
    private void updateConvertedAmount() {
        try {
            Cursor c = smCurrency.getSelectedItem();
            Currency dummyCur = new Currency();
            dummyCur.toDTO(c);

            String a = amount.getStringText();
            TmhLogger.d(AddOrEditOperationFragment.class, "Current amount to convert = " + a);
            a = a != null ? a.trim() : a;

            Double currentAmount = 0d;
            if (a != null && !"".equals(a))
                currentAmount = Math.abs(Double.parseDouble(a));

            Double rate;
            if (checkboxUpdateRate.isChecked())
                rate = dummyCur.getRateCurrencyLinked();
            else
                rate = getObj().getCurrencyValueOnCreated();

            textViewConvertedAmount.setText("" + NumberUtil.formatDecimal(currentAmount / rate) + " " + StaticData.getMainCurrency().getShortName());
            textViewAmount.setText("" + NumberUtil.formatDecimal(currentAmount) + " " + dummyCur.getShortName());
        } catch (NumberFormatException nfe) {
            // No conversion
            TmhLogger.d(AddOrEditOperationFragment.class, "NumberFormatException occurred, no conversion is done");
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
