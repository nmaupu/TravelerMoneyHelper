package org.maupu.android.tmh;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Currency;

import android.database.Cursor;

public class ManageCurrencyActivity extends ManageableObjectActivity<Currency> {
	public ManageCurrencyActivity() {
		super(R.string.activity_title_manage_currency, AddOrEditCurrencyActivity.class, new Currency(), true);
	}

	@Override
	public void refreshDisplay() {
		Currency currency = new Currency();
		Cursor c = currency.fetchAll();
		
		super.activateUpdateButton();

		super.setAdapter(
				R.layout.currency_item,
				c,
				new String[]{CurrencyData.KEY_LONG_NAME, CurrencyData.KEY_SHORT_NAME, CurrencyData.KEY_TAUX_EURO, CurrencyData.KEY_LAST_UPDATE},
				new int[]{R.id.longName, R.id.shortName, R.id.tauxEuro, R.id.lastUpdate});
	}

	@Override
	protected boolean validateConstraintsForDeletion(Currency obj) {
		int nb = TmhApplication.getDatabaseHelper().getDb().query(OperationData.TABLE_NAME, 
				new String[] {OperationData.KEY_ID}, 
				OperationData.KEY_ID_CURRENCY+"="+obj.getId(), 
				null, null, null, null).getCount();
		nb += TmhApplication.getDatabaseHelper().getDb().query(AccountData.TABLE_NAME,
				new String[] {AccountData.KEY_ID},
				AccountData.KEY_ID_CURRENCY+"="+obj.getId(),
				null, null, null, null).getCount();
		
		return nb == 0;
	}

	@Override
	protected void onClickUpdate(Integer[] objs) {
		Currency cur = new Currency();
		
		// TODO Display a popup with a progress
		for(int i=0; i<objs.length; i++) {
			Cursor c = cur.fetch(objs[i]);
			cur.toDTO(c);
			
			try {
				cur.updateRateFromGoogle();
				cur.update();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Refresh list when finished
		refreshDisplay();
	}
}