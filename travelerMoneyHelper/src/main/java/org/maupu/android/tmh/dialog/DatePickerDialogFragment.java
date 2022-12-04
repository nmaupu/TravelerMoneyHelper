package org.maupu.android.tmh.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.maupu.android.tmh.ui.widget.CustomDatePickerDialog;

public class DatePickerDialogFragment extends DialogFragment {
    private int year;
    private int month;
    private int day;
    private DatePickerDialog.OnDateSetListener listener;

    public static String TAG = "DatePickerDialogFragment";

    public DatePickerDialogFragment(int year, int month, int day, DatePickerDialog.OnDateSetListener listener) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new CustomDatePickerDialog(requireContext(), listener, year, month, day);
    }

}
