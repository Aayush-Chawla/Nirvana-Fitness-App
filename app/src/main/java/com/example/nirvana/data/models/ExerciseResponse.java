package com.example.nirvana.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExerciseResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("exercises")
    private List<ExerciseDetails> exercises;
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<ExerciseDetails> getExercises() {
        return exercises;
    }
    
    public static class ExerciseDetails {
        @SerializedName("id")
        private String id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("muscle_groups")
        private List<String> muscleGroups;
        
        @SerializedName("difficulty")
        private String difficulty;
        
        @SerializedName("equipment")
        private String equipment;
        
        @SerializedName("category")
        private String category;
        
        @SerializedName("image_url")
        private String imageUrl;
        
        @SerializedName("instructions")
        private List<String> instructions;
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<String> getMuscleGroups() {
            return muscleGroups;
        }
        
        public String getDifficulty() {
            return difficulty;
        }
        
        public String getEquipment() {
            return equipment;
        }
        
        public String getCategory() {
            return category;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public List<String> getInstructions() {
            return instructions;
        }
    }
}
