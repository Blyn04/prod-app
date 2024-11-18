package com.example.moodchecker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

public class TimerActivity extends AppCompatActivity {
    private TextView taskNameTextView;
    private TextView deadlineTextView;
    private TextView timerTextView;
    private long timerDuration;
    private Handler handler;
    private Runnable timerRunnable;
    private Button pauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        taskNameTextView = findViewById(R.id.taskNameTextView);
        deadlineTextView = findViewById(R.id.deadlineTextView);
        timerTextView = findViewById(R.id.timerTextView);
        pauseButton = findViewById(R.id.pauseButton);

        Intent intent = getIntent();
        String taskName = intent.getStringExtra("taskName");
        String deadline = intent.getStringExtra("deadline");
        timerDuration = intent.getLongExtra("timerDuration", 0); // Get timer duration in milliseconds

        taskNameTextView.setText(taskName);
        deadlineTextView.setText(deadline);

        handler = new Handler();

        // Timer logic
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timerDuration > 0) {
                    timerDuration -= 1000; // Decrease by 1 second
                    long hours = timerDuration / 3600000;
                    long minutes = (timerDuration % 3600000) / 60000;
                    long seconds = (timerDuration % 60000) / 1000;

                    timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                    handler.postDelayed(this, 1000); // Repeat every second
                } else {
                    timerTextView.setText("00:00:00");
                    handler.removeCallbacks(timerRunnable); // Stop the timer
                }
            }
        };

        handler.post(timerRunnable); // Start the timer

        pauseButton.setOnClickListener(v -> {
            handler.removeCallbacks(timerRunnable); // Pause the timer
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(timerRunnable); // Stop the handler when the activity is destroyed
        }
    }
}

