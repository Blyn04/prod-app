package com.example.moodchecker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodchecker.adapter.TodoAdapter;
import com.example.moodchecker.model.TodoItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private Button flashcardsButton;
    private Button idCardProfileButton;
    private Button playSoundButton;
    private TextView moodMessageTextView;
    private RecyclerView todoRecyclerView;
    private TodoAdapter todoAdapter;
    private List<TodoItem> todoList;
    private Button addTaskButton;
    private Button removeTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        moodMessageTextView = findViewById(R.id.moodMessageTextView);
        flashcardsButton = findViewById(R.id.flashcardsButton);
        idCardProfileButton = findViewById(R.id.idCardProfileButton);
        playSoundButton = findViewById(R.id.playSoundButton);
        addTaskButton = findViewById(R.id.addTaskButton);
        removeTaskButton = findViewById(R.id.removeTaskButton);

        Intent intent = getIntent();
        String selectedMood = intent.getStringExtra("selectedMood");
        String moodMessage = intent.getStringExtra("moodMessage");

        if (selectedMood != null) {
            switch (selectedMood) {
                case "Tired":
                    moodMessageTextView.setText("Take a 10-minute break to recharge, then try active recall with a few flashcards. If you're still tired, break your study into smaller chunks.");
                    break;
                case "Happy":
                    moodMessageTextView.setText("Use your positive energy to tackle something challenging—create a new reviewer or review a difficult flashcard. It’ll help you retain the info better!");
                    break;
                case "Sick":
                    moodMessageTextView.setText("Rest up, but you can still stay engaged. Review easy flashcards or listen to our lofi calm music to keep your mind active without overdoing it.");
                    break;
                case "Calm":
                    moodMessageTextView.setText("Use this focused energy for spaced repetition with flashcards, or review complex topics at a steady pace to fully absorb the material.");
                    break;
                default:
                    moodMessageTextView.setText("Welcome to your dashboard!");
                    break;
            }
        } else {
            moodMessageTextView.setText("Welcome to your dashboard!");
        }

        todoRecyclerView = findViewById(R.id.todoRecyclerView);
        todoRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data
        todoList = new ArrayList<>();
        todoList.add(new TodoItem("Networking HW#1", "In Progress", "09/12/24"));
        todoList.add(new TodoItem("Networking HW#1", "Complete", "09/12/24"));
        todoList.add(new TodoItem("Networking HW#1", "Not Started", "09/12/24"));
        todoList.add(new TodoItem("Networking HW#1", "Not Started", "09/12/24"));
        todoList.add(new TodoItem("Networking HW#1", "In Progress", "09/12/24"));

        // Set adapter
        todoAdapter = new TodoAdapter(todoList);
        todoRecyclerView.setAdapter(todoAdapter);

        flashcardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Flashcards Activity
                Intent flashcardsIntent = new Intent(DashboardActivity.this, FlashcardsActivity.class);
                startActivity(flashcardsIntent);
            }
        });

        idCardProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ID Card Profile Activity
                Intent profileIntent = new Intent(DashboardActivity.this, IDCardProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        playSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(DashboardActivity.this, SoundPageActivity.class);
                startActivity(profileIntent);
            }
        });

        addTaskButton.setOnClickListener(v -> {
            // Inflate the custom layout
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

            // Initialize the dialog fields
            EditText taskNameEditText = dialogView.findViewById(R.id.taskNameEditText);
            Spinner statusSpinner = dialogView.findViewById(R.id.statusSpinner);
            TextView deadlineTextView = dialogView.findViewById(R.id.deadlineTextView);  // Updated to TextView for date picker
            EditText hoursEditText = dialogView.findViewById(R.id.hoursEditText);
            EditText minutesEditText = dialogView.findViewById(R.id.minutesEditText);
            EditText secondsEditText = dialogView.findViewById(R.id.secondsEditText);
            Button startTimerButton = dialogView.findViewById(R.id.startTimerButton);
            Button stopTimerButton = dialogView.findViewById(R.id.stopTimerButton);

            // Set up the spinner with status options
            ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, new String[]{"Not Started", "In Progress", "Complete"});
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusSpinner.setAdapter(statusAdapter);

            // Set up the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView)
                    .setTitle("Add Task")
                    .setPositiveButton("Save", (dialog, which) -> {
                        // Get input values
                        String taskName = taskNameEditText.getText().toString();
                        String status = statusSpinner.getSelectedItem().toString();
                        String deadline = deadlineTextView.getText().toString();

                        // Add a new task to the list
                        todoList.add(new TodoItem(taskName, status, deadline));
                        todoAdapter.notifyItemInserted(todoList.size() - 1);
                        todoRecyclerView.scrollToPosition(todoList.size() - 1);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            // Show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();

            // Set up the DatePickerDialog for the deadline
            deadlineTextView.setOnClickListener(deadlineView -> {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(DashboardActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            // Format the selected date
                            String selectedDate = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            deadlineTextView.setText(selectedDate);
                        }, year, month, day);
                datePickerDialog.show();
            });

            // Timer button actions (optional, implement your timer functionality here)
            startTimerButton.setOnClickListener(timerView -> {
                // Start timer logic
            });

            stopTimerButton.setOnClickListener(timerView -> {
                // Stop timer logic
            });
        });


        removeTaskButton.setOnClickListener(v -> {
            if (!todoList.isEmpty()) {
                int lastPosition = todoList.size() - 1;
                todoList.remove(lastPosition);
                todoAdapter.notifyItemRemoved(lastPosition);
            }
        });
    }

}
