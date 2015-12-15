package org.maupu.android.tmh;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

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
	private IconCheckableCursorAdapter iconCheckableCursorAdapter = null;
	//private ViewPagerAdapter vpAdapter;

	public ManageOperationActivity() {
		// animList is disabled because of bad performance
		super(R.string.activity_title_manage_operation, AddOrEditOperationActivity.class, new Operation(), false);
	}

    @Override
    public int whatIsMyDrawerIdentifier() {
        return super.DRAWER_ITEM_OPERATIONS;
    }

	@Override
	protected boolean validateConstraintsForDeletion(Operation obj) {
		return true;
	}
	
	@Override
	protected void onDestroy() {
		spinnerAccountManager.closeAdapterCursor();
		closeIconCheckableCursorAdapterIfNeeded();
		
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Spinner spinnerAccount = new Spinner(this);
		spinnerAccount.setOnItemSelectedListener(this);
		spinnerAccount.setLayoutParams(
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		spinnerAccountManager = new SpinnerManager(this, spinnerAccount);
		Account dummy = new Account();
		Cursor c = dummy.fetchAll();
		spinnerAccountManager.setAdapter(c, AccountData.KEY_NAME);
		
		ViewGroup header = (ViewGroup)findViewById(R.id.header);
		header.setVisibility(View.VISIBLE);
		header.addView(spinnerAccount);
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

	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		Cursor cursor = spinnerAccountManager.getSelectedItem();
		int idxId = cursor.getColumnIndexOrThrow(AccountData.KEY_ID);
		int id = cursor.getInt(idxId);
		
		dummyOperation.getFilter().addFilter(AFilter.FUNCTION_EQUAL, OperationData.KEY_ID_ACCOUNT, String.valueOf(id));
		Cursor c = dummyOperation.fetchByMonth(new GregorianCalendar().getTime());
		
		Map<Integer, Object> results = new HashMap<Integer, Object>();
		results.put(0, c);
		
		return results;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> results) {
		if(spinnerAccountManager == null)
			return;
		
		Cursor c = (Cursor) results.get(0);
		
		closeIconCheckableCursorAdapterIfNeeded();
		
		// reset adapter
		iconCheckableCursorAdapter = new IconCheckableCursorAdapter(this, 
				R.layout.operation_item,
				c,
				new String[]{"icon", "category", "dateStringHours", "amountString", "convertedAmount"},
				new int[]{R.id.icon, R.id.category, R.id.date, R.id.amount, R.id.convAmount});
		super.setAdapter(iconCheckableCursorAdapter);
	}
	
	private void closeIconCheckableCursorAdapterIfNeeded() {
		try {
			iconCheckableCursorAdapter.getCursor().close();
		} catch(NullPointerException npe) {
			// nothing to be done
		}
	}
}
