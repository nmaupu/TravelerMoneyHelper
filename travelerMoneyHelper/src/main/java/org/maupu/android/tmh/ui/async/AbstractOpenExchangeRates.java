package org.maupu.android.tmh.ui.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import androidx.fragment.app.FragmentActivity;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.StaticData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class AbstractOpenExchangeRates extends AsyncTask<Currency, Integer, Exception> {
    public static final long DEFAULT_CACHE_LIMIT_TIME = 3600; // 1h
    protected FragmentActivity context;
    protected ProgressDialog waitSpinner = null;
    protected final static String URL = "https://openexchangerates.org/api/";
    protected final static int ACTION_CURRENCY_LIST = 0;
    protected final static int ACTION_CURRENCY_LATEST = 1;

    private IAsync asyncListener = null;

    public AbstractOpenExchangeRates(FragmentActivity context, String popupTitle) {
        this.context = context;

        // Constructor should be called when onCreate is executed
        waitSpinner = new ProgressDialog(this.context);
        waitSpinner.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        waitSpinner.setTitle(popupTitle);
        waitSpinner.setMax(100);
    }

    public void setAsyncListener(IAsync listener) {
        this.asyncListener = listener;
    }

    protected void onProgressUpdate(Integer... newProgresses) {
        if (!waitSpinner.isShowing())
            waitSpinner.show();

        waitSpinner.incrementProgressBy(newProgresses[0]);

        if (newProgresses[0] == 100) {
            waitSpinner.dismiss();
            //context.refreshDisplay();
            if (asyncListener != null)
                asyncListener.onFinishAsync();
        }
    }

    public static StringBuilder getUrl(int action, String apiKey) {
        StringBuilder sbRet = new StringBuilder(URL);

        switch (action) {
            case ACTION_CURRENCY_LIST:
                sbRet.append("currencies.json"); // no api key needed
                break;
            case ACTION_CURRENCY_LATEST:
                sbRet.append("latest.json?app_id=")
                        .append(apiKey);
                break;
        }

        return sbRet;
    }

    public void saveToCache(String json, String filename) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();

            // Storing date to invalidate cache
            StaticData.setDateField(filename, new Date());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StringBuilder loadFromCache(String filename, long cacheLimit) {
        StringBuilder sbRet = new StringBuilder();

        Date now = new Date();
        long tnow = now.getTime();
        Date cacheDate = StaticData.getDateField(filename);

        if (cacheLimit > 0 && (cacheDate == null || tnow - cacheDate.getTime() >= cacheLimit * 1000)) {
            // no cache available or cache not valid anymore
            return null;
        }

        // Load from cache
        byte[] buf = new byte[32];

        try {
            FileInputStream fis = context.openFileInput(filename);
            int c = 0;
            while ((c = fis.read(buf)) > 0) {
                sbRet.append(new String(buf, 0, c));
            }

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sbRet;
    }

    /**
     * Verify api key against server - needs network to function
     *
     * @param apiKey
     */
    public static Boolean isValidApiKey(Context ctx, String apiKey) throws Exception {
        VerifApiKey verif = new VerifApiKey(ctx);

        verif.execute(apiKey);
        try {
            Exception e = verif.get();
            if (e == null)
                return true;
            else
                throw e;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
    }
}

class VerifApiKey extends AsyncTask<String, Integer, Exception> {
    private Context ctx;
    protected ProgressDialog waitSpinner;

    public VerifApiKey(Context ctx) {
        this.ctx = ctx;

        waitSpinner = new ProgressDialog(this.ctx);
        waitSpinner.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        waitSpinner.setTitle(R.string.refreshing);
        waitSpinner.setMax(100);
    }

    protected void onProgressUpdate(Integer... newProgresses) {
        if (!waitSpinner.isShowing())
            waitSpinner.show();

        waitSpinner.incrementProgressBy(newProgresses[0]);

        if (newProgresses[0] == 100) {
            waitSpinner.dismiss();
        }
    }

    @Override
    protected Exception doInBackground(String... keys) {
        publishProgress(0);

        if (keys == null || keys.length == 0) {
            return new Exception("no key provided");
        }

        String key = keys[0];

        StringBuilder sbUrl = AbstractOpenExchangeRates.getUrl(AbstractOpenExchangeRates.ACTION_CURRENCY_LATEST, key);

        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .build();
        Request req = new Request.Builder()
                .url(sbUrl.toString())
                .build();

        int statusCode = -1;
        try (Response response = client.newCall(req).execute()) {
            statusCode = response.code();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            publishProgress(100);
            return ioe;
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }

        Boolean ret = statusCode == 200;
        publishProgress(100);
        if (ret)
            return null;
        else
            return new Exception("error querying Open Exchange Rate API, status code=" + statusCode);
    }
}
