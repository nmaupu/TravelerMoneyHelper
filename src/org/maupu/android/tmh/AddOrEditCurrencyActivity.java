package org.maupu.android.tmh;

import java.util.GregorianCalendar;

import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.CurrencyHelper;
import org.maupu.android.tmh.ui.CurrencyISO4217;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class AddOrEditCurrencyActivity extends AddOrEditActivity<Currency> implements OnItemSelectedListener {
	private EditText editTextLongName = null;
	private EditText editTextShortName = null;
	private EditText editTextValue = null;
	private CheckBox checkBoxUpdate = null;
	private Spinner spinnerCurrencyCode = null;
	private boolean viewCreated = false;

	public AddOrEditCurrencyActivity() {
		super(R.string.activity_title_edition_currency, R.drawable.ic_stat_categories, R.layout.add_or_edit_currency, new Currency());
	}

	@Override
	protected void initResources() {
		// Get resource instances
		editTextLongName = (EditText)findViewById(R.id.long_name);
		editTextShortName = (EditText)findViewById(R.id.short_name);
		editTextValue = (EditText)findViewById(R.id.euro_value);
		checkBoxUpdate = (CheckBox)findViewById(R.id.checkbox_last_update);
		spinnerCurrencyCode = (Spinner)findViewById(R.id.currency_code);

		// init locales
		ArrayAdapter<CurrencyISO4217> adapter = new ArrayAdapter<CurrencyISO4217>(this, 
				android.R.layout.simple_spinner_item,
				CurrencyHelper.getListCurrencyISO4217(this));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCurrencyCode.setAdapter(adapter);

		spinnerCurrencyCode.setOnItemSelectedListener(this);
	}

	protected boolean validate() {
		try {
			Float.parseFloat(editTextValue.getText().toString());
			return  !editTextLongName.getText().toString().trim().equals("") && 
					!editTextShortName.getText().toString().trim().equals("");
		} catch (NumberFormatException nfe) {
			return false;
		}

	}

	@Override
	protected void baseObjectToFields(Currency obj) {
		if(obj == null) {
			// Force fields to be fill with value corresponding to spinner
			onItemSelected(null, null, 0, 0);
			editTextValue.setText("");
			if(checkBoxUpdate.isEnabled()) {
				checkBoxUpdate.setChecked(true);
				checkBoxUpdate.setEnabled(true);
			}
		} else {
			editTextLongName.setText(obj.getLongName());
			editTextShortName.setText(obj.getShortName());

			if(obj.getTauxEuro() != null)
				editTextValue.setText(""+obj.getTauxEuro());

			if(obj.getLastUpdate() == null) {
				checkBoxUpdate.setChecked(true);
				checkBoxUpdate.setEnabled(false);
			} else {
				checkBoxUpdate.setChecked(false);
				checkBoxUpdate.setEnabled(true);
			}

			// Searching for locale in spinner and select it
			for(int i=0; i<spinnerCurrencyCode.getCount(); i++) {
				CurrencyISO4217 c = (CurrencyISO4217)spinnerCurrencyCode.getItemAtPosition(i);
				if(c.getCode().equals(obj.getIsoCode()))
					spinnerCurrencyCode.setSelection(i);
			}
		}
	}

	@Override
	protected void fieldsToBaseObject(Currency obj) {
		if(obj != null) {
			obj.setLongName(editTextLongName.getText().toString().trim());
			obj.setShortName(editTextShortName.getText().toString().trim());
			obj.setTauxEuro(Float.parseFloat(editTextValue.getText().toString().trim()));

			if(obj.getLastUpdate() == null || checkBoxUpdate.isChecked())
				obj.setLastUpdate(new GregorianCalendar().getTime());

			obj.setIsoCode(((CurrencyISO4217)spinnerCurrencyCode.getSelectedItem()).getCode());
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if(! viewCreated) {
			// First call here, ignore it
			viewCreated = true;
			return;
		}
		
		CurrencyISO4217 c = (CurrencyISO4217)spinnerCurrencyCode.getSelectedItem();
		if(c != null) {
			java.util.Currency currency = java.util.Currency.getInstance(c.getCode());
			editTextShortName.setText(currency.getSymbol());
			editTextLongName.setText(c.getName());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}
}
