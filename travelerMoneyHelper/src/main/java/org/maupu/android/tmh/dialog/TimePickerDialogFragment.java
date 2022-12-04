package org.maupu.android.tmh.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TimePickerDialogFragment extends DialogFragment {
    private int hour;
    private int minute;
    private boolean is24HourView;
    private TimePickerDialog.OnTimeSetListener listener;

    public static String TAG = "TimePickerDialogFragment";

    public TimePickerDialogFragment(int hourOfDay, int minute, boolean is24HourView, TimePickerDialog.OnTimeSetListener listener) {
        this.hour = hourOfDay;
        this.minute = minute;
        this.is24HourView = is24HourView;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new TimePickerDialog(requireContext(), listener, hour, minute, is24HourView);
    }
}
