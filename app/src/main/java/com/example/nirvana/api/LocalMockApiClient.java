package com.example.nirvana.api;

import android.content.Context;
import android.util.Log;

import com.example.nirvana.models.FoodItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock API client that provides local data without making network calls
 * Used for testing and development
 */
public class LocalMockApiClient {
    private static final String TAG = "LocalMockApiClient";
    private final Context context;

    public LocalMockApiClient(Context context) {
        this.context = context;
        Log.d(TAG, "LocalMockApiClient initialized");
    }

    /**
     * Get a list of food items that match the search term
     * @param searchTerm Search term
     * @return List of matching food items
     */
    public List<FoodItem> searchFoodItems(String searchTerm) {
        List<FoodItem> results = new ArrayList<>();
        
        // Add dummy food items for testing
        if (searchTerm == null || searchTerm.isEmpty() || "chicken".contains(searchTerm.toLowerCase())) {
            FoodItem chicken = new FoodItem("1", "Grilled Chicken Breast", 165, "100g", 31, 0, 3.6);
            results.add(chicken);
        }
        
        if (searchTerm == null || searchTerm.isEmpty() || "rice".contains(searchTerm.toLowerCase())) {
            FoodItem rice = new FoodItem("2", "Brown Rice", 111, "100g", 2.6, 23, 0.9);
            results.add(rice);
        }
        
        if (searchTerm == null || searchTerm.isEmpty() || "broccoli".contains(searchTerm.toLowerCase())) {
            FoodItem broccoli = new FoodItem("3", "Steamed Broccoli", 55, "100g", 3.7, 11.2, 0.6);
            results.add(broccoli);
        }
        
        return results;
    }

    /**
     * Get predefined workout routines
     * @return List of workout routines
     */
    public List<String> getPredefinedWorkouts() {
        List<String> workouts = new ArrayList<>();
        workouts.add("Upper Body Strength");
        workouts.add("Lower Body Power");
        workouts.add("Full Body HIIT");
        workouts.add("Core Stability");
        workouts.add("Cardio Endurance");
        return workouts;
    }
} 