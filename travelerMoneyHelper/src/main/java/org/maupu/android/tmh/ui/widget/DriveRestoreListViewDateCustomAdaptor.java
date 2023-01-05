package org.maupu.android.tmh.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.maupu.android.tmh.R;

public class DriveRestoreListViewDateCustomAdaptor extends ArrayAdapter<File> {
    private int mLastPosition = -1;
    private View.OnClickListener mClickListener;

    private static class ViewHolder {
        TextView textView;
    }

    public DriveRestoreListViewDateCustomAdaptor(@NonNull Context context, @NonNull FileList fileList, View.OnClickListener clickListener) {
        super(context, R.layout.dialog_prefs_drive_restore_dates_list_item, fileList.getFiles());
        this.mClickListener = clickListener;
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

        //Animation animation = AnimationUtils.loadAnimation(getContext(), (position > mLastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        //resultView.startAnimation(animation);
        mLastPosition = position;

        viewHolder.textView.setText(data.getName());
        viewHolder.textView.setTag(data);
        viewHolder.textView.setOnClickListener(this.mClickListener);

        return convertView;
    }
}
