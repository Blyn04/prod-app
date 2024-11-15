package com.example.moodchecker;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundManager {
    private MediaPlayer mainMediaPlayer;
    private MediaPlayer backgroundMediaPlayer;

    public void playMainSound(Context context, int soundResId) {
        if (mainMediaPlayer != null) {
            mainMediaPlayer.release();
        }
        mainMediaPlayer = MediaPlayer.create(context, soundResId);
        mainMediaPlayer.setLooping(true);
        mainMediaPlayer.start();
    }

    public void playBackgroundSound(Context context, int soundResId) {
        if (backgroundMediaPlayer != null) {
            backgroundMediaPlayer.release();
        }
        backgroundMediaPlayer = MediaPlayer.create(context, soundResId);
        backgroundMediaPlayer.setLooping(true);
        backgroundMediaPlayer.start();
    }

    public void stopAllSounds() {
        // Stop main sound
        if (mainMediaPlayer != null && mainMediaPlayer.isPlaying()) {
            mainMediaPlayer.stop();
            mainMediaPlayer.release();
            mainMediaPlayer = null;
        }

        // Stop background sound
        if (backgroundMediaPlayer != null && backgroundMediaPlayer.isPlaying()) {
            backgroundMediaPlayer.stop();
            backgroundMediaPlayer.release();
            backgroundMediaPlayer = null;
        }
    }
}
