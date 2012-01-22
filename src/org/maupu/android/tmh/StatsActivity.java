package org.maupu.android.tmh;

import java.util.Date;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.ExpenseData;
import org.maupu.android.tmh.database.UserData;
import org.maupu.android.tmh.database.object.Expense;

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
		
		
		Expense dummyExpense = new Expense();
		Cursor sumWithdrawal = dummyExpense.sumExpenseByMonth(dbHelper, new Date(), ExpenseData.EXPENSE_TYPE_WITHDRAWAL);
		Cursor sumCreditCard = dummyExpense.sumExpenseByMonth(dbHelper, new Date(), ExpenseData.EXPENSE_TYPE_CREDITCARD);
		
		String sWithdrawalBalance = getStringBalance(sumWithdrawal);
		String sCreditCardBalance = getStringBalance(sumCreditCard);
		
		
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		TextView tvWithdrawal = new TextView(this);
		tvWithdrawal.setText("Withdrawal : "+sWithdrawalBalance);
		TextView tvCredit = new TextView(this);
		tvCredit.setText("Credit : "+sCreditCardBalance);
		
		
		ll.addView(tvWithdrawal);
		ll.addView(tvCredit);
		setContentView(ll);
	}
	
	private String getStringBalance(Cursor c) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<c.getCount(); i++) {
			int userIdx = c.getColumnIndex(UserData.KEY_NAME);
			int sumIdx = c.getColumnIndex(Expense.KEY_SUM);
			String user = c.getString(userIdx);
			Float sum = c.getFloat(sumIdx);
			
			sb.append(user+"="+sum+" | ");
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
