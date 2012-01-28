package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.ui.widget.ViewPagerOperationAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class ViewPagerTestActivity extends TmhActivity implements OnPageChangeListener {
	private DatabaseHelper dbHelper = new DatabaseHelper(this);
	private ViewPagerOperationAdapter adapter;
	private int currentPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main2);

		dbHelper.openReadable();

		adapter = new ViewPagerOperationAdapter(this, dbHelper);
		ViewPager vp = (ViewPager)findViewById(R.id.viewpager);
		vp.setAdapter(adapter);
		currentPosition = adapter.getCount()/2;
		vp.setCurrentItem(currentPosition);
		
		vp.setOnPageChangeListener(this);
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
	protected void onAddClicked() {
		// Add operation
		Intent intent = new Intent(this, AddOrEditOperationActivity.class);
		this.startActivityForResult(intent, 0);
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
