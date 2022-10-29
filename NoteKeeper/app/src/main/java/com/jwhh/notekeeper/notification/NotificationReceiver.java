package com.jwhh.notekeeper.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        String msg = intent.getStringExtra("ToastMessage");
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
