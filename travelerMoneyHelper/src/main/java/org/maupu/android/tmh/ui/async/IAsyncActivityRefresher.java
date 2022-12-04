package org.maupu.android.tmh.ui.async;

import java.util.Map;

public interface IAsyncActivityRefresher {
    public Map<Integer, Object> handleRefreshBackground();

    public void handleRefreshEnding(Map<Integer, Object> results);
}
