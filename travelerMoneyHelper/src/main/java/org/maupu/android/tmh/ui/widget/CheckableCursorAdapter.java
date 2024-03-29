package org.maupu.android.tmh.ui.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;

import org.maupu.android.tmh.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Class providing an easy way to have a item with a checkbox
 * Must provide a view with a checkbox with id <em>checkbox</em>
 *
 * @author nmaupu
 */
public class CheckableCursorAdapter extends SimpleCursorAdapter implements OnClickListener {
    private NumberCheckedListener listener = null;
    private final Map<Integer, Boolean> positionsChecked = new HashMap<>();
    private int numberChecked = 0;
    private Integer[] toCheck;
    private final Map<Integer, Boolean> inits = new HashMap<>();

    public CheckableCursorAdapter(Context context, int layout, Cursor c,
                                  String[] from, int[] to) {
        super(context, layout, c, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    public CheckableCursorAdapter(Context context, int layout, Cursor c,
                                  String[] from, int[] to, Integer[] toCheck) {
        super(context, layout, c, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setToCheck(toCheck);
    }

    public void setToCheck(Integer[] toCheck) {
        this.toCheck = toCheck;
        clearCheckedItems();
        for (int i = 0; i < toCheck.length; i++)
            addToCheckedItems(toCheck[i]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        row.setOnClickListener(this);
        CheckBox cb = row.findViewById(R.id.checkbox);

        if (cb != null) {
            cb.setTag(position);
            cb.setOnClickListener(this);

            boolean status = positionsChecked.get(position) != null ? positionsChecked.get(position) : false;
            cb.setChecked(status);

            /**
             * Check if checkbox is first init and check if asked
             * but only the first time it is displayed to avoid having a checkbox impossible to
             * check
             */
            if (inits.get(position) == null) {
                // This cb has been initialized
                inits.put(position, Boolean.valueOf(true));

                if (toCheck != null && toCheck.length > 0) {
                    for (int pos : toCheck) {
                        if (position == pos)
                            cb.setChecked(true);
                    } //for
                } //if
            } //if cb not initialized
        }

        return row;
    }

    public void setOnNumberCheckedListener(NumberCheckedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
        if (cb == null)
            return;

        if (v.getId() != R.id.checkbox) {
            // When click on checkbox, do nothing because it is toggled automatically
            cb.toggle();
        }

        Integer position = (Integer) cb.getTag();

        // Store only if checked (memory consumption issue)
        if (cb.isChecked()) {
            addToCheckedItems(position);
        } else {
            removeFromCheckedItems(position);
        }

        if (listener != null)
            listener.onCheckedItem(numberChecked);
    }

    private void addToCheckedItems(int position) {
        positionsChecked.put(position, true);
        numberChecked++;
    }

    private void removeFromCheckedItems(int position) {
        positionsChecked.remove(position);
        numberChecked--;
    }

    public void clearCheckedItems() {
        positionsChecked.clear();
        numberChecked = 0;
    }

    @Override
    public void changeCursor(Cursor c) {
        Cursor oldCursor = super.getCursor();

        super.changeCursor(c);

        if (oldCursor != null)
            oldCursor.close();
    }

    public Integer[] getCheckedPositions() {
        return positionsChecked.keySet().toArray(new Integer[0]);
    }
}
