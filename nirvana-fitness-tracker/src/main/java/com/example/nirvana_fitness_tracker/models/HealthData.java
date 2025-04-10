package com.example.nirvana_fitness_tracker.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class to store health and fitness tracking data
 */
public class HealthData {
    private long steps;
    private double heartRate;
    private double calories;
    private double distance;
    private long timestamp;
    private String userId;

    public HealthData() {
        // Required empty constructor for Firebase
        this.timestamp = System.currentTimeMillis();
    }

    public HealthData(long steps, double heartRate, double calories, double distance, String userId) {
        this.steps = steps;
        this.heartRate = heartRate;
        this.calories = calories;
        this.distance = distance;
        this.timestamp = System.currentTimeMillis();
        this.userId = userId;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(double heartRate) {
        this.heartRate = heartRate;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Convert to a Map for Firebase
     */
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("steps", steps);
        result.put("heartRate", heartRate);
        result.put("calories", calories);
        result.put("distance", distance);
        result.put("timestamp", timestamp);
        result.put("userId", userId);
        
        return result;
    }
} 