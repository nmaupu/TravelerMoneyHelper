package org.maupu.android.tmh.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ui.SimpleIconItem;

public class IconArrayAdapter extends ArrayAdapter<SimpleIconItem> {
    private Context context;
    private int layoutResource;
    private SimpleIconItem[] data;
    private int selectedItem = 0;

    public IconArrayAdapter(Context context, int layoutRes, SimpleIconItem[] data) {
        super(context, layoutRes, data);
        this.context = context;
        this.layoutResource = layoutRes;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SimpleIconHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(this.layoutResource, parent, false);
            holder = new SimpleIconHolder();
            holder.imageView = (ImageView)row.findViewById(R.id.icon);
            holder.textView = (TextView)row.findViewById(R.id.text);

            row.setTag(holder);
        } else {
            holder = (SimpleIconHolder)row.getTag();
        }

        SimpleIconItem item = data[position];
        holder.textView.setText(item.getText());

        // Handling selected item vs. other items
        int tf = Typeface.NORMAL;
        int color = row.getResources().getColor(R.color.white);
        if(this.selectedItem == position) {
            tf = Typeface.BOLD;
            color = row.getResources().getColor(R.color.light_gray);
        }
        row.setBackgroundColor(color);
        holder.textView.setTypeface(null, tf);
        holder.imageView.setImageResource(item.getIconResource());

        return row;
    }

    public void selectItem(int position) {
        this.selectedItem = position;
        notifyDataSetChanged();
    }

    static class SimpleIconHolder {
        ImageView imageView;
        TextView textView;
    }
}
