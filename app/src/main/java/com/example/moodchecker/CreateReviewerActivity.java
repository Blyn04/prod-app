package com.example.moodchecker;


import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreateReviewerActivity extends AppCompatActivity {

    private Button addReviewerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reviewer);

        addReviewerButton = findViewById(R.id.addReviewerButton);

        addReviewerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddReviewerDialog();
            }
        });
    }

    private void showAddReviewerDialog() {
        // Create a new dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_reviewer);

        // Find dialog views
        EditText reviewerNameEditText = dialog.findViewById(R.id.reviewerNameEditText);
        EditText reviewerEmailEditText = dialog.findViewById(R.id.reviewerDescriptionEditText);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        // Set click listener for the Save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = reviewerNameEditText.getText().toString().trim();
                String email = reviewerEmailEditText.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty()) {
                    Toast.makeText(CreateReviewerActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                } else {
                    // Perform action to save reviewer details
                    Toast.makeText(CreateReviewerActivity.this, "Reviewer added", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });

        // Set click listener for the Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }
}