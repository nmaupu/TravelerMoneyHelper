package org.maupu.android.tmh;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import org.maupu.android.tmh.database.AccountData;
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
import org.maupu.android.tmh.ui.async.AbstractAsyncTask;
import org.maupu.android.tmh.ui.widget.NumberEditText;
import org.maupu.android.tmh.ui.widget.SpinnerManager;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.ImageUtil;
import org.maupu.android.tmh.util.NumberUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public class WithdrawalFragment extends TmhFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, TextWatcher {
    private static final Class TAG = WithdrawalFragment.class;
    private static final int DATE_DIALOG_ID = 0;
    private static final int TIME_DIALOG_ID = 1;

    private ImageView ivFrom;
    private Spinner spinnerFrom;
    private ImageView ivTo;
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
    private MenuProvider menuProvider;

    public WithdrawalFragment() {
        super(R.layout.withdrawal);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().setTitle(R.string.fragment_title_edition_withdrawal);
        setupMenu();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TmhLogger.d(TAG, "Calling onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        // Set current time
        Calendar cal = Calendar.getInstance();
        mHours = cal.get(Calendar.HOUR_OF_DAY);
        mMinutes = cal.get(Calendar.MINUTE);
        mSeconds = cal.get(Calendar.SECOND);

        ivFrom = view.findViewById(R.id.account_icon_from);
        spinnerFrom = view.findViewById(R.id.spinner_from);
        ivTo = view.findViewById(R.id.account_icon_to);
        spinnerTo = view.findViewById(R.id.spinner_to);
        spinnerCurrency = view.findViewById(R.id.spinner_currency);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        amount = view.findViewById(R.id.amount);
        amount.addTextChangedListener(this);
        textViewAmount = view.findViewById(R.id.text_amount);
        textViewConvertedAmount = view.findViewById(R.id.converted_amount);
        textViewDate = view.findViewById(R.id.date);
        textViewDate.setOnClickListener(this);
        textViewTime = view.findViewById(R.id.time);
        textViewTime.setOnClickListener(this);
        buttonToday = view.findViewById(R.id.button_today);
        buttonToday.setOnClickListener(this);

        spinnerFrom.setOnItemSelectedListener(this);
        spinnerTo.setOnItemSelectedListener(this);

        initSpinnerManagers();
        initDatePickerTextView(Calendar.getInstance().getTime());

        updateAccountInfo(ivFrom, spinnerManagerFrom.getSelectedItem());
        updateAccountInfo(ivTo, spinnerManagerTo.getSelectedItem());

        // Force edit text to get focus on startup
        amount.requestFocus();
        SoftKeyboardHelper.forceShowUp(requireActivity());
    }

    private void setupMenu() {
        if (menuProvider == null) {
            menuProvider = new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.add_or_edit_menu, menu);
                    menu.findItem(R.id.action_add).setVisible(false);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_save)
                        if (save())
                            requireActivity().onBackPressed();
                    return true;
                }
            };
        }
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (spinnerManagerCategory != null) {
            Category category = StaticData.getWithdrawalCategory();
            if (category != null)
                spinnerManagerCategory.setSpinnerPositionCursor(category.getName(), new Category());
        }
    }

    @Override
    public void onDestroy() {
        spinnerManagerFrom.closeAdapterCursor();
        spinnerManagerTo.closeAdapterCursor();
        spinnerManagerCurrency.closeAdapterCursor();
        spinnerManagerCategory.closeAdapterCursor();
        super.onDestroy();
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
        spinnerManagerFrom = new SpinnerManager(requireContext(), spinnerFrom);
        spinnerManagerTo = new SpinnerManager(requireContext(), spinnerTo);
        spinnerManagerCurrency = new SpinnerManager(requireContext(), spinnerCurrency);
        spinnerManagerCategory = new SpinnerManager(requireContext(), spinnerCategory);

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
        if (category != null) {
            spinnerManagerCategory.setSpinnerPositionCursor(category.getName(), new Category());
        } else {
            SimpleDialog.errorDialog(
                    requireActivity(),
                    getString(R.string.warning),
                    getString(R.string.default_category_warning),
                    (dialog, which) -> {
                        startActivity(new Intent(requireActivity(), PreferencesActivity.class));
                    }).show();
        }

        // To
        Account current = StaticData.getCurrentAccount();
        spinnerManagerTo.setSpinnerPositionCursor(current.getName(), new Account());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinner_from) {
            updateAccountInfo(ivFrom, spinnerManagerFrom.getSelectedItem());
        } else if (parent.getId() == R.id.spinner_to) {
            Cursor item = spinnerManagerTo.getSelectedItem();

            // Set icon and text to this account
            updateAccountInfo(ivTo, item);

            // Select corresponding currency to 'to' account
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

    private void updateAccountInfo(ImageView ivAccount, Cursor cursorItem) {
        if (cursorItem == null || ivAccount == null)
            return;

        Account a = new Account();
        a.toDTO(cursorItem);
        ImageUtil.setIcon(ivAccount, a.getIcon());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    protected boolean validate() {
        Date d = new GregorianCalendar(mYear, mMonth, mDay).getTime();
        StaticData.setCurrentOperationDatePickerDate(d);

        return amount.getText() != null && !"".equals(amount.getText().toString().trim());
    }

    @Override
    public void onTimeSet(TimePicker ctx, int hours, int minutes) {
        mHours = hours;
        mMinutes = minutes;
        updateDatePickerTextView();
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground(AbstractAsyncTask asyncTask) {
        return null;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
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

    private void updateConvertedAmount() {
        try {
            Cursor c = spinnerManagerCurrency.getSelectedItem();
            Currency dummyCur = new Currency();
            dummyCur.toDTO(c);

            String a = amount.getStringText();
            TmhLogger.d(TAG, "Current amount to convert = " + a);
            a = a != null ? a.trim() : a;

            Double currentAmount = 0d;
            if (a != null && !"".equals(a))
                currentAmount = Math.abs(Double.parseDouble(a));

            Double rate = dummyCur.getRateCurrencyLinked();
            textViewConvertedAmount.setText("" + NumberUtil.formatDecimal(currentAmount / rate) + " " + StaticData.getMainCurrency().getShortName());
            textViewAmount.setText("" + NumberUtil.formatDecimal(currentAmount) + " " + dummyCur.getShortName());
        } catch (NumberFormatException nfe) {
            // No conversion
            TmhLogger.d(TAG, "NumberFormatException occured, no conversion is done");
        }
    }

    private boolean save() {
        if (!validate()) {
            SimpleDialog.errorDialog(requireContext(), getString(R.string.error), getString(R.string.error_add_object)).show();
            return false;
        }

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
        return true;
    }
}
