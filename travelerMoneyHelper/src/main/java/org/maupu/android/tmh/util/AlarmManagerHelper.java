package org.maupu.android.tmh.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.drive.DriveBackupBroadcastReceiver;

import java.util.TimeZone;

public class AlarmManagerHelper {
    private long nextAlarmMillis;
    private long nextAlarmRepeatMillis;

    public void registerDriveBackupAlarm(Context context) {
        boolean prefDriveActivated = StaticData.getPreferenceValueBoolean(StaticData.PREF_KEY_DRIVE_ACTIVATE);
        final int prefDriveAutomaticType = StaticData.getPreferenceValueInt(StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent broadcastIntent = new Intent(context, DriveBackupBroadcastReceiver.class);

        if (!prefDriveActivated || prefDriveAutomaticType == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_NEVER) {
            // Cancel current alarm if set
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, DriveBackupBroadcastReceiver.requestCode, broadcastIntent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null)
                alarmManager.cancel(pendingIntent);
            nextAlarmMillis = -1;
            nextAlarmRepeatMillis = -1;
            return;
        }

        // Setup alarm (next backup with repetition)
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, DriveBackupBroadcastReceiver.requestCode, broadcastIntent, 0);


        long nowMillis = System.currentTimeMillis();
        LocalDateTime next = LocalDateTime.now();
        long nextRepeatMillis = -1;
        LocalDateTime today = LocalDateTime.now();
        // Compute first occurrence
        if (prefDriveAutomaticType == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_DAILY) {// every day @10pm
            nextRepeatMillis = 24L * 3600L * 1000L;
            int plusDay = today.getHourOfDay() < StaticData.BACKUP_DEFAULT_HOUR_OF_DAY ? 0 : 1;
            next = today
                    .plusDays(plusDay)
                    .withField(DateTimeFieldType.hourOfDay(), StaticData.BACKUP_DEFAULT_HOUR_OF_DAY)
                    .withField(DateTimeFieldType.minuteOfHour(), 0)
                    .withField(DateTimeFieldType.secondOfMinute(), 0);
        } else if (prefDriveAutomaticType == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_WEEKLY) { // every week on sunday @10pm
            nextRepeatMillis = 7L * 24L * 3600L * 1000L;
            if (today.getDayOfWeek() < StaticData.BACKUP_DEFAULT_DAY_OF_WEEK ||
                    (today.getDayOfWeek() == StaticData.BACKUP_DEFAULT_DAY_OF_WEEK && today.getHourOfDay() < StaticData.BACKUP_DEFAULT_HOUR_OF_DAY)) {
                next = today
                        .plusDays(StaticData.BACKUP_DEFAULT_DAY_OF_WEEK - today.getDayOfWeek())
                        .withField(DateTimeFieldType.hourOfDay(), StaticData.BACKUP_DEFAULT_HOUR_OF_DAY)
                        .withField(DateTimeFieldType.minuteOfHour(), 0)
                        .withField(DateTimeFieldType.secondOfMinute(), 0);
            } else {
                next = today
                        .plusDays(7 - today.getDayOfWeek() - StaticData.BACKUP_DEFAULT_DAY_OF_WEEK)
                        .withField(DateTimeFieldType.hourOfDay(), StaticData.BACKUP_DEFAULT_HOUR_OF_DAY)
                        .withField(DateTimeFieldType.minuteOfHour(), 0)
                        .withField(DateTimeFieldType.secondOfMinute(), 0);
            }
        } else if (prefDriveAutomaticType == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_MONTHLY) { // every 1st of the month @10pm
            nextRepeatMillis = 30L * 24L * 3600L * 1000L;
            if (today.getDayOfMonth() == 1 && today.getHourOfDay() < StaticData.BACKUP_DEFAULT_HOUR_OF_DAY) {
                next = today
                        .withField(DateTimeFieldType.hourOfDay(), StaticData.BACKUP_DEFAULT_HOUR_OF_DAY)
                        .withField(DateTimeFieldType.minuteOfHour(), 0)
                        .withField(DateTimeFieldType.secondOfMinute(), 0);
            } else {
                next = today
                        .plusMonths(1)
                        .withField(DateTimeFieldType.dayOfMonth(), StaticData.BACKUP_DEFAULT_DAY_OF_MONTH)
                        .withField(DateTimeFieldType.hourOfDay(), StaticData.BACKUP_DEFAULT_HOUR_OF_DAY)
                        .withField(DateTimeFieldType.minuteOfHour(), 0)
                        .withField(DateTimeFieldType.secondOfMinute(), 0);
            }
        }

        if (nextRepeatMillis > 0) {
            DateTimeZone tz = DateTimeZone.forTimeZone(TimeZone.getDefault());
            DateTime nextWithTz = next.toDateTime(tz);
            this.nextAlarmMillis = nextWithTz.getMillis();
            this.nextAlarmRepeatMillis = nextRepeatMillis;
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    nextWithTz.getMillis(),
                    nextRepeatMillis,
                    pendingIntent);
        }
    }

    public long getNextAlarmMillis() {
        return nextAlarmMillis;
    }

    public long getNextAlarmRepeatMillis() {
        return nextAlarmRepeatMillis;
    }

    public DateTime getNextAlarm() {
        if (nextAlarmMillis < 0 || nextAlarmRepeatMillis < 0)
            return null;

        DateTime dt = new DateTime(nextAlarmMillis);
        return dt;
    }
}
