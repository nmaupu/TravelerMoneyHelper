package org.maupu.android.tmh.ui.widget;

import java.util.List;
import java.util.Map;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ui.Flag;
import org.w3c.dom.Text;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Adapter to display icon named 'icon' and its related text named 'name'
 * @author nmaupu
 *
 */
public class SimpleIconAdapter extends SimpleAdapter {
	public SimpleIconAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		
		ImageView icon = (ImageView)view.findViewById(R.id.icon);
        TextView text = (TextView)view.findViewById(R.id.name);

		@SuppressWarnings("unchecked")
		Map<String,?> item = (Map<String,?>)super.getItem(position);
        int resId = Integer.parseInt((String)item.get("icon"));
        String name = (String)item.get("name");

		icon.setImageResource(resId);
        text.setText(name);
		
		return view;
	}

}
