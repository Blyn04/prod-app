package com.example.moodchecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SoundPageActivity extends AppCompatActivity {
    private Button focusButton, calmButton, studyButton, stopButton;
    private Switch rainSoundSwitch, coffeeShopSwitch;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_page);

        soundManager = new SoundManager();

        // Initialize buttons and switches
        focusButton = findViewById(R.id.focusButton);
        calmButton = findViewById(R.id.calmButton);
        studyButton = findViewById(R.id.studyButton);
        rainSoundSwitch = findViewById(R.id.rainSoundSwitch);
        coffeeShopSwitch = findViewById(R.id.coffeeShopSwitch);
        stopButton = findViewById(R.id.stopButton);

        // Load saved preferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean rainSoundState = sharedPreferences.getBoolean("rainSoundState", false);
        boolean coffeeSoundState = sharedPreferences.getBoolean("coffeeSoundState", false);

        // If rain sound was enabled previously, start it again (if it's not already playing)
        if (rainSoundState) {
            soundManager.playBackgroundSound(this, R.raw.rain);
        }

        rainSoundSwitch.setChecked(rainSoundState);
        coffeeShopSwitch.setChecked(coffeeSoundState);

        // Button click listeners for main sounds
        focusButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.focus));
        calmButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.calm));
        studyButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.study));

        // Rain sound toggle
        rainSoundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.rain);
            } else {
                soundManager.stopAllSounds(); // Stop all sounds when rain sound is toggled off
            }
            saveSoundPreferences(); // Save the state of rain sound
        });

        // Coffee shop sound toggle
        coffeeShopSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.coffee);
            } else {
                soundManager.stopAllSounds(); // Stop all sounds when coffee sound is toggled off
            }
            saveSoundPreferences(); // Save the state of coffee shop sound
        });

        // Stop button click listener
        stopButton.setOnClickListener(v -> {
            soundManager.stopAllSounds(); // Stop all sounds when the stop button is clicked

            // Update preferences to reflect that no sound is playing
            SharedPreferences.Editor editor = sharedPreferences.edit(); // Use the already defined sharedPreferences
            editor.putBoolean("rainSoundState", false); // Set to false since rain sound is stopped
            editor.putBoolean("coffeeSoundState", false); // Set to false since coffee sound is stopped
            editor.apply();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if any sound is playing (but don't stop them, just ensure they continue if previously playing)
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean rainSoundState = sharedPreferences.getBoolean("rainSoundState", false);
        boolean coffeeSoundState = sharedPreferences.getBoolean("coffeeSoundState", false);

        // If the rain sound was enabled previously, keep it playing
        if (rainSoundState) {
            soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.rain);
        }

        // If the coffee shop sound was enabled previously, keep it playing
        if (coffeeSoundState) {
            soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.coffee);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Keep sounds playing when navigating away from the SoundPageActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up sounds on activity destroy
        soundManager.stopAllSounds();
    }

    // Save sound preferences to SharedPreferences
    private void saveSoundPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the state of the switches (rain sound and coffee shop sound)
        editor.putBoolean("rainSoundState", rainSoundSwitch.isChecked());
        editor.putBoolean("coffeeSoundState", coffeeShopSwitch.isChecked());

        editor.apply();
    }

    public void navigateBack(View view) {
        Intent intent = new Intent(SoundPageActivity.this, DashboardActivity.class);
        startActivity(intent);
    }
}
