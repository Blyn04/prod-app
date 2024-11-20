package com.example.moodchecker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText;
    Button loginButton;
    TextView signUpPrompt;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
//        FirebaseApp.initializeApp(this);
//        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpPrompt = findViewById(R.id.signUpPrompt);

        // Login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Basic validation
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Attempt login with Firebase Authentication
                    getEmailByUsernameAndLogin(username, password);
                }
            }
        });

        // Navigate to SignUpActivity
        signUpPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getEmailByUsernameAndLogin(String username, String password) {
        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore to get the document with the provided username
        db.collection("users") // Assuming the collection is named "users"
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Extract the email from the retrieved document
                        String email = task.getResult().getDocuments().get(0).getString("email");

                        // Now login with the retrieved email and password
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(authTask -> {
                                    if (authTask.isSuccessful()) {
                                        // Login successful
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                        // Navigate to MainActivity or Home
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Login failed
                                        Toast.makeText(LoginActivity.this, "Login failed: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Username not found or query failed
                        Toast.makeText(LoginActivity.this, "Username not found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    private void loginWithFirebase(String email, String password) {
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        // Login successful
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
//
//                        // Navigate to MainActivity or Home
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        startActivity(intent);
//                        finish();
//                    } else {
//                        // Login failed
//                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    private void loginWithFirebase(String username, String password) {
        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore to get the document with the provided username
        db.collection("users") // Assuming the collection is named "users"
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Extract the email from the retrieved document
                        String email = task.getResult().getDocuments().get(0).getString("email");

                        // Use the retrieved email to sign in
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(authTask -> {
                                    if (authTask.isSuccessful()) {
                                        // Login successful
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                        // Navigate to MainActivity or Home
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Login failed
                                        Toast.makeText(LoginActivity.this, "Login failed: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Username not found or query failed
                        Toast.makeText(LoginActivity.this, "Username not found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
