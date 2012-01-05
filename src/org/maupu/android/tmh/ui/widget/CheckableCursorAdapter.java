package org.maupu.android.tmh.ui.widget;

import java.util.HashMap;
import java.util.Map;

import org.maupu.android.tmh.R;

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
 *
 */
public class CheckableCursorAdapter extends SimpleCursorAdapter implements OnClickListener {
	private NumberCheckedListener listener = null;
	private Map<Integer, Boolean> positionsChecked = new HashMap<Integer, Boolean>();
	private int numberChecked = 0;

	public CheckableCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
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
