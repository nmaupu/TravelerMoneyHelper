package org.maupu.android.tmh;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import org.maupu.android.tmh.ui.ApplicationDrawer;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;

public abstract class TmhActivity extends AppCompatActivity {
    public void changeFragment(Class fragment, boolean addToBackStack) {
        changeFragment(fragment, addToBackStack, null);
    }

    public void changeFragment(Class fragment, boolean addToBackStack, Bundle args) {
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();

        if (addToBackStack) {
            transaction
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.fragment_content_main, fragment, args)
                    .setReorderingAllowed(true)
                    .addToBackStack(null);

            ApplicationDrawer.getInstance().setNavigationBarDrawerIconToBackArrow();
        } else {
            transaction
                    .replace(R.id.fragment_content_main, fragment, args)
                    .setReorderingAllowed(true);
        }

        transaction.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public void onBackPressed() {
        SoftKeyboardHelper.hide(this, getCurrentFocus());
        if (ApplicationDrawer.getDrawer() != null && ApplicationDrawer.getDrawer().isDrawerOpen()) {
            ApplicationDrawer.getDrawer().closeDrawer();
        } else if (ApplicationDrawer.getDrawer() != null && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            ApplicationDrawer.getInstance().setNavigationBarDrawerIconToHamburger();
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
