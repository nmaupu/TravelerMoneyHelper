package org.maupu.android.tmh;

import java.util.GregorianCalendar;

import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.filter.AFilter;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.widget.IViewPagerAdapter;
import org.maupu.android.tmh.ui.widget.IconCheckableCursorAdapter;
import org.maupu.android.tmh.ui.widget.SpinnerManager;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;

public class ManageOperationActivity extends ManageableObjectActivity<Operation> implements OnItemSelectedListener, IViewPagerAdapter{
	private static Operation dummyOperation = new Operation();
	private SpinnerManager spinnerAccountManager;
	//private ViewPagerAdapter vpAdapter;

	public ManageOperationActivity() {
		// animList is disabled because of bad performance
		super(R.string.activity_title_manage_operation, AddOrEditOperationActivity.class, new Operation(), false);
	}

	@Override
	protected boolean validateConstraintsForDeletion(Operation obj) {
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Spinner spinnerAccount = new Spinner(this);
		spinnerAccount.setOnItemSelectedListener(this);
		spinnerAccount.setLayoutParams(
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		spinnerAccountManager = new SpinnerManager(this, spinnerAccount);
		Account dummy = new Account();
		Cursor c = dummy.fetchAll();
		spinnerAccountManager.setAdapter(c, AccountData.KEY_NAME);
		
		ViewGroup header = (ViewGroup)findViewById(R.id.header);
		header.setVisibility(View.VISIBLE);
		header.addView(spinnerAccount);
	}

	@Override
	public void refreshDisplay() {
		if(spinnerAccountManager == null)
			return;
		
		Cursor c = spinnerAccountManager.getSelectedItem();
		int idxId = c.getColumnIndexOrThrow(AccountData.KEY_ID);
		int id = c.getInt(idxId);
		
		dummyOperation.getFilter().addFilter(AFilter.FUNCTION_EQUAL, OperationData.KEY_ID_ACCOUNT, String.valueOf(id));
		c = dummyOperation.fetchByMonth(new GregorianCalendar().getTime());

		IconCheckableCursorAdapter adapter = new IconCheckableCursorAdapter(this, 
				R.layout.operation_item,
				c,
				new String[]{"icon", "account", "category", "dateString", "amountString", "euroAmount"},
				new int[]{R.id.icon, R.id.account, R.id.category, R.id.date, R.id.amount, R.id.euroAmount});
		super.setAdapter(adapter);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		refreshDisplay();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public View getView(int position) {
		return this.getListView();
	}
	
	@Override
	protected void onClickUpdate(Integer[] objs) {}
}
