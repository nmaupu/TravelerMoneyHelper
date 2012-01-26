package org.maupu.android.tmh;

import org.maupu.android.tmh.database.OperationData;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(R.layout.main);
		//customTB.setName("Operation");
		//customTB.setIcon(R.drawable.ic_stat_categories);

		
		addTab(ManageOperationActivity.class, OperationData.OPERATION_TYPE_CASH, R.string.tab_operation, R.drawable.ic_tab_home);
		addTab(ManageOperationActivity.class, OperationData.OPERATION_TYPE_WITHDRAWAL, R.string.tab_withdrawal, R.drawable.ic_tab_withdrawal);
		addTab(ManageOperationActivity.class, OperationData.OPERATION_TYPE_CREDITCARD, R.string.tab_credit, R.drawable.ic_tab_credit);
		addTab(StatsActivity.class, null, R.string.tab_stats, R.drawable.ic_tab_trombone);
		
		getTabHost().setCurrentTab(0);
	}
	
	private void addTab(Class<?> activity, String operationType, int titleId, int drawableId) {
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		intent = new Intent().setClass(this, activity);
		if(operationType != null)
			intent.putExtra(ManageOperationActivity.EXTRA_OPERATION_TYPE, new String[]{operationType});
		spec = tabHost.newTabSpec(getString(titleId)).setIndicator(getString(titleId),
				res.getDrawable(drawableId)).setContent(intent);
		tabHost.addTab(spec);
	}
}