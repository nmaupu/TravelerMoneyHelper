package org.maupu.android.tmh.ui.widget;

import java.util.HashMap;
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
@SuppressLint("UseSparseArrays")
public class CheckableCursorAdapter extends SimpleCursorAdapter implements OnClickListener {
	private NumberCheckedListener listener = null;
	private Map<Integer, Boolean> positionsChecked = new HashMap<Integer, Boolean>();
	private int numberChecked = 0;
	private Integer[] toCheck;
	private Boolean[] toCheckChanged;

	public CheckableCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}
	
	public CheckableCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, Integer[] toCheck) {
		super(context, layout, c, from, to);
		this.toCheck = toCheck;
		if(toCheck != null) {
			toCheckChanged = new Boolean[toCheck.length];
			for(int i=0; i<toCheckChanged.length; i++) {
				toCheckChanged[i] = false;
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);
		
		row.setOnClickListener(this);
		CheckBox cb = (CheckBox)row.findViewById(R.id.checkbox);
		//cb.setVisibility(View.GONE);
		if(cb != null) {
			cb.setTag(position);
			cb.setOnClickListener(this);
			
			boolean status = positionsChecked.get(position) != null ? positionsChecked.get(position) : false;
			cb.setChecked(status);
			
			if(toCheck != null && toCheck.length > 0) {
				for(int c=0; c<toCheck.length; c++) {
					int p = toCheck[c];
					// Set to true only if this particular checkbox has never been changed
					if(position == p && ! toCheckChanged[c])
						cb.setChecked(true);
				}
			}
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
		
		//
		if(toCheck != null && toCheck.length > 0) {
			for(int c=0; c<toCheck.length; c++) {
				int p = toCheck[c];
				if(position == p)
					toCheckChanged[c] = true;
			}
		}
		
		// Store only if checked (memory consumption issue)
		if(cb.isChecked()) {
			positionsChecked.put(position, true);
			numberChecked++;
		} else {
			positionsChecked.remove(position);
			numberChecked--;
		}
		
		if(listener != null)
			listener.onCheckedItem(numberChecked);
	}
	
	public Integer[] getCheckedPositions() {
		return positionsChecked.keySet().toArray(new Integer[0]);
	}
}
