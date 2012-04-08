package org.maupu.android.tmh;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem.Type;

import org.maupu.android.tmh.core.TmhApplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class DashboardActivity extends GDActivity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.dashboard);
		setTitle(getString(R.string.app_name));
		addActionBarItem(Type.Info, TmhApplication.ACTION_BAR_INFO);
		
		
	    findViewById(R.id.dashboard_button_operations).setOnClickListener(this);
	    findViewById(R.id.dashboard_button_stats).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		
		switch(v.getId()) {
		case R.id.dashboard_button_operations:
			intent = new Intent(DashboardActivity.this, ViewPagerOperationActivity.class);
			break;
		case R.id.dashboard_button_stats:
			intent = new Intent(DashboardActivity.this, StatsActivity.class);
			break;
		}
		
		if(intent != null)
			startActivity(intent);
	}

	
}
