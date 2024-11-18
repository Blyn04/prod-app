package com.example.moodchecker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class TimerService extends Service {
    private static final String CHANNEL_ID = "timer_channel";
    private static final int NOTIFICATION_ID = 1;
    private long timerDuration;
    private Handler handler;
    private Runnable timerRunnable;
    private MediaPlayer mediaPlayer;
    private boolean isPaused = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (intent != null) {
//            timerDuration = intent.getLongExtra("timerDuration", 0); // Get timer duration in milliseconds
//            startForegroundService(); // Start the service in the foreground immediately
////            startTimer(); // Start the timer after starting the service
//
//            if (!isPaused) {
//                startTimer(); // Start the timer if not paused
//            }
//        }

        if (intent != null) {
            // Retrieve timer duration only once, not every time a pause/resume is called
            if (timerDuration == 0) {
                timerDuration = intent.getLongExtra("timerDuration", 0); // Get timer duration in milliseconds
            }

            startForegroundService(); // Start the service in the foreground immediately

            // Start the timer if it's not paused
            if (!isPaused) {
                startTimer();
            }
        }

        // Handle pause action
        if ("PAUSE_TIMER".equals(intent.getAction())) {
            pauseTimer(); // Pause the timer
            updateNotification("Timer Paused: " + formatTime(timerDuration)); // Update notification
            return START_NOT_STICKY;
        }

        // Handle resume action
        if ("RESUME_TIMER".equals(intent.getAction())) {
            resumeTimer(); // Resume the timer if it's paused
            updateNotification("Timer Resumed: " + formatTime(timerDuration)); // Update notification
            return START_NOT_STICKY;
        }

        return START_NOT_STICKY;
    }

    private void startForegroundService() {
        createNotificationChannel();
        Notification notification = createNotification("Timer Started", formatTime(timerDuration));
        startForeground(NOTIFICATION_ID, notification);
    }

//    private void startTimer() {
//        timerRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (timerDuration > 0) {
//                    timerDuration -= 1000; // Decrease by 1 second
//                    updateNotification(formatTime(timerDuration));
//                    handler.postDelayed(this, 1000); // Repeat every second
//                } else {
//                    triggerAlarm();
//                    stopSelf(); // Stop the service when the timer ends
//                }
//            }
//        };
//        handler.post(timerRunnable); // Start the timer
//    }

    private void startTimer() {
        // Only start the timer if it isn't already running
        if (timerRunnable == null) {
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (timerDuration > 0) {
                        timerDuration -= 1000; // Decrease by 1 second
                        updateNotification(formatTime(timerDuration));
                        handler.postDelayed(this, 1000); // Repeat every second
                    } else {
                        triggerAlarm();
                        stopSelf(); // Stop the service when the timer ends
                    }
                }
            };
        }
        handler.post(timerRunnable); // Start the timer
    }

    private void pauseTimer() {
        isPaused = true; // Set the flag to true to indicate the timer is paused
        handler.removeCallbacks(timerRunnable); // Stop the timer
        updateNotification("Timer Paused: " + formatTime(timerDuration)); // Show paused state in notification
    }

    private void resumeTimer() {
        isPaused = false; // Set the flag to false to indicate the timer is resumed
        startTimer(); // Resume the timer
        updateNotification("Timer Resumed: " + formatTime(timerDuration)); // Show resumed state in notification
    }


    private void triggerAlarm() {
        // Play the alarm sound
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm); // Make sure alarm.mp3 is placed in res/raw
        mediaPlayer.setOnCompletionListener(mp -> {
            // Stop the service after the alarm sound finishes
            stopSelf();
        });
        mediaPlayer.start(); // Start the alarm sound

        // Optionally, you can show a notification with sound when the timer ends
        Notification notification = createNotification("Time's Up!", "The timer has ended.");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification); // Update notification
        }
    }


    private void updateNotification(String timerText) {
        Notification notification = createNotification("Task Timer", timerText);  // Update notification with timer text
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);  // Update notification with new timer value
        }
    }

    private Notification createNotification(String title, String text) {
        Intent openActivityIntent = new Intent(this, TimerActivity.class);  // Open the TimerActivity on notification click
        PendingIntent openActivityPendingIntent = PendingIntent.getActivity(this, 0, openActivityIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_timer) // Use your own icon here
                .addAction(createPauseAction()) // Add a pause button
                .setOngoing(true) // Make notification non-dismissible
                .setContentIntent(openActivityPendingIntent) // Set the intent to open the TimerActivity
                .build();
    }

    private NotificationCompat.Action createPauseAction() {
//        Intent pauseIntent = new Intent(this, TimerService.class);
//        pauseIntent.setAction("PAUSE_TIMER");

        String actionText = isPaused ? "Resume" : "Pause"; // Toggle button text
        Intent actionIntent = new Intent(this, TimerService.class);
        actionIntent.setAction(isPaused ? "RESUME_TIMER" : "PAUSE_TIMER");

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_pause, // Use your own pause icon
                actionText,
//                PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                PendingIntent.getService(this, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE)

        ).build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Notifications",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private String formatTime(long durationMillis) {
        long hours = durationMillis / 3600000;
        long minutes = (durationMillis % 3600000) / 60000;
        long seconds = (durationMillis % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(timerRunnable); // Stop the handler
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();  // Stop the media player if it's playing
            mediaPlayer.release();  // Release the resources
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}