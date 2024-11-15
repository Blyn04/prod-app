package com.example.moodchecker;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MoodHistoryActivity extends AppCompatActivity {

    private TextView moodHistoryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        moodHistoryTextView = findViewById(R.id.moodHistoryTextView);

        // Retrieve the saved mood data
        SharedPreferences sharedPreferences = getSharedPreferences("MoodPreferences", MODE_PRIVATE);
        String selectedMood = sharedPreferences.getString("selectedMood", "No mood selected");
        String moodDate = sharedPreferences.getString("moodDate", "N/A");

        // Display the mood history
        moodHistoryTextView.setText("Mood: " + selectedMood + "\nDate: " + moodDate);
    }
}