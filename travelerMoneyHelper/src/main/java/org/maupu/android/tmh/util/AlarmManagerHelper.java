package org.maupu.android.tmh.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.receiver.DriveBackupBroadcastReceiver;

import java.util.TimeZone;

public class AlarmManagerHelper {
    private final Context context;
    private long nextAlarmMillis;
    private long nextAlarmRepeatMillis;
    private DateTime nextAlarmDateTime;

    public AlarmManagerHelper(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    private int getPendingIntentFlag() {
        // FLAG_IMMUTABLE / FLAG_MUTABLE is only needed for android >= 23
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
    }

    public synchronized void registerDriveBackupAlarm() {
        boolean prefDriveActivated = StaticData.getPreferenceValueBoolean(StaticData.PREF_KEY_DRIVE_ACTIVATE);
        final int prefDriveAutomaticType = StaticData.getPreferenceValueInt(StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_KEY);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent broadcastIntent = new Intent(context, DriveBackupBroadcastReceiver.class);

        if (!prefDriveActivated || prefDriveAutomaticType == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_NEVER) {
            disableAndCancelAlarm();
            return;
        }

        // Setup alarm (next backup with repetition)
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, DriveBackupBroadcastReceiver.requestCode, broadcastIntent, getPendingIntentFlag());

        LocalDateTime next = LocalDateTime.now();
        long nextRepeatMillis = -1;
        LocalDateTime today = LocalDateTime.now();
        // Compute first occurrence
        if (prefDriveAutomaticType == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_TEST) { // test and debug only
            nextRepeatMillis = 60000L; // repeat every 60 seconds
            next = today.plusSeconds(10); // next backup 10 seconds
        } else if (prefDriveAutomaticType == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_DAILY) {// every day @10pm
            nextRepeatMillis = AlarmManager.INTERVAL_DAY;
            int plusDay = today.getHourOfDay() < StaticData.BACKUP_DEFAULT_HOUR_OF_DAY ? 0 : 1;
            next = today
                    .plusDays(plusDay)
                    .withField(DateTimeFieldType.hourOfDay(), StaticData.BACKUP_DEFAULT_HOUR_OF_DAY)
                    .withField(DateTimeFieldType.minuteOfHour(), 0)
                    .withField(DateTimeFieldType.secondOfMinute(), 0);
        } else if (prefDriveAutomaticType == StaticData.PREF_DRIVE_AUTOMATIC_BACKUP_FREQ_WEEKLY) { // every week on sunday @10pm
            nextRepeatMillis = 7L * AlarmManager.INTERVAL_DAY;
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
            nextRepeatMillis = 30L * AlarmManager.INTERVAL_DAY;
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
            resetNextAlarmDateTime();
            calculateNextAlarmDateTime();
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    nextWithTz.getMillis(),
                    nextRepeatMillis,
                    pendingIntent);
        }
    }

    /**
     * Disable and cancel next alarm is set
     */
    public synchronized void disableAndCancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent broadcastIntent = new Intent(context, DriveBackupBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, DriveBackupBroadcastReceiver.requestCode, broadcastIntent, getPendingIntentFlag() | PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
            alarmManager.cancel(pendingIntent);
        nextAlarmMillis = -1;
        nextAlarmRepeatMillis = -1;
        resetNextAlarmDateTime();
    }

    private void savePrefs() {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(StaticData.KEY_AUTOMATIC_BACKUP_NEXT_ALARM_DATE_TIME, getNextAlarmDateTimeAsString())
                .apply();
    }

    private void resetNextAlarmDateTime() {
        nextAlarmDateTime = null;
        savePrefs();
    }

    /**
     * Set the next alarm date field
     * (this method is not setting the next alarm, it just updates the next date for UI update)
     */
    public synchronized void calculateNextAlarmDateTime() {
        if (nextAlarmDateTime == null) {
            DateTimeZone tz = DateTimeZone.forTimeZone(TimeZone.getDefault());
            nextAlarmDateTime = new DateTime(nextAlarmMillis, tz);
        } else {
            nextAlarmDateTime = nextAlarmDateTime.plus(nextAlarmRepeatMillis);
        }
        savePrefs();
    }

    public DateTime getNextAlarmDateTime() {
        return nextAlarmDateTime;
    }

    public String getNextAlarmDateTimeAsString() {
        if (nextAlarmDateTime == null)
            return "";

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
        return nextAlarmDateTime.toString(dtf);
    }
}
