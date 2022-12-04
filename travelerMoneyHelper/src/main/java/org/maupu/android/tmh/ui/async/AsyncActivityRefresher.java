package org.maupu.android.tmh.ui.async;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import androidx.fragment.app.FragmentActivity;

import org.maupu.android.tmh.R;

import java.util.Map;

public class AsyncActivityRefresher extends AsyncTask<Void, Integer, Map<Integer, Object>> {
    private FragmentActivity context;
    private ProgressDialog waitSpinner;
    private IAsyncActivityRefresher listener;
    private boolean displayPopup;

    public AsyncActivityRefresher(FragmentActivity context, IAsyncActivityRefresher listener, boolean displayPopup) {
        this.context = context;
        this.listener = listener;
        this.displayPopup = displayPopup;

        if (displayPopup) {
            waitSpinner = new ProgressDialog(this.context);
            waitSpinner.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            waitSpinner.setIndeterminate(true);
            waitSpinner.setTitle(R.string.refreshing);
            waitSpinner.setCancelable(true);
            waitSpinner.setCanceledOnTouchOutside(true);
        }
    }

    public boolean isDisplayPopup() {
        return displayPopup;
    }

    @Override
    protected Map<Integer, Object> doInBackground(Void... activities) {
        publishProgress(0);
        Map<Integer, Object> results = listener.handleRefreshBackground();
        publishProgress(100);
        return results;
    }

    protected void onProgressUpdate(Integer... newProgresses) {
        if (!context.isFinishing() && displayPopup && !waitSpinner.isShowing())
            waitSpinner.show();

        if (waitSpinner != null && !context.isFinishing()) {
            waitSpinner.incrementProgressBy(newProgresses[0]);

            if (newProgresses[0] == 100 && !context.isFinishing()) {
                waitSpinner.dismiss();
            }
        }
    }

    @Override
    protected void onPreExecute() {
        //blockScreenRotation();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Map<Integer, Object> results) {
        //resetScreenOrientation();
        listener.handleRefreshEnding(results);
        super.onPostExecute(results);
    }
	
	/*
	private void blockScreenRotation() {
		if(context.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_NOSENSOR) {
			contextPreviousOrientation = context.getRequestedOrientation();
			context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		}
	}
	
	private void resetScreenOrientation() {
		if(contextPreviousOrientation != null && context.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_NOSENSOR)
			context.setRequestedOrientation(contextPreviousOrientation);
	}*/
}
