package org.maupu.android.tmh.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.maupu.android.tmh.BuildConfig;
import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ui.StaticData;

public class DriveAutomaticBackupBottomSheetDialog extends BottomSheetDialogFragment {
    private BottomSheetDialogListener<Integer> listener;

    public DriveAutomaticBackupBottomSheetDialog(BottomSheetDialogListener<Integer> listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_prefs_drive_automatic_backup, container, false);

        // Loading existing prefs
        int currentPref = StaticData.getPrefs().getInt(StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY, StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_NEVER);

        RadioButton rbNever = v.findViewById(R.id.never);
        RadioButton rbDaily = v.findViewById(R.id.daily);
        RadioButton rbWeekly = v.findViewById(R.id.weekly);
        RadioButton rbMonthly = v.findViewById(R.id.monthly);
        RadioButton rbTest = v.findViewById(R.id.test);
        if (BuildConfig.DEBUG)
            rbTest.setVisibility(View.VISIBLE);
        else
            rbTest.setVisibility(View.GONE);

        if (BuildConfig.DEBUG && currentPref == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_TEST) {
            rbTest.setChecked(true);
        } else if (currentPref == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_NEVER) {
            rbNever.setChecked(true);
        } else if (currentPref == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_DAILY) {
            rbDaily.setChecked(true);
        } else if (currentPref == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_WEEKLY) {
            rbWeekly.setChecked(true);
        } else if (currentPref == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_MONTHLY) {
            rbMonthly.setChecked(true);
        } else {
            rbNever.setChecked(true);
        }

        if (BuildConfig.DEBUG)
            rbTest.setOnClickListener(v1 -> savePref(requireView(), StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_TEST));
        rbNever.setOnClickListener(v1 -> savePref(requireView(), StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_NEVER));
        rbDaily.setOnClickListener(v1 -> savePref(requireView(), StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_DAILY));
        rbWeekly.setOnClickListener(v1 -> savePref(requireView(), StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_WEEKLY));
        rbMonthly.setOnClickListener(v1 -> savePref(requireView(), StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_MONTHLY));

        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
        return v;
    }

    private void savePref(View v, int val) {
        StaticData.setPreferenceValueInt(StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY, val);
        if (listener != null)
            listener.onValidateEvent(v, this, val);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }
}
