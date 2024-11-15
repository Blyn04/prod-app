package com.example.moodchecker.model;

public class TodoItem {
    private String name;
    private String status;
    private String deadline;

    public TodoItem(String name, String status, String deadline) {
        this.name = name;
        this.status = status;
        this.deadline = deadline;
    }

    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getDeadline() { return deadline; }
}
