package org.maupu.android.tmh;

import java.util.Date;
import java.util.GregorianCalendar;

import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.database.util.DateUtil;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.StatsCursorAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stats_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.item_period:
			//startActivityFromMenu(ManageCategoryActivity.class);
			Toast.makeText(this, "Poping up period settings", Toast.LENGTH_SHORT).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
	
	@Override
	public void refreshDisplay() {
		Account account = StaticData.getCurrentAccount();
		
		Date now = new GregorianCalendar().getTime();
		Operation dummyOp = new Operation();
		Cursor cursor = dummyOp.sumOperationsGroupByDay(account, DateUtil.getFirstDayOfMonth(now), DateUtil.getLastDayOfMonth(now));
		
		StatsCursorAdapter adapter = new StatsCursorAdapter(this,
				R.layout.stats_item,
				cursor,
				new String[]{"dateString", "amountString"},
				new int[]{R.id.date, R.id.amount});
		listView.setAdapter(adapter);
	}
	
	@Override
	protected Intent onAddClicked() {
		return null; 
	}
}
