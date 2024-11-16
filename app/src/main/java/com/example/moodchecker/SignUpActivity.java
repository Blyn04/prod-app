package com.example.moodchecker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.example.moodchecker.model.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText, confirmPasswordEditText, emailEditText;
    Button signUpButton;
    TextView loginPrompt;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    private static final String CHANNEL_ID = "sign_up_notification_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        emailEditText = findViewById(R.id.email);
        signUpButton = findViewById(R.id.signUpButton);
        loginPrompt = findViewById(R.id.loginPrompt);

        // Set up sign-up button click listener
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                String email = emailEditText.getText().toString();

                // Basic validation
                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Create user with Firebase Authentication
                   // signUpWithFirebase(username, email, password);
                    checkUsernameAvailability(username, email, password);

                }
            }
        });

        // Create notification channel if Android version is Oreo or above
        createNotificationChannel();
    }

    private void checkUsernameAvailability(String username, String email, String password) {
        // Check if the username already exists in Firestore
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // If the username already exists, show a message
                            Toast.makeText(SignUpActivity.this, "Username is already taken, please choose another", Toast.LENGTH_SHORT).show();
                        } else {
                            // Proceed with sign-up if username is unique
                            signUpWithFirebase(username, email, password);
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Error checking username", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signUpWithFirebase(String username, String email, String password) {
        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-up successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Optionally store the username and other details in Firestore
                        storeUserDataInDatabase(username, email);

                    } else {
                        // If sign-up fails, display a message to the user
                        Toast.makeText(SignUpActivity.this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void storeUserDataInDatabase(String username, String email) {
        // Create a map to store user data in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User user = new User(username, email);

        // Store the user data in Firestore
        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // Show notification after successful data storage
                    showSuccessNotification();

                    // After storing user data, navigate to the main activity or home screen
                    Toast.makeText(SignUpActivity.this, "User data saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessNotification() {
        // Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_celebration) // Use a suitable icon from your resources
                .setContentTitle("Sign-Up Success")
                .setContentText("You have successfully signed up!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Automatically removes the notification when tapped

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Create a notification channel for Android Oreo or above
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SignUp Notifications";
            String description = "Notifications for successful sign-ups";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
