package org.maupu.android.tmh.ui.async;

import android.os.AsyncTask;

import org.maupu.android.tmh.util.drive.DriveBackupNotifier;

import java.util.Map;

public abstract class AbstractAsyncTask extends AsyncTask<Void, Integer, Map<Integer, Object>> implements DriveBackupNotifier {
    public void publishProgress(int p) {
        super.publishProgress(p);
    }

    public void publishProgress(int nb, int total) {
        this.publishProgress(nb * 100 / total);
    }
}
