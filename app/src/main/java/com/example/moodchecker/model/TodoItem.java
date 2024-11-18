package com.example.moodchecker.model;

public class TodoItem {
    private String name;
    private String status;
    private String deadline;
    private long timerDuration;

    // No-argument constructor (required for deserialization)
    public TodoItem() {
        // Initialize fields with default values if needed
        this.name = "";
        this.status = "";
        this.deadline = "";
        this.timerDuration = 0;
    }

    // Constructor with arguments (your original constructor)
    public TodoItem(String name, String status, String deadline) {
        this.name = name;
        this.status = status;
        this.deadline = deadline;
        this.timerDuration = 0;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getDeadline() { return deadline; }
    public long getTimerDuration() { return timerDuration; }
    public void setTimerDuration(long timerDuration) { this.timerDuration = timerDuration; }
}
