package org.maupu.android.tmh;

import java.util.Date;
import java.util.GregorianCalendar;

import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.database.util.DateUtil;
import org.maupu.android.tmh.ui.StaticData;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class StatsActivity extends TmhActivity {
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// No custom title bar because activity used in a TabHost
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.stats_activity);
		
		listView = (ListView)findViewById(R.id.list);
		refreshDisplay();
	}
	
	/*
	private String getStringBalance(Cursor c) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<c.getCount(); i++) {
			int accountIdx = c.getColumnIndex(AccountData.KEY_NAME);
			int sumIdx = c.getColumnIndex(Operation.KEY_SUM);
			String account = c.getString(accountIdx);
			Float sum = c.getFloat(sumIdx);
			
			sb.append(account+"="+sum+" | ");
			c.moveToNext();
		}
		
		return sb.toString();
	}*/
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi = menu.findItem(R.id.item_add);
		if(mi != null)
			mi.setEnabled(false);
		return true;
	}
	
	@Override
	public void refreshDisplay() {
		Account account = StaticData.getCurrentAccount();
		
		Date now = new GregorianCalendar().getTime();
		Operation dummyOp = new Operation();
		Cursor cursor = dummyOp.sumOperationsGroupByDay(account, DateUtil.getFirstDayOfMonth(now), DateUtil.getLastDayOfMonth(now));
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.stats_item,
				cursor,
				new String[]{"dateString", "amountString", CurrencyData.KEY_TAUX_EURO},
				new int[]{R.id.date, R.id.amount, R.id.amount_converted});
		listView.setAdapter(adapter);
	}
	
	@Override
	protected Intent onAddClicked() {
		return null; 
	}
}
