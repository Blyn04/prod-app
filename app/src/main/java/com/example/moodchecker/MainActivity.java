package com.example.moodchecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView dateTextView;
    private TextView moodQuestion;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateTextView = findViewById(R.id.dateTextView);
        moodQuestion = findViewById(R.id.moodQuestion);

        sharedPreferences = getSharedPreferences("MoodPreferences", MODE_PRIVATE);

        // Set the date
        String currentDate = new SimpleDateFormat("dd MMMM", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        // Set mood selection buttons
        setupMoodButton(R.id.anxietyButton, "Anxiety");
        setupMoodButton(R.id.joyButton, "Joy");
        setupMoodButton(R.id.calmButton, "Calm");
        setupMoodButton(R.id.distractedButton, "Distracted");

        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MoodHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupMoodButton(int buttonId, final String mood) {
        Button moodButton = findViewById(buttonId);
        moodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMood(mood);
            }
        });
    }

    private void saveMood(String mood) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedMood", mood);
        editor.putString("moodDate", dateTextView.getText().toString());
        editor.apply();

        // Optional: Show a confirmation message
        moodQuestion.setText("Mood saved: " + mood);
    }

}