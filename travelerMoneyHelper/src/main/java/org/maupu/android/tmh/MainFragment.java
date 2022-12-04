package org.maupu.android.tmh;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mikepenz.materialdrawer.Drawer;

public class MainFragment extends Fragment {
    public static final String BUNDLE_NAVIGATION_DRAWER_KEY = "navigationDrawerKey";

    public MainFragment() {
        super(R.layout.main_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*Bundle bundle = this.requireArguments();
        SerializableNavigationDrawer sDrawer = (SerializableNavigationDrawer) bundle.getSerializable(BUNDLE_NAVIGATION_DRAWER_KEY);*/
        Drawer drawer = TmhActivity.navigationDrawer;


        drawer.setOnDrawerItemClickListener((drawerView, position, drawerItem) -> {
            if (drawerItem.getIdentifier() == TmhActivity.DRAWER_ITEM_CONVERTER) {
                Toast.makeText(getContext(), "Yolo !", Toast.LENGTH_LONG).show();
            }
            return false;
        });

        if (savedInstanceState == null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.main_fragment_container_view, AddOrEditOperationFragment.class, null)
                    .commit();
        }
    }
}
