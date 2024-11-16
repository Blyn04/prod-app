package com.example.moodchecker;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvStreak;
    private EditText etName, etGmail;
    private ImageView colorOption1, colorOption2, colorOption3, avatarOption1, avatarOption2, avatarOption3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        etGmail = findViewById(R.id.etGmail);
        tvStreak = findViewById(R.id.tvStreak);

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

        // Handle Save Changes button
        findViewById(R.id.btnSaveChanges).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = etName.getText().toString();
                String newGmail = etGmail.getText().toString();

                // Update the profile with the new values
                tvName.setText(newName);

                // Show toast message
                Toast.makeText(ProfileActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateColor(String color) {
        // Update color logic (e.g., change background or store selection)
        Toast.makeText(this, "Color updated to " + color, Toast.LENGTH_SHORT).show();
    }

    private void updateAvatar(int avatarId) {
        // Update avatar logic (e.g., store selected avatar ID)
        Toast.makeText(this, "Avatar " + avatarId + " selected", Toast.LENGTH_SHORT).show();
    }

    private void saveChanges() {
        Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
    }
}