package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;

import android.os.Bundle;
import android.widget.TextView;

public class ShareExpensesActivity extends TmhActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// No custom title bar because activity used in a TabHost
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setText("ShareExpensesActivity");
		setContentView(tv);
	}
	
	@Override
	public void refreshDisplay(DatabaseHelper dbHelper) {}
}
