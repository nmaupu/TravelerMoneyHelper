package org.maupu.android.tmh.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ui.NavigationDrawerIconItem;
import org.maupu.android.tmh.ui.StaticData;

import java.util.List;

public class IconArrayAdapter extends ArrayAdapter<NavigationDrawerIconItem> {
    private Context context;
    private int layoutResource;
    private List<NavigationDrawerIconItem> data;
    private int selectedItem = 0;

    public IconArrayAdapter(Context context, int layoutRes, List<NavigationDrawerIconItem> data) {
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
            holder.layout = (LinearLayout)row.findViewById(R.id.linear_layout_root);
            holder.separator = (TextView)row.findViewById(R.id.separator);

            row.setTag(holder);
        } else {
            holder = (SimpleIconHolder)row.getTag();
        }

        NavigationDrawerIconItem item = data.get(position);
        holder.textView.setText(item.getText());
        holder.textView.setTextSize(item.getTextSize());
        holder.textView.setTextColor(item.getTextColor());
        holder.textView.setTypeface(item.getTypeface());
        ViewGroup.LayoutParams params = holder.layout.getLayoutParams();
        params.height=item.getHeight();
        if(item.hasSeparator()) {
            holder.separator.setHeight(20);
            holder.separator.setVisibility(View.VISIBLE);
            holder.separator.setBackgroundColor(Color.DKGRAY);
        } else {
            holder.separator.setVisibility(View.GONE);
        }


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
        StaticData.navigationDrawerItemSelected = position;
        notifyDataSetChanged();
    }

    public int getSelectedItemPosition() {
        return this.selectedItem;
    }

    static class SimpleIconHolder {
        ImageView imageView;
        TextView textView;
        LinearLayout layout;
        TextView separator;
    }
}
