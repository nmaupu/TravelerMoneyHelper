package org.maupu.android.tmh.ui.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

public class OperationCheckableCursorAdapter extends CheckableCursorAdapter {
    private OperationCursorAdapter adapter;

    public OperationCheckableCursorAdapter(Context context, int layout,
                                           Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        adapter = new OperationCursorAdapter(context, layout, c, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        adapter.bindView(view, context, cursor);
    }

}
