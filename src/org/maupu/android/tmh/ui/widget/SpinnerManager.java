package org.maupu.android.tmh.ui.widget;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.BaseObject;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * Easier management of spinners
 * @author nmaupu
 *
 */
public class SpinnerManager {
	private Context ctx;
	private Spinner spinner;
	
	public SpinnerManager(Context ctx, Spinner spinner) {
		this.ctx = ctx;
		this.spinner = spinner;
	}
	
	/**
	 * Create a spinner adapter based on Cursor
	 * @param c
	 * @param from
	 * @return a SpinnerAdapter based on Cursor parameter
	 */
	protected SpinnerAdapter createSpinnerCursorAdapter(Cursor c, String from) {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(ctx, android.R.layout.simple_spinner_item,
				c, new String[]{from}, new int[]{android.R.id.text1});
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}
	
	public void setAdapter(Cursor c, String from) {
		spinner.setAdapter(createSpinnerCursorAdapter(c, from));
	}
	
	/**
	 * Set spinner to position corresponding to value
	 * @param spinner
	 * @param value
	 */
	public void setSpinnerPositionCursor(DatabaseHelper dbHelper, String value, BaseObject dummy) {
		// Finding value's position
		int count = spinner.getCount();
		SimpleCursorAdapter adapter = (SimpleCursorAdapter)spinner.getAdapter();

		for(int i=0; i<count; i++) {
			Cursor c = (Cursor)adapter.getItem(i);
			dummy.toDTO(dbHelper, c);
			if(isSpinnerValueEqualsToBaseObject(dummy, value)) {
				spinner.setSelection(i, true);
				break;
			}
		}
	}
	
	/**
	 * Set position of spinner by provided a String
	 * @param spinner
	 * @param value
	 */
	/*public void setSpinnerPositionString(String value) {
		int count = spinner.getCount();
		SpinnerAdapter adapter = spinner.getAdapter();
		
		for(int i=0; i<count; i++) {
			String val = (String)adapter.getItem(i);
			if(val.equals(value))
				spinner.setSelection(i, true);
		}
	}*/
	
	private boolean isSpinnerValueEqualsToBaseObject(BaseObject bo, String value) {
		return value.equals(bo.toString());
	}
	
	public Cursor getSelectedItem() {
		return (Cursor)spinner.getSelectedItem();
	}
	
	public Spinner getSpinner() {
		return spinner;
	}
}
