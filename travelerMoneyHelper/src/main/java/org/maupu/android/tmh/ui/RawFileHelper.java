package org.maupu.android.tmh.ui;

import android.content.Context;

import org.maupu.android.tmh.util.TmhLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class RawFileHelper<T extends Object> {
    private static final Class TAG = RawFileHelper.class;
    public static final String FIELD_SEPARATOR = "|";
    private final Context ctx;
    private final int rawFileId;
    private ICallback<T> listener;

    public RawFileHelper(Context ctx, int rawFileId) {
        this.ctx = ctx;
        this.rawFileId = rawFileId;
    }

    public void setListener(ICallback<T> listener) {
        this.listener = listener;
    }

    @SuppressWarnings("unchecked")
    public List<T> getRawFile() {
        List<T> ret = new ArrayList<T>();

        // Loading list
        InputStream inputStream = ctx.getResources().openRawResource(rawFileId);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line;
            while ((line = br.readLine()) != null) {
                TmhLogger.d(TAG, "Current line = " + line);
                if (listener != null)
                    ret.add(listener.callback(line));
                else
                    ret.add((T) line);
            }
            br.close();
        } catch (IOException ioe) {
            TmhLogger.e(TAG, "RawFileHelper" + ioe.getMessage());
        }

        return ret;
    }
}
