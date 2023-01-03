package org.maupu.android.tmh.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.maupu.android.tmh.R;

public class DriveRestoreListViewDateCustomAdaptor extends ArrayAdapter<File> implements View.OnClickListener {
    private int lastPosition = -1;

    private static class ViewHolder {
        TextView textView;
    }

    public DriveRestoreListViewDateCustomAdaptor(@NonNull Context context, @NonNull FileList fileList) {
        super(context, R.layout.dialog_prefs_drive_restore_dates_list_item, fileList.getFiles());
    }

    @Override
    public void onClick(View v) {
        File data = (File) v.getTag();
        Snackbar
                .make(getContext(), v, data.getName(), Snackbar.LENGTH_LONG)
                .setAction("no action", null)
                .show();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        File data = getItem(position);
        ViewHolder viewHolder;

        final View resultView;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.dialog_prefs_drive_restore_dates_list_item, parent, false);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.text_view_date);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        resultView = convertView;

        Animation animation = AnimationUtils.loadAnimation(getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        resultView.startAnimation(animation);
        lastPosition = position;

        viewHolder.textView.setText(data.getName());
        viewHolder.textView.setTag(data);
        viewHolder.textView.setOnClickListener(this);

        return convertView;
    }
}
