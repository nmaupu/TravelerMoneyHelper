package org.maupu.android.tmh;

import androidx.fragment.app.Fragment;

import org.maupu.android.tmh.ui.ApplicationDrawer;
import org.maupu.android.tmh.ui.async.AbstractAsyncTask;
import org.maupu.android.tmh.ui.async.AsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;

import java.util.Map;

public class TmhFragment extends Fragment implements IAsyncActivityRefresher {
    public TmhFragment(int contentView) {
        super(contentView);
    }

    public void refreshDisplay() {
        AsyncActivityRefresher refresher = new AsyncActivityRefresher(requireActivity(), this, false, true, true);
        try {
            // Execute background task implemented by client class
            refresher.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        // Unregister listener so that a remaining one won't be called by error
        // with a non existent context
        ApplicationDrawer.getInstance().setOnAccountChangeListener(null);
        super.onDestroy();
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground(AbstractAsyncTask asyncTask) {
        return null;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
    }
}
