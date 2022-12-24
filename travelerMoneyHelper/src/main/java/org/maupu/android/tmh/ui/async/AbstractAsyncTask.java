package org.maupu.android.tmh.ui.async;

import android.os.AsyncTask;

import java.util.Map;

public abstract class AbstractAsyncTask extends AsyncTask<Void, Integer, Map<Integer, Object>> {
    public void publishProgress(int nb, int total) {
        this.publishProgress(nb * 100 / total);
    }

    public void publishProgress(int p) {
        super.publishProgress(p);
    }
}
