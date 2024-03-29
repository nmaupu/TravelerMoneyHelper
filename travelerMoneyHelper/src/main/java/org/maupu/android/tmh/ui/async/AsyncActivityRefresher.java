package org.maupu.android.tmh.ui.async;

import android.app.ProgressDialog;

import androidx.fragment.app.FragmentActivity;

import org.maupu.android.tmh.R;

import java.util.Map;

public class AsyncActivityRefresher extends AbstractAsyncTask {
    private final FragmentActivity context;
    private ProgressDialog waitSpinner;
    private final IAsyncActivityRefresher listener;
    private final boolean displayPopup;

    public AsyncActivityRefresher(FragmentActivity context, IAsyncActivityRefresher listener, boolean displayPopup, boolean indeterminate, boolean cancelable) {
        this.context = context;
        this.listener = listener;
        this.displayPopup = displayPopup;

        if (displayPopup) {
            waitSpinner = new ProgressDialog(this.context);
            waitSpinner.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            waitSpinner.setIndeterminate(indeterminate);
            waitSpinner.setTitle(R.string.refreshing);
            waitSpinner.setCancelable(cancelable);
            if (cancelable)
                waitSpinner.setCanceledOnTouchOutside(true);
        }
    }

    public boolean isDisplayPopup() {
        return displayPopup;
    }

    @Override
    protected Map<Integer, Object> doInBackground(Void... activities) {
        publishProgress(0);
        Map<Integer, Object> results = listener.handleRefreshBackground(this);
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
