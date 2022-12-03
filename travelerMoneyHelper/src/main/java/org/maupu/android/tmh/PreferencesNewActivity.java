package org.maupu.android.tmh;

import android.os.Bundle;

import androidx.annotation.Nullable;

public class PreferencesNewActivity extends TmhActivity {
    public static final Class TAG = PreferencesNewActivity.class;

    public PreferencesNewActivity() {
        super(R.layout.preferences_new, R.string.activity_title_preferences);
    }

    @Override
    public int whatIsMyDrawerIdentifier() {
        return super.DRAWER_ITEM_PARAMETERS;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (findViewById(R.id.idFrameLayout) != null) {
            if (savedInstanceState != null) {
                return;
            }
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.idFrameLayout, new PreferencesNewFragment())
                    .commit();
        }
    }
}
