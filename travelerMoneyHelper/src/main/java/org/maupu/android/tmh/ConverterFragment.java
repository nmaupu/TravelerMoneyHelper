package org.maupu.android.tmh;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.dialog.CurrencyChooserBottomSheetDialog;
import org.maupu.android.tmh.ui.CurrencyISO4217;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AbstractAsyncTask;
import org.maupu.android.tmh.ui.async.IAsync;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncFetcher;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncUpdater;
import org.maupu.android.tmh.ui.widget.NumberEditText;
import org.maupu.android.tmh.util.DateUtil;
import org.maupu.android.tmh.util.NumberUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ConverterFragment extends TmhFragment implements View.OnClickListener, IAsync, TextWatcher, IAsyncActivityRefresher {
    private static final Class TAG = ConverterFragment.class;
    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;
    private static final String PREFS_CONVERTER_CURRENCY_1 = "ConverterActivity_currency_1";
    private static final String PREFS_CONVERTER_CURRENCY_2 = "ConverterActivity_currency_2";
    private static final String PREFS_CONVERTER_AMOUNT = "ConverterActivity_amount";

    /**
     * Chooser widgets
     **/
    private TextView tvCurrencyCode1, tvCurrencyCode2;
    private ImageView ivSwitch;

    /**
     * Info widgets
     **/
    private TextView tvCurrencyInfo1, tvCurrencyInfo2;

    /**
     * Amounts widgets
     **/
    private NumberEditText netAmount;
    private TextView tvAmountSymbol;
    private TextView tvConverterResult1, tvConverterResult2;

    /**
     * Mist widgets
     **/
    private TextView tvRatesLastUpdate;

    /**
     * Currency tools
     **/
    private OpenExchangeRatesAsyncFetcher oerFetcher;
    private boolean apiKeyValid = false;

    /**
     * Adapters
     **/
    ArrayAdapter<CurrencyISO4217> currencyAdapter;
    List<CurrencyISO4217> currenciesList;

    /**
     * Internal objects
     **/
    private CurrencyISO4217 currency1, currency2;
    private Double rate1, rate2, rateConverter;
    private Double convertedAmount1 = 0d, convertedAmount2 = 0d;
    private Date ratesLastUpdate = null;
    private boolean ratesCacheEnabled = true;

    public ConverterFragment() {
        super(R.layout.converter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.fragment_title_currency_converter);

        // Chooser widgets
        tvCurrencyCode1 = view.findViewById(R.id.currency_code_1);
        tvCurrencyCode2 = view.findViewById(R.id.currency_code_2);
        ivSwitch = view.findViewById(R.id.switch_image);

        // Info widgets
        tvCurrencyInfo1 = view.findViewById(R.id.currency_info_1);
        tvCurrencyInfo2 = view.findViewById(R.id.currency_info_2);

        // Amounts widgets
        netAmount = view.findViewById(R.id.amount);
        tvAmountSymbol = view.findViewById(R.id.amount_symbol);
        tvConverterResult1 = view.findViewById(R.id.converter_result_1);
        tvConverterResult2 = view.findViewById(R.id.converter_result_2);

        // Misc widgets
        tvRatesLastUpdate = view.findViewById(R.id.rates_last_update);

        // Events
        ivSwitch.setOnClickListener(this);
        tvCurrencyCode1.setOnClickListener(this);
        tvCurrencyCode2.setOnClickListener(this);
        netAmount.addTextChangedListener(this);
        tvConverterResult1.setOnClickListener(this);
        tvConverterResult2.setOnClickListener(this);

        handleFocus();


        // OpenExchangeRate initialization
        // Before going further, verify we have api key to open exchange rates api
        String apiKey = StaticData.getPreferenceValueString(StaticData.PREF_KEY_OER_EDIT);
        final Context ctx = getActivity();
        apiKeyValid = StaticData.getPreferenceValueBoolean(StaticData.PREF_OER_VALID);
        if (apiKey == null || "".equals(apiKey) || !apiKeyValid) {
            SimpleDialog.errorDialog(ctx, getString(R.string.error), getString(R.string.error_no_oer_api_key), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Display preferences
                    Intent intent = new Intent(ctx, PreferencesActivity.class);
                    startActivity(intent);
                }
            }).show();
        } else {
            apiKeyValid = true;
        }

        initOerFetcher();
    }

    @Override
    public void onPause() {
        savePrefs();
        super.onPause();
    }

    @Override
    public void onAccountChange() {
    }

    private void initOerFetcher() {
        oerFetcher = new OpenExchangeRatesAsyncFetcher(getActivity());
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
    public void onClick(View v) {
        if (v instanceof ImageView) {
            // Click on 'swap' image
            CurrencyISO4217 currencyBackup = currency1;
            currency1 = currency2;
            currency2 = currencyBackup;
            resetRates();
            updateConvertedAmounts();
            savePrefs();
            refreshDisplay();
        } else if (v instanceof TextView) {
            if (v == tvCurrencyCode1 || v == tvCurrencyCode2) {
                createBottomSheetCurrencyDialog(v == tvCurrencyCode1 ? TYPE_LEFT : TYPE_RIGHT)
                        .show(requireActivity().getSupportFragmentManager(), "ModalBottomSheet");
            } else if (v == tvConverterResult1 || v == tvConverterResult2) {
                // Setting edit text's text with the content of corresponding result's text view
                netAmount.setText(((TextView) v).getText().toString());
            }
        }
    }

    @Override
    public void onFinishAsync() {
        // Called when initOerFetcher finishes to update
        currenciesList = oerFetcher.getCurrencies();
        currencyAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                currenciesList);
        loadPrefs();
    }

    public void savePrefs() {
        if (this.currency1 != null)
            StaticData.setPreferenceValueString(PREFS_CONVERTER_CURRENCY_1, currency1.getCode());

        if (this.currency2 != null)
            StaticData.setPreferenceValueString(PREFS_CONVERTER_CURRENCY_2, currency2.getCode());

        StaticData.setPreferenceValueString(PREFS_CONVERTER_AMOUNT, netAmount.getStringText());
    }

    public void loadPrefs() {
        String currencyCode1 = StaticData.getPreferenceValueString(PREFS_CONVERTER_CURRENCY_1);
        String currencyCode2 = StaticData.getPreferenceValueString(PREFS_CONVERTER_CURRENCY_2);
        String amount = StaticData.getPreferenceValueString(PREFS_CONVERTER_AMOUNT);

        if (oerFetcher != null) {
            this.currency1 = oerFetcher.getCurrency(currencyCode1);
            this.currency2 = oerFetcher.getCurrency(currencyCode2);
            this.netAmount.setText(amount);
        }

        refreshDisplay();
    }

    private void resetRates() {
        rate1 = null;
        rate2 = null;
        rateConverter = null;
    }

    private void updateConvertedAmounts() {
        // Need Open Exchange Rates API key to update from server
        if (apiKeyValid) {
            if (currency1 != null && currency2 != null) {
                try {
                    final Double amount = Double.parseDouble(netAmount.getStringText());

                    if (!ratesCacheEnabled || rateConverter == null || rateConverter == 0d) {
                        final Currency dummyCurrency1 = new Currency();
                        final Currency dummyCurrency2 = new Currency();
                        dummyCurrency1.setIsoCode(currency1.getCode());
                        dummyCurrency2.setIsoCode(currency2.getCode());

                        OpenExchangeRatesAsyncUpdater updater = new OpenExchangeRatesAsyncUpdater(getActivity(), StaticData.getPreferenceValueString(StaticData.PREF_KEY_OER_EDIT), ratesCacheEnabled);
                        updater.setAsyncListener(() -> {
                            // rates are calculated against main currency
                            rate1 = dummyCurrency1.getRateCurrencyLinked();
                            rate2 = dummyCurrency2.getRateCurrencyLinked();
                            TmhLogger.d(TAG, "Setting rate1 = " + rate1);
                            TmhLogger.d(TAG, "Setting rate2 = " + rate2);
                            rateConverter = rate2 / rate1;
                            convertedAmount1 = amount;
                            convertedAmount2 = amount * rateConverter;
                            ratesLastUpdate = OpenExchangeRatesAsyncUpdater.getCurrencyRatesCacheDate();
                            ratesCacheEnabled = true;
                        });
                        updater.execute(dummyCurrency1, dummyCurrency2);
                    } else {
                        convertedAmount1 = amount;
                        convertedAmount2 = amount * rateConverter;
                    }
                } catch (NumberFormatException nfe) {
                    // Nothing to convert
                    convertedAmount1 = 0d;
                    convertedAmount2 = 0d;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private CurrencyChooserBottomSheetDialog createBottomSheetCurrencyDialog(final int type) {
        final CurrencyChooserBottomSheetDialog dialog = new CurrencyChooserBottomSheetDialog(
                currencyAdapter,
                (v, dlg, currency) -> {
                    if (type == TYPE_LEFT)
                        currency1 = currency;
                    else
                        currency2 = currency;

                    resetRates();
                    updateConvertedAmounts();
                    refreshDisplay();
                    dlg.dismiss();
                    handleFocus();
                });

        return dialog;
    }

    private void handleFocus() {
        try {
            Double value = Double.parseDouble(netAmount.getStringText());
            if (value == null || value == 0d)
                netAmount.setText("");
        } catch (NumberFormatException nfe) {
            // Nothing to do
        }

        if (netAmount != null) {
            netAmount.requestFocus();
            SoftKeyboardHelper.forceShowUp(requireActivity());
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        TmhLogger.d(TAG, "afterTextChanged called");
        updateConvertedAmounts();
        refreshDisplay();
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        // Call by parent class when refreshDisplay is called
        refreshFields(currency1, tvCurrencyCode1, tvCurrencyInfo1, true);
        refreshFields(currency2, tvCurrencyCode2, tvCurrencyInfo2, false);

        // Rates last update
        StringBuilder sb = new StringBuilder(getResources().getString(R.string.rates_last_update));
        sb.append(" "); // Last space is trimmed if put inside string resource file
        if (ratesLastUpdate != null)
            sb.append(DateUtil.dateToString(ratesLastUpdate));
        else
            sb.append("N/A");
        tvRatesLastUpdate.setText(sb.toString());
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground(AbstractAsyncTask asyncTask) {
        return null;
    }

    private void refreshFields(CurrencyISO4217 currency, TextView tvCurrencyCode, TextView tvCurrencyInfo, boolean mainCurrency) {
        if (currency != null) {
            tvCurrencyCode.setText(currency.getCode());
            tvCurrencyInfo.setText(currency.getName() + " (" + currency.getCurrencySymbol() + ")");
            if (mainCurrency)
                tvAmountSymbol.setText(currency.getCurrencySymbol());
        }

        tvConverterResult1.setText(NumberUtil.formatDecimal(convertedAmount1));
        tvConverterResult2.setText(NumberUtil.formatDecimal(convertedAmount2));
    }
}
