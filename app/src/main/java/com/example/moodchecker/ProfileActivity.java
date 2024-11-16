package com.example.moodchecker;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private ImageView colorOption1, colorOption2, colorOption3, avatarOption1, avatarOption2, avatarOption3;
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

        // Set click listeners for color options
        colorOption1.setOnClickListener(view -> updateColor("#A3E4D7"));
        colorOption2.setOnClickListener(view -> updateColor("#FAD7A0"));
        colorOption3.setOnClickListener(view -> updateColor("#F5B7B1"));

        // Set click listeners for avatar options
        avatarOption1.setOnClickListener(view -> updateAvatar(1));
        avatarOption2.setOnClickListener(view -> updateAvatar(2));
        avatarOption3.setOnClickListener(view -> updateAvatar(3));

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


                        User user = new User(name, email);

                        // Set the fetched details in the UI
                        etName.setText(user.getUsername());
                        etGmail.setText(user.getEmail());
                    } else {
                        Toast.makeText(ProfileActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Error loading profile data", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateColor(String color) {
        try {
            Drawable background = idCardSection.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable backgroundDrawable = (GradientDrawable) background;
                backgroundDrawable.setColor(android.graphics.Color.parseColor(color));
                Toast.makeText(this, "Color updated to " + color, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Background is not a GradientDrawable!", Toast.LENGTH_SHORT).show();
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Invalid color value!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAvatar(int avatarId) {
        // Update avatar logic (e.g., store selected avatar ID)
        Toast.makeText(this, "Avatar " + avatarId + " selected", Toast.LENGTH_SHORT).show();
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
}
