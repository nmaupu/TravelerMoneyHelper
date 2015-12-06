package org.maupu.android.tmh.ui;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

import org.maupu.android.tmh.TmhActivity;

/**
 * Helper class to help with showing and hiding soft keyboard.
 */
public abstract class SoftKeyboardHelper {
    public static void forceShowUp(TmhActivity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void hide(TmhActivity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}
