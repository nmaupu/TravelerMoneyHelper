package org.maupu.android;

import org.maupu.android.database.DatabaseHelper;
import org.maupu.android.database.object.Currency;
import org.maupu.android.ui.SimpleDialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddCurrencyActivity extends Activity implements OnClickListener {
	private DatabaseHelper dbHelper = new DatabaseHelper(this);
	private EditText editTextLongName = null;
	private EditText editTextShortName = null;
	private EditText editTextValue = null;
	private Button buttonContinue = null;
	private Button buttonReset = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_currency);
		
		dbHelper.openWritable();
		
		editTextLongName = (EditText)findViewById(R.id.long_name);
		editTextShortName = (EditText)findViewById(R.id.short_name);
		editTextValue = (EditText)findViewById(R.id.euro_value);
		buttonContinue = (Button)findViewById(R.id.add_currency_button_continue);
		buttonReset = (Button)findViewById(R.id.add_currency_button_reset);
		
		buttonContinue.setOnClickListener(this);
		buttonReset.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.add_currency_button_continue:
				if(validate()) {
					Currency c = new Currency();
					c.setLongName(editTextLongName.getText().toString().trim());
					c.setShortName(editTextShortName.getText().toString().trim());
					c.setTauxEuro(Float.parseFloat(editTextValue.getText().toString().trim()));
					c.insert(dbHelper);
					super.finish();
				} else {
					SimpleDialog.errorDialog(this, "Error", "Impossible to add currency, error in fields !").show();
				}
				break;
			case R.id.add_currency_button_reset:
				editTextLongName.setText("");
				editTextShortName.setText("");
				editTextValue.setText("");
				break;
		}
		
	}

	private boolean validate() {
		try {
		Float.parseFloat(editTextValue.getText().toString());
		return  !editTextLongName.getText().toString().trim().equals("") && 
				!editTextShortName.getText().toString().trim().equals("");
		} catch (NumberFormatException nfe) {
			return false;
		}
				
	}
}
