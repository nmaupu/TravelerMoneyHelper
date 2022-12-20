package org.maupu.android.tmh.dialog;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import org.maupu.android.tmh.R;

public class GDriveBackupDialogPreference extends DialogPreference {
    public GDriveBackupDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_prefs_gdrive_configure);
    }

    @Override
    public boolean callChangeListener(Object newValue) {
        return super.callChangeListener(newValue);
    }
}
