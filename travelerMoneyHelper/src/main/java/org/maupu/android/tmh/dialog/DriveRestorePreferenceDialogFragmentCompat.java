package org.maupu.android.tmh.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.DriveRestoreListViewDateCustomAdaptor;
import org.maupu.android.tmh.util.drive.DriveServiceHelper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriveRestorePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    public static final Class<DriveRestorePreferenceDialogFragmentCompat> TAG = DriveRestorePreferenceDialogFragmentCompat.class;
    private ListView listView;
    private ProgressBar progressBar;

    private ActivityResultLauncher<Intent> startForResult;

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

        startForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                    }
                });
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        progressBar = (ProgressBar) view.findViewById(R.id.progress_circular);
        listView = (ListView) view.findViewById(R.id.drive_restore_listview);
        listView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (googleSignInAccount != null) {
            GoogleAccountCredential googleAccountCredential = GoogleAccountCredential.usingOAuth2(requireContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
            googleAccountCredential.setSelectedAccount(googleSignInAccount.getAccount());

            NetHttpTransport netHttpTransport = null;
            try {
                netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
            Drive googleDriveService = new Drive.Builder(
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
                    listView.setAdapter(new DriveRestoreListViewDateCustomAdaptor(requireContext(), finalFileList));
                    listView.setVisibility(View.VISIBLE);
                });
            });
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
