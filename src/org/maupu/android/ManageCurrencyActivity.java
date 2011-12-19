package org.maupu.android;

import org.maupu.android.database.CurrencyData;
import org.maupu.android.database.DatabaseHelper;
import org.maupu.android.database.object.Currency;
import org.maupu.android.ui.CustomTitleBar;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ManageCurrencyActivity extends Activity implements OnClickListener {
	private DatabaseHelper dbHelper = new DatabaseHelper(this);
	private ListView listView = null;
	private TextView tvEmpty = null;
//	private Button editButton = null;
//	private Button addButton = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(R.layout.manage_currency);
		customTB.setName("Currencies");
		customTB.setIcon(R.drawable.ic_stat_categories);
		
		dbHelper.openWritable();
		listView = (ListView) findViewById(R.id.currency_list);
		tvEmpty = (TextView) findViewById(R.id.currency_tv_empty);
		/*
		editButton = (Button) findViewById(R.id.manageable_header_button_edit);
		editButton.setOnClickListener(this);
		addButton = (Button) findViewById(R.id.manageable_header_button_add);
		addButton.setOnClickListener(this);
		*/
		
		fillData();
	}
	
	private void fillData() {
		Currency currency = new Currency();
		Cursor c = currency.fetchAll(dbHelper);
		
		if(c.getCount() == 0) {
			tvEmpty.setText("No currency found");
			tvEmpty.setVisibility(View.VISIBLE);
		} else {
			tvEmpty.setVisibility(View.GONE);
			
			CustomCurrencyCursorAdapter adapter = new CustomCurrencyCursorAdapter(this, 
					R.layout.currency_item, 
					c,
					new String[]{CurrencyData.KEY_LONG_NAME, CurrencyData.KEY_SHORT_NAME, CurrencyData.KEY_TAUX_EURO, CurrencyData.KEY_LAST_UPDATE},
					new int[]{R.id.longName, R.id.shortName, R.id.tauxEuro, R.id.lastUpdate});
			listView.setAdapter(adapter);
		}
	}

	@Override
	public void onClick(View v) {
		
	}
}
