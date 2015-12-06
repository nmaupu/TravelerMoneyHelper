package org.maupu.android.tmh.ui.widget;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.util.NumberUtil;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class StatsCursorAdapter extends SimpleCursorAdapter {
	
	public StatsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);
		
		TextView tvAmount = (TextView)row.findViewById(R.id.amount);
		TextView tvAvg = (TextView)row.findViewById(R.id.average);
		LinearLayout llHead = (LinearLayout)row.findViewById(R.id.ll_head);
		
		int oldPosition = getCursor().getPosition();
		getCursor().moveToPosition(position);
		
		int idxShortName = getCursor().getColumnIndex(CurrencyData.KEY_SHORT_NAME);
		int idxAmount = getCursor().getColumnIndex("amountString");
		int idxAvg = getCursor().getColumnIndex("avg");
		
		String shortName = getCursor().getString(idxShortName);
		
		Double amount = getCursor().getDouble(idxAmount);
		tvAmount.setText(NumberUtil.formatDecimal(amount)+" "+shortName);
		
		if(idxAvg == -1) {
			tvAvg.setVisibility(View.GONE);
			llHead.setVisibility(View.GONE);
		} else {
			tvAvg.setVisibility(View.VISIBLE);
			llHead.setVisibility(View.VISIBLE);
			Double average = getCursor().getDouble(idxAvg);
			tvAvg.setText(NumberUtil.formatDecimal(average)+" "+shortName);
		}
		
		
		getCursor().moveToPosition(oldPosition);
		
		return row;
	}
}
