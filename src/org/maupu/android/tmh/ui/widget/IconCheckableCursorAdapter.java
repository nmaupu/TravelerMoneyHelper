package org.maupu.android.tmh.ui.widget;

import java.io.FileInputStream;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.AccountData;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class IconCheckableCursorAdapter extends CheckableCursorAdapter {
	private Cursor cursor;
	private Context context;
	
	public IconCheckableCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.cursor = c;
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		
		ImageView icon = (ImageView)v.findViewById(R.id.icon);
		
		int oldPosition = cursor.getPosition();
		cursor.moveToPosition(position);
		int idx = cursor.getColumnIndex(AccountData.KEY_ICON);
		String iconFilename = cursor.getString(idx);
		
		// restore initial cursor position
		cursor.moveToPosition(oldPosition);
		
		try {
			FileInputStream fIn = context.openFileInput(iconFilename);
			Bitmap bm = BitmapFactory.decodeStream(fIn);
			icon.setImageBitmap(bm);
		} catch (Exception fnfe) {
			icon.setImageResource(R.drawable.icon_default);
		}
		
		return v;
	}
}
