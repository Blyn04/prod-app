package com.example.moodchecker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

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

//        createNotificationChannel();
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

        if ("STOP_TIMER".equals(intent.getAction())) {
            stopTimer();
            stopForeground(true); // Remove the notification
            stopSelf(); // Stop the service
            return START_NOT_STICKY;
        }

        if (timerDuration == 0) {
            timerDuration = intent.getLongExtra("timerDuration", 0);
        }
        startForegroundService();

        if (!isPaused) {
            startTimer();
        }

        return START_NOT_STICKY;
    }

    private void stopTimer() {
        if (handler != null) {
            handler.removeCallbacks(timerRunnable);
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPaused = false; // Reset paused state
        timerRunnable = null; // Clear the timer
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
                        Log.d("TimerService", "Timer finished. Triggering alarm.");
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


//    private void triggerAlarm() {
//        // Play the alarm sound
//        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
//        if (mediaPlayer != null) {
//            mediaPlayer.setOnCompletionListener(mp -> stopSelf());
//            mediaPlayer.start();
//            Log.d("TimerService", "Alarm sound started.");
//
//        } else {
//            Log.e("TimerService", "MediaPlayer creation failed");
//        }
//
//        // Show "Time's Up!" as a new notification
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        if (manager != null) {
//            Notification notification = createNotification("Time's Up!", "The timer has ended.");
//            manager.notify(2, notification); // Use a different ID to show a new notification
//        }
//
//        handler.postDelayed(() -> stopSelf(), 2000);
//        stopForeground(true); // Remove the foreground service notification
//        stopSelf(); // Stop the service
//    }

    private void triggerAlarm() {
        Log.d("TimerService", "Triggering alarm...");

        // Trigger alarm sound only once when the timer ends
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                    0
            );
        }

        try {
            // Check if the MediaPlayer is properly initialized
            if (mediaPlayer == null) {
                Log.d("TimerService", "Creating MediaPlayer for alarm sound...");
                mediaPlayer = MediaPlayer.create(this, R.raw.alarm);  // Make sure you have the correct path here
            }

            if (mediaPlayer != null) {
                Log.d("TimerService", "MediaPlayer is initialized, starting alarm.");
                mediaPlayer.setOnCompletionListener(mp -> {
                    Log.d("TimerService", "Alarm finished playing.");
                    mp.reset();
                    mp.release();
                });
                mediaPlayer.start();  // Play the alarm
            } else {
                Log.e("TimerService", "MediaPlayer is null. Cannot play alarm sound.");
            }
        } catch (Exception e) {
            Log.e("TimerService", "Error occurred while playing alarm sound: " + e.getMessage());
        }

        // Show "Time's Up!" notification
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            Notification notification = createNotification("Time's Up!", "The timer has ended.");
            manager.notify(2, notification);  // Use a different ID to show a new notification
        }

        stopForeground(true); // Remove the foreground service notification
        stopSelf(); // Stop the service immediately after the alarm plays
    }



//    private void triggerAlarm() {
//        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
//        if (audioManager != null) {
//            // Force music stream volume to max
//            audioManager.setStreamVolume(
//                    AudioManager.STREAM_MUSIC,
//                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
//                    0
//            );
//        }
//
//        // Initialize MediaPlayer and play alarm
//        if (mediaPlayer == null) {
//            mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
//        }
//        if (mediaPlayer != null) {
//            mediaPlayer.setOnCompletionListener(mp -> {
//                mp.reset();
//                mp.release();
//                stopSelf(); // Stop the service after alarm finishes
//            });
//            mediaPlayer.start();
//            Log.d("TimerService", "Alarm sound started.");
//        } else {
//            Log.e("TimerService", "MediaPlayer creation failed.");
//        }
//
//        // Show "Time's Up!" notification
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        if (manager != null) {
//            Notification notification = createNotification("Time's Up!", "The timer has ended.");
//            manager.notify(2, notification);
//        }
//
//        stopForeground(true); // Remove the foreground service notification
//        handler.postDelayed(this::stopSelf, 2000); // Stop the service after delay
//    }



    private void updateNotification(String timerText) {
        Notification notification = createNotification("Task Timer", timerText);  // Update notification with timer text
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);  // Update notification with new timer value
        }
    }

//    private Notification createNotification(String title, String text) {
//        Intent openActivityIntent = new Intent(this, TimerActivity.class);  // Open the TimerActivity on notification click
//        PendingIntent openActivityPendingIntent = PendingIntent.getActivity(this, 0, openActivityIntent, PendingIntent.FLAG_IMMUTABLE);
//
//        return new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(text)
//                .setSmallIcon(R.drawable.ic_timer) // Use your own icon here
//                .addAction(createPauseAction()) // Add a pause button
//                .setOngoing(true) // Make notification non-dismissible
//                .setContentIntent(openActivityPendingIntent) // Set the intent to open the TimerActivity
//                .build();
//    }

    private Notification createNotification(String title, String text) {
        Intent openActivityIntent = new Intent(this, TimerActivity.class);
        PendingIntent openActivityPendingIntent = PendingIntent.getActivity(this, 0, openActivityIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(openActivityPendingIntent)
                .setOngoing(!"Time's Up!".equals(title)); // Make it dismissible only for "Time's Up!"

        // Ensure no sound for the timer countdown, only the "Time's Up!" notification should have sound
        if ("Time's Up!".equals(title)) {
            builder.setSound(Settings.System.DEFAULT_RINGTONE_URI); // Set the sound only for the final notification

        } else {
            builder.setSound(null);  // No sound for the countdown
        }

        if (!"Time's Up!".equals(title)) {
            builder.addAction(createPauseAction());
            builder.addAction(createStopAction());
        }

        return builder.build();
    }

    private NotificationCompat.Action createStopAction() {
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction("STOP_TIMER");

        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_stop, // Use your own stop icon
                "Stop",
                stopPendingIntent
        ).build();
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
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            channel.setDescription("Notifications for timer events");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            } else {
                Log.e("TimerService", "NotificationManager is null");
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