package org.maupu.android.tmh;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.maupu.android.tmh.dialog.DriveRestoreDialogPreference;
import org.maupu.android.tmh.dialog.DriveRestorePreferenceDialogFragmentCompat;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference instanceof DriveRestoreDialogPreference) {
            DialogFragment dialogFragment = DriveRestorePreferenceDialogFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), DriveRestorePreferenceDialogFragmentCompat.TAG.getName());
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
