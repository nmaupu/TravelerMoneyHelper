package org.maupu.android.tmh;

import greendroid.app.GDActivity;
import android.os.Bundle;

public class DashboardActivity extends GDActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.dashboard_activity);
		setTitle(getString(R.string.app_name));
	}

	
}
