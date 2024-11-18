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
//        todoList.add(new TodoItem("Networking HW#1", "In Progress", "09/12/24"));
//        todoList.add(new TodoItem("Networking HW#1", "Complete", "09/12/24"));
//        todoList.add(new TodoItem("Networking HW#1", "Not Started", "09/12/24"));
//        todoList.add(new TodoItem("Networking HW#1", "Not Started", "09/12/24"));
//        todoList.add(new TodoItem("Networking HW#1", "In Progress", "09/12/24"));

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
//                EditText hoursEditText = dialogView.findViewById(R.id.hoursEditText);
//                EditText minutesEditText = dialogView.findViewById(R.id.minutesEditText);
//                EditText secondsEditText = dialogView.findViewById(R.id.secondsEditText);
//                Button startTimerButton = dialogView.findViewById(R.id.startTimerButton);
//                Button stopTimerButton = dialogView.findViewById(R.id.stopTimerButton);

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

//                                // Get timer values (hours, minutes, seconds)
//                                int hours = Integer.parseInt(hoursEditText.getText().toString());
//                                int minutes = Integer.parseInt(minutesEditText.getText().toString());
//                                int seconds = Integer.parseInt(secondsEditText.getText().toString());
//
//                                // Calculate the total duration in milliseconds
//                                long timerDuration = (hours * 3600 + minutes * 60 + seconds) * 1000;

                                // Add a new task to the list with the timer duration
//                                TodoItem newTask = new TodoItem(taskName, status, deadline);
//                                todoList.add(newTask);
//                                todoAdapter.notifyItemInserted(todoList.size() - 1);
//                                todoRecyclerView.scrollToPosition(todoList.size() - 1);

                                TodoItem newTask = new TodoItem(taskName, status, deadline);

                                // Add the task to Firestore
                                db.collection("users")
                                        .document(userId) // User's unique document
                                        .collection("tasks") // Subcollection for tasks
                                        .add(newTask)
                                        .addOnSuccessListener(documentReference -> {
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


//                                // Step 4: Set the alarm for the notification
//                                long currentTime = System.currentTimeMillis();
//                                long triggerTime = currentTime + timerDuration; // Set the alarm to trigger after timerDuration
//
//                                if (triggerTime - currentTime < 30000) {
//                                    triggerTime = currentTime + 30000; // Set trigger time to be 30 seconds from now
//                                }
//
//                                Log.d("Alarm", "Trigger time: " + triggerTime + ", Current time: " + currentTime);
//                                // Check if the app can schedule exact alarms (Android 12 and above)
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                                    if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
//                                        // Set the alarm to trigger the notification
//                                        Intent notificationIntent = new Intent(DashboardActivity.this, NotificationReceiver.class);
//                                        notificationIntent.putExtra("taskName", taskName);
//
//                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                                                DashboardActivity.this,
//                                                0,
//                                                notificationIntent,
//                                                PendingIntent.FLAG_IMMUTABLE);
//
//
//                                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
//                                    } else {
//                                        // Redirect user to request permission to schedule exact alarms
//                                        Intent intents = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
//                                        startActivity(intents);
//                                    }
//                                } else {
//                                    // For Android versions below 12, directly schedule the exact alarm
//                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                                    if (alarmManager != null) {
//                                        Intent notificationIntent = new Intent(DashboardActivity.this, NotificationReceiver.class);
//                                        notificationIntent.putExtra("taskName", taskName);
//
//                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                                                DashboardActivity.this,
//                                                0,
//                                                notificationIntent,
//                                                PendingIntent.FLAG_IMMUTABLE);
//
//
//                                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
//                                    }
//                                }
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

            // Timer logic: Start the timer when the button is clicked
//                startTimerButton.setOnClickListener(v1 -> {
//                startTimerButton.setEnabled(false);  // Disable start button once started
//                stopTimerButton.setEnabled(true);   // Enable stop button
//
//                // You can implement the timer logic here using a Handler or TimerTask
//                // Example of updating the timer every second:
//                final Handler handler = new Handler();
//                final int[] secondsRemaining = {0};  // To keep track of the elapsed time
//                Runnable timerRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        // Update the timer fields every second
//                        secondsRemaining[0]++;
//
//                        int hours = secondsRemaining[0] / 3600;
//                        int minutes = (secondsRemaining[0] % 3600) / 60;
//                        int seconds = secondsRemaining[0] % 60;
//
//                        // Update the EditText fields with the current time
//                        hoursEditText.setText(String.format("%02d", hours));
//                        minutesEditText.setText(String.format("%02d", minutes));
//                        secondsEditText.setText(String.format("%02d", seconds));
//
//                        // Continue running the timer every second
//                        handler.postDelayed(this, 1000);
//                    }
//                };
//
//                // Start the timer
//                handler.post(timerRunnable);
//            });

//            // Stop the timer logic
//            stopTimerButton.setOnClickListener(v12 -> {
//                startTimerButton.setEnabled(true);  // Re-enable the start button
//                stopTimerButton.setEnabled(false); // Disable stop button
//
//                // Stop the timer and update the task with the final time
//                // Store the time in timerDuration (convert back to milliseconds)
//                // You may need a way to store the elapsed time in a variable for future tasks
//            });
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
