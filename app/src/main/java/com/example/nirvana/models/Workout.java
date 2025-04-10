package com.example.nirvana.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for workout data
 */
public class Workout implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String type; // Push, Pull, Legs, Cardio, HIIT, etc.
    private String description;
    private int duration; // in minutes
    private List<Exercise> exercises;
    
    public Workout() {
        exercises = new ArrayList<>();
    }
    
    public Workout(String id, String title, String type, String description, int duration) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.description = description;
        this.duration = duration;
        this.exercises = new ArrayList<>();
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
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
    
    /**
     * Calculate estimated calories burned during this workout
     * @param weight User's weight in kg
     * @return Estimated calories burned
     */
    public int estimateCaloriesBurned(float weight) {
        // Simple estimate based on workout type and duration
        int caloriesPerMinute;
        
        switch (type.toLowerCase()) {
            case "hiit":
                caloriesPerMinute = 12; // High intensity
                break;
            case "cardio":
                caloriesPerMinute = 10; // Moderate-high intensity
                break;
            case "push":
            case "pull":
            case "legs":
                caloriesPerMinute = 8; // Moderate intensity
                break;
            default:
                caloriesPerMinute = 6; // Lower intensity
        }
        
        // Adjust based on weight (reference weight of 70kg)
        float weightFactor = weight / 70f;
        
        return Math.round(caloriesPerMinute * duration * weightFactor);
    }
    
    /**
     * Get a summary of the workout
     * @return Summary description
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(title).append(" (").append(duration).append(" min)\n");
        summary.append("Type: ").append(type).append("\n");
        
        if (exercises != null && !exercises.isEmpty()) {
            summary.append("Exercises: ").append(exercises.size()).append("\n");
            for (int i = 0; i < Math.min(exercises.size(), 3); i++) {
                Exercise exercise = exercises.get(i);
                summary.append("- ").append(exercise.getName());
                if (exercise.getSets() > 0 && exercise.getReps() > 0) {
                    summary.append(": ").append(exercise.getSets()).append(" sets x ")
                           .append(exercise.getReps()).append(" reps");
                } else if (exercise.getDuration() > 0) {
                    summary.append(": ").append(exercise.getDuration()).append(" min");
                }
                summary.append("\n");
            }
            
            if (exercises.size() > 3) {
                summary.append("- and ").append(exercises.size() - 3).append(" more...\n");
            }
        }
        
        return summary.toString();
    }
} 