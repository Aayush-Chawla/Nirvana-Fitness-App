package com.example.nirvana.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomWorkout implements Serializable {
    private String id;
    private String name;
    private String description;
    private String createdBy; // User ID
    private long createdAt;
    private List<Exercise> exercises;

    public CustomWorkout() {
        // Required empty constructor for Firebase
        exercises = new ArrayList<>();
    }

    public CustomWorkout(String id, String name, String description, String createdBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.exercises = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public void addExercise(Exercise exercise) {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        exercises.add(exercise);
    }

    public void removeExercise(Exercise exercise) {
        if (exercises != null) {
            exercises.remove(exercise);
        }
    }

    public void removeExercise(int position) {
        if (exercises != null && position >= 0 && position < exercises.size()) {
            exercises.remove(position);
        }
    }

    public int getTotalDuration() {
        if (exercises == null || exercises.isEmpty()) {
            return 0;
        }
        
        int totalDuration = 0;
        for (Exercise exercise : exercises) {
            totalDuration += exercise.getDuration();
        }
        return totalDuration;
    }
} 