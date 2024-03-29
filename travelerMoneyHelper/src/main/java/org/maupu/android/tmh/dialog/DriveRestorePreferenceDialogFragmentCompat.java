package org.maupu.android.tmh.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.io.Files;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.DriveRestoreListViewDateCustomAdaptor;
import org.maupu.android.tmh.util.drive.DriveServiceHelper;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriveRestorePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    public static final Class<DriveRestorePreferenceDialogFragmentCompat> TAG = DriveRestorePreferenceDialogFragmentCompat.class;
    private ListView listView;
    private ProgressBar progressBar;
    private View dialogView;
    private TextView textViewTitle;

    private Drive googleDriveService;

    @NonNull
    public static DriveRestorePreferenceDialogFragmentCompat newInstance(String key) {
        final DriveRestorePreferenceDialogFragmentCompat fragment = new DriveRestorePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        dialogView = view;

        progressBar = view.findViewById(R.id.progress_circular);
        listView = view.findViewById(R.id.drive_restore_listview);
        textViewTitle = view.findViewById(R.id.drive_restore_title);
        listView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        textViewTitle.setVisibility(View.GONE);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (googleSignInAccount != null) {
            GoogleAccountCredential googleAccountCredential = GoogleAccountCredential.usingOAuth2(requireContext(), Collections.singleton(DriveScopes.DRIVE));
            googleAccountCredential.setSelectedAccount(googleSignInAccount.getAccount());

            NetHttpTransport netHttpTransport = null;
            try {
                netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
            googleDriveService = new Drive.Builder(
                    netHttpTransport,
                    GsonFactory.getDefaultInstance(),
                    googleAccountCredential)
                    .setApplicationName(TmhApplication.APP_NAME)
                    .build();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                String folderNamePref = StaticData.getPreferenceValueString(StaticData.PREF_KEY_DRIVE_BACKUP_FOLDER);
                FileList fileList = null;
                try {
                    fileList = DriveServiceHelper.getAllBackups(
                            googleDriveService,
                            folderNamePref == null || "".equals(folderNamePref) ? getString(R.string.pref_drive_default_folder) : folderNamePref);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                FileList finalFileList = fileList;
                handler.post(() -> {
                    // UI Thread
                    progressBar.setVisibility(View.GONE);
                    if (finalFileList != null && finalFileList.size() > 0) {
                        listView.setAdapter(new DriveRestoreListViewDateCustomAdaptor(
                                requireContext(),
                                finalFileList,
                                onClickListener));
                        textViewTitle.setText(getString(R.string.drive_restore_results_title) + " (" + folderNamePref + ")");
                        textViewTitle.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.VISIBLE);
                    } else {
                        textViewTitle.setText(getString(R.string.backup_restore_no_content_found) + " (" + folderNamePref + ")");
                        textViewTitle.setVisibility(View.VISIBLE);
                    }
                });
            });
        }
    }

    private final View.OnClickListener onClickListener = v -> {
        listView.setVisibility(View.GONE);
        textViewTitle.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        File backupFolder = (File) v.getTag();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String folderName = backupFolder.getName();
            FileList fileList = null;
            try {
                fileList = DriveServiceHelper.getAllBackupFiles(googleDriveService, folderName);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            final FileList finalFileList = fileList;
            handler.post(() -> {
                // UI Thread
                handleUIThread(v, finalFileList, folderName);
            });
        });

    };

    private void handleUIThread(@NonNull View v, @NonNull FileList fileList, @NonNull String folderName) {
        if (fileList.size() == 0) {
            listView.setVisibility(View.VISIBLE);
            textViewTitle.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            Snackbar.make(requireContext(), v, getString(R.string.backup_restore_no_content_found), Snackbar.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.GONE);
        listView.setAdapter(new DriveRestoreListViewDateCustomAdaptor(
                requireContext(),
                fileList,
                v2 -> {
                    File f = (File) v2.getTag();
                    SimpleDialog.confirmDialog(
                            requireContext(),
                            getString(R.string.backup_restore_confirmation_message) + " " + folderName + "/" + f.getName() + "?",
                            (dialog, which) -> {
                                String outputDbName = Files.getNameWithoutExtension(DatabaseHelper.DATABASE_PREFIX + f.getName());
                                if (DatabaseHelper.isDatabaseExists(f.getName())) {
                                    // Creating with a new name to avoid erasing an existing db
                                    String rawName = Files.getNameWithoutExtension(f.getName());
                                    outputDbName = DatabaseHelper.DATABASE_PREFIX + rawName + "_" + folderName;
                                }

                                ExecutorService executor = Executors.newSingleThreadExecutor();
                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(@NonNull Message msg) {
                                        dialog.dismiss();
                                        String errorMsg = msg.getData() != null ? msg.getData().getString("error") : null;
                                        if (errorMsg != null) {
                                            SimpleDialog.errorDialog(
                                                            getContext(),
                                                            getString(R.string.error), errorMsg)
                                                    .show();
                                        } else {
                                            String dbName = msg.getData().getString("database");
                                            Snackbar.make(
                                                            requireContext(),
                                                            dialogView,
                                                            getString(R.string.database_created_successfully) + " (" + DatabaseHelper.stripDatabaseFileName(dbName) + ")",
                                                            Snackbar.LENGTH_LONG)
                                                    .show();

                                            // Call onPreferenceChange listener as a new DB has been created
                                            DriveRestoreDialogPreference dialogPreference = (DriveRestoreDialogPreference) getPreference();
                                            if (dialogPreference.getOnPreferenceChangeListener() != null) {
                                                dialogPreference
                                                        .getOnPreferenceChangeListener()
                                                        .onPreferenceChange(dialogPreference, null);
                                            }
                                        }
                                    }
                                };

                                final String finalOutputDbName = outputDbName;
                                executor.execute(() -> {
                                    Message message = handler.obtainMessage(0);
                                    Bundle bundle = new Bundle();

                                    try {
                                        ByteArrayOutputStream outputStream = DriveServiceHelper.downloadFile(googleDriveService, f);
                                        OutputStream outputFile = new FileOutputStream(DatabaseHelper.getAppDatabasesDirectory() + "/" + finalOutputDbName);
                                        outputStream.writeTo(outputFile);
                                        outputFile.close();
                                        outputStream.close();
                                        bundle.putString("database", finalOutputDbName);
                                    } catch (IOException ioe) {
                                        ioe.printStackTrace();
                                        bundle.putString("error", ioe.getMessage());
                                    } finally {
                                        message.setData(bundle);
                                        message.sendToTarget();
                                    }
                                });
                            }).show();
                }));
        listView.setVisibility(View.VISIBLE);
        textViewTitle.setText(getString(R.string.drive_restore_results_title) + " (" + folderName + ")");
        textViewTitle.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
