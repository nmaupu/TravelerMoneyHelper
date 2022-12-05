package org.maupu.android.tmh.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceDialogFragmentCompat;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.util.ImportExportUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.io.IOException;

public class ImportDBPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    public static final Class<ImportDBPreferenceDialogFragmentCompat> TAG = ImportDBPreferenceDialogFragmentCompat.class;

    private EditText dbFilename;
    private EditText dbName;
    private Button buttonBrowse;
    private Uri databaseUri;

    private ActivityResultLauncher<Intent> startForResult;

    public static ImportDBPreferenceDialogFragmentCompat newInstance(String key) {
        final ImportDBPreferenceDialogFragmentCompat fragment = new ImportDBPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // startForResult HAS to be declared before view is created
        startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            databaseUri = intent.getData();
                            final ImportDBDialogPreference preference = (ImportDBDialogPreference) getPreference();
                            preference.setDatabaseUri(databaseUri);
                            dbFilename.setText(databaseUri.toString());
                        }
                    }
                });
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        dbFilename = (EditText) view.findViewById(R.id.db_filename);
        dbFilename.setEnabled(false);

        dbName = (EditText) view.findViewById(R.id.db_name);

        FragmentActivity activity = getActivity();
        buttonBrowse = (Button) view.findViewById(R.id.pref_dialog_button_browse);
        buttonBrowse.setOnClickListener(v -> {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startForResult.launch(intent);
                }
            });
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            TmhLogger.d(TAG, "Importing DB");
            try {
                ImportExportUtil.importDatabase(
                        getActivity(),
                        databaseUri,
                        dbName.getText().toString()
                );

                // Notify listeners for change
                getPreference().callChangeListener(dbName.getText().toString());
            } catch (IOException ioe) {
                TmhLogger.d(TAG, "Impossible to import database");
                ioe.printStackTrace();
            }
        } else {
            TmhLogger.d(TAG, "Import canceled, doing nothing");
        }
    }

    public Uri getDatabaseUri() {
        return databaseUri;
    }

    public EditText getDbFilename() {
        return dbFilename;
    }

    public Button getButtonBrowse() {
        return buttonBrowse;
    }
}
