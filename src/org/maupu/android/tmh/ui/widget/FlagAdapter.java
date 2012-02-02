package org.maupu.android.tmh.ui.widget;

import java.util.List;
import java.util.Map;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ui.Flag;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Adapter to display icon named 'icon' and its related name named 'name'
 * @author nmaupu
 *
 */
public class FlagAdapter extends SimpleAdapter {
	public FlagAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		
		ImageView icon = (ImageView)view.findViewById(R.id.icon);
		
		//Flag flag = (Flag)super.getItem(position);
		@SuppressWarnings("unchecked")
		Map<String,?> item = (Map<String,?>)super.getItem(position);
		//name.setText(flag.getCountry());
		icon.setImageResource((Integer)item.get("icon"));
		
		return view;
	}

}
