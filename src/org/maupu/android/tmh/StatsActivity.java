package org.maupu.android.tmh;

import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Operation;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatsActivity extends TmhActivity {
	DatabaseHelper dbHelper = new DatabaseHelper(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// No custom title bar because activity used in a TabHost
		super.onCreate(savedInstanceState);
		
		dbHelper.openReadable();
		
		
		//Operation dummyOperation = new Operation();
		//Cursor sumWithdrawal = dummyOperation.sumOperationByMonth(dbHelper, new Date(), OperationData.OPERATION_TYPE_WITHDRAWAL);
		//Cursor sumCreditCard = dummyOperation.sumOperationByMonth(dbHelper, new Date(), OperationData.OPERATION_TYPE_CREDITCARD);
		
		//String sWithdrawalBalance = getStringBalance(sumWithdrawal);
		//String sCreditCardBalance = getStringBalance(sumCreditCard);
		
		
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		TextView tvWithdrawal = new TextView(this);
		tvWithdrawal.setText("Withdrawal : ");
		TextView tvCredit = new TextView(this);
		tvCredit.setText("Credit : ");
		
		
		ll.addView(tvWithdrawal);
		ll.addView(tvCredit);
		setContentView(ll);
	}
	
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
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi = menu.findItem(R.id.item_add);
		if(mi != null)
			mi.setEnabled(false);
		return true;
	}
	
	@Override
	public void refreshDisplay(DatabaseHelper dbHelper) {}
	
	@Override
	protected void onAddClicked() {}
}
