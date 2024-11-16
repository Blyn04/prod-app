package com.example.moodchecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SoundPageActivity extends AppCompatActivity {
    private Button focusButton, calmButton, studyButton, stopButton, startReviewingButton;
    private Switch rainSoundSwitch, coffeeShopSwitch;
    private SoundManager soundManager;

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
        startReviewingButton = findViewById(R.id.startReviewingButton);


        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean rainSoundState = sharedPreferences.getBoolean("rainSoundState", false);
        boolean coffeeSoundState = sharedPreferences.getBoolean("coffeeSoundState", false);

        if (rainSoundState) {
            soundManager.playBackgroundSound(this, R.raw.rain);
        }

        rainSoundSwitch.setChecked(rainSoundState);
        coffeeShopSwitch.setChecked(coffeeSoundState);

        focusButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.focus));
        calmButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.calm));
        studyButton.setOnClickListener(v -> soundManager.playMainSound(SoundPageActivity.this, R.raw.study));

        startReviewingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SoundPageActivity.this, ReviewerActivity.class);
                startActivity(intent);
            }
        });

        rainSoundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.rain);
            } else {
                soundManager.stopAllSounds();
            }
            saveSoundPreferences();
        });


        coffeeShopSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                soundManager.playBackgroundSound(SoundPageActivity.this, R.raw.coffee);
            } else {
                soundManager.stopAllSounds();
            }
            saveSoundPreferences();
        });

        stopButton.setOnClickListener(v -> {
            soundManager.stopAllSounds();


            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("rainSoundState", false);
            editor.putBoolean("coffeeSoundState", false);
            editor.apply();
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
}
