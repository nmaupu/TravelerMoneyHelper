package org.maupu.android.tmh.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.util.ImportExportUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.io.IOException;

public class ImportPreference extends DialogPreference {
    private static final Class TAG = ImportPreference.class;

    private EditText dbFilename;
    private EditText dbName;
    private Button buttonBrowse;
    private Activity parentActivity;
    private Uri databaseUri;

    public ImportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_dialog_import);
    }

    public void setParentActivity(Activity activity) {
        this.parentActivity = activity;
    }

    public void setDatabaseUri(Uri uri) {
        databaseUri = uri;
    }

    public EditText getDbFilename() {
        return dbFilename;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        dbFilename = (EditText)view.findViewById(R.id.db_filename);
        dbFilename.setEnabled(false);
        dbName = (EditText)view.findViewById(R.id.db_name);

        buttonBrowse = (Button)view.findViewById(R.id.pref_dialog_button_browse);
        buttonBrowse.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(parentActivity != null) {
                    parentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            parentActivity.startActivityForResult(intent, 0);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            // Importing DB
            TmhLogger.d(TAG, "Importing DB");
            try {
                ImportExportUtil.importDatabase(
                        parentActivity,
                        databaseUri,
                        dbName.getText().toString()
                );

                // Notify listeners for change
                callChangeListener(dbName.getText().toString());
            } catch(IOException ioe) {
                TmhLogger.d(TAG, "Impossible to import database");
                ioe.printStackTrace();
            }
        } else {
            TmhLogger.d(TAG, "Import canceled, doing nothing");
        }
    }
}
