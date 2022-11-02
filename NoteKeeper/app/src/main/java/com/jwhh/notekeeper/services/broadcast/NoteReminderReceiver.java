package com.jwhh.notekeeper.services.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jwhh.notekeeper.notification.NoteReminderNotification;
import com.jwhh.notekeeper.ui.screens.NoteActivity;

import java.util.Random;

public class NoteReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTE_TITLE = "com.jwhh.notekeeper.services.NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "com.jwhh.notekeeper.services.NOTE_TEXT";
    public static final String EXTRA_NOTE_ID = "com.jwhh.notekeeper.services.NOTE_ID";


    @Override
    public void onReceive(Context context, Intent intent) {
        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
        int noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0);

        Random random = new Random();
        int notificationId = random.nextInt();
        NoteReminderNotification.notify(context,
                noteTitle,
                noteText,
                notificationId, noteId);

    }
}