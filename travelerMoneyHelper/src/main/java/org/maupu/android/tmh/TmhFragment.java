package org.maupu.android.tmh;

import androidx.fragment.app.Fragment;

import org.maupu.android.tmh.ui.async.AsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;

import java.util.Map;

public class TmhFragment extends Fragment implements IAsyncActivityRefresher {
    public TmhFragment(int contentView) {
        super(contentView);
    }

    public void refreshDisplay() {
        AsyncActivityRefresher refresher = new AsyncActivityRefresher(getActivity(), this, false);
        try {
            // Execute background task implemented by client class
            refresher.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground() {
        return null;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
    }
}
