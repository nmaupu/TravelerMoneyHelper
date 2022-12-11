package org.maupu.android.tmh.ui;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.FragmentActivity;

/**
 * Helper class to help with showing and hiding soft keyboard.
 */
public abstract class SoftKeyboardHelper {
    public static void forceShowUp(FragmentActivity activity) {
        if (activity == null)
            return;

        InputMethodManager imm = getInputMethodManager(activity);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            //activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    public static void hide(FragmentActivity activity) {
        if (activity == null)
            return;

        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }

        InputMethodManager imm = getInputMethodManager(activity);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hide(Context context, View view) {
        InputMethodManager imm = getInputMethodManager(context);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static InputMethodManager getInputMethodManager(Context context) {
        if (context != null)
            return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        return null;
    }
}
