package org.maupu.android.tmh.util.drive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.ui.StaticData;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriveBackupBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> processBackup(context));
    }

    private void processBackup(Context context) {
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context.getApplicationContext());
        if (googleSignInAccount != null) {
            GoogleAccountCredential googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                    context.getApplicationContext(),
                    Collections.singleton(DriveScopes.DRIVE_FILE)
            );
            googleAccountCredential.setSelectedAccount(googleSignInAccount.getAccount());

            NetHttpTransport netHttpTransport = null;
            try {
                netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
                // TODO notification to alert there is problem during backup
                return;
            }

            Drive googleDriveService = new Drive.Builder(
                    netHttpTransport,
                    GsonFactory.getDefaultInstance(),
                    googleAccountCredential)
                    .setApplicationName(TmhApplication.APP_NAME)
                    .build();

            CharSequence[] listDbNames = DatabaseHelper.getAllDatabases(false);
            BackupDbFileHelper[] listDbPath = new BackupDbFileHelper[listDbNames.length];
            for (int i = 0; i < listDbNames.length; i++) {
                listDbPath[i] = new BackupDbFileHelper(
                        DatabaseHelper.getDbAbsolutePath(context, listDbNames[i].toString()));
            }

            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
            String folderNamePref = prefManager.getString(StaticData.PREF_KEY_DRIVE_BACKUP_FOLDER, context.getString(R.string.pref_drive_default_folder));
            String retentionPref = prefManager.getString(StaticData.PREF_KEY_DRIVE_RETENTION, context.getString(R.string.pref_drive_retention_default));
            boolean deleteOldPref = prefManager.getBoolean(StaticData.PREF_KEY_DRIVE_DELETE_OLD, false);
            int retentionDurationDays = 0;
            try {
                retentionDurationDays = Integer.parseInt(retentionPref);
            } catch (NumberFormatException nfe) {
                //
            } finally {
                if (retentionDurationDays == 0)
                    retentionDurationDays = Integer.parseInt(context.getString(R.string.pref_drive_retention_default));
            }
            try {
                DriveServiceHelper.upload(
                                googleDriveService,
                                null,
                                folderNamePref == null || "".equals(folderNamePref) ? context.getString(R.string.pref_drive_default_folder) : folderNamePref,
                                listDbPath,
                                deleteOldPref ? retentionDurationDays : 0)
                        .addOnSuccessListener(file -> {
                            // TODO remove notification
                        })
                        .addOnFailureListener(e -> {
                            // TODO add notification ?
                        });
            } catch (IOException e) {
                e.printStackTrace();
                String err = context.getString(R.string.pref_upload_file_to_drive_error) + " (" + e.getMessage() + ")";
                // TODO notification
            }
        }
    }
}
