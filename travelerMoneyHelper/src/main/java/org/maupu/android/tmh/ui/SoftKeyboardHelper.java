package org.maupu.android.tmh.ui;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * Helper class to help with showing and hiding soft keyboard.
 */
public abstract class SoftKeyboardHelper {
    public static void forceShowUp(FragmentActivity activity) {
        if (activity != null && activity.getWindow() != null)
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void forceShowUp(Fragment fragment) {
        if (fragment != null && fragment.getActivity() != null)
            fragment.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void hide(FragmentActivity activity) {
        // Other method to hide keyboard
        if (activity != null) {
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
