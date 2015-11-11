package org.maupu.android.tmh.ui.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.maupu.android.tmh.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DateGalleryAdapter extends BaseAdapter {
	@SuppressLint("SimpleDateFormat")
	private DateFormat dateFormatFirst = new SimpleDateFormat("MMMM");
	@SuppressLint("SimpleDateFormat")
	private DateFormat dateFormatSecond = new SimpleDateFormat("yyyy");
	private List<Date> dates;
	private Context context;

	public DateGalleryAdapter(Context context) {
		super();
		this.context = context;
	}

	public DateGalleryAdapter(Context context, List<Date> list) {
		this(context);
		setData(list);
	}
	
	public void setFirstDateFormat(DateFormat df) {
		dateFormatFirst = df;
	}
	
	public void setSecondDateFormat(DateFormat df) {
		dateFormatSecond = df;
	}

	public void setData(List<Date> list) {
		this.dates = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.date_gallery_item, null);
		}

		LinearLayout layout = (LinearLayout)convertView.findViewById(R.id.root_layout);
		TextView tvFirst = (TextView)convertView.findViewById(R.id.text_view_first);
		TextView tvSecond = (TextView)convertView.findViewById(R.id.text_view_second);

		if(dateFormatFirst != null)
			tvFirst.setText(dateFormatFirst.format(getItem(position)));
		else
			tvFirst.setVisibility(View.GONE);
		
		if(dateFormatSecond != null)
			tvSecond.setText(dateFormatSecond.format(getItem(position)));
		else
			tvSecond.setVisibility(View.GONE);
		
		Gallery gallery = (Gallery)parent;
		if(gallery.getSelectedItemPosition() == position) {
			layout.setBackgroundColor(Color.rgb(255, 255, 153));
		}
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return dates == null ? 0 : dates.size(); 
	}

	@Override
	public Object getItem(int position) {
		return dates == null ? null : dates.get(position);
	}
	
	public int getItemPosition(Date d) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(d);
		
		int ret = -1;
		int size = dates.size();
		for (int i=0; i<size; i++) {
			Calendar cal2 = GregorianCalendar.getInstance();
			cal2.setTime((Date)getItem(i));
			if(		cal2.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
					cal2.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && 
					cal2.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)) {
				ret = i;
				break;
			}
		}
		
		return ret;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
