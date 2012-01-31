package org.maupu.android.tmh.ui.widget;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Operation;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

public class OperationCheckableCursorAdapter extends CheckableCursorAdapter {

	public OperationCheckableCursorAdapter(Context context, int layout,
			Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		
		TextView tvAmount = (TextView)view.findViewById(R.id.amount);
		int idxAmount = cursor.getColumnIndexOrThrow(OperationData.KEY_AMOUNT);
		float amount = cursor.getFloat(idxAmount);
		
		// Set color for amount
		if(amount >= 0)
			tvAmount.setTextColor(Operation.COLOR_POSITIVE_AMOUNT);
		else
			tvAmount.setTextColor(Operation.COLOR_NEGATIVE_AMOUNT);
	}

}
