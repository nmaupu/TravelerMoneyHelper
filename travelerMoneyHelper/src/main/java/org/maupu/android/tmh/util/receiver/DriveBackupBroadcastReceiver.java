package org.maupu.android.tmh.util.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import org.maupu.android.tmh.util.NetworkUtil;
import org.maupu.android.tmh.util.NotificationHelper;
import org.maupu.android.tmh.util.drive.BackupDbFileHelper;
import org.maupu.android.tmh.util.drive.DriveBackupNotifier;
import org.maupu.android.tmh.util.drive.DriveServiceHelper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriveBackupBroadcastReceiver extends BroadcastReceiver {
    // private requestCode to use for the broadcast
    public static final int requestCode = 6453;
    public static final String notificationChannelId = "tmh_drive_backup";
    public static final int WAIT_FOR_NETWORK_TIMEOUT = 30 * 60;

    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Set date for the next repeated alarm so that it can be displayed in UI
            TmhApplication.alarmManagerHelper.calculateNextAlarmDateTime();
            processBackup(context);
        });
    }

    private void processBackup(Context context) {
        // Run a notification to the user
        int notificationId = NotificationHelper.getRandomNotificationId();

        NotificationCompat.Builder notifBuilder = NotificationHelper.getNewNotificationBuilder(context, notificationChannelId, R.drawable.ic_baseline_backup_black_24);
        NotificationManagerCompat notifManager = NotificationHelper.getNotificationManager(context);

        notifBuilder
                .setContentTitle(context.getString(R.string.backup_notification_waiting_for_network))
                .setContentText("")
                .setProgress(0, 0, true);
        notifManager.notify(notificationId, notifBuilder.build());

        int nbSecs = 0;
        while (!NetworkUtil.isNetworkAvailable(context) && nbSecs < WAIT_FOR_NETWORK_TIMEOUT) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                NotificationHelper.notifyError(context, notificationId, notifBuilder, notifManager, R.string.pref_upload_file_to_drive_error, ie);
            } finally {
                nbSecs++;
            }
        }
        if (nbSecs >= WAIT_FOR_NETWORK_TIMEOUT) {
            NotificationHelper.notifyError(context, notificationId, notifBuilder, notifManager,
                    R.string.pref_upload_file_to_drive_error,
                    new Exception(context.getString(R.string.backup_notification_timeout_waiting_for_network)));
            return;
        }

        // Handle notification
        notifBuilder
                .setProgress(100, 0, false)
                .setContentTitle(context.getString(R.string.backup_notification_backup_in_progress));
        notifManager.notify(notificationId, notifBuilder.build());

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context.getApplicationContext());
        if (googleSignInAccount == null) {
            NotificationHelper.notifyError(context, notificationId, notifBuilder, notifManager,
                    R.string.pref_upload_file_to_drive_error,
                    new Exception(context.getString(R.string.backup_notification_google_not_signed_in)));
            return;
        }

        // We have a connection, upload can begin
        GoogleAccountCredential googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                context.getApplicationContext(),
                Collections.singleton(DriveScopes.DRIVE_FILE)
        );
        googleAccountCredential.setSelectedAccount(googleSignInAccount.getAccount());

        NetHttpTransport netHttpTransport;
        try {
            netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            // Notify error
            NotificationHelper.notifyError(context, notificationId, notifBuilder, notifManager, R.string.pref_upload_file_to_drive_error, e);
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
            listDbPath[i] = new BackupDbFileHelper(DatabaseHelper.getDbAbsolutePath(context, listDbNames[i].toString()));
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

        // notifier to publish progress to the notification
        DriveBackupNotifier notifier = new DriveBackupNotifier() {
            @Override
            public void publishProgress(int progress) {
                notifBuilder
                        .setProgress(100, progress, false)
                        .setContentTitle(context.getString(R.string.backup_notification_backup_in_progress));
                notifManager.notify(notificationId, notifBuilder.build());
            }

            @Override
            public void publishProgress(int progress, int total) {
                notifBuilder
                        .setProgress(100, progress * 100 / total, false)
                        .setContentTitle(context.getString(R.string.backup_notification_backup_in_progress));
                notifManager.notify(notificationId, notifBuilder.build());
            }
        };

        try {
            DriveServiceHelper.upload(
                            googleDriveService,
                            notifier,
                            folderNamePref == null || "".equals(folderNamePref) ? context.getString(R.string.pref_drive_default_folder) : folderNamePref,
                            listDbPath,
                            deleteOldPref ? retentionDurationDays : 0)
                    .addOnSuccessListener(file -> notifManager.cancel(notificationId))
                    .addOnFailureListener(e -> NotificationHelper.notifyError(context, notificationId, notifBuilder, notifManager, R.string.pref_upload_file_to_drive_error, e));
        } catch (IOException e) {
            e.printStackTrace();
            NotificationHelper.notifyError(context, notificationId, notifBuilder, notifManager, R.string.pref_upload_file_to_drive_error, e);
        }
    }
}
