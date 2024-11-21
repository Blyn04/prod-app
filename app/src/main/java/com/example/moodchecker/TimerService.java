package com.example.moodchecker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
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

    private boolean isTimerRunning = false; // Flag to track if the timer is already running

    private void startTimer() {
        // Only start the timer if it isn't already running
        if (!isTimerRunning) {
            isTimerRunning = true; // Set the flag to true indicating the timer is running

            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (timerDuration > 0) {
                        timerDuration -= 1000; // Decrease by 1 second
                        updateNotification(formatTime(timerDuration)); // Update notification with the new time
                        handler.postDelayed(this, 1000); // Repeat every second
                    } else {
                        Log.d("TimerService", "Timer finished. Triggering alarm.");
                        triggerAlarm();
                        stopSelf(); // Stop the service when the timer ends
                        isTimerRunning = false; // Reset the flag when timer finishes
                    }
                }
            };

            handler.post(timerRunnable); // Start the timer
        }
    }

    private void pauseTimer() {
        isPaused = true; // Set the flag to true to indicate the timer is paused
        handler.removeCallbacks(timerRunnable); // Stop the timer
        updateNotification("Timer Paused: " + formatTime(timerDuration)); // Show paused state in notification
        isTimerRunning = false; // Reset the flag when the timer is paused
    }

    private void resumeTimer() {
        if (isPaused) {
            isPaused = false; // Set the flag to false to indicate the timer is resumed
            startTimer(); // Resume the timer from where it was paused
            updateNotification("Timer Resumed: " + formatTime(timerDuration)); // Show resumed state in notification
        }
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

//    private void triggerAlarm() {
//    Log.d("TimerService", "Triggering alarm...");
//
//    // Request audio focus before starting the alarm sound
//    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
//    if (audioManager != null) {
//        AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_ALARM)
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                .build();
//
//        AudioFocusRequest audioFocusRequest = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                    .setAudioAttributes(audioAttributes)
//                    .setOnAudioFocusChangeListener(focusChange -> {
//                        // Handle audio focus changes if needed
//                    })
//                    .build();
//        }
//
//        // Request audio focus
//        int result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            result = audioManager.requestAudioFocus(audioFocusRequest);
//        }
//
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            try {
//                // Initialize the MediaPlayer if it's null
//                if (mediaPlayer == null) {
//                    mediaPlayer = MediaPlayer.create(this, R.raw.alarm); // Replace with your alarm resource
//                }
//
//                if (mediaPlayer != null) {
//                    // Set the error listener
//                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
//                        Log.e("MediaPlayer", "Error occurred: what=" + what + ", extra=" + extra);
//                        mp.reset(); // Reset the MediaPlayer
//                        return true; // Indicate the error was handled
//                    });
//
//                    // Play the alarm sound
//                    mediaPlayer.setOnCompletionListener(mp -> {
//                        mp.release(); // Release resources when playback finishes
//                        mediaPlayer = null; // Reset the MediaPlayer reference
//                    });
//                    mediaPlayer.start();
//                } else {
//                    Log.e("TimerService", "MediaPlayer is null. Cannot play alarm sound.");
//                }
//            } catch (Exception e) {
//                Log.e("TimerService", "Error occurred while playing alarm sound: " + e.getMessage());
//            }
//        } else {
//            Log.e("TimerService", "Audio focus request failed.");
//        }
//    }
//
//    // Show "Time's Up!" notification
//    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//    if (manager != null) {
//        Notification notification = createNotification("Time's Up!", "The timer has ended.");
//        manager.notify(2, notification); // Use a different ID to show a new notification
//    }
//
//    stopForeground(true); // Remove the foreground service notification
//    stopSelf(); // Stop the service immediately after the alarm plays
//}

    private void triggerAlarm() {
        Log.d("TimerService", "Triggering alarm...");

        // Play alarm sound (existing code)
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            // MediaPlayer setup (unchanged)
        }

        // Start the TimerDialogActivity to show the pop-up
        Intent dialogIntent = new Intent(this, TimerDialogActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Required to start an activity from a service
        dialogIntent.putExtra("message", "Time's Up! The timer has ended.");
        startActivity(dialogIntent);

        stopForeground(true); // Remove the foreground service notification
        stopSelf(); // Stop the service
    }



    private void updateNotification(String timerText) {
        Notification notification = createNotification("Task Timer", timerText);  // Update notification with timer text
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);  // Update notification with new timer value
        }
    }

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