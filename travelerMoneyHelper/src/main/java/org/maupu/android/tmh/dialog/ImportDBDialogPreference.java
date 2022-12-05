package org.maupu.android.tmh.dialog;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import org.maupu.android.tmh.R;

public class ImportDBDialogPreference extends DialogPreference {
    public static final Class<ImportDBDialogPreference> TAG = ImportDBDialogPreference.class;

    private Uri databaseUri;

    public ImportDBDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_prefs_import_db);
    }

    @Override
    public boolean callChangeListener(Object newValue) {
        return super.callChangeListener(newValue);
    }

    public void setDatabaseUri(Uri databaseUri) {
        this.databaseUri = databaseUri;
    }

    public Uri getDatabaseUri() {
        return databaseUri;
    }
}
