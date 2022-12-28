package org.maupu.android.tmh.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.maupu.android.tmh.core.TmhApplication;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManagerHelper amh = new AlarmManagerHelper();
        amh.registerDriveBackupAlarm(context);
        TmhApplication.alarmManagerHelper = amh;
    }
}
