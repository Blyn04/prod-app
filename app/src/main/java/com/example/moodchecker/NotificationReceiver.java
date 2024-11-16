package com.example.moodchecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("taskName");
        Toast.makeText(context, "Task: " + taskName + " is overdue!", Toast.LENGTH_LONG).show();

        // Here you can add code to send a notification instead of Toast.
    }
}

