package org.maupu.android.tmh;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;

public class PreferencesNewFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_new);
    }
}
