package com.example.nirvana.models;

import java.io.Serializable;

/**
 * Model class representing an exercise
 */
public class Exercise implements Serializable {
    private String id;
    private String name;
    private String description;
    private String muscleGroup; // e.g., "Chest", "Back", "Legs", etc.
    private String imageUrl;
    private String videoUrl;
    private int sets;
    private int reps;
    private int durationSeconds; // For timed exercises like planks
    private boolean requiresEquipment;
    private String equipmentNeeded; // e.g., "Dumbbells", "Resistance Band", etc.
    private String difficultyLevel; // "Beginner", "Intermediate", "Advanced"
    private String historyId; // ID for workout history reference

    public Exercise() {
        // Required empty constructor for Firebase
    }

    public Exercise(String id, String name, String description, String muscleGroup, 
                   String difficultyLevel, int durationSeconds, String imageUrl, String videoUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.difficultyLevel = difficultyLevel;
        this.durationSeconds = durationSeconds;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
    }

    public Exercise(String name, String description, String muscleGroup, 
                   int sets, int reps, boolean requiresEquipment) {
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.sets = sets;
        this.reps = reps;
        this.requiresEquipment = requiresEquipment;
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

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isRequiresEquipment() {
        return requiresEquipment;
    }

    public void setRequiresEquipment(boolean requiresEquipment) {
        this.requiresEquipment = requiresEquipment;
    }

    public String getEquipmentNeeded() {
        return equipmentNeeded;
    }

    public void setEquipmentNeeded(String equipmentNeeded) {
        this.equipmentNeeded = equipmentNeeded;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    /**
     * Get formatted representation of sets and reps
     */
    public String getSetsRepsFormatted() {
        if (durationSeconds > 0) {
            // This is a timed exercise
            int minutes = durationSeconds / 60;
            int seconds = durationSeconds % 60;
            if (minutes > 0) {
                return sets + " sets x " + minutes + "m" + (seconds > 0 ? " " + seconds + "s" : "");
            } else {
                return sets + " sets x " + seconds + "s";
            }
        } else {
            // This is a repetition-based exercise
            return sets + " sets x " + reps + " reps";
        }
    }

    /**
     * Convenience method for backward compatibility
     * @return duration in minutes
     */
    public int getDuration() {
        return durationSeconds / 60;  // Convert to minutes for compatibility
    }

    /**
     * Convenience method for backward compatibility
     * @param minutes duration in minutes
     */
    public void setDuration(int minutes) {
        this.durationSeconds = minutes * 60;  // Convert minutes to seconds
    }
} 