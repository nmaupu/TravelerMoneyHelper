package org.maupu.android.tmh;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TravelerMoneyHelperActivity extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(R.layout.main);
		//customTB.setName("Expense");
		//customTB.setIcon(R.drawable.ic_stat_categories);

		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		
		intent = new Intent().setClass(this, ManageExpenseActivity.class);
		spec = tabHost.newTabSpec("Expenses").setIndicator("Expenses",
				res.getDrawable(R.drawable.ic_tab_home)).setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, ShareExpensesActivity.class);
		spec = tabHost.newTabSpec("Sharing").setIndicator("Sharing",
				res.getDrawable(R.drawable.ic_tab_trombone)).setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
	}
}