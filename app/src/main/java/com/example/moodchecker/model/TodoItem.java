package com.example.moodchecker.model;

public class TodoItem {
    private String id;
    private String name;
    private String status;
    private String deadline;
    private long timerDuration;
    private boolean checked;

    // No-argument constructor (required for deserialization)
    public TodoItem() {
        // Initialize fields with default values if needed
        this.id = "";
        this.name = "";
        this.status = "";
        this.deadline = "";
        this.timerDuration = 0;
    }

    // Constructor with arguments (your original constructor)
    public TodoItem(String name, String status, String deadline, String id) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.deadline = deadline;
        this.timerDuration = 0;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getDeadline() { return deadline; }
    public long getTimerDuration() { return timerDuration; }
    public void setTimerDuration(long timerDuration) { this.timerDuration = timerDuration; }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
