package com.example.moodchecker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodchecker.adapter.FlashcardAdapter;
import com.example.moodchecker.model.Flashcard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FlashcardsActivity extends AppCompatActivity {
    private List<Flashcard> flashcards;
    private FlashcardAdapter adapter;

    private EditText questionInput;
    private EditText answerInput;
    private Spinner answerTypeSpinner;
    private String selectedAnswerType;
    private EditText shortTextInput;  // For short text input

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);

        // Initialize flashcards list and RecyclerView
        flashcards = new ArrayList<>();

        questionInput = findViewById(R.id.questionInput);
        answerInput = findViewById(R.id.answerInput);
        answerTypeSpinner = findViewById(R.id.answerTypeSpinner);
        shortTextInput = findViewById(R.id.shortTextInput); // Get reference to the short text input field
        RecyclerView recyclerView = findViewById(R.id.flashcardsRecyclerView);
        Button addFlashcardButton = findViewById(R.id.addFlashcardButton);
        Button viewFlashcardsButton = findViewById(R.id.viewFlashcardsButton);

        // Get the reviewer ID from the Intent
        String reviewerId = getIntent().getStringExtra("reviewerId");

        // Setup RecyclerView with the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FlashcardAdapter(flashcards);
        recyclerView.setAdapter(adapter);

        // Setup Spinner with answer types
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.answer_types, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        answerTypeSpinner.setAdapter(spinnerAdapter);

        answerTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAnswerType = parent.getItemAtPosition(position).toString();
                if (selectedAnswerType.equals("Multiple Choice")) {
                    answerInput.setVisibility(View.VISIBLE);
                    shortTextInput.setVisibility(View.GONE);  // Hide short text input
                    answerInput.setHint("Enter options separated by commas");
                } else if (selectedAnswerType.equals("Short Text")) {
                    answerInput.setVisibility(View.GONE);  // Hide answer input
                    shortTextInput.setVisibility(View.VISIBLE);  // Show short text input
                    shortTextInput.setHint("Enter your short answer");
                } else {
                    answerInput.setVisibility(View.GONE);
                    shortTextInput.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedAnswerType = "Short Text";
            }
        });

        addFlashcardButton.setOnClickListener(view -> {
            if (flashcards.size() < 100) {
                String question = questionInput.getText().toString();
                String answer = selectedAnswerType.equals("Short Text") ? shortTextInput.getText().toString() : answerInput.getText().toString();

                // Check if the question already exists in the list
                boolean isDuplicate = false;
                for (Flashcard flashcard : flashcards) {
                    if (flashcard.getQuestion().equalsIgnoreCase(question)) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    Toast.makeText(this, "This question already exists. Please enter a unique question.", Toast.LENGTH_SHORT).show();
                } else if (!question.isEmpty() && !answer.isEmpty()) {
                    List<String> options = new ArrayList<>();
                    String correctAnswer = "";

                    // For Multiple Choice questions, split options by commas
                    if (selectedAnswerType.equals("Multiple Choice")) {
                        String[] optionArray = answer.split(",");
                        for (String option : optionArray) {
                            options.add(option.trim());
                        }
                        correctAnswer = "yes";  // Hardcoded correct answer for now (could be dynamic based on user input)

                    } else {
                        options.add(answer);  // For short text questions, the answer is the correct one
                        correctAnswer = answer;
                    }

                    // Create a new Flashcard and add it to the list
                    Flashcard newFlashcard = new Flashcard(question, answer, selectedAnswerType, options);
                    newFlashcard.setCorrectAnswer(correctAnswer);  // Set the correct answer
                    flashcards.add(newFlashcard);


                    // Notify the adapter to update the RecyclerView
                    adapter.notifyDataSetChanged();

//                    // Clear the input fields after adding the flashcard
//                    questionInput.setText("");
//                    answerInput.setText("");
//                    shortTextInput.setText("");  // Clear short text input
//
//                    Toast.makeText(this, "Flashcard added", Toast.LENGTH_SHORT).show();

                    // Firestore reference
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Assuming you're using Firebase Auth
                    DocumentReference flashcardsRef = db.collection("users")
                            .document(userId)
                            .collection("reviewers")
                            .document(reviewerId)
                            .collection("flashcards")
                            .document();

                    flashcardsRef.set(newFlashcard)
                            .addOnSuccessListener(aVoid -> {
                                flashcards.add(newFlashcard);  // Add to local list
                                adapter.notifyDataSetChanged(); // Update UI

                                // Clear input fields
                                questionInput.setText("");
                                answerInput.setText("");
                                shortTextInput.setText("");

                                Toast.makeText(this, "Flashcard added successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to save flashcard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                } else {
                    Toast.makeText(this, "Please enter a question and answer", Toast.LENGTH_SHORT).show();
                }

            } else {
                // If the number of flashcards exceeds 100, show a warning
                Toast.makeText(this, "You can only add up to 100 flashcards", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate to view flashcards page
        viewFlashcardsButton.setOnClickListener(view -> {
            Intent intent = new Intent(FlashcardsActivity.this, ViewFlashcardsActivity.class);
            intent.putExtra("flashcardsList", (ArrayList<Flashcard>) flashcards);
            startActivity(intent);
        });

        if (reviewerId == null || reviewerId.isEmpty()) {
            Toast.makeText(this, "Reviewer ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        // Firestore reference to the reviewer's flashcards
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference flashcardsRef = db.collection("users")
                .document(userId)
                .collection("reviewer")
                .document(reviewerId)
                .collection("flashcards");

        // Initialize RecyclerView and FlashcardAdapter
        flashcards = new ArrayList<>();
        recyclerView = findViewById(R.id.flashcardsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FlashcardAdapter(flashcards);
        recyclerView.setAdapter(adapter);

        // Load flashcards from Firestore
        loadFlashcards(flashcardsRef);

        // Add Flashcard Button
//        findViewById(R.id.addFlashcardButton).setOnClickListener(view -> addFlashcard(flashcardsRef));
        findViewById(R.id.addFlashcardButton).setOnClickListener(view -> {
            String question = questionInput.getText().toString().trim();
            String answer = selectedAnswerType.equals("Short Text")
                    ? shortTextInput.getText().toString().trim()
                    : answerInput.getText().toString().trim();

            if (!question.isEmpty() && !answer.isEmpty()) {
                List<String> options = new ArrayList<>();

                if (selectedAnswerType.equals("Multiple Choice")) {
                    String[] optionArray = answer.split(",");
                    for (String option : optionArray) {
                        options.add(option.trim());
                    }
                } else {
                    options.add(answer); // For short text, the answer itself is the only option
                }

                addFlashcard(question, answer, selectedAnswerType, options); // Pass the parameters
            } else {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            }
        });


        // View Flashcards Button
        findViewById(R.id.viewFlashcardsButton).setOnClickListener(view -> {
            Intent intent = new Intent(FlashcardsActivity.this, ViewFlashcardsActivity.class);
            intent.putExtra("flashcardsList", (ArrayList<Flashcard>) flashcards);
            startActivity(intent);
        });
    }

//    private void loadFlashcards(CollectionReference flashcardsRef) {
//        flashcardsRef.addSnapshotListener((querySnapshot, e) -> {
//            if (e != null) {
//                Log.e("FlashcardsActivity", "Error loading flashcards", e);
//                return;
//            }
//
//            if (querySnapshot != null) {
//                flashcards.clear();
//                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
//                    Flashcard flashcard = document.toObject(Flashcard.class);
//                    flashcards.add(flashcard);
//                }
//                adapter.notifyDataSetChanged();
//            }
//        });

    private void loadFlashcards(CollectionReference flashcardsRef) {
        flashcardsRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FlashcardsActivity", "Loaded flashcards: " + querySnapshot.size());

                    // Clear the current list to avoid duplication
                    flashcards.clear();

                    // Process the documents and convert them to Flashcard objects
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Flashcard flashcard = document.toObject(Flashcard.class);
                        flashcards.add(flashcard);
                        Log.d("FlashcardsActivity", "Flashcard: " + flashcard.getQuestion());
                    }

                    // Notify the adapter to refresh the RecyclerView
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FlashcardsActivity", "Error loading flashcards", e);
                    Toast.makeText(this, "Failed to load flashcards: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addFlashcard(String question, String answer, String answerType, List<String> options) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get the current user's ID
            String reviewerId = getIntent().getStringExtra("reviewerId"); // Get the reviewer ID from Intent

            if (reviewerId == null) {
                Toast.makeText(this, "Error: Reviewer ID is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference flashcardsRef = db.collection("users")
                    .document(userId)
                    .collection("reviewer")
                    .document(reviewerId)
                    .collection("flashcards");

            // Create a new flashcard object
            Flashcard flashcard = new Flashcard(question, answer, answerType, options);

            // Save the flashcard to Firestore
            flashcardsRef.add(flashcard)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Flashcard added successfully!", Toast.LENGTH_SHORT).show();
                        loadFlashcards(flashcardsRef);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding flashcard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }


}
