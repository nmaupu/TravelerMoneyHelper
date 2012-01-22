package org.maupu.android.tmh;

import org.maupu.android.tmh.database.ExpenseData;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class TravelerMoneyHelperActivity extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(R.layout.main);
		//customTB.setName("Expense");
		//customTB.setIcon(R.drawable.ic_stat_categories);

		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		
		intent = new Intent().setClass(this, ManageExpenseActivity.class);
		intent.putExtra(ManageExpenseActivity.EXTRA_EXPENSE_TYPE, new String[]{ExpenseData.EXPENSE_TYPE_CASH});
		spec = tabHost.newTabSpec("Expenses").setIndicator(getString(R.string.tab_expense),
				res.getDrawable(R.drawable.ic_tab_home)).setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, ManageExpenseActivity.class);
		intent.putExtra(ManageExpenseActivity.EXTRA_EXPENSE_TYPE, new String[]{ExpenseData.EXPENSE_TYPE_WITHDRAWAL});
		spec = tabHost.newTabSpec("Withdrawal").setIndicator(getString(R.string.tab_withdrawal),
				res.getDrawable(R.drawable.ic_tab_withdrawal)).setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, ManageExpenseActivity.class);
		intent.putExtra(ManageExpenseActivity.EXTRA_EXPENSE_TYPE, new String[]{ExpenseData.EXPENSE_TYPE_CREDITCARD});
		spec = tabHost.newTabSpec("Credit").setIndicator(getString(R.string.tab_credit),
				res.getDrawable(R.drawable.ic_tab_credit)).setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, ShareExpensesActivity.class);
		spec = tabHost.newTabSpec("Stats").setIndicator(getString(R.string.tab_stats),
				res.getDrawable(R.drawable.ic_tab_trombone)).setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
	}
}