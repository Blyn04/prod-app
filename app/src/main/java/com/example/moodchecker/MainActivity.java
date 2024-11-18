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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView dateTextView;
    private TextView moodQuestion;
    private MediaPlayer mediaPlayer;
    private DatabaseReference databaseReference;

    private final List<String> tiredMessages = Arrays.asList(
            "Feeling tired? It's okay to pause. Even a short break can recharge your mind and body.",
            "Exhausted? Remember, rest isn’t a luxury—it’s a necessity. Take time for yourself.",
            "You’ve been working so hard! Don’t forget to breathe and give yourself some downtime.",
            "It’s natural to feel worn out. Grab some water, stretch, and refocus when you’re ready.",
            "Fatigue catching up? Prioritize sleep and relaxation—you deserve it.",
            "Sometimes the best thing you can do is stop, rest, and recharge. You've earned it!",
            "Tiredness is a sign to slow down. Listen to your body and let it recover.",
            "Your hard work is admirable, but balance is key. Rest now so you can thrive later.",
            "Even superheroes need rest. Take it easy, and let yourself unwind.",
            "Burnout isn’t worth it. Rest today so you can shine tomorrow."
    );

    private final List<String> happyMessages = Arrays.asList(
            "Happiness looks amazing on you! Keep the positivity flowing and make today unforgettable.",
            "Your smile is shining bright! Treasure this joyful moment and share it with others.",
            "Feeling happy is a gift. Celebrate it and let it uplift your day even more.",
            "Keep riding this wave of joy! Moments like this are what life’s all about.",
            "Happiness is contagious—spread it far and wide! You never know who might need it.",
            "This kind of positivity is worth celebrating. Hold on to it and enjoy every bit of it.",
            "You’re glowing with good vibes! Take a moment to appreciate what’s making you smile.",
            "Happiness is energy—use it to power through your goals and inspire others!",
            "Smiling suits you! Enjoy this moment and create memories you’ll cherish.",
            "You’ve found your happy place. Stay there as long as you can!"
    );

    private final List<String> sickMessages = Arrays.asList(
            "Rest up and take it easy. Your health is your top priority right now.",
            "Not feeling your best? That’s okay—allow yourself the time to heal and recover.",
            "You’re strong, but even the strongest need rest. Take care of yourself!",
            "Feeling sick can be tough, but this is your body’s way of asking for a break.",
            "Sending you warm wishes for a speedy recovery. Take all the rest you need!",
            "Listen to your body and don’t push yourself. You’ll feel better soon.",
            "Healing takes time—focus on resting and getting better day by day.",
            "Don’t forget to stay hydrated and nourished while you rest. You’ve got this!",
            "Even a small step toward recovery is progress. Hang in there—you’re on the mend.",
            "Take a deep breath and let yourself heal. You’ll feel like yourself again soon."
    );

    private final List<String> calmMessages = Arrays.asList(
            "Peaceful moments are precious. Take a deep breath and savor this calmness.",
            "Feeling calm is like hitting the reset button. Use it to focus and recharge.",
            "A calm mind can accomplish great things. Stay centered and trust the process.",
            "In this quiet moment, find strength and clarity to take on what’s next.",
            "Calmness brings perspective. Use this time to reflect and realign with your goals.",
            "Peace is a rare treasure—hold onto it and let it ground you.",
            "The calm you feel now can be your anchor through anything. Cherish it.",
            "Moments like these are for you to enjoy. Stay present and let the world fade away.",
            "Calmness is contagious. Share it with those around you and create harmony.",
            "This peaceful energy is your superpower. Let it guide you through your day."
    );


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
        setupMoodButton(R.id.sleepyButton, "Tired", R.raw.yawns, tiredMessages);
        setupMoodButton(R.id.sickButton, "Sick", R.raw.sneeze, sickMessages);
        setupMoodButton(R.id.calmButton, "Calm", R.raw.sparkle, calmMessages);
        setupMoodButton(R.id.happyButton, "Happy", R.raw.horn, happyMessages);
    }

    private void setupMoodButton(int buttonId, final String mood, final int soundResourceId, final List<String> moodMessages) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(soundResourceId);
                saveMoodToFirestore(mood);

                String moodMessage = getRandomMessage(moodMessages);

                Intent intent = new Intent(MainActivity.this, MoodPage.class);
                intent.putExtra("selectedMood", mood);
                intent.putExtra("moodMessage", moodMessage);
                startActivity(intent);
            }
        });
    }

    private String getRandomMessage(List<String> messages) {
        Random random = new Random();
        return messages.get(random.nextInt(messages.size()));
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
