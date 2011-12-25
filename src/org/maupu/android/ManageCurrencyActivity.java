package org.maupu.android;

import org.maupu.android.database.CurrencyData;
import org.maupu.android.database.DatabaseHelper;
import org.maupu.android.database.object.Currency;
import org.maupu.android.ui.CustomTitleBar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ManageCurrencyActivity extends Activity implements OnClickListener {
	private static final int ACTIVITY_ADD = 0;
	private DatabaseHelper dbHelper = new DatabaseHelper(this);
	private ListView listView = null;
	private TextView tvEmpty = null;
	private Button buttonAdd = null;
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
		//listView.setItemsCanFocus(true);
        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		tvEmpty = (TextView) findViewById(R.id.currency_tv_empty);
		buttonAdd = (Button) findViewById(R.id.button_add);
		buttonAdd.setOnClickListener(this);
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
		
		/* Delete button to hide if needed */
		View currentPushUpView = (View)((Activity)this).findViewById(R.id.push_up_menu_root_layout);
		LinearLayout layout = (LinearLayout)((Activity)this).findViewById(R.id.currency_root_layout);
		if(currentPushUpView != null) {
			currentPushUpView.setVisibility(View.GONE);
			layout.removeView(currentPushUpView);
		}
		
		
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
		switch(v.getId()) {
		case R.id.button_add:
			Log.d(ManageCurrencyActivity.class.getCanonicalName(), "Adding a currency button pressed");
			Intent intent = new Intent(this, AddCurrencyActivity.class);
	        startActivityForResult(intent, ACTIVITY_ADD);
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case ACTIVITY_ADD:
			fillData();
			break;
		}
	}
}
