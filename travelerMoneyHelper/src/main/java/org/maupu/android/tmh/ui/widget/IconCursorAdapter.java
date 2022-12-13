package org.maupu.android.tmh.ui.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.ui.ICallback;
import org.maupu.android.tmh.util.ImageUtil;

/**
 * A cursor adapter with an ImageView named 'icon'
 *
 * @author nmaupu
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

        ImageView imageViewIcon = v.findViewById(R.id.icon);

        int oldPosition = cursor.getPosition();
        cursor.moveToPosition(position);
        int idx = cursor.getColumnIndex(AccountData.KEY_ICON_BYTES);
        byte[] bytes = cursor.getBlob(idx);

        ImageUtil.setIcon(imageViewIcon, bytes);

        // restore initial cursor position
        cursor.moveToPosition(oldPosition);

        return v;
    }

    @Override
    public void onClick(View v) {
        if (listener != null)
            listener.callback(v);
    }
}
