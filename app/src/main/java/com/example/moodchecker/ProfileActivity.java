package com.example.moodchecker;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moodchecker.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvStreak;
    private EditText etName, etGmail;
    private Button btnEditSave;
    private boolean isEditing = false;
    private ImageView colorOption1, colorOption2, colorOption3, avatarOption1, avatarOption2, avatarOption3, avatarOption4, avatarOption5, avatarOption6, avatarOption7, avatarOption8, avatarOption9, avatarOption10;
    private LinearLayout idCardSection;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnEditSave = findViewById(R.id.btnEditSave);

        // Initialize UI components
        etName = findViewById(R.id.etName);
        etGmail = findViewById(R.id.etGmail);
        tvStreak = findViewById(R.id.tvStreak);

        idCardSection = findViewById(R.id.idCardSection);
        colorOption1 = findViewById(R.id.colorOption1);
        colorOption2 = findViewById(R.id.colorOption2);
        colorOption3 = findViewById(R.id.colorOption3);

        avatarOption1 = findViewById(R.id.avatarOption1);
        avatarOption2 = findViewById(R.id.avatarOption2);
        avatarOption3 = findViewById(R.id.avatarOption3);
        avatarOption4 = findViewById(R.id.avatarOption4);
        avatarOption5 = findViewById(R.id.avatarOption5);
        avatarOption6 = findViewById(R.id.avatarOption6);
        avatarOption7 = findViewById(R.id.avatarOption7);
        avatarOption8 = findViewById(R.id.avatarOption8);
        avatarOption9 = findViewById(R.id.avatarOption9);
        avatarOption10 = findViewById(R.id.avatarOption10);

        // Set click listeners for color options
        colorOption1.setOnClickListener(view -> updateColor("#A3E4D7"));
        colorOption2.setOnClickListener(view -> updateColor("#FAD7A0"));
        colorOption3.setOnClickListener(view -> updateColor("#F5B7B1"));

        // Set click listeners for avatar options
        avatarOption1.setOnClickListener(view -> updateAvatar(1));
        avatarOption2.setOnClickListener(view -> updateAvatar(2));
        avatarOption3.setOnClickListener(view -> updateAvatar(3));
        avatarOption4.setOnClickListener(view -> updateAvatar(4));
        avatarOption5.setOnClickListener(view -> updateAvatar(5));
        avatarOption6.setOnClickListener(view -> updateAvatar(6));
        avatarOption7.setOnClickListener(view -> updateAvatar(7));
        avatarOption8.setOnClickListener(view -> updateAvatar(8));
        avatarOption9.setOnClickListener(view -> updateAvatar(9));
        avatarOption10.setOnClickListener(view -> updateAvatar(10));

        if (tvStreak != null) {
            tvStreak.setText("0"); // Set the streak to 0
        } else {
            Log.e("MyActivity", "TextView with ID tvStreak not found");
        }

        // Handle Save Changes button
        findViewById(R.id.btnSaveChanges).setOnClickListener(view -> {
            String newName = etName.getText().toString();
            String newGmail = etGmail.getText().toString();

            // Update the profile with the new values
            etName.setText(newName);

            // Show toast message
            Toast.makeText(ProfileActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();

            // Save changes to Firestore if needed (optional)
            saveChanges(newName, newGmail);
        });

        btnEditSave.setOnClickListener(view -> {
            if (isEditing) {
                // Save changes
                saveChanges(etName.getText().toString(), etGmail.getText().toString());
                toggleEditing(false); // Exit editing mode
            } else {
                // Enter editing mode
                toggleEditing(true);
            }
        });
        toggleEditing(false);

        // Load the current user's profile data from Firestore
        loadUserProfile();
    }

    // Fetch the current user's data from Firestore
    private void loadUserProfile() {
        // Get the current user from FirebaseAuth
        String userId = mAuth.getCurrentUser().getUid();  // Current logged-in user's UID

        db.collection("users")
                .document(userId)
                .collection("streaks") // Access the streaks sub-collection
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Loop through the documents in the streaks collection
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Retrieve the 'streak' field from each document
                            Object streakField = documentSnapshot.get("streak");

                            if (streakField != null) {
                                // Check if the streak is a number (long or int)
                                if (streakField instanceof Long) {
                                    // If the streak is a Long (number), display it as String
                                    tvStreak.setText(String.valueOf(streakField));
                                } else if (streakField instanceof Integer) {
                                    // If the streak is an Integer, display it as String
                                    tvStreak.setText(String.valueOf(streakField));
                                } else if (streakField instanceof Double) {
                                    // If the streak is a Double, display it as String
                                    tvStreak.setText(String.valueOf(streakField));
                                } else {
                                    // If the streak is of an unsupported type, handle it
                                    tvStreak.setText("0");
                                }
                                break;  // Assuming you only need the first streak document
                            }
                        }
                    } else {
                        // If no streak document exists, set default value (e.g., "0")
                        tvStreak.setText("0");
                        Toast.makeText(ProfileActivity.this, "Streak data not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Error loading streak data", Toast.LENGTH_SHORT).show();
                });

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get user details from the document
                        String name = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String cardColor = documentSnapshot.getString("cardColor");

                        User user = new User(name, email);

                        // Set the fetched details in the UI
                        if (name != null) etName.setText(name);
                        if (email != null) etGmail.setText(email);
                        if (cardColor != null) {
                            GradientDrawable background = (GradientDrawable) idCardSection.getBackground();
                            background.setColor(android.graphics.Color.parseColor(cardColor));
                        }

                        toggleEditing(false);
                    } else {
                        Toast.makeText(ProfileActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Error loading profile data", Toast.LENGTH_SHORT).show();
                });

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String avatar = documentSnapshot.getString("avatar");
                        if (avatar != null) {
                            switch (avatar) {
                                case "avatar1":
                                    updateAvatar(1);
                                    break;
                                case "avatar2":
                                    updateAvatar(2);
                                    break;
                                case "avatar3":
                                    updateAvatar(3);
                                    break;
                                case "avatar4":
                                    updateAvatar(4);
                                    break;
                                case "avatar5":
                                    updateAvatar(5);
                                    break;
                                case "avatar6":
                                    updateAvatar(6);
                                    break;
                                case "avatar7":
                                    updateAvatar(7);
                                    break;
                                case "avatar8":
                                    updateAvatar(8);
                                    break;
                                case "avatar9":
                                    updateAvatar(9);
                                    break;
                                case "avatar10":
                                    updateAvatar(10);
                                    break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Failed to load user profile", e));
    }

    private void updateColor(String color) {
        // Change the color of the ID Card section
        GradientDrawable background = (GradientDrawable) idCardSection.getBackground();
        background.setColor(android.graphics.Color.parseColor(color));

        // Save the selected color to Firestore
        saveColorToDatabase(color);

        // Optional: Show a confirmation message
        Toast.makeText(ProfileActivity.this, "Color updated", Toast.LENGTH_SHORT).show();
    }

    private void saveColorToDatabase(String color) {
        // Get the current user's ID
        String userId = mAuth.getCurrentUser().getUid();

        // Create a map to store the color data
        Map<String, Object> updates = new HashMap<>();
        updates.put("cardColor", color);

        // Update the user's document in Firestore
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileActivity", "Color updated successfully in Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error updating color in Firestore", e);
                });
    }

    private void updateAvatar(int avatarIndex) {
        String selectedAvatar = "avatar" + avatarIndex;
        // Save the selected avatar to Firestore or SharedPreferences
        saveAvatarToDatabase(selectedAvatar);

        // Provide feedback (e.g., highlight the selected avatar)
        switch (avatarIndex) {
            case 1:
                avatarOption1.setBackgroundResource(R.drawable.selected_border); // Optional visual feedback
                avatarOption2.setBackgroundResource(0);
                avatarOption3.setBackgroundResource(0);
                avatarOption4.setBackgroundResource(0);
                avatarOption5.setBackgroundResource(0);
                avatarOption6.setBackgroundResource(0);
                avatarOption7.setBackgroundResource(0);
                avatarOption8.setBackgroundResource(0);
                avatarOption9.setBackgroundResource(0);
                avatarOption10.setBackgroundResource(0);
                break;
            case 2:
                avatarOption1.setBackgroundResource(0);
                avatarOption2.setBackgroundResource(R.drawable.selected_border);
                avatarOption3.setBackgroundResource(0);
                avatarOption4.setBackgroundResource(0);
                avatarOption5.setBackgroundResource(0);
                avatarOption6.setBackgroundResource(0);
                avatarOption7.setBackgroundResource(0);
                avatarOption8.setBackgroundResource(0);
                avatarOption9.setBackgroundResource(0);
                avatarOption10.setBackgroundResource(0);
                break;
            case 3:
                avatarOption1.setBackgroundResource(0);
                avatarOption2.setBackgroundResource(0);
                avatarOption3.setBackgroundResource(R.drawable.selected_border);
                avatarOption4.setBackgroundResource(0);
                avatarOption5.setBackgroundResource(0);
                avatarOption6.setBackgroundResource(0);
                avatarOption7.setBackgroundResource(0);
                avatarOption8.setBackgroundResource(0);
                avatarOption9.setBackgroundResource(0);
                avatarOption10.setBackgroundResource(0);
                break;
            case 4:
                avatarOption1.setBackgroundResource(0);
                avatarOption2.setBackgroundResource(0);
                avatarOption3.setBackgroundResource(0);
                avatarOption4.setBackgroundResource(R.drawable.selected_border);
                avatarOption5.setBackgroundResource(0);
                avatarOption6.setBackgroundResource(0);
                avatarOption7.setBackgroundResource(0);
                avatarOption8.setBackgroundResource(0);
                avatarOption9.setBackgroundResource(0);
                avatarOption10.setBackgroundResource(0);
                break;
            case 5:
                avatarOption1.setBackgroundResource(0);
                avatarOption2.setBackgroundResource(0);
                avatarOption3.setBackgroundResource(0);
                avatarOption4.setBackgroundResource(0);
                avatarOption5.setBackgroundResource(R.drawable.selected_border);
                avatarOption6.setBackgroundResource(0);
                avatarOption7.setBackgroundResource(0);
                avatarOption8.setBackgroundResource(0);
                avatarOption9.setBackgroundResource(0);
                avatarOption10.setBackgroundResource(0);
                break;
            case 6:
                avatarOption1.setBackgroundResource(0);
                avatarOption2.setBackgroundResource(0);
                avatarOption3.setBackgroundResource(0);
                avatarOption4.setBackgroundResource(0);
                avatarOption5.setBackgroundResource(0);
                avatarOption6.setBackgroundResource(R.drawable.selected_border);
                avatarOption7.setBackgroundResource(0);
                avatarOption8.setBackgroundResource(0);
                avatarOption9.setBackgroundResource(0);
                avatarOption10.setBackgroundResource(0);
                break;
            case 7:
                avatarOption1.setBackgroundResource(0);
                avatarOption2.setBackgroundResource(0);
                avatarOption3.setBackgroundResource(0);
                avatarOption4.setBackgroundResource(0);
                avatarOption5.setBackgroundResource(0);
                avatarOption6.setBackgroundResource(0);
                avatarOption7.setBackgroundResource(R.drawable.selected_border);
                avatarOption8.setBackgroundResource(0);
                avatarOption9.setBackgroundResource(0);
                avatarOption10.setBackgroundResource(0);
                break;
            case 8:
                avatarOption1.setBackgroundResource(0);
                avatarOption2.setBackgroundResource(0);
                avatarOption3.setBackgroundResource(0);
                avatarOption4.setBackgroundResource(0);
                avatarOption5.setBackgroundResource(0);
                avatarOption6.setBackgroundResource(0);
                avatarOption7.setBackgroundResource(0);
                avatarOption8.setBackgroundResource(R.drawable.selected_border);
                avatarOption9.setBackgroundResource(0);
                avatarOption10.setBackgroundResource(0);
                break;
            case 9:
                avatarOption1.setBackgroundResource(0);
                avatarOption2.setBackgroundResource(0);
                avatarOption3.setBackgroundResource(0);
                avatarOption4.setBackgroundResource(0);
                avatarOption5.setBackgroundResource(0);
                avatarOption6.setBackgroundResource(0);
                avatarOption7.setBackgroundResource(0);
                avatarOption8.setBackgroundResource(0);
                avatarOption9.setBackgroundResource(R.drawable.selected_border);
                avatarOption10.setBackgroundResource(0);
                break;

            case 10:
                avatarOption1.setBackgroundResource(0);
                avatarOption2.setBackgroundResource(0);
                avatarOption3.setBackgroundResource(0);
                avatarOption4.setBackgroundResource(0);
                avatarOption5.setBackgroundResource(0);
                avatarOption6.setBackgroundResource(0);
                avatarOption7.setBackgroundResource(0);
                avatarOption8.setBackgroundResource(0);
                avatarOption9.setBackgroundResource(0);
                avatarOption10.setBackgroundResource(R.drawable.selected_border);
                break;
        }
    }

    private void saveAvatarToDatabase(String avatarName) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("avatar", avatarName);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Avatar updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Failed to update avatar", e));
    }

    // Optional: Save changes to Firestore when the user edits their profile
    private void saveChanges(String newName, String newGmail) {
        String userId = mAuth.getCurrentUser().getUid();

        // Create a map of updated profile data
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("username", newName);
        userUpdates.put("email", newGmail);

        // Save updated data to Firestore
        db.collection("users").document(userId)
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show());
    }

    private void toggleEditing(boolean enable) {
        isEditing = enable;

        // Toggle EditText fields' editable state
        etName.setFocusableInTouchMode(enable);
        etName.setCursorVisible(enable);
        etGmail.setFocusableInTouchMode(enable);
        etGmail.setCursorVisible(enable);

        // Update button text
        btnEditSave.setText(enable ? "Save Changes" : "Edit");
    }
}
