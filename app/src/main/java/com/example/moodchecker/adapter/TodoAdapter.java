package com.example.moodchecker.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodchecker.R;
import com.example.moodchecker.TimerActivity;
import com.example.moodchecker.TimerService;
import com.example.moodchecker.model.TodoItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<TodoItem> todoList;
    private TaskClickListener listener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TodoAdapter(List<TodoItem> todoList) {
        this.todoList = todoList;
    }

    public void setTaskClickListener(TaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem item = todoList.get(position);
        holder.name.setText(item.getName());
        holder.deadline.setText(item.getDeadline());
        holder.checkBox.setChecked(false);

        // Spinner setup: ArrayAdapter and selection
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                holder.itemView.getContext(),
                R.array.task_status_array, // Array in strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(adapter);

        // Set Spinner selection based on status
        int statusIndex = adapter.getPosition(item.getStatus());
        if (statusIndex >= 0) {
            holder.statusSpinner.setSelection(statusIndex);
        } else {
            holder.statusSpinner.setSelection(0); // Default to the first item if status is invalid
        }

        // Handle Spinner item selection change
        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newStatus = parent.getItemAtPosition(position).toString();
                item.setStatus(newStatus); // Update the status in the TodoItem
                Log.d("TodoAdapter", "Status updated: " + newStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(item);
            } else {
                showTaskDetailsDialog(item, v.getContext());
            }
        });

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    Log.d("TodoAdapter", "Checkbox checked: " + isChecked);
                    if (isChecked) {
                        buttonView.setEnabled(false);
                        // Show confirmation dialog to remove the task
                        String taskName = item.getName();
                        showRemoveConfirmationDialog(holder.itemView.getContext(), taskName, position);
                    }
                }
        );
    }

    private void updateTaskStatusInFirestore(String userId, String taskName, String newStatus) {
        db.collection("users")
                .document(userId)
                .collection("tasks")
                .whereEqualTo("name", taskName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference().update("status", newStatus)
                                .addOnSuccessListener(aVoid -> Log.d("TodoAdapter", "Status updated in Firestore"))
                                .addOnFailureListener(e -> Log.e("TodoAdapter", "Error updating status", e));
                    }
                });
    }


    // Get the status color based on task status
    private int getStatusColor(Context context, String status) {
        switch (status) {
            case "In Progress":
                return ContextCompat.getColor(context, R.color.yellow);
            case "Complete":
                return ContextCompat.getColor(context, R.color.green);
            case "Not Started":
                return ContextCompat.getColor(context, R.color.red);
            default:
                return ContextCompat.getColor(context, R.color.black);
        }
    }

    // Method to show the confirmation dialog when the checkbox is checked
    private void showRemoveConfirmationDialog(Context context, String taskName, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Remove Task")
                .setMessage("Do you want to remove this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    removeTaskFromFirestore(context, userId, taskName, position);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    todoList.get(position).setChecked(false); // Update the model if you store the checked state
                    notifyItemChanged(position); // Refresh the item view
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    // Method to remove task from Firestore and update the UI
    private void removeTaskFromFirestore(Context context, String userId, String taskName, int position) {
        // Log the userId and taskName to debug
        Log.d("TodoAdapter", "Removing task with taskName: " + taskName + " for userId: " + userId);

        if (taskName == null || taskName.isEmpty()) {
            showErrorDialog(context, "Invalid Task Name.");
            return;
        }

        if (userId == null || userId.isEmpty()) {
            showErrorDialog(context, "Invalid User ID.");
            return;
        }

        CollectionReference tasksRef = db.collection("users")
                .document(userId)
                .collection("tasks");

        tasksRef.whereEqualTo("name", taskName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            tasksRef.document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully deleted the task
                                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                                        todoList.remove(position);
                                        notifyItemRemoved(position);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error occurred during task deletion
                                        Toast.makeText(context, "Error deleting task", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(context, "Task not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showErrorDialog(context, "Error querying tasks: " + e.getMessage());
                });
    }

    private void showErrorDialog(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

//    private void showTaskDetailsDialog(TodoItem task, Context context) {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View dialogView = inflater.inflate(R.layout.custom_timer_dialog, null);
//
//        EditText taskNameEditText = dialogView.findViewById(R.id.taskNameEditText);
//        Spinner statusSpinner = dialogView.findViewById(R.id.todoStatusSpinner);
//        TextView deadlineTextView = dialogView.findViewById(R.id.deadlineTextView);
//        EditText hoursEditText = dialogView.findViewById(R.id.hoursEditText);
//        EditText minutesEditText = dialogView.findViewById(R.id.minutesEditText);
//        EditText secondsEditText = dialogView.findViewById(R.id.secondsEditText);
//        Button startTimerButton = dialogView.findViewById(R.id.startTimerButton);
//        Button stopTimerButton = dialogView.findViewById(R.id.stopTimerButton);
//
//        taskNameEditText.setText(task.getName());
//
//        // List of possible statuses
//        String[] statuses = {"Not Started", "In Progress", "Complete"};
//
//        // Find the index of task status in the list
//        int statusIndex = -1;
//        for (int i = 0; i < statuses.length; i++) {
//            if (statuses[i].equals(task.getStatus())) {
//                statusIndex = i;
//                break;
//            }
//        }
//
//        // Debugging log to check the value of task.getStatus() and statusIndex
//        Log.d("TodoAdapter", "Task status: " + task.getStatus());
//        Log.d("TodoAdapter", "Status index: " + statusIndex);
//
//        // Set the status spinner selection
//        if (statusIndex != -1) {
//            statusSpinner.setSelection(statusIndex);
//        } else {
//            // Set to default if status is not found
//            statusSpinner.setSelection(0);
//        }
//
//        deadlineTextView.setText(task.getDeadline());
//        long timerDuration = task.getTimerDuration();
//        hoursEditText.setText(String.format("%02d", timerDuration / 3600000));
//        minutesEditText.setText(String.format("%02d", (timerDuration % 3600000) / 60000));
//        secondsEditText.setText(String.format("%02d", (timerDuration % 60000) / 1000));
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setView(dialogView);
//
//        AlertDialog dialog = builder.create();
//
//        startTimerButton.setOnClickListener(v -> {
//            String hours = hoursEditText.getText().toString();
//            String minutes = minutesEditText.getText().toString();
//            String seconds = secondsEditText.getText().toString();
//
//            long duration = 0;
//            try {
//                duration += Long.parseLong(hours) * 3600000;
//                duration += Long.parseLong(minutes) * 60000;
//                duration += Long.parseLong(seconds) * 1000;
//            } catch (NumberFormatException e) {
//                showErrorDialog(context, "Please enter valid time values.");
//                return;
//            }
//
//            task.setTimerDuration(duration);
//            notifyDataSetChanged();
//
//            Intent serviceIntent = new Intent(context, TimerService.class);
//            serviceIntent.putExtra("timerDuration", duration);
//            ContextCompat.startForegroundService(context, serviceIntent);
//            dialog.dismiss();
//        });
//
//        stopTimerButton.setOnClickListener(v -> dialog.dismiss());
//
//        dialog.show();
//    }

    private void showTaskDetailsDialog(TodoItem task, Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.custom_timer_dialog, null);

        EditText taskNameEditText = dialogView.findViewById(R.id.taskNameEditText);
        Spinner statusSpinner = dialogView.findViewById(R.id.todoStatusSpinner);
        TextView deadlineTextView = dialogView.findViewById(R.id.deadlineTextView);
        EditText hoursEditText = dialogView.findViewById(R.id.hoursEditText);
        EditText minutesEditText = dialogView.findViewById(R.id.minutesEditText);
        EditText secondsEditText = dialogView.findViewById(R.id.secondsEditText);
        Button startTimerButton = dialogView.findViewById(R.id.startTimerButton);
        Button stopTimerButton = dialogView.findViewById(R.id.stopTimerButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);  // Add a save button in your dialog

        taskNameEditText.setText(task.getName());

        hoursEditText.requestFocus();
        hoursEditText.setSelection(hoursEditText.getText().length());  // Ensure the cursor is at the end of the input field

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(hoursEditText, InputMethodManager.SHOW_IMPLICIT);


        // List of possible statuses
        String[] statuses = {"Not Started", "In Progress", "Complete"};

        // Find the index of task status in the list
        int statusIndex = -1;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(task.getStatus())) {
                statusIndex = i;
                break;
            }
        }

        // Set the status spinner selection
        if (statusIndex != -1) {
            statusSpinner.setSelection(statusIndex);
        } else {
            // Set to default if status is not found
            statusSpinner.setSelection(0);
        }

        deadlineTextView.setText(task.getDeadline());
        long timerDuration = task.getTimerDuration();
        hoursEditText.setText(String.format("%02d", timerDuration / 3600000));
        minutesEditText.setText(String.format("%02d", (timerDuration % 3600000) / 60000));
        secondsEditText.setText(String.format("%02d", (timerDuration % 60000) / 1000));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Save button click listener
        saveButton.setOnClickListener(v -> {
            String newStatus = statusSpinner.getSelectedItem().toString();
            String taskName = taskNameEditText.getText().toString();

            // Get the current user's ID
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Update the task status in Firestore
            updateTaskStatusInFirestore(userId, taskName, newStatus);

            // Update the task object with the new status
            task.setStatus(newStatus);

            // Notify the adapter that the item has been updated
            notifyDataSetChanged();

            // Dismiss the dialog
            dialog.dismiss();
        });

        startTimerButton.setOnClickListener(v -> {
            String hours = hoursEditText.getText().toString();
            String minutes = minutesEditText.getText().toString();
            String seconds = secondsEditText.getText().toString();

            long duration = 0;
            try {
                duration += Long.parseLong(hours) * 3600000;
                duration += Long.parseLong(minutes) * 60000;
                duration += Long.parseLong(seconds) * 1000;
            } catch (NumberFormatException e) {
                showErrorDialog(context, "Please enter valid time values.");
                return;
            }

            task.setTimerDuration(duration);
            notifyDataSetChanged();

            Intent serviceIntent = new Intent(context, TimerService.class);
            serviceIntent.putExtra("timerDuration", duration);
            ContextCompat.startForegroundService(context, serviceIntent);
            dialog.dismiss();
        });

        stopTimerButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }



    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public interface TaskClickListener {
        void onTaskClick(TodoItem task);
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView name, deadline;
        Spinner statusSpinner; // Changed from TextView to Spinner
        CheckBox checkBox;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.todoName);
            statusSpinner = itemView.findViewById(R.id.todoStatusSpinner);
            deadline = itemView.findViewById(R.id.todoDeadline);
            checkBox = itemView.findViewById(R.id.todoCheckbox);
        }
    }
}