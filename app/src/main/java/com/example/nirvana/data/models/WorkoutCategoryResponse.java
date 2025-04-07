package com.example.nirvana.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WorkoutCategoryResponse {
    @SerializedName("categories")
    private List<WorkoutCategory> categories;
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    public List<WorkoutCategory> getCategories() {
        return categories;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
} 