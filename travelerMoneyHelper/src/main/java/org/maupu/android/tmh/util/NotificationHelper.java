package org.maupu.android.tmh.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.maupu.android.tmh.R;

public abstract class NotificationHelper {
    public static NotificationCompat.Builder getNewNotificationBuilder(Context context, String notificationChannelId, int iconRes) {
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context, notificationChannelId)
                .setSmallIcon(iconRes)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "TMH";
            String description = "TMH notification channel";
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelId,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        return notifBuilder;
    }

    public static NotificationManagerCompat getNotificationManager(Context context) {
        return NotificationManagerCompat.from(context);
    }

    public static void notifyError(Context context, int notificationId, NotificationCompat.Builder builder, NotificationManagerCompat notifManager, int titleRes, Exception e) {
        builder
                .setProgress(0, 0, false)
                .setContentTitle(context.getString(titleRes))
                .setContentText(e.getMessage());
        notifManager.notify(notificationId, builder.build());
    }

    public static int getRandomNotificationId() {
        return (int) Math.floor(Math.random() * (99999 - 1000 + 1) + 1000);
    }
}
