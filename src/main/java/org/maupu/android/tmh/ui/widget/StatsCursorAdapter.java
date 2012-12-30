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
	
	public StatsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);
		
		TextView tvAmount = (TextView)row.findViewById(R.id.amount);
		int oldPosition = getCursor().getPosition();
		getCursor().moveToPosition(position);
		
		int idxShortName = getCursor().getColumnIndex(CurrencyData.KEY_SHORT_NAME);
		int idxAmount = getCursor().getColumnIndex("amountString");
		
		Double amount = getCursor().getDouble(idxAmount);
		tvAmount.setText(NumberUtil.formatDecimalLocale(amount)+" "+getCursor().getString(idxShortName));
		
		getCursor().moveToPosition(oldPosition);
		
		return row;
	}
}
