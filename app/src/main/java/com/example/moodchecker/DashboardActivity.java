package com.example.moodchecker;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodchecker.adapter.TodoAdapter;
import com.example.moodchecker.model.TodoItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class DashboardActivity extends AppCompatActivity {
    private ImageButton flashcardsButton;
    private Button idCardProfileButton;
    private ImageButton playSoundButton;
    private TextView moodMessageTextView;
    private RecyclerView todoRecyclerView;
    private TodoAdapter todoAdapter;
    private List<TodoItem> todoList;
    private Button addTaskButton;
    private Button removeTaskButton;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Firestore initialization
        db = FirebaseFirestore.getInstance();

        // Assume the user is authenticated (use Firebase Auth)
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        moodMessageTextView = findViewById(R.id.moodMessageTextView);
        flashcardsButton = findViewById(R.id.flashcardsButton);
        idCardProfileButton = findViewById(R.id.idCardProfileButton);
        playSoundButton = findViewById(R.id.playSoundButton);
        addTaskButton = findViewById(R.id.addTaskButton);
        removeTaskButton = findViewById(R.id.removeTaskButton);

        // Define lists of messages for each mood
        List<String> tiredMessages = Arrays.asList(
                "You've been giving it your all! A short break might be just what you need to recharge and come back stronger.",
                "Tiredness is a sign of effort! Take a moment to rest and gather your energy—you deserve it.",
                "Feeling drained? Stretch, sip some water, and let yourself reset before diving back in.",
                "You’ve been working hard—your body and mind might need a breather. Take care of yourself!",
                "Don’t forget to pause and rejuvenate. Even a 5-minute break can make a difference.",
                "Pace yourself—it’s okay to slow down and rest when needed. You’re doing great.",
                "A quick power nap or relaxing activity might help you bounce back with full energy!",
                "Rest isn’t a luxury; it’s essential. Take a moment to breathe deeply and regain focus.",
                "Feeling worn out? A snack, some hydration, and a short pause can help you refuel.",
                "You’re doing an amazing job! Be kind to yourself and give your body the rest it needs."
        );

        List<String> happyMessages = Arrays.asList(
                "Your positivity is inspiring! Share your joy and let it brighten someone else’s day.",
                "What an awesome feeling! Celebrate this moment—you’ve earned it.",
                "Happiness looks good on you! Keep spreading those good vibes wherever you go.",
                "Hold onto this joy and use it as fuel for the rest of your day!",
                "It’s amazing to see you so cheerful! Whatever’s making you smile, cherish it.",
                "Keep radiating that happiness! It’s contagious in the best way.",
                "Being happy is a treasure. Savor it and let it boost your day even more.",
                "Your good mood can light up a room! Share the love and positivity with those around you.",
                "Moments like this remind us how wonderful life can be. Hold onto this joy tightly!",
                "A happy heart is a powerful thing! Keep shining and enjoying the little things."
        );

        List<String> sickMessages = Arrays.asList(
                "Take it easy today—your health is your priority. Rest up and let yourself heal.",
                "Feeling under the weather? Stay cozy, hydrate, and give yourself the care you need.",
                "Rest is your best medicine. Take it slow and let your body recover.",
                "Sending you healing vibes! Relax, take it easy, and focus on feeling better.",
                "Don’t push yourself too hard. Rest, recover, and remember, it’s okay to take time for yourself.",
                "Listen to your body—it knows what it needs. Wishing you a quick recovery!",
                "Get plenty of rest and let your body do the work to bounce back.",
                "Take things one step at a time and prioritize your well-being. Hope you feel better soon!",
                "A sick day is a self-care day. Give yourself all the TLC you deserve.",
                "It’s okay to pause when you’re feeling unwell. Rest up, and you’ll bounce back in no time."
        );

        List<String> calmMessages = Arrays.asList(
                "Enjoy this serene moment—it’s perfect for clear thoughts and steady progress.",
                "Your calmness is a strength. Use it to stay focused and centered.",
                "Peace of mind is a gift—embrace it and let it guide you through your day.",
                "You’re in a great headspace! Use this tranquility to tackle tasks with ease.",
                "Feeling calm is a superpower. Use it to stay balanced and steady.",
                "This sense of calm is your anchor—stay grounded and enjoy this moment.",
                "A calm mind is a creative mind. Let your ideas flow freely!",
                "Feeling at peace? Use this clarity to take things one step at a time.",
                "Tranquility is the foundation of success. Keep riding this wave of calm.",
                "Your peaceful energy is inspiring! Carry it with you and make the most of it."
        );

        Intent intent = getIntent();
        String selectedMood = intent.getStringExtra("selectedMood");
//        String moodMessage = intent.getStringExtra("moodMessage");

        if (selectedMood != null) {
            Random random = new Random();
            String moodMessage = "";

            switch (selectedMood) {
                case "Tired":
                    moodMessage = tiredMessages.get(random.nextInt(tiredMessages.size()));
                    break;
                case "Happy":
                    moodMessage = happyMessages.get(random.nextInt(happyMessages.size()));
                    break;
                case "Sick":
                    moodMessage = sickMessages.get(random.nextInt(sickMessages.size()));
                    break;
                case "Calm":
                    moodMessage = calmMessages.get(random.nextInt(calmMessages.size()));
                    break;
                default:
                    moodMessage = "Welcome to your dashboard!";
                    break;
            }
            moodMessageTextView.setText(moodMessage);

        } else {
            moodMessageTextView.setText("Welcome to your dashboard!");
        }

        todoRecyclerView = findViewById(R.id.todoRecyclerView);
        todoRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data
        todoList = new ArrayList<>();

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
                Intent profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);
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

                            if (taskName.isEmpty()) {
                                // Show an alert if the task name is empty
                                new AlertDialog.Builder(DashboardActivity.this)
                                        .setTitle("Error")
                                        .setMessage("Please enter a task name.")
                                        .setPositiveButton("OK", null)
                                        .show();

//                                AlertDialog dialogx = builder.create();
//                                dialogx.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
//
//                                dialogx.show();

                                return;  // Don't proceed to save the task
                            }

                            String status = statusSpinner.getSelectedItem().toString();
                            String deadline = deadlineTextView.getText().toString();

                            boolean taskNameExists = false;
                            for (TodoItem task : todoList) {
                                if (task.getName().equalsIgnoreCase(taskName)) {
                                    taskNameExists = true;
                                    break;
                                }
                            }

                            if (taskNameExists) {
                                // Show an alert dialog if the task name already exists
                                new AlertDialog.Builder(DashboardActivity.this)
                                        .setTitle("Error")
                                        .setMessage("Task name must be unique!")
                                        .setPositiveButton("OK", null)
                                        .show();
                            } else {

                                TodoItem newTask = new TodoItem(taskName, status, deadline, null); // Set id as null initially

                                // Add the task to Firestore
                                db.collection("users")
                                        .document(userId) // User's unique document
                                        .collection("tasks") // Subcollection for tasks
                                        .add(newTask)
                                        .addOnSuccessListener(documentReference -> {
                                            // Assign the generated id to the task after it's added to Firestore
                                            newTask.setId(documentReference.getId());

                                            // Add task to the local list and update RecyclerView
                                            todoList.add(newTask);
                                            todoAdapter.notifyItemInserted(todoList.size() - 1);
                                            todoRecyclerView.scrollToPosition(todoList.size() - 1);

                                            Log.d("Firestore", "Task added successfully: " + documentReference.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error adding task", e);
                                            new AlertDialog.Builder(DashboardActivity.this)
                                                    .setTitle("Error")
                                                    .setMessage("Failed to save the task. Please try again.")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                        });
                            }
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

                    // Disable past dates
                    calendar.set(year, month, day);
                    datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis()); // This disables all past dates.

                    // Show the DatePickerDialog
                    datePickerDialog.show();
                });
        });

        removeTaskButton.setOnClickListener(v -> {
            if (!todoList.isEmpty()) {
                int lastPosition = todoList.size() - 1;
                todoList.remove(lastPosition);
                todoAdapter.notifyItemRemoved(lastPosition);
            }
        });

        db.collection("users")
                .document(userId)
                .collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        TodoItem task = document.toObject(TodoItem.class);
                        todoList.add(task);
                    }
                    todoAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching tasks", e));

    }

}
