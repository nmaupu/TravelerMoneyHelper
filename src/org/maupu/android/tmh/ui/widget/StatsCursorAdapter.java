package org.maupu.android.tmh.ui.widget;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.CurrencyData;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class StatsCursorAdapter extends SimpleCursorAdapter {
	private Cursor cursor;
	
	public StatsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		
		this.cursor = c;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);
		
		TextView tvAmount = (TextView)row.findViewById(R.id.amount);
		int oldPosition = cursor.getPosition();
		cursor.moveToPosition(position);
		int idx = cursor.getColumnIndex(CurrencyData.KEY_SHORT_NAME);
		tvAmount.setText(tvAmount.getText()+" "+cursor.getString(idx));
		cursor.moveToPosition(oldPosition);
		
		return row;
	}
}
