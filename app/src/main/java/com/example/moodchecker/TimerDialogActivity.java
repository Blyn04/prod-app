package com.example.moodchecker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TimerDialogActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_dialog);

        // Retrieve any passed message
        String message = getIntent().getStringExtra("message");

        TextView messageTextView = findViewById(R.id.messageTextView);
        messageTextView.setText(message);

        Button closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> finish()); // Close the dialog on button click
    }
}
