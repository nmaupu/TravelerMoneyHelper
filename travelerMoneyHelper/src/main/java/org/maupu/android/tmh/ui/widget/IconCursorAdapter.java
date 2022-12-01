package org.maupu.android.tmh.ui.widget;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.ui.ICallback;
import org.maupu.android.tmh.ui.ImageViewHelper;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

/**
 * A cursor adapter with an ImageView named 'icon'
 * @author nmaupu
 *
 */
public class IconCursorAdapter extends SimpleCursorAdapter implements OnClickListener {
	private Cursor cursor;
	private Context context;
	private ICallback<View> listener;
	
	public IconCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, ICallback<View> listener) {
		super(context, layout, c, from, to, SimpleCursorAdapter.NO_SELECTION);
		this.cursor = c;
		this.context = context;
		this.listener = listener;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		v.setOnClickListener(this);
		v.setTag(position);
		
		ImageView icon = (ImageView)v.findViewById(R.id.icon);
		
		int oldPosition = cursor.getPosition();
		cursor.moveToPosition(position);
		int idx = cursor.getColumnIndex(AccountData.KEY_ICON);
		String iconFilename = cursor.getString(idx);
		
		// restore initial cursor position
		cursor.moveToPosition(oldPosition);
		
		ImageViewHelper.setIcon(context, icon, iconFilename);
		
		return v;
	}

	@Override
	public void onClick(View v) {
		if(listener != null)
			listener.callback(v);
	}	
}
