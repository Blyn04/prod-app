package com.example.moodchecker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.moodchecker.model.Flashcard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewFlashcardsActivity extends AppCompatActivity {

    private List<Flashcard> flashcards;
    private int currentIndex = 0;
    private RadioGroup optionsGroup;
    private TextView questionNumberTextView;
    private TextView questionTextView;
    private RadioButton optionOne, optionTwo, optionThree, optionFour;
    private TextView streakTextView;
    private int streakCount = 0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flashcards);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid(); // Make sure to authenticate users

        loadStreakFromFirebase();

        // Initialize flashcards list from intent
        flashcards = (List<Flashcard>) getIntent().getSerializableExtra("flashcardsList");
        if (flashcards == null) {
            flashcards = new ArrayList<>();
        }

        questionNumberTextView = findViewById(R.id.questionNumber);
        questionTextView = findViewById(R.id.questionText);
        optionOne = findViewById(R.id.optionOne);
        optionTwo = findViewById(R.id.optionTwo);
        optionThree = findViewById(R.id.optionThree);
        optionFour = findViewById(R.id.optionFour);
        optionsGroup = findViewById(R.id.optionsGroup);
        streakTextView = findViewById(R.id.streakTextView);

        Button backButton = findViewById(R.id.backButton);
        Button nextButton = findViewById(R.id.nextButton);
        Button submitButton = findViewById(R.id.submitButton);

        // Display the first question
        displayFlashcard(currentIndex);

        // Handle Next button
        nextButton.setOnClickListener(view -> {
            if (currentIndex < flashcards.size() - 1) {
                currentIndex++;
                Flashcard flashcard = flashcards.get(currentIndex);
                flashcard.setAnswered(false); // Reset the answer status
                flashcard.setCorrect(false);  // Reset correctness
                displayFlashcard(currentIndex);
            }
        });

        // Handle Back button
        backButton.setOnClickListener(view -> {
            if (currentIndex > 0) {
                currentIndex--;
                displayFlashcard(currentIndex);
            }
        });

        submitButton.setOnClickListener(view -> {
            Flashcard flashcard = flashcards.get(currentIndex);
            String correctAnswer = flashcard.getCorrectAnswer();
            String answer = flashcard.getAnswer();

            if (flashcard.isAnswered() && flashcard.isCorrect()) {
                // Don't allow resubmission if the answer was correct
                Toast.makeText(ViewFlashcardsActivity.this, "You have already answered this question correctly!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (flashcard.getAnswerType().equals("Multiple Choice")) {
                String selectedAnswer = getSelectedOption();

                if (selectedAnswer != null) {
                    if (selectedAnswer.equals(correctAnswer)) {
                        streakCount++;
                        flashcard.setCorrect(true); // Mark as correct
                        saveStreakToFirebase(streakCount);

                        flashcard.setAnswered(true);
                        updateStreakDisplay();

                        goToNextQuestion();

                        Toast.makeText(ViewFlashcardsActivity.this, "Correct!", Toast.LENGTH_SHORT).show();
                    } else {
                        streakCount = 0;
                        flashcard.setCorrect(false); // Mark as incorrect
                        saveStreakToFirebase(streakCount);

                        updateStreakDisplay();
                        flashcard.setAnswered(true);

                        Toast.makeText(ViewFlashcardsActivity.this, "Incorrect. The correct answer is: " + correctAnswer, Toast.LENGTH_SHORT).show();
                    }
//                    flashcard.setAnswered(true);
//                    updateStreakDisplay();

                } else {
                    Toast.makeText(ViewFlashcardsActivity.this, "Please select an answer.", Toast.LENGTH_SHORT).show();
                }

            } else if (flashcard.getAnswerType().equals("Short Text")) {
                EditText shortTextInput = findViewById(R.id.shortTextInput);
                String userAnswer = shortTextInput.getText().toString().trim();

                if (!userAnswer.isEmpty()) {
                    if (userAnswer.equalsIgnoreCase(answer)) {
                        streakCount++;
                        flashcard.setCorrect(true); // Mark as correct
                        saveStreakToFirebase(streakCount);

                        flashcard.setAnswered(true);
                        updateStreakDisplay();
                        goToNextQuestion();

                        Toast.makeText(ViewFlashcardsActivity.this, "Correct!", Toast.LENGTH_SHORT).show();

                    } else {
                        streakCount = 0;
                        flashcard.setCorrect(false); // Mark as incorrect
                        saveStreakToFirebase(streakCount);

                        updateStreakDisplay();
                        flashcard.setAnswered(true);

                        Toast.makeText(ViewFlashcardsActivity.this, "Incorrect. The correct answer is: " + answer, Toast.LENGTH_SHORT).show();
                    }
//                    flashcard.setAnswered(true);
//                    updateStreakDisplay();
                } else {
                    Toast.makeText(ViewFlashcardsActivity.this, "Please enter an answer.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToNextQuestion() {
        if (currentIndex < flashcards.size() - 1) {
            currentIndex++;
            Flashcard flashcard = flashcards.get(currentIndex);
            flashcard.setAnswered(false); // Reset the answer status for the new question
            flashcard.setCorrect(false);  // Reset correctness
            displayFlashcard(currentIndex); // Display the next question
        } else {
            // If no more questions, show a message or finish the activity
            Toast.makeText(ViewFlashcardsActivity.this, "You've completed all the questions!", Toast.LENGTH_SHORT).show();
        }
    }


    private void displayFlashcard(int index) {
        if (index < flashcards.size()) {
            Flashcard flashcard = flashcards.get(index);
            questionNumberTextView.setText("QUESTION " + String.format("%02d", index + 1));
            questionTextView.setText(flashcard.getQuestion());

            List<String> options = flashcard.getOptions();

            if (flashcard.getAnswerType().equals("Multiple Choice")) {
                optionsGroup.setVisibility(View.VISIBLE);

                if (options != null && options.size() > 0) {
                    optionOne.setText(options.get(0));
                    optionOne.setVisibility(View.VISIBLE);
                } else {
                    optionOne.setVisibility(View.GONE);
                }

                if (options.size() > 1) {
                    optionTwo.setText(options.get(1));
                    optionTwo.setVisibility(View.VISIBLE);
                } else {
                    optionTwo.setVisibility(View.GONE);
                }

                if (options.size() > 2) {
                    optionThree.setText(options.get(2));
                    optionThree.setVisibility(View.VISIBLE);
                } else {
                    optionThree.setVisibility(View.GONE);
                }

                if (options.size() > 3) {
                    optionFour.setText(options.get(3));
                    optionFour.setVisibility(View.VISIBLE);
                } else {
                    optionFour.setVisibility(View.GONE);
                }

                findViewById(R.id.shortTextInput).setVisibility(View.GONE);

            } else if (flashcard.getAnswerType().equals("Short Text")) {
                optionsGroup.setVisibility(View.GONE);

                findViewById(R.id.shortTextInput).setVisibility(View.VISIBLE);
                EditText shortTextInput = findViewById(R.id.shortTextInput);
                shortTextInput.setText("");
            }
        }
    }

    private String getSelectedOption() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            return selectedRadioButton.getText().toString();
        }
        return null;
    }

    private void updateStreakDisplay() {
        streakTextView.setText("Streak: " + streakCount);

        // Show specific message based on the streak count
        String message = "";

        switch (streakCount) {
            case 10:
                message = "You're doing great! Keep up the momentum!";
                break;
            case 20:
                message = "Fantastic progress! Your hard work is paying off!";
                break;
            case 30:
                message = "Impressive! You’re mastering this material!";
                break;
            case 40:
                message = "Amazing focus! You’re well on your way to acing this.";
                break;
            case 50:
                message = "Halfway to 100! Keep that knowledge growing!";
                break;
            case 60:
                message = "Incredible dedication! You're truly committed.";
                break;
            case 70:
                message = "You're unstoppable! Just a few more to hit 100!";
                break;
            case 80:
                message = "Your effort is inspiring! Keep pushing forward.";
                break;
            case 90:
                message = "Almost at 100! You're a reviewing champion!";
                break;
            case 100:
                message = "Congratulations on 100! Your dedication is remarkable!";
                break;
            default:
                message = "";
                break;
        }

        // If a message is available for the streak, show it
        if (!message.isEmpty()) {
            showStreakPopup(streakCount, message);
        }
    }

    private void showStreakPopup(int streak, String message) {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_streak, null);

        // Create the dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Customize dialog elements
        TextView streakMessage = dialogView.findViewById(R.id.streakMessage);
        TextView streakCountMessage = dialogView.findViewById(R.id.streakCountMessage);
        Button okButton = dialogView.findViewById(R.id.okButton);

        // Update the dialog message based on streak count
        streakCountMessage.setText("You've reached a streak of " + streak + "!");
        streakMessage.setText(message);

        // Show the dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Handle the OK button click
        okButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void saveStreakToFirebase(int streakCount) {
        // Get the current user's UID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get Firestore instance and reference to the user's streaks
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference streakRef = db.collection("users").document(userId).collection("streaks").document();

        // Create a streak data object
        HashMap<String, Object> streakData = new HashMap<>();
        streakData.put("streak", streakCount);

        // Save streak data to Firestore
        streakRef.set(streakData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Streak updated successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error updating streak", e));
    }


    private void loadStreakFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

//        db.collection("users")
//                .document(userId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        Integer savedStreak = documentSnapshot.getLong("streak").intValue();
//                        streakCount = savedStreak; // Set the streak from Firebase
//                        updateStreakDisplay(); // Update UI
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Handle failure to retrieve streak
//                    Log.w("Firestore", "Error getting streak", e);
//                });

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the streak value safely
                        Long savedStreakLong = documentSnapshot.getLong("streak");

                        if (savedStreakLong != null) {
                            streakCount = savedStreakLong.intValue(); // If valid, set it
                        } else {
                            streakCount = 0; // Default to 0 if the streak is missing
                        }

                        updateStreakDisplay(); // Update UI
                    } else {
                        // Handle case where the document does not exist
                        streakCount = 0; // Default to 0 if the user document does not exist
                        updateStreakDisplay();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to retrieve streak
                    Log.w("Firestore", "Error getting streak", e);
                });

    }
}
