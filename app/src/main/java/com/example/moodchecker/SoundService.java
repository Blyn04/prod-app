package com.example.moodchecker;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

//public class SoundService extends Service {
//    private MediaPlayer mainMediaPlayer;
//    private MediaPlayer backgroundMediaPlayer;
//
//    private static final String TAG = "SoundService";
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        int soundResId = intent.getIntExtra("soundResId", -1);
//        boolean isBackground = intent.getBooleanExtra("isBackground", false);
//
//        Log.d("SoundService", "Received soundResId: " + soundResId);
//
//        if (soundResId != -1) {
//            if (isBackground) {
//                playBackgroundSound(soundResId);
//            } else {
//                playMainSound(soundResId);
//            }
//        }
//
//        return START_STICKY;
//    }
//
//    private void playMainSound(int soundResId) {
//        if (mainMediaPlayer != null) {
//            mainMediaPlayer.release();
//        }
//
//        mainMediaPlayer = MediaPlayer.create(this, soundResId);
//        if (mainMediaPlayer == null) {
//            Log.e("SoundService", "Failed to create MediaPlayer for sound: " + soundResId);
//            return;
//        }
//
//        mainMediaPlayer.setLooping(false); // Set to false for main sound
//        mainMediaPlayer.start();
//        Log.d("SoundService", "Main sound started: " + soundResId);
//    }
//
//
//
//    private void playBackgroundSound(int soundResId) {
//        if (backgroundMediaPlayer != null) {
//            backgroundMediaPlayer.release();
//        }
//        backgroundMediaPlayer = MediaPlayer.create(this, soundResId);
//        backgroundMediaPlayer.setLooping(true);
//        backgroundMediaPlayer.start();
//        Log.d(TAG, "Background sound started: " + soundResId);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        stopAllSounds();
//    }
//
//    private void stopAllSounds() {
//        if (mainMediaPlayer != null && mainMediaPlayer.isPlaying()) {
//            mainMediaPlayer.stop();
//            mainMediaPlayer.release();
//            mainMediaPlayer = null;
//        }
//
//        if (backgroundMediaPlayer != null && backgroundMediaPlayer.isPlaying()) {
//            backgroundMediaPlayer.stop();
//            backgroundMediaPlayer.release();
//            backgroundMediaPlayer = null;
//        }
//
//        Log.d("SoundService", "All sounds stopped.");
//    }
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}

public class SoundService extends Service {
    private MediaPlayer mainMediaPlayer;
    private MediaPlayer backgroundMediaPlayer;
    private boolean isBackgroundPlaying = false;

    private static final String TAG = "SoundService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int soundResId = intent.getIntExtra("soundResId", -1);
        boolean isBackground = intent.getBooleanExtra("isBackground", false);

        if (soundResId != -1) {
            if (isBackground) {
                playBackgroundSound(soundResId);
            } else {
                playMainSound(soundResId);
            }
        }
        return START_STICKY;
    }

    private void playMainSound(int soundResId) {
        if (mainMediaPlayer != null && mainMediaPlayer.isPlaying()) {
            mainMediaPlayer.stop();
        }

        mainMediaPlayer = MediaPlayer.create(this, soundResId);
        mainMediaPlayer.setLooping(false);
        mainMediaPlayer.start();
        Log.d("SoundService", "Main sound started: " + soundResId);
    }

    private void playBackgroundSound(int soundResId) {
        if (backgroundMediaPlayer != null && backgroundMediaPlayer.isPlaying()) {
            backgroundMediaPlayer.stop();
        }

        backgroundMediaPlayer = MediaPlayer.create(this, soundResId);
        backgroundMediaPlayer.setLooping(true);
        backgroundMediaPlayer.start();
        isBackgroundPlaying = true;
        Log.d(TAG, "Background sound started: " + soundResId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAllSounds();
    }

    private void stopAllSounds() {
        if (mainMediaPlayer != null && mainMediaPlayer.isPlaying()) {
            mainMediaPlayer.stop();
            mainMediaPlayer.release();
            mainMediaPlayer = null;
        }

        if (backgroundMediaPlayer != null && backgroundMediaPlayer.isPlaying()) {
            backgroundMediaPlayer.stop();
            backgroundMediaPlayer.release();
            backgroundMediaPlayer = null;
        }

        Log.d("SoundService", "All sounds stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
