package org.maupu.android.tmh.dialog;

import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public interface BottomSheetDialogListener<T> {
    void onValidateEvent(View v, BottomSheetDialogFragment dialog, T obj);
}
