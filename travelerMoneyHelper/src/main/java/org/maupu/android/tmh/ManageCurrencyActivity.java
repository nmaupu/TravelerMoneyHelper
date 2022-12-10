package org.maupu.android.tmh;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncUpdater;

import android.annotation.SuppressLint;
import android.database.Cursor;

@SuppressLint("UseSparseArrays")
public class ManageCurrencyActivity extends ManageableObjectActivity<Currency> {
	public ManageCurrencyActivity() {
		super(R.string.activity_title_manage_currency, AddOrEditCurrencyActivity.class, new Currency(), true);
	}

    @Override
    public int whatIsMyDrawerIdentifier() {
        return super.DRAWER_ITEM_CURRENCIES;
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
	protected void onDestroy() {
		closeCursorAdapterIfNeeded();
		
		super.onDestroy();
	}

	@Override
	protected void onClickUpdate(Integer[] objs) {
		Currency[] currencies = new Currency[objs.length];
		OpenExchangeRatesAsyncUpdater rateUpdater = new OpenExchangeRatesAsyncUpdater(this, StaticData.getPreferenceValueString(StaticData.PREF_OER_EDIT));
		//GoogleRateAsyncUpdater rateUpdater = new GoogleRateAsyncUpdater(this);

		// Loading currencies list from objs
		for(int i=0; i<objs.length; i++) {
			Currency cur = new Currency();
			Cursor c = cur.fetch(objs[i]);
			cur.toDTO(c);
			c.close();

			currencies[i] = cur;
		}

		try {
			rateUpdater.execute(currencies);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		Currency currency = new Currency();
		Cursor c = currency.fetchAll();
		
		Map<Integer, Object> results = new HashMap<Integer, Object>();
		results.put(0, c);
		
		return results;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> results) {
		super.activateUpdateButton();
		
		Cursor c = (Cursor)results.get(0);
		
		closeCursorAdapterIfNeeded();

		super.setAdapter(
				R.layout.currency_item,
				c,
				new String[]{CurrencyData.KEY_LONG_NAME, CurrencyData.KEY_SHORT_NAME, CurrencyData.KEY_CURRENCY_LINKED, CurrencyData.KEY_LAST_UPDATE},
				new int[]{R.id.longName, R.id.shortName, R.id.rateCurrencyLinked, R.id.lastUpdate});
	}
	
	private void closeCursorAdapterIfNeeded() {
		try {
			super.getAdapter().getCursor().close();
		} catch(NullPointerException npe) {
			// nothing to be done
		}
	}
}
