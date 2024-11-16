package com.example.moodchecker;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ReviewerActivity extends AppCompatActivity {

    private ImageView backButton;
    private Button createNewReviewerButton;

    // List to keep track of existing reviewer names
    private List<String> reviewerNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewer);

        backButton = findViewById(R.id.backButton);
        createNewReviewerButton = findViewById(R.id.createNewReviewerButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        createNewReviewerButton.setOnClickListener(new View.OnClickListener() {
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
        EditText reviewerDescriptionEditText = dialog.findViewById(R.id.reviewerDescriptionEditText);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        // Set click listener for the Save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = reviewerNameEditText.getText().toString().trim();
                String description = reviewerDescriptionEditText.getText().toString().trim();

                if (name.isEmpty() || description.isEmpty()) {
                    Toast.makeText(ReviewerActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                } else {
                    // Check if the reviewer name is unique
                    if (reviewerNames.contains(name)) {
                        Toast.makeText(ReviewerActivity.this, "This reviewer title already exists. Please choose a different title.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Add the new reviewer name to the list
                        reviewerNames.add(name);

                        // Perform action to save reviewer details
                        addReviewerFolder(name);
                        Toast.makeText(ReviewerActivity.this, "Reviewer added", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
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

    private void addReviewerFolder(String reviewerName) {
        GridLayout folderGrid = findViewById(R.id.folderGrid);

        // Inflate the folder_item layout
        View folderItem = LayoutInflater.from(this).inflate(R.layout.folder_item, folderGrid, false);

        // Set the name for the folder
        TextView folderNameTextView = folderItem.findViewById(R.id.folderNameTextView);
        folderNameTextView.setText(reviewerName);

        // Add the folder item to the GridLayout
        folderGrid.addView(folderItem);
    }
}
