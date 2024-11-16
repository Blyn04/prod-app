package com.example.moodchecker;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView dateTextView;
    private TextView moodQuestion;
    private MediaPlayer mediaPlayer;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("moods");

        dateTextView = findViewById(R.id.dateTextView);
        moodQuestion = findViewById(R.id.moodQuestion);

        String currentDate = new SimpleDateFormat("dd MMMM", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        // Set up mood buttons
        setupMoodButton(R.id.sleepyButton, "Tired", R.raw.yawns, "Looks like you've been working hard! Remember to take breaks and stay hydrated. A quick walk or some deep breaths can help refresh you.");
        setupMoodButton(R.id.sickButton, "Sick", R.raw.sneeze, "Sorry, you're not feeling well. Rest as much as possible and don’t push yourself too hard. Your health comes first!");
        setupMoodButton(R.id.calmButton, "Calm", R.raw.sparkle, "Feeling calm is a gift! Use this moment to focus and enjoy a productive, peaceful study session. Stay grounded and keep up the great work.");
        setupMoodButton(R.id.happyButton, "Happy", R.raw.horn, "We’re glad to see you’re feeling great! Keep that positive energy going and spread it to those around you. If something made your day, keep it close to heart!");
    }

    private void setupMoodButton(int buttonId, final String mood, final int soundResourceId, final String moodMessage) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(soundResourceId);
                saveMoodToFirestore(mood);

                Intent intent = new Intent(MainActivity.this, MoodPage.class);
                intent.putExtra("selectedMood", mood);
                intent.putExtra("moodMessage", moodMessage);
                startActivity(intent);
            }
        });
    }

    private void playSound(int soundResourceId) {
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            mediaPlayer = MediaPlayer.create(this, soundResourceId);
            mediaPlayer.start();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                mediaPlayer = null;
            }
        });
    }

    private void saveMoodToFirestore(String mood) {
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());

        // Get the current user's UID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get Firestore instance and reference to the user's moods
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference moodRef = db.collection("users").document(userId).collection("moods").document();

        // Create a mood data object
        HashMap<String, String> moodData = new HashMap<>();
        moodData.put("date", currentDate);
        moodData.put("mood", mood);

        // Save mood data to Firestore
        moodRef.set(moodData)
                .addOnSuccessListener(aVoid -> moodQuestion.setText("Mood saved: " + mood))
                .addOnFailureListener(e -> moodQuestion.setText("Failed to save mood"));
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
