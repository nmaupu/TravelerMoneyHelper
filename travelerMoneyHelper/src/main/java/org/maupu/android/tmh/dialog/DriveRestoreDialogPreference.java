package org.maupu.android.tmh.dialog;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import org.maupu.android.tmh.R;

public class DriveRestoreDialogPreference extends DialogPreference {
    public DriveRestoreDialogPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_prefs_drive_restore_listview);
    }
}
