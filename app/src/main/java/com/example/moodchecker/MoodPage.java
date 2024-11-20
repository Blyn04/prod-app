package com.example.moodchecker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MoodPage extends AppCompatActivity {
    private TextView moodTitleTextView;
    private TextView moodMessageTextView;
    private TextView moodStatusTextView;
    private Button continueButton;
    private ImageView nadzFaceImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_page);

        moodTitleTextView = findViewById(R.id.moodTitleTextView);
        moodMessageTextView = findViewById(R.id.moodMessageTextView);
        moodStatusTextView = findViewById(R.id.moodStatusTextView);
        continueButton = findViewById(R.id.continueButton);
        nadzFaceImageView = findViewById(R.id.nadzface);

        // Get the mood and message passed from MainActivity
        Intent intent = getIntent();
        String mood = intent.getStringExtra("selectedMood");
        String message = intent.getStringExtra("moodMessage");

        // Set mood and message in the UI
        moodTitleTextView.setText("You're feeling..");
        moodStatusTextView.setText(mood.toUpperCase());
        moodMessageTextView.setText(message);

        changeMoodImage(mood);

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

    private void changeMoodImage(String mood) {
        int imageResId = 0;

        switch (mood.toLowerCase()) {
            case "happy":
                imageResId = R.drawable.happy; // happy.png
                break;
            case "calm":
                imageResId = R.drawable.calm; // calm.png
                break;
            case "sick":
                imageResId = R.drawable.sick; // sick.png
                break;
            case "tired":
                imageResId = R.drawable.tired; // tired.png
                break;
            default:
                // Set a default image if the mood is not recognized
                imageResId = R.drawable.happy; // You can replace with a default image if needed
                break;
        }

        // Set the ImageView's srcCompat to the correct image
        nadzFaceImageView.setImageResource(imageResId);
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
