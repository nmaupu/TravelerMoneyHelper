package org.maupu.android.tmh;

import org.maupu.android.R;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.ExpenseData;
import org.maupu.android.tmh.database.object.Currency;

import android.database.Cursor;
import android.os.Bundle;

public class ManageCurrencyActivity extends ManageableObjectActivity {
	private DatabaseHelper dbHelper = new DatabaseHelper(this);

	public ManageCurrencyActivity() {
		super("Currencies", R.drawable.ic_stat_categories, AddOrEditCurrencyActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper.openWritable();
		refreshListView();
	}

	protected void refreshListView() {
		Currency currency = new Currency();
		Cursor c = currency.fetchAll(dbHelper);

		super.setAdapter(
				R.layout.currency_item,
				c,
				new String[]{CurrencyData.KEY_LONG_NAME, CurrencyData.KEY_SHORT_NAME, CurrencyData.KEY_TAUX_EURO, CurrencyData.KEY_LAST_UPDATE},
				new int[]{R.id.longName, R.id.shortName, R.id.tauxEuro, R.id.lastUpdate});
	}

	@Override
	protected boolean delete(int itemId) {
		Currency currency = new Currency();
		Cursor c = currency.fetch(dbHelper, itemId);
		currency.toDTO(dbHelper, c);
		
		int nb = dbHelper.getDb().query(ExpenseData.TABLE_NAME, 
				new String[] {ExpenseData.KEY_ID}, 
				ExpenseData.KEY_ID_CURRENCY+"="+currency.getId(), 
				null, null, null, null).getCount();
		
		if(nb == 0)
			return currency.delete(dbHelper);
		else
			return false;
	}
}