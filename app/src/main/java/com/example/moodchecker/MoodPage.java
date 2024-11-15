package com.example.moodchecker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MoodPage extends AppCompatActivity {
    private TextView moodTitleTextView;
    private TextView moodMessageTextView;
    private TextView moodStatusTextView;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_page);

        moodTitleTextView = findViewById(R.id.moodTitleTextView);
        moodMessageTextView = findViewById(R.id.moodMessageTextView);
        moodStatusTextView = findViewById(R.id.moodStatusTextView);
        continueButton = findViewById(R.id.continueButton);

        // Get the mood and message passed from MainActivity
        Intent intent = getIntent();
        String mood = intent.getStringExtra("selectedMood");
        String message = intent.getStringExtra("moodMessage");

        // Set mood and message in the UI
        moodTitleTextView.setText("You're feeling..");
        moodStatusTextView.setText(mood.toUpperCase());
        moodMessageTextView.setText(message);

        // Set up the continue button click listener to navigate to the Dashboard
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to DashboardActivity with the selected mood
                Intent dashboardIntent = new Intent(MoodPage.this, DashboardActivity.class);
                dashboardIntent.putExtra("selectedMood", mood);
                startActivity(dashboardIntent);
            }
        });
    }

    // Optionally, you can keep the method for setting up other mood buttons if needed.
    private void setupMoodButton(int buttonId, final String mood) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to DashboardActivity with the selected mood
                Intent intent = new Intent(MoodPage.this, DashboardActivity.class);
                intent.putExtra("selectedMood", mood);
                startActivity(intent);
            }
        });
    }
}
