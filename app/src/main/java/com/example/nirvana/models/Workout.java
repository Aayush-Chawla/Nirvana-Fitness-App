package com.example.nirvana.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a workout
 */
public class Workout {
    private String id;
    private String name;
    private String description;
    private String day; // e.g., "Monday", "Tuesday", etc.
    private int durationMinutes;
    private String focusArea; // e.g., "Upper Body", "Lower Body", "Core", "Full Body"
    private String intensity; // "Light", "Moderate", "High"
    private List<Exercise> exercises;

    public Workout() {
        // Required empty constructor for Firebase
        exercises = new ArrayList<>();
    }

    public Workout(String name, String description, String day, int durationMinutes, String focusArea, String intensity) {
        this.name = name;
        this.description = description;
        this.day = day;
        this.durationMinutes = durationMinutes;
        this.focusArea = focusArea;
        this.intensity = intensity;
        this.exercises = new ArrayList<>();
    }

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

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getFocusArea() {
        return focusArea;
    }

    public void setFocusArea(String focusArea) {
        this.focusArea = focusArea;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
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
} 