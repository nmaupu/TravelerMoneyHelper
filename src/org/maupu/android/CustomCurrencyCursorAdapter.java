package org.maupu.android;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;

public class CustomCurrencyCursorAdapter extends SimpleCursorAdapter implements OnCheckedChangeListener {
	private Context context;
	private int numberChecked = 0;
	
	public CustomCurrencyCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);
		
		CheckBox cb = (CheckBox)row.findViewById(R.id.checkbox);
		cb.setOnCheckedChangeListener(this);
		
		return row;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if(isChecked)
			numberChecked++;
		else
			numberChecked--;
		
		View v = inflater.inflate(R.layout.currency_push_up_menu, null);
		LinearLayout layout = (LinearLayout)((Activity)context).findViewById(R.id.currency_root_layout);
		
		if(numberChecked > 0) {
			layout.addView(v);
		} else {
			v.setVisibility(View.GONE);
			layout.removeView(v);
		}
	}
}
