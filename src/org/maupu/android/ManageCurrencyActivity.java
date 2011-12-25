package org.maupu.android;

import org.maupu.android.database.CurrencyData;
import org.maupu.android.database.DatabaseHelper;
import org.maupu.android.database.ExpenseData;
import org.maupu.android.database.object.Currency;
import org.maupu.android.ui.CustomTitleBar;
import org.maupu.android.ui.SimpleDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ManageCurrencyActivity extends Activity implements OnClickListener {
	private static final int ACTIVITY_ADD = 0;
	private DatabaseHelper dbHelper = new DatabaseHelper(this);
	private ListView listView = null;
	private TextView tvEmpty = null;
	private Button buttonAdd = null;
	private Button buttonDelete = null;
	private Button buttonEdit = null;
	//	private Button editButton = null;
	//	private Button addButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(R.layout.manage_currency);
		customTB.setName("Currencies");
		customTB.setIcon(R.drawable.ic_stat_categories);

		tvEmpty = (TextView) findViewById(R.id.currency_tv_empty);
		buttonAdd = (Button) findViewById(R.id.button_add);
		buttonAdd.setOnClickListener(this);
		buttonDelete = (Button) findViewById(R.id.button_delete);
		buttonDelete.setOnClickListener(this);
		buttonEdit = (Button) findViewById(R.id.button_edit);
		buttonEdit.setOnClickListener(this);
		
		listView = (ListView) findViewById(R.id.currency_list);
		//listView.setItemsCanFocus(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		
		/*
		editButton = (Button) findViewById(R.id.manageable_header_button_edit);
		editButton.setOnClickListener(this);
		addButton = (Button) findViewById(R.id.manageable_header_button_add);
		addButton.setOnClickListener(this);
		 */

		dbHelper.openWritable();
		
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
			listView.setAdapter(null);
		} else {
			tvEmpty.setVisibility(View.GONE);

			CustomCurrencyCursorAdapter adapter = new CustomCurrencyCursorAdapter(this, 
					R.layout.currency_item, 
					c,
					new String[]{CurrencyData.KEY_LONG_NAME, CurrencyData.KEY_SHORT_NAME, CurrencyData.KEY_TAUX_EURO, CurrencyData.KEY_LAST_UPDATE},
					new int[]{R.id.longName, R.id.shortName, R.id.tauxEuro, R.id.lastUpdate});
			listView.setAdapter(adapter);
		}

		buttonDelete.setEnabled(false);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_add:
			Log.d(ManageCurrencyActivity.class.getCanonicalName(), "Adding a currency button pressed");
			Intent intent = new Intent(this, AddCurrencyActivity.class);
			startActivityForResult(intent, ACTIVITY_ADD);
			break;
		case R.id.button_delete:
			deleteCurrencies();
			break;
		case R.id.button_edit:
			Currency currency = null;
			for(int i=0; i<listView.getCount(); i++) {
				CheckBox cb = (CheckBox)((View)listView.getChildAt(i)).findViewById(R.id.checkbox);
				if(cb.isChecked()) {
					Cursor cursor = (Cursor)listView.getItemAtPosition(i);
					currency = new Currency();
					currency.toDTO(dbHelper, cursor);
				}
			}
			
			Log.d(ManageCurrencyActivity.class.getName(), "Editing "+currency.getLongName());
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

	private void deleteCurrencies() {
		final Context parentContext = this;
		
		// Confirm
		SimpleDialog.confirmDialog(this, "Are you sure you want to delete these currencies ?", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String err = "";
				
				for(int i=0; i<listView.getCount(); i++) {
					CheckBox cb = (CheckBox)((View)listView.getChildAt(i)).findViewById(R.id.checkbox);
					if(cb.isChecked()) {
						// Verify that no expenses need this currency
						Cursor cursor = (Cursor)listView.getItemAtPosition(i);
						Currency currency = new Currency();
						currency.toDTO(dbHelper, cursor);
						
						int nb = dbHelper.getDb().query(ExpenseData.TABLE_NAME, 
								new String[] {ExpenseData.KEY_ID}, 
								ExpenseData.KEY_ID_CURRENCY+"="+currency.getId(), 
								null, null, null, null).getCount();
						
						if(nb == 0)
							currency.delete(dbHelper);
						else 
							err += "  - "+currency.getLongName()+"\n";
					}
				}
				
				if(! "".equals(err))
					SimpleDialog.errorDialog(parentContext, "Error", "One or more currencies were not deleted (used by expenses)\n"+err).show();
				
				// Refresh display
				fillData();
				dialog.dismiss();
			}
		}).show();
	}
	
	
}
