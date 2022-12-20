package org.maupu.android.tmh;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.maupu.android.tmh.dialog.GDriveBackupDialogPreference;
import org.maupu.android.tmh.dialog.GDriveBackupPreferenceDialogFragmentCompat;
import org.maupu.android.tmh.dialog.ImportDBDialogPreference;
import org.maupu.android.tmh.dialog.ImportDBPreferenceDialogFragmentCompat;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference instanceof ImportDBDialogPreference) {
            DialogFragment dialogFragment = ImportDBPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), ImportDBPreferenceDialogFragmentCompat.TAG.getName());
        } else if (preference instanceof GDriveBackupDialogPreference) {
            DialogFragment dialogFragment = GDriveBackupPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), GDriveBackupPreferenceDialogFragmentCompat.TAG.getName());
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
