package com.example.moodchecker.model;

public class Reviewer {
    private String name;
    private String description;

    // Default constructor required for Firestore serialization
    public Reviewer() {
    }

    // Constructor to initialize name and description
    public Reviewer(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }
}
