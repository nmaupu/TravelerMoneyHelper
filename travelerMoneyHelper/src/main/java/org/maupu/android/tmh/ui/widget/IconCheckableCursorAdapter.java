package org.maupu.android.tmh.ui.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.util.ImageUtil;

public class IconCheckableCursorAdapter extends CheckableCursorAdapter {
    private static final String TAG = IconCheckableCursorAdapter.class.getName();
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

        ImageView imageViewIcon = (ImageView) v.findViewById(R.id.icon);

        int oldPosition = cursor.getPosition();
        cursor.moveToPosition(position);
        int idx = cursor.getColumnIndex(AccountData.KEY_ICON_BYTES);
        byte[] iconBytes = cursor.getBlob(idx);
        ImageUtil.setIcon(imageViewIcon, iconBytes);

        // restore initial cursor position
        cursor.moveToPosition(oldPosition);

        return v;
    }

    protected Context getContext() {
        return context;
    }
}
