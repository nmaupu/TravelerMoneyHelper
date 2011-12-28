package org.maupu.android.tmh;

import java.util.Date;

import org.maupu.android.tmh.database.object.Currency;

import android.widget.CheckBox;
import android.widget.EditText;

public class AddOrEditCurrencyActivity extends AddOrEditActivity<Currency> {
	private EditText editTextLongName = null;
	private EditText editTextShortName = null;
	private EditText editTextValue = null;
	private CheckBox checkBoxUpdate = null;

	public AddOrEditCurrencyActivity() {
		super("Currency edition", R.drawable.ic_stat_categories, R.layout.add_or_edit_currency, new Currency());
	}
	
	@Override
	protected void initResources() {
		// Get resource instances
		editTextLongName = (EditText)findViewById(R.id.long_name);
		editTextShortName = (EditText)findViewById(R.id.short_name);
		editTextValue = (EditText)findViewById(R.id.euro_value);
		checkBoxUpdate = (CheckBox)findViewById(R.id.checkbox_last_update);
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
			editTextLongName.setText("");
			editTextShortName.setText("");
			editTextValue.setText("");
			checkBoxUpdate.setChecked(true);
			checkBoxUpdate.setEnabled(true);
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
		}
	}

	@Override
	protected void fieldsToBaseObject(Currency obj) {
		if(obj != null) {
			obj.setLongName(editTextLongName.getText().toString().trim());
			obj.setShortName(editTextShortName.getText().toString().trim());
			obj.setTauxEuro(Float.parseFloat(editTextValue.getText().toString().trim()));
			
			if(obj.getLastUpdate() == null || checkBoxUpdate.isChecked())
				obj.setLastUpdate(new Date());
		}
	}
}
