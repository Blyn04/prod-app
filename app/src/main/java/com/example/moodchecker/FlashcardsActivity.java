package com.example.moodchecker;

import android.content.Intent;
import android.os.Bundle;
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

                if (!question.isEmpty() && !answer.isEmpty()) {
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

                    // Clear the input fields after adding the flashcard
                    questionInput.setText("");
                    answerInput.setText("");
                    shortTextInput.setText("");  // Clear short text input

                    Toast.makeText(this, "Flashcard added", Toast.LENGTH_SHORT).show();

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
    }
}
