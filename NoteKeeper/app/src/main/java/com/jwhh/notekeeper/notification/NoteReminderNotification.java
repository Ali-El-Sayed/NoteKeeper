package com.jwhh.notekeeper.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.services.NoteBackup;
import com.jwhh.notekeeper.services.NoteBackupService;
import com.jwhh.notekeeper.ui.screens.NoteActivity;
import com.jwhh.notekeeper.ui.screens.NoteListActivity;

public class NoteReminderNotification {

    private static final String NOTIFICATION_TAG = "Reminder";
    public static final String REMINDER_CHANNEL = "reminders";

    public static void notify(Context context,
                              String noteTitle,
                              String noteText,
                              int number,
                              long noteId) {
        final Bitmap picture = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_launcher_foreground);

        Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra(NoteActivity.NOTE_ID, noteId);

        Intent backupServiceIntent = new Intent(context, NoteBackupService.class);
        backupServiceIntent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);

        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(context, REMINDER_CHANNEL);

        builder.setDefaults(Notification.DEFAULT_ALL)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(noteTitle)
                .setContentText(noteText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setTicker(noteTitle)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(noteText)
                        .setBigContentTitle(noteTitle)
                        .setSummaryText("Review Note"))
                .setNumber(number)
                .setLargeIcon(picture)
//                .setContentIntent(
//                        PendingIntent.getActivity(context,
//                                0,
//                                intent,
//                                PendingIntent.FLAG_MUTABLE)
//                )

                .addAction(0, "View All notes",
                        PendingIntent.getActivity(
                                context, 0
                                , new Intent(context, NoteListActivity.class),
                                PendingIntent.FLAG_MUTABLE

                        ))
                .addAction(0,
                        "Backup Notes",
                        PendingIntent.getService(context,
                                2,
                                backupServiceIntent
                                , PendingIntent.FLAG_MUTABLE)).setAutoCancel(true);
        notify(context, builder.build());
    }

    private static void notify(Context context, Notification notification) {
        NotificationManager notificationManager = registerNotificationChannel(context);

        notificationManager.notify(NOTIFICATION_TAG, 0, notification);
    }

    private static NotificationManager registerNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NoteReminderNotification.REMINDER_CHANNEL, "Note Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Notification Description");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return notificationManager;
    }

}
