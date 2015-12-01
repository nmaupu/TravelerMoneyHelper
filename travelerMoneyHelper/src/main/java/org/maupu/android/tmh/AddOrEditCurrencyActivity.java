package org.maupu.android.tmh;

import java.util.GregorianCalendar;
import java.util.List;

import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.CurrencyISO4217;
import org.maupu.android.tmh.ui.Flag;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.IAsync;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncFetcher;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncUpdater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddOrEditCurrencyActivity extends AddOrEditActivity<Currency> implements AdapterView.OnItemClickListener, IAsync {
	private EditText editTextLongName = null;
	private EditText editTextShortName = null;
	private EditText editTextValue = null;
	private CheckBox checkBoxUpdate = null;
	private AutoCompleteTextView actvCurrencyCode = null;
	private TextView textViewRateValue = null;
	private OpenExchangeRatesAsyncFetcher oerFetcher;
	private boolean apiKeyValid = false;

	public AddOrEditCurrencyActivity() {
		super(R.string.activity_title_edition_currency, R.layout.add_or_edit_currency, new Currency());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Before going further, verify we have api key to open exchange rates api
		String apiKey = StaticData.getPreferenceValueString(StaticData.PREF_OER_EDIT);
		final Context ctx = this;
		apiKeyValid = StaticData.getPreferenceValueBoolean(StaticData.PREF_OER_VALID);
		if(apiKey == null || "".equals(apiKey) || !apiKeyValid) {
			SimpleDialog.errorDialog(this, getString(R.string.error), getString(R.string.error_no_oer_api_key), new DialogInterface.OnClickListener() {
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
	protected void onResume() {
		apiKeyValid = StaticData.getPreferenceValueBoolean(StaticData.PREF_OER_VALID);
		super.onResume();
	}
	
	private void initOerFetcher() {
		oerFetcher = new OpenExchangeRatesAsyncFetcher(this);
		// init currencies list - set on callback method (async call)
		try {
			oerFetcher.setAsyncListener(this);
			oerFetcher.execute((Currency[])null);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onFinishAsync() {
		List<CurrencyISO4217> currenciesList = oerFetcher.getCurrencies();
		
		ArrayAdapter<CurrencyISO4217> adapter = new ArrayAdapter<CurrencyISO4217>(this, 
				android.R.layout.simple_dropdown_item_1line,
				currenciesList);
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		actvCurrencyCode.setAdapter(adapter);
	}

	@Override
	protected void initResources() {
		// Get resource instances
		editTextLongName = (EditText)findViewById(R.id.long_name);
		editTextShortName = (EditText)findViewById(R.id.short_name);
		editTextValue = (EditText)findViewById(R.id.rate_value);
		checkBoxUpdate = (CheckBox)findViewById(R.id.checkbox_last_update);
		actvCurrencyCode = (AutoCompleteTextView)findViewById(R.id.currency_code);
		textViewRateValue = (TextView)findViewById(R.id.text_rate_value);
		
		updateTextViewRate();

		actvCurrencyCode.setOnItemClickListener(this);
	}
	
	private void updateTextViewRate() {
		textViewRateValue.setText(getString(R.string.form_addedit_value));
		
		if(textViewRateValue != null && StaticData.getMainCurrency() != null) {
			textViewRateValue.setText(textViewRateValue.getText() + 
					java.util.Currency.getInstance(StaticData.getMainCurrency().getIsoCode()).getSymbol());
		} else if (StaticData.getMainCurrency() == null) {
			if(editTextShortName.getText() != null)
			    textViewRateValue.setText(textViewRateValue.getText() + " " + editTextShortName.getText().toString());
		}
	}

	protected boolean validate() {
		try {
			Float.parseFloat(editTextValue.getText().toString());
			return  !editTextLongName.getText().toString().trim().equals("") && 
					!editTextShortName.getText().toString().trim().equals("") &&
					oerFetcher.getCurrency(actvCurrencyCode.getText().toString()) != null;
		} catch (NumberFormatException nfe) {
			return false;
		}

	}

	@Override
	protected void baseObjectToFields(Currency obj) {
		if(obj == null) {
			// Force fields to be fill with value corresponding to spinner
			onItemClick(null, null, 0, 0);
			editTextValue.setText("");
			if(checkBoxUpdate.isEnabled()) {
				checkBoxUpdate.setChecked(true);
				checkBoxUpdate.setEnabled(true);
			}
		} else {
			editTextLongName.setText(obj.getLongName());
			editTextShortName.setText(obj.getShortName());

			if(obj.getRateCurrencyLinked() != null)
				editTextValue.setText(""+obj.getRateCurrencyLinked());

			if(obj.getLastUpdate() == null) {
				checkBoxUpdate.setChecked(true);
				checkBoxUpdate.setEnabled(false);
			} else {
				checkBoxUpdate.setChecked(false);
				checkBoxUpdate.setEnabled(true);
			}

			// Searching for locale in spinner and select it
			/*for(int i=0; i<spinnerCurrencyCode.getCount(); i++) {
				CurrencyISO4217 c = (CurrencyISO4217)spinnerCurrencyCode.getItemAtPosition(i);
				if(c.getCode().equals(obj.getIsoCode()))
					spinnerCurrencyCode.setSelection(i);
			}*/

			actvCurrencyCode.setText(obj.getIsoCode());
		}
	}

	@Override
	protected void fieldsToBaseObject(Currency obj) {
		if(obj != null) {
			obj.setLongName(editTextLongName.getText().toString().trim());
			obj.setShortName(editTextShortName.getText().toString().trim());
			obj.setRateCurrencyLinked(Double.parseDouble(editTextValue.getText().toString().trim()));

			if(obj.getLastUpdate() == null || checkBoxUpdate.isChecked())
				obj.setLastUpdate(new GregorianCalendar().getTime());

			String isoCode =  oerFetcher.getCurrency(actvCurrencyCode.getText().toString()).getCode();
			obj.setIsoCode(isoCode);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Log.d(AddOrEditCurrencyActivity.class.getName(), "on item clicked called !");

		String isoCode = actvCurrencyCode.getText().toString();
		CurrencyISO4217 cur = oerFetcher.getCurrency(isoCode);
		String currencySymbol = cur.getCode();
		
		if(cur != null) {
			
			try {
				java.util.Currency currency = java.util.Currency.getInstance(cur.getCode());
				currencySymbol = currency.getSymbol();
			} catch(IllegalArgumentException iae) {
				// Not a supported ISO4217, so we do not have a symbol available
				Log.e(AddOrEditCurrencyActivity.class.getName(), cur.getCode() + "/"+ cur.getName()+ " is not a valid ISO4217 currency !");
			}

			editTextShortName.setText(currencySymbol);
			editTextLongName.setText(cur.getName());
			
			// Need Open Exchange Rates API key to update from server
			if(apiKeyValid) {
				final Currency dummyCurrency = new Currency();
				dummyCurrency.setIsoCode(cur.getCode());
				
				try {
					OpenExchangeRatesAsyncUpdater updater = new OpenExchangeRatesAsyncUpdater(this, StaticData.getPreferenceValueString(StaticData.PREF_OER_EDIT));
					updater.setAsyncListener(new IAsync() {
						@Override
						public void onFinishAsync() {
                            if(dummyCurrency.getRateCurrencyLinked() == null) {
                                updateTextViewRate();
                                editTextValue.setText("1");
                            } else {
                                editTextValue.setText(String.valueOf(dummyCurrency.getRateCurrencyLinked()));
                            }
						}
					});
					updater.execute(new Currency[]{dummyCurrency});
				} catch (Exception e) {
					Toast.makeText(this, getString(R.string.error_currency_update)+" - "+e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			} else {
				// Set to 1 by default
				editTextValue.setText("1");
			}
		} // if
	}
	
	@Override
	public void refreshDisplay() {
		// Do not call super method
	}
}
