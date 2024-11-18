package com.example.moodchecker.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodchecker.R;
import com.example.moodchecker.TimerActivity;
import com.example.moodchecker.TimerService;
import com.example.moodchecker.model.TodoItem;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<TodoItem> todoList;
    private TaskClickListener listener;

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
        holder.status.setText(item.getStatus());
        holder.deadline.setText(item.getDeadline());

        // Set status text color based on task status
        if (item.getStatus().equals("In Progress")) {
            holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), R.color.yellow));
        } else if (item.getStatus().equals("Complete")) {
            holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), R.color.green));
        } else if (item.getStatus().equals("Not Started")) {
            holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), R.color.red));
        } else {
            holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(item);
            } else {
                showTaskDetailsDialog(item, v.getContext());
            }
        });
    }

    // Method to show the edit timer dialog
    private void showTimerEditDialog(Context context, TodoItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Timer for " + item.getName());

        // Create an EditText for entering the new timer value
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter timer value in seconds");

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String inputValue = input.getText().toString();
            if (!inputValue.isEmpty()) {
                try {
                    long newTimerValue = Long.parseLong(inputValue) * 1000; // Convert to milliseconds
                    item.setTimerDuration(newTimerValue); // Update the timer duration in the TodoItem
                    notifyDataSetChanged(); // Notify the adapter to refresh the item display
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    showErrorDialog(context, "Invalid input. Please enter a valid number.");
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showTaskDetailsDialog(TodoItem task, Context context) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.custom_timer_dialog, null);

        // Initialize views from the custom layout
        EditText taskNameEditText = dialogView.findViewById(R.id.taskNameEditText);
        Spinner statusSpinner = dialogView.findViewById(R.id.statusSpinner);
        TextView deadlineTextView = dialogView.findViewById(R.id.deadlineTextView);
        EditText hoursEditText = dialogView.findViewById(R.id.hoursEditText);
        EditText minutesEditText = dialogView.findViewById(R.id.minutesEditText);
        EditText secondsEditText = dialogView.findViewById(R.id.secondsEditText);
        Button startTimerButton = dialogView.findViewById(R.id.startTimerButton);
        Button stopTimerButton = dialogView.findViewById(R.id.stopTimerButton);

        // Prepopulate views with task data
        taskNameEditText.setText(task.getName());
        deadlineTextView.setText(task.getDeadline());
        long timerDuration = task.getTimerDuration();
        hoursEditText.setText(String.format("%02d", timerDuration / 3600000));
        minutesEditText.setText(String.format("%02d", (timerDuration % 3600000) / 60000));
        secondsEditText.setText(String.format("%02d", (timerDuration % 60000) / 1000));

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Set button click listeners
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
//                // Handle invalid input
//                showErrorDialog(context, "Please enter valid time values.");
//                return;
//            }
//
//            // Update the task's timer and notify the adapter
//            task.setTimerDuration(duration);
//            notifyDataSetChanged();
//
//            // Start the timer activity
//            Intent intent = new Intent(context, TimerActivity.class);
//            intent.putExtra("taskName", task.getName());
//            intent.putExtra("deadline", task.getDeadline());
//            intent.putExtra("timerDuration", task.getTimerDuration());
//            context.startActivity(intent);
//            dialog.dismiss();
//        });

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
                // Handle invalid input
                showErrorDialog(context, "Please enter valid time values.");
                return;
            }

            // Update the task's timer
            task.setTimerDuration(duration);
            notifyDataSetChanged();

            // Start the timer service
            Intent serviceIntent = new Intent(context, TimerService.class);
            serviceIntent.putExtra("timerDuration", duration);
            ContextCompat.startForegroundService(context, serviceIntent); // Requires API 26+
            dialog.dismiss();
        });


        stopTimerButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Method to format the timer into a string (e.g., "01:30:00")
    private String formatTimer(long durationMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public interface TaskClickListener {
        void onTaskClick(TodoItem task);
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView name, status, deadline;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.todoName);
            status = itemView.findViewById(R.id.todoStatus);
            deadline = itemView.findViewById(R.id.todoDeadline);
        }
    }

    private void showErrorDialog(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
