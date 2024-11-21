package com.example.moodchecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SoundPageActivity extends AppCompatActivity {
    private Button focusButton, calmButton, studyButton, sleepButton, stopButton, startReviewingButton;
    private Switch rainSoundSwitch, coffeeShopSwitch;
    private SoundManager soundManager;
    private MediaPlayer rainMediaPlayer, coffeeShopMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_page);

        soundManager = new SoundManager();

        focusButton = findViewById(R.id.focusButton);
        calmButton = findViewById(R.id.calmButton);
        studyButton = findViewById(R.id.studyButton);
        rainSoundSwitch = findViewById(R.id.rainSoundSwitch);
        coffeeShopSwitch = findViewById(R.id.coffeeShopSwitch);
        stopButton = findViewById(R.id.stopButton);
        sleepButton = findViewById(R.id.sleepButton);
        startReviewingButton = findViewById(R.id.startReviewingButton);
        SeekBar volumeSeekBar = findViewById(R.id.volumeSeekBar);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean rainSoundState = sharedPreferences.getBoolean("rainSoundState", false);
        boolean coffeeSoundState = sharedPreferences.getBoolean("coffeeSoundState", false);

//        if (rainSoundState) {
//            soundManager.playBackgroundSound(this, R.raw.rain);
//        }

        if (rainSoundState) {
            rainMediaPlayer = MediaPlayer.create(this, R.raw.rain);
            rainMediaPlayer.start();
        }

        if (coffeeSoundState) {
            coffeeShopMediaPlayer = MediaPlayer.create(this, R.raw.coffee);
            coffeeShopMediaPlayer.start();
        }

        rainSoundSwitch.setChecked(rainSoundState);
        coffeeShopSwitch.setChecked(coffeeSoundState);

        focusButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.focus1));
        calmButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.calm1));
        studyButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.study1));
        sleepButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.sleep1));

        startReviewingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SoundPageActivity.this, ReviewerActivity.class);
                startActivity(intent);
            }
        });

//        rainSoundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.rain);
//            } else {
//                soundManager.stopAllSounds();
//            }
//            saveSoundPreferences();
//        });
//
//
//        coffeeShopSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.coffee);
//            } else {
//                soundManager.stopAllSounds();
//            }
//            saveSoundPreferences();
//        });

        rainSoundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rainMediaPlayer = MediaPlayer.create(SoundPageActivity.this, R.raw.rain);
                rainMediaPlayer.start();
            } else {
                if (rainMediaPlayer != null) {
                    rainMediaPlayer.stop();
                    rainMediaPlayer.release();
                }
            }
            saveSoundPreferences();
        });

        coffeeShopSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                coffeeShopMediaPlayer = MediaPlayer.create(SoundPageActivity.this, R.raw.coffee);
                coffeeShopMediaPlayer.start();
            } else {
                if (coffeeShopMediaPlayer != null) {
                    coffeeShopMediaPlayer.stop();
                    coffeeShopMediaPlayer.release();
                }
            }
            saveSoundPreferences();
        });

//        stopButton.setOnClickListener(v -> {
//
//            if (rainMediaPlayer != null) {
//                rainMediaPlayer.stop();
//                rainMediaPlayer.release();
//            }
//            if (coffeeShopMediaPlayer != null) {
//                coffeeShopMediaPlayer.stop();
//                coffeeShopMediaPlayer.release();
//            }
//
//            soundManager.stopAllSounds();
//
//
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("rainSoundState", false);
//            editor.putBoolean("coffeeSoundState", false);
//            editor.apply();
//
//            rainSoundSwitch.setChecked(false);
//            coffeeShopSwitch.setChecked(false);
//        });

        stopButton.setOnClickListener(v -> {
            if (rainMediaPlayer != null) {
                if (rainMediaPlayer.isPlaying()) {
                    rainMediaPlayer.stop();
                }
                rainMediaPlayer.release();
                rainMediaPlayer = null;  // Make sure to nullify the reference
            }

            if (coffeeShopMediaPlayer != null) {
                if (coffeeShopMediaPlayer.isPlaying()) {
                    coffeeShopMediaPlayer.stop();
                }
                coffeeShopMediaPlayer.release();
                coffeeShopMediaPlayer = null;  // Make sure to nullify the reference
            }

            soundManager.stopAllSounds();  // Ensure this is valid for your use case

            // Save the state in SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("rainSoundState", false);
            editor.putBoolean("coffeeSoundState", false);
            editor.apply();

            // Update UI state
            rainSoundSwitch.setChecked(false);
            coffeeShopSwitch.setChecked(false);
        });


        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Set the volume for rain and coffee sounds here
                setRainVolume(progress);
                setCoffeeShopVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean rainSoundState = sharedPreferences.getBoolean("rainSoundState", false);
        boolean coffeeSoundState = sharedPreferences.getBoolean("coffeeSoundState", false);

        if (rainSoundState) {
            soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.rain);
        }

        if (coffeeSoundState) {
            soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.coffee);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.stopAllSounds();

        if (rainMediaPlayer != null) {
            rainMediaPlayer.release();
        }
        if (coffeeShopMediaPlayer != null) {
            coffeeShopMediaPlayer.release();
        }
    }

    private void saveSoundPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("rainSoundState", rainSoundSwitch.isChecked());
        editor.putBoolean("coffeeSoundState", coffeeShopSwitch.isChecked());

        editor.apply();
    }

    public void navigateBack(View view) {
        Intent intent = new Intent(SoundPageActivity.this, DashboardActivity.class);
        startActivity(intent);
    }

    private void setRainVolume(int progress) {
        if (rainMediaPlayer != null && rainMediaPlayer.isPlaying()) {
            float volume = progress / 100f;
            rainMediaPlayer.setVolume(volume, volume);
        }
    }

    // Set volume for coffee shop sound
    private void setCoffeeShopVolume(int progress) {
        if (coffeeShopMediaPlayer != null && coffeeShopMediaPlayer.isPlaying()) {
            float volume = progress / 100f;
            coffeeShopMediaPlayer.setVolume(volume, volume);
        }
    }
}