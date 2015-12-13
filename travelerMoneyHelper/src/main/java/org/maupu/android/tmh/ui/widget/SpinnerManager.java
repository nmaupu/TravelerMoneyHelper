package org.maupu.android.tmh.ui.widget;

import org.maupu.android.tmh.database.object.BaseObject;

import android.content.Context;
import android.database.Cursor;
import android.widget.CursorAdapter;
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
    private SpinnerAdapter adapter;
	
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
				c, new String[]{from}, new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.adapter = adapter;
		return adapter;
	}
	
	public void setAdapter(Cursor c, String from) {
		// Close previous cursor
		closeAdapterCursor();

        SpinnerAdapter a = createSpinnerCursorAdapter(c, from);
		spinner.setAdapter(a);
		spinner.setEnabled(c != null && c.getCount() > 1);
        this.adapter = a;
	}
	
	public void closeAdapterCursor() {
		try {
            ((SimpleCursorAdapter)this.adapter).getCursor().close();
		} catch (NullPointerException npe) {
			// Nothing to be done
		}
	}

    /**
     * Set spinner to position corresponding to value.
     * @param value
     * @param dummy
     */
	public void setSpinnerPositionCursor(String value, BaseObject dummy) {
		// Finding value's position
		int count = spinner.getCount();
		SimpleCursorAdapter adapter = (SimpleCursorAdapter)this.adapter;

		for(int i=0; i<count; i++) {
			Cursor c = (Cursor)adapter.getItem(i);
			dummy.toDTO(c);
			if(isSpinnerValueEqualsToBaseObject(dummy, value)) {
				spinner.setSelection(i, true);
				break;
			}
		}
	}

    /**
     * Set position of spinner by provided a String
     * @param value
     */
	public void setSpinnerPositionString(String value) {
		int count = spinner.getCount();
		SpinnerAdapter adapter = spinner.getAdapter();
		
		for(int i=0; i<count; i++) {
			String val = (String)adapter.getItem(i);
			if(val.equals(value))
				spinner.setSelection(i, true);
		}
	}
	
	private boolean isSpinnerValueEqualsToBaseObject(BaseObject bo, String value) {
		return bo != null && value != null && value.equals(bo.toString());
	}
	
	public Cursor getSelectedItem() {
        try {
            return (Cursor) spinner.getSelectedItem();
        } catch(ClassCastException cce) {
            return null;
        }
	}
	
	public Spinner getSpinner() {
		return spinner;
	}
}
