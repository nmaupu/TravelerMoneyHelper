package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.ui.widget.ViewPagerOperationAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class ViewPagerOperationActivity extends TmhActivity implements OnPageChangeListener {
	private ViewPagerOperationAdapter adapter;
	private int currentPosition;
	private Account currentAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main2);

		// Get first account
		currentAccount = new Account();
		Cursor c = currentAccount.fetch(dbHelper, 1);
		currentAccount.toDTO(dbHelper, c);

		adapter = new ViewPagerOperationAdapter(this, dbHelper);
		ViewPager vp = (ViewPager)findViewById(R.id.viewpager);
		vp.setAdapter(adapter);
		currentPosition = adapter.getCount()/2;
		vp.setCurrentItem(currentPosition);

		vp.setOnPageChangeListener(this);
	}
	
	public Account getCurrentAccount() {
		return currentAccount;
	}
	
	public void setCurrentAccount(Account account) {
		this.currentAccount = account;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("onActivityResult", "message="+resultCode);
		refreshDisplay(dbHelper);
	}

	@Override
	public void refreshDisplay(DatabaseHelper dbHelper) {
		adapter.refreshItemView(currentPosition);
	}

	@Override
	protected Intent onAddClicked() {
		// Add operation
		Intent intent = new Intent(this, AddOrEditOperationActivity.class);
		intent.putExtra("account", currentAccount);
		this.startActivityForResult(intent, 0);
		
		return intent;
	}

	@Override
	public void onPageScrollStateChanged(int position) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int position) {
		currentPosition = position;
	}
}
