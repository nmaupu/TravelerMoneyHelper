package org.maupu.android.tmh;

import android.database.Cursor;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncUpdater;

import java.util.HashMap;
import java.util.Map;

public class ManageCurrencyFragment extends ManageableObjectFragment<Currency> {
    public ManageCurrencyFragment() {
        super(R.string.fragment_title_manage_currency, AddOrEditCurrencyFragment.class, new Currency(), true);
    }

    @Override
    protected boolean validateConstraintsForDeletion(Currency obj) {
        int nbOperation = TmhApplication.getDatabaseHelper().getDb().query(OperationData.TABLE_NAME,
                new String[]{OperationData.KEY_ID},
                OperationData.KEY_ID_CURRENCY + "=" + obj.getId(),
                null, null, null, null).getCount();
        int nbAccount = TmhApplication.getDatabaseHelper().getDb().query(AccountData.TABLE_NAME,
                new String[]{AccountData.KEY_ID},
                AccountData.KEY_ID_CURRENCY + "=" + obj.getId(),
                null, null, null, null).getCount();

        return nbOperation + nbAccount == 0;
    }

    @Override
    public void onDestroy() {
        closeCursorAdapterIfNeeded();
        super.onDestroy();
    }

    @Override
    protected void onClickUpdate(Integer[] objs) {
        Currency[] currencies = new Currency[objs.length];
        OpenExchangeRatesAsyncUpdater rateUpdater = new OpenExchangeRatesAsyncUpdater(getActivity(), StaticData.getPreferenceValueString(StaticData.PREF_OER_EDIT));
        //GoogleRateAsyncUpdater rateUpdater = new GoogleRateAsyncUpdater(this);

        // Loading currencies list from objs
        for (int i = 0; i < objs.length; i++) {
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

        refreshDisplay();
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground() {
        Currency currency = new Currency();
        Cursor c = currency.fetchAll();

        Map<Integer, Object> results = new HashMap<>();
        results.put(0, c);

        return results;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        super.activateUpdateButton();

        Cursor c = (Cursor) results.get(0);

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
        } catch (NullPointerException npe) {
            // nothing to be done
        }
    }
}
