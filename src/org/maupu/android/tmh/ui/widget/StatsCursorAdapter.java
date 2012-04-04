package org.maupu.android.tmh.ui.widget;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.util.NumberUtil;

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
		
		int idxShortName = cursor.getColumnIndex(CurrencyData.KEY_SHORT_NAME);
		int idxAmount = cursor.getColumnIndex("amountString");
		
		Double amount = cursor.getDouble(idxAmount);
		tvAmount.setText(NumberUtil.formatDecimalLocale(amount)+" "+cursor.getString(idxShortName));
		
		cursor.moveToPosition(oldPosition);
		
		return row;
	}
}
