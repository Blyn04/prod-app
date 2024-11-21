package com.example.moodchecker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;

public class CustomAddTaskDialog extends Dialog {

    private EditText taskNameEditText;
    private Spinner statusSpinner;
    private TextView deadlineTextView;
    private Button addTaskButton;
    private OnSaveListener onSaveListener;

    public CustomAddTaskDialog(Context context, OnSaveListener listener) {
        super(context);
        this.onSaveListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_task); // Your custom layout XML

        taskNameEditText = findViewById(R.id.taskNameEditText);
        statusSpinner = findViewById(R.id.statusSpinner);
        deadlineTextView = findViewById(R.id.deadlineTextView);
        addTaskButton = findViewById(R.id.saveBtn);

        // Set up listeners
        addTaskButton.setOnClickListener(v -> {
            String taskName = taskNameEditText.getText().toString();
            String status = statusSpinner.getSelectedItem().toString();
            String deadline = deadlineTextView.getText().toString();

            if (taskName.isEmpty()) {
                showErrorDialog("Please enter a task name.");
                return;
            }

            if (taskName.length() < 4 || taskName.length() > 15) {
                showErrorDialog("Task name must be between 4 and 15 characters.");
                return;
            }

            if (onSaveListener != null) {
                onSaveListener.onSave(taskName, status, deadline);
                dismiss();  // Close the dialog after saving
            }
        });

        deadlineTextView.setOnClickListener(v -> {
            // Show the DatePickerDialog (You can use your existing code here)
        });
    }

    private void showErrorDialog(String message) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public interface OnSaveListener {
        void onSave(String taskName, String status, String deadline);
    }
}

