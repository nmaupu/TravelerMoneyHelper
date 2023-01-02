package org.maupu.android.tmh.util.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.AlarmManagerHelper;
import org.maupu.android.tmh.util.NotificationHelper;

public class BootBroadcastReceiver extends BroadcastReceiver {
    public static final String notificationChannelId = "tmh_boot_notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManagerHelper amh = new AlarmManagerHelper(context);
        amh.registerDriveBackupAlarm();
        TmhApplication.alarmManagerHelper = amh;

        String nextAlarm = amh.getNextAlarmDateTimeAsString();

        if (!"".equals(nextAlarm)
                && StaticData.getPreferenceValueBoolean(StaticData.PREF_KEY_DRIVE_ACTIVATE)
                && StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_NEVER != StaticData.getPreferenceValueInt(StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY)
                && StaticData.getPreferenceValueBoolean(StaticData.PREF_KEY_DRIVE_AUTOMATIC_BACKUP_BOOT_NOTIFICATION)) {

            NotificationCompat.Builder builder = NotificationHelper.getNewNotificationBuilder(context, notificationChannelId, R.drawable.ic_baseline_backup_black_24);
            builder
                    .setContentTitle(context.getString(R.string.backup_boot_notification_title))
                    .setContentText(nextAlarm);
            NotificationHelper.getNotificationManager(context).notify(NotificationHelper.getRandomNotificationId(), builder.build());
        }
    }
}
