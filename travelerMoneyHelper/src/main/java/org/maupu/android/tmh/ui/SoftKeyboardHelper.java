package org.maupu.android.tmh.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import org.maupu.android.tmh.TmhActivity;

/**
 * Helper class to help with showing and hiding soft keyboard.
 */
public abstract class SoftKeyboardHelper {
    public static void forceShowUp(TmhActivity activity) {
        if(activity != null && activity.getWindow() != null)
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void hide(TmhActivity activity) {
        // Not working
        /*
        if(activity != null) {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        */

        // Other method to hide keyboard
        if(activity != null) {
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
