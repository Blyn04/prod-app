package com.example.moodchecker;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.graphics.Color;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import com.example.moodchecker.model.Reviewer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ReviewerActivity extends AppCompatActivity {

    private ImageView backButton;
    private Button createNewReviewerButton;
    private FirebaseFirestore db; // Firestore instance
    private FirebaseAuth mAuth; // Firebase Authentication instance
    // List to keep track of existing reviewer names
    private List<String> reviewerNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewer);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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

        fetchReviewersFromFirestore();
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

                        // Add the new reviewer to Firebase
                        addReviewerToFirestore(name, description);

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

    private void addReviewerToFirestore(String name, String description) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();  // Get current user ID
            CollectionReference reviewersRef = db.collection("users")
                    .document(userId)
                    .collection("reviewer");

            // Create a new reviewer document
            Reviewer reviewer = new Reviewer(name, description);
            reviewersRef.add(reviewer)
                    .addOnSuccessListener(documentReference -> {
                        // Successfully added reviewer
                        Log.d("Firestore", "Reviewer added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        // Failed to add reviewer
                        Log.w("Firestore", "Error adding reviewer", e);
                    });

        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
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

        // Get the 3-dots menu icon
        ImageView moreOptionsIcon = folderItem.findViewById(R.id.moreOptionsIcon);

        // Set click listener for the 3-dots menu
        moreOptionsIcon.setOnClickListener(v -> {

            moreOptionsIcon.setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

            // Create and show the PopupMenu
            PopupMenu popupMenu = new PopupMenu(ReviewerActivity.this, v);
            // Inflate the menu resource file (we'll define it below)
            popupMenu.getMenuInflater().inflate(R.menu.menu_folder_options, popupMenu.getMenu());

            // Set a listener for item clicks in the popup menu
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();  // Get the item ID
                if (id == R.id.rename_option) {
                    // Handle rename logic here
                    showRenameDialog(reviewerName);
                    return true;
                } else if (id == R.id.delete_option) {
                    // Handle delete logic here
                    deleteReviewerFolder(reviewerName);
                    deleteReviewerFolderFromUI(reviewerName);
                    return true;
                } else if (id == R.id.change_color_option) {
                    // Show a dialog with color options
                    showColorChangeDialog(reviewerName);
                    return true;
                } else {
                    return false;
                }
            });

            // Show the popup menu
            popupMenu.show();
        });

        // Add this click listener in the addReviewerFolder method
        folderItem.setOnClickListener(v -> {
            // Start FlashcardsActivity and pass the reviewer ID
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid(); // Get the current user ID
                CollectionReference reviewersRef = db.collection("users")
                        .document(userId)
                        .collection("reviewer");

                reviewersRef.whereEqualTo("name", reviewerName)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    String reviewerId = document.getId(); // Get the document ID as the reviewer ID
                                    Intent intent = new Intent(ReviewerActivity.this, FlashcardsActivity.class);
                                    intent.putExtra("reviewerId", reviewerId); // Pass the reviewer ID to the next activity
                                    startActivity(intent);
                                    break;
                                }
                            } else {
                                Toast.makeText(ReviewerActivity.this, "Error: Reviewer not found", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void showColorChangeDialog(String reviewerName) {
        Dialog colorDialog = new Dialog(this);
        colorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        colorDialog.setContentView(R.layout.dialog_change_color);

        RadioGroup colorRadioGroup = colorDialog.findViewById(R.id.colorRadioGroup);
        Button applyButton = colorDialog.findViewById(R.id.applyColorButton);

        applyButton.setOnClickListener(v -> {
            int selectedColorId = colorRadioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = colorDialog.findViewById(selectedColorId);

            if (selectedRadioButton != null) {
                String color = selectedRadioButton.getText().toString(); // Get color name (e.g., "Red")
                saveColorToFirestore(reviewerName, color); // Save the color to Firestore
            }

            colorDialog.dismiss();
        });

        colorDialog.show();
    }

    private void saveColorToFirestore(String reviewerName, String color) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            CollectionReference reviewersRef = db.collection("users")
                    .document(userId)
                    .collection("reviewer");

            // Find the reviewer by name and update the color
            reviewersRef.whereEqualTo("name", reviewerName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String documentId = document.getId();

                                // Update the color field in Firestore
                                reviewersRef.document(documentId)
                                        .update("color", color)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(ReviewerActivity.this, "Color updated", Toast.LENGTH_SHORT).show();
                                            applyColorToFolder(reviewerName, color); // Reflect the color in the UI
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(ReviewerActivity.this, "Failed to update color", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    });
        }
    }

    private void applyColorToFolder(String reviewerName, String color) {
        GridLayout folderGrid = findViewById(R.id.folderGrid);
        int childCount = folderGrid.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View folderItem = folderGrid.getChildAt(i);
            TextView folderNameTextView = folderItem.findViewById(R.id.folderNameTextView);

            if (folderNameTextView != null && folderNameTextView.getText().toString().equals(reviewerName)) {
                // Change the color of the folder icon only
                int colorCode = getColorCode(color);
                ImageView folderIcon = folderItem.findViewById(R.id.folderIcon);
                folderIcon.setColorFilter(colorCode, PorterDuff.Mode.SRC_IN);

                break;
            }
        }
    }

    private int getColorCode(String colorName) {
        switch (colorName) {
            case "Red":
                return Color.RED;
            case "Green":
                return Color.GREEN;
            case "Blue":
                return Color.BLUE;
            case "Yellow":
                return Color.YELLOW;
            case "Purple":
                return Color.parseColor("#800080"); // Purple color code
            default:
                return Color.WHITE; // Default color
        }
    }

    // Show the rename dialog
    private void showRenameDialog(String oldName) {
        // Create a dialog for renaming the reviewer
        Dialog renameDialog = new Dialog(this);
        renameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        renameDialog.setContentView(R.layout.dialog_rename_reviewer);

        EditText renameEditText = renameDialog.findViewById(R.id.renameEditText);
        renameEditText.setText(oldName);

        Button renameButton = renameDialog.findViewById(R.id.renameButton);
        Button cancelButton = renameDialog.findViewById(R.id.cancelButton);

        // Set click listener for the Rename button
        renameButton.setOnClickListener(v -> {
            String newName = renameEditText.getText().toString().trim();
            if (!newName.isEmpty()) {
                // Check if the new name already exists
                if (reviewerNames.contains(newName)) {
                    Toast.makeText(ReviewerActivity.this, "This name is already taken. Please choose a different one.", Toast.LENGTH_SHORT).show();
                } else {
                    // Update the name in Firestore and the UI
                    updateReviewerName(oldName, newName);
                    renameDialog.dismiss();
                }
            } else {
                Toast.makeText(ReviewerActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for the Cancel button
        cancelButton.setOnClickListener(v -> renameDialog.dismiss());

        // Show the dialog
        renameDialog.show();
    }


    // Update reviewer name in Firestore
    private void updateReviewerName(String oldName, String newName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            CollectionReference reviewersRef = db.collection("users")
                    .document(userId)
                    .collection("reviewer");

            // Find the reviewer by old name and update the name
            reviewersRef.whereEqualTo("name", oldName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                // Update the document in Firestore
                                reviewersRef.document(document.getId())
                                        .update("name", newName)
                                        .addOnSuccessListener(aVoid -> {
                                            // Successfully updated the reviewer name
                                            Toast.makeText(ReviewerActivity.this, "Reviewer renamed", Toast.LENGTH_SHORT).show();

                                            // Now, update the UI to reflect the new name
                                            updateReviewerFolderInUI(oldName, newName);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Failed to update the reviewer name
                                            Toast.makeText(ReviewerActivity.this, "Error renaming reviewer", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    });
        }
    }

    private void updateReviewerFolderInUI(String oldName, String newName) {
        GridLayout folderGrid = findViewById(R.id.folderGrid);
        int childCount = folderGrid.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View folderItem = folderGrid.getChildAt(i);

            TextView folderNameTextView = folderItem.findViewById(R.id.folderNameTextView);
            if (folderNameTextView != null && folderNameTextView.getText().toString().equals(oldName)) {
                // Change folder icon color to green upon rename
                ImageView folderIcon = folderItem.findViewById(R.id.folderIcon);
                folderIcon.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);

                // Update the name in the UI
                folderNameTextView.setText(newName);
                break;
            }
        }
    }

    // Delete reviewer folder
    private void deleteReviewerFolder(String reviewerName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            CollectionReference reviewersRef = db.collection("users")
                    .document(userId)
                    .collection("reviewer");

            // Delete the reviewer from Firestore
            reviewersRef.whereEqualTo("name", reviewerName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                // Delete the reviewer document
                                reviewersRef.document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Successfully deleted the reviewer
                                            Toast.makeText(ReviewerActivity.this, "Reviewer deleted", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Failed to delete the reviewer
                                            Toast.makeText(ReviewerActivity.this, "Error deleting reviewer", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    });
        }
    }

    private void deleteReviewerFolderFromUI(String reviewerName) {
        GridLayout folderGrid = findViewById(R.id.folderGrid);
        int childCount = folderGrid.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View folderItem = folderGrid.getChildAt(i);
            TextView folderNameTextView = folderItem.findViewById(R.id.folderNameTextView);

            if (folderNameTextView != null && folderNameTextView.getText().toString().equals(reviewerName)) {
                folderGrid.removeViewAt(i); // Remove the folder from the GridLayout
                break;
            }
        }
    }
    private void fetchReviewersFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get the current user ID
            CollectionReference reviewersRef = db.collection("users")
                    .document(userId)
                    .collection("reviewer");

            reviewersRef.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String reviewerName = document.getString("name");
                                String color = document.getString("color");
                                if (reviewerName != null) {
                                    // Add each reviewer to the UI
                                    addReviewerFolder(reviewerName);
                                    if (color != null) {
                                        applyColorToFolder(reviewerName, color); // Apply saved color
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(ReviewerActivity.this, "No reviewers found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error getting documents", e);
                        Toast.makeText(ReviewerActivity.this, "Error loading reviewers", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

}
