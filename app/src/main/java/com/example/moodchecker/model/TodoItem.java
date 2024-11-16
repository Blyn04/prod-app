package com.example.moodchecker.model;

public class TodoItem {
    private String name;
    private String status;
    private String deadline;
    private long timerDuration;

    public TodoItem(String name, String status, String deadline, long timerDuration) {
        this.name = name;
        this.status = status;
        this.deadline = deadline;
        this.timerDuration = timerDuration;
    }

    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getDeadline() { return deadline; }

    public long getTimerDuration() {
        return timerDuration;
    }

    public void setTimerDuration(long timerDuration) {
        this.timerDuration = timerDuration;
    }
}
