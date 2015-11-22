package org.maupu.android.tmh.ui.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.maupu.android.tmh.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;

/**
 * Class providing an easy way to have a item with a checkbox
 * Must provide a view with a checkbox with id <em>checkbox</em>
 * @author nmaupu
 */
public class CheckableCursorAdapter extends SimpleCursorAdapter implements OnClickListener {
	private NumberCheckedListener listener = null;
	private Map<Integer, Boolean> positionsChecked = new HashMap<Integer, Boolean>();
	private int numberChecked = 0;
	private Integer[] toCheck;
    private Map<Integer, Boolean> inits = new HashMap<Integer, Boolean>();

	public CheckableCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}
	
	public CheckableCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, Integer[] toCheck) {
		super(context, layout, c, from, to);
		setToCheck(toCheck);
	}

    public void setToCheck(Integer[] toCheck) {
        this.toCheck = toCheck;
        clearCheckedItems();
        for(int i = 0; i<toCheck.length; i++)
            addToCheckedItems(toCheck[i]);
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);
		
		row.setOnClickListener(this);
		CheckBox cb = (CheckBox)row.findViewById(R.id.checkbox);

		if(cb != null) {
			cb.setTag(position);
			cb.setOnClickListener(this);
			
			boolean status = positionsChecked.get(position) != null ? positionsChecked.get(position) : false;
			cb.setChecked(status);

            /**
             * Check if checkbox is first init and check if asked
             * but only the first time it is displayed to avoid having a checkbox impossible to
             * check
             */
            if(inits.get(position) == null) {
                // This cb has been initialized
                inits.put(position, new Boolean(true));

                if (toCheck != null && toCheck.length > 0) {
                    for(int c = 0; c<toCheck.length; c++) {
                        int pos = toCheck[c];
                        if(position == pos)
                            cb.setChecked(true);
                    } //for
                } //if
            } //if cb not initialized
		}

		return row;
	}
	
	public void setOnNumberCheckedListener(NumberCheckedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onClick(View v) {
		CheckBox cb = (CheckBox)v.findViewById(R.id.checkbox);
		if(cb == null)
			return;
		
		if(v.getId() != R.id.checkbox) {
			// When click on checkbox, do nothing because it is toggled automatically
			cb.toggle();
		}
		
		Integer position = (Integer)cb.getTag();
		
		// Store only if checked (memory consumption issue)
		if(cb.isChecked()) {
			addToCheckedItems(position);
		} else {
			removeFromCheckedItems(position);
		}
		
		if(listener != null)
			listener.onCheckedItem(numberChecked);
	}

    private void addToCheckedItems(int position) {
        positionsChecked.put(position, true);
        numberChecked++;
    }

    private void removeFromCheckedItems(int position) {
        positionsChecked.remove(position);
        numberChecked--;
    }

    public void clearCheckedItems() {
        positionsChecked.clear();
        numberChecked = 0;
    }

    @Override
    public void changeCursor(Cursor c) {
        super.changeCursor(c);
    }

    public Integer[] getCheckedPositions() {
		return positionsChecked.keySet().toArray(new Integer[0]);
	}
}
