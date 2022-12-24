package org.maupu.android.tmh.ui.async;

import java.util.Map;

public interface IAsyncActivityRefresher {
    Map<Integer, Object> handleRefreshBackground(AbstractAsyncTask asyncTask);

    void handleRefreshEnding(Map<Integer, Object> results);
}
