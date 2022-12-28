package org.maupu.android.tmh.util.drive;

public interface DriveBackupNotifier {
    void publishProgress(int progress);

    void publishProgress(int progress, int total);
}
