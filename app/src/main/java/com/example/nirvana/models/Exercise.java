package com.example.nirvana.models;

import java.io.Serializable;

/**
 * Model class for exercise data
 */
public class Exercise implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String description;
    private int sets; // Number of sets, if applicable
    private int reps; // Number of repetitions per set, if applicable
    private int duration; // Duration in minutes, if applicable
    private boolean requiresEquipment;
    private int difficulty; // 1-5 scale
    private String muscleGroup; // Primary muscle group
    private String imageUrl;
    private String videoUrl;
    private String category; // Added for compatibility with existing code
    private String historyId; // Added for compatibility with existing code
    
    public Exercise() {
        // Required empty constructor for Firebase
    }
    
    public Exercise(String id, String name, String description, 
                   String category, String difficultyLevel, int duration, 
                   String imageUrl, String videoUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        
        // Convert difficulty level string to int
        switch (difficultyLevel != null ? difficultyLevel.toLowerCase() : "") {
            case "beginner":
                this.difficulty = 1;
                break;
            case "intermediate":
                this.difficulty = 3;
                break;
            case "advanced":
                this.difficulty = 5;
                break;
            default:
                this.difficulty = 1;
                break;
        }
        
        // Default values
        this.sets = 3;
        this.reps = 10;
        this.requiresEquipment = false;
        this.muscleGroup = category;
    }
    
    // Getters and setters
    
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
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public boolean isRequiresEquipment() {
        return requiresEquipment;
    }
    
    public void setRequiresEquipment(boolean requiresEquipment) {
        this.requiresEquipment = requiresEquipment;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
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
    
    // Added getters and setters for compatibility
    
    public String getCategory() {
        return category != null ? category : muscleGroup;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getHistoryId() {
        return historyId != null ? historyId : id;
    }
    
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }
    
    /**
     * Get exercise details as formatted text
     * @return Formatted exercise details
     */
    public String getFormattedDetails() {
        StringBuilder details = new StringBuilder();
        details.append(name).append("\n");
        
        if (description != null && !description.isEmpty()) {
            details.append(description).append("\n\n");
        }
        
        details.append("Muscle Group: ").append(muscleGroup).append("\n");
        details.append("Difficulty: ");
        for (int i = 0; i < difficulty; i++) {
            details.append("★");
        }
        for (int i = difficulty; i < 5; i++) {
            details.append("☆");
        }
        details.append("\n");
        
        if (sets > 0 && reps > 0) {
            details.append("Sets: ").append(sets).append("\n");
            details.append("Reps: ").append(reps).append("\n");
        } else if (duration > 0) {
            details.append("Duration: ").append(duration).append(" min\n");
        }
        
        details.append("Equipment Needed: ").append(requiresEquipment ? "Yes" : "No").append("\n");
        
        return details.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Exercise exercise = (Exercise) obj;
        
        if (id != null) {
            return id.equals(exercise.id);
        } else {
            return name.equals(exercise.name) && muscleGroup.equals(exercise.muscleGroup);
        }
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (muscleGroup != null ? muscleGroup.hashCode() : 0);
        return result;
    }
} 