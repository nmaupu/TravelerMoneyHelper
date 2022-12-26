package org.maupu.android.tmh;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.CurrencyISO4217;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.IAsync;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncFetcher;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncUpdater;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.GregorianCalendar;
import java.util.List;

public class AddOrEditCurrencyFragment extends AddOrEditFragment<Currency> implements AdapterView.OnItemClickListener, IAsync {
    private EditText editTextLongName = null;
    private EditText editTextShortName = null;
    private EditText editTextValue = null;
    private CheckBox checkBoxUpdate = null;
    private AutoCompleteTextView actvCurrencyCode = null;
    private OpenExchangeRatesAsyncFetcher oerFetcher;
    private boolean apiKeyValid = false;

    public AddOrEditCurrencyFragment() {
        super(R.string.fragment_title_edition_currency, R.layout.add_or_edit_currency, new Currency());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Before going further, verify we have api key to open exchange rates api
        String apiKey = StaticData.getPreferenceValueString(StaticData.PREF_KEY_OER_EDIT);
        this.apiKeyValid = StaticData.getPreferenceValueBoolean(StaticData.PREF_OER_VALID);
        if (apiKey == null || "".equals(apiKey) || !apiKeyValid) {
            SimpleDialog.errorDialog(
                    requireContext(),
                    getString(R.string.error),
                    getString(R.string.error_no_oer_api_key),
                    (dialog, which) -> {
                        // Display preferences
                        Intent intent = new Intent(requireContext(), PreferencesActivity.class);
                        startActivity(intent);
                    }).show();
        } else {
            this.apiKeyValid = true;
        }

        updateTextViewRate();

        initOerFetcher();
    }

    @Override
    protected View initResources(View view) {
        // Get resource instances
        editTextLongName = view.findViewById(R.id.long_name);
        editTextShortName = view.findViewById(R.id.short_name);
        editTextValue = view.findViewById(R.id.rate_value);
        checkBoxUpdate = view.findViewById(R.id.checkbox_last_update);
        actvCurrencyCode = view.findViewById(R.id.currency_code);


        actvCurrencyCode.setOnItemClickListener(this);
        return actvCurrencyCode;
    }


    @Override
    public void onResume() {
        apiKeyValid = StaticData.getPreferenceValueBoolean(StaticData.PREF_OER_VALID);

        if (StaticData.getMainCurrency() == null || StaticData.getMainCurrency().getId() == null) {
            StaticData.setMainCurrency(StaticData.getDefaultMainCurrency().getId());
        }

        super.onResume();
    }

    private void initOerFetcher() {
        oerFetcher = new OpenExchangeRatesAsyncFetcher(requireActivity());
        // init currencies list - set on callback method (async call)
        try {
            oerFetcher.setAsyncListener(this);
            oerFetcher.execute((Currency[]) null);
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(
                            requireContext(),
                            requireView(),
                            getString(R.string.error) + " err=" + e.getMessage(),
                            Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onFinishAsync() {
        List<CurrencyISO4217> currenciesList = oerFetcher.getCurrencies();

        ArrayAdapter<CurrencyISO4217> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                currenciesList);
        actvCurrencyCode.setAdapter(adapter);
    }

    private void updateTextViewRate() {
        MaterialEditText met = (MaterialEditText) editTextValue;
        if (met == null)
            return;

        CharSequence curText = getString(R.string.form_addedit_value);
        met.setFloatingLabelText(curText);

        if (StaticData.getMainCurrency() != null && StaticData.getMainCurrency().getId() != null) {
            String hint = curText + java.util.Currency.getInstance(StaticData.getMainCurrency().getIsoCode()).getSymbol();
            met.setFloatingLabelText(hint);
            met.setHint(hint);
        } else if (StaticData.getMainCurrency() == null) {
            String hint = curText + " " + editTextShortName.getText().toString();
            met.setFloatingLabelText(hint);
            met.setHint(hint);
        }
    }

    protected boolean validate() {
        try {
            Float.parseFloat(editTextValue.getText().toString());
            return !editTextLongName.getText().toString().trim().equals("") &&
                    !editTextShortName.getText().toString().trim().equals("") &&
                    oerFetcher.getCurrency(actvCurrencyCode.getText().toString()) != null;
        } catch (NumberFormatException nfe) {
            return false;
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void baseObjectToFields(Currency obj) {
        if (obj == null) {
            // Force fields to be filled with value corresponding to spinner
            onItemClick(null, null, 0, 0);
            editTextValue.setText("");
            if (checkBoxUpdate.isEnabled()) {
                checkBoxUpdate.setChecked(true);
                checkBoxUpdate.setEnabled(true);
            }
        } else {
            editTextLongName.setText(obj.getLongName());
            editTextShortName.setText(obj.getShortName());

            if (obj.getRateCurrencyLinked() != null)
                editTextValue.setText("" + obj.getRateCurrencyLinked());
            else
                editTextValue.setText("");

            if (obj.getLastUpdate() == null) {
                checkBoxUpdate.setChecked(true);
                checkBoxUpdate.setEnabled(false);
            } else {
                checkBoxUpdate.setChecked(false);
                checkBoxUpdate.setEnabled(true);
            }

            actvCurrencyCode.setText(obj.getIsoCode());
        }
    }

    @Override
    protected void fieldsToBaseObject(Currency obj) {
        if (obj != null) {
            obj.setLongName(editTextLongName.getText().toString().trim());
            obj.setShortName(editTextShortName.getText().toString().trim());
            obj.setRateCurrencyLinked(Double.parseDouble(editTextValue.getText().toString().trim()));

            if (obj.getLastUpdate() == null || checkBoxUpdate.isChecked())
                obj.setLastUpdate(new GregorianCalendar().getTime());

            String isoCode = oerFetcher.getCurrency(actvCurrencyCode.getText().toString()).getCode();
            obj.setIsoCode(isoCode);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TmhLogger.d(AddOrEditCurrencyFragment.class, "on item clicked called !");

        String isoCode = actvCurrencyCode.getText().toString();
        CurrencyISO4217 cur = oerFetcher.getCurrency(isoCode);

        if (cur != null) {
            SoftKeyboardHelper.hide(requireContext(), getView().getRootView());
            editTextShortName.setText(cur.getCurrencySymbol());
            editTextLongName.setText(cur.getName());

            // Need Open Exchange Rates API key to update from server
            if (apiKeyValid) {
                final Currency dummyCurrency = new Currency();
                dummyCurrency.setIsoCode(cur.getCode());

                try {
                    OpenExchangeRatesAsyncUpdater updater = new OpenExchangeRatesAsyncUpdater(requireActivity(), StaticData.getPreferenceValueString(StaticData.PREF_KEY_OER_EDIT));
                    updater.setAsyncListener(() -> {
                        if (dummyCurrency.getRateCurrencyLinked() == null) {
                            updateTextViewRate();
                            editTextValue.setText("1");
                        } else {
                            editTextValue.setText(String.valueOf(dummyCurrency.getRateCurrencyLinked()));
                        }
                    });
                    updater.execute(dummyCurrency);
                } catch (Exception e) {
                    Snackbar.make(
                            getView(),
                            getString(R.string.error_currency_update) + " - " + e.getMessage(),
                            Snackbar.LENGTH_SHORT).show();
                }
            } else {
                // Set to 1 by default
                editTextValue.setText("1");
            }
        } // if
    }
}
