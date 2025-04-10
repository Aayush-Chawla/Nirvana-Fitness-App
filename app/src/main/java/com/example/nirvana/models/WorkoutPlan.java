package com.example.nirvana.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a complete workout plan
 */
public class WorkoutPlan {
    private String id;
    private String name;
    private String summary;
    private String fitnessLevel;
    private String primaryGoal;
    private int daysPerWeek;
    private int timePerWorkoutMinutes;
    private boolean requiresEquipment;
    private Map<String, Workout> workouts;

    public WorkoutPlan() {
        // Required empty constructor for Firebase
        workouts = new HashMap<>();
    }

    public WorkoutPlan(String name, String summary, String fitnessLevel, 
                      String primaryGoal, int daysPerWeek, 
                      int timePerWorkoutMinutes, boolean requiresEquipment) {
        this.name = name;
        this.summary = summary;
        this.fitnessLevel = fitnessLevel;
        this.primaryGoal = primaryGoal;
        this.daysPerWeek = daysPerWeek;
        this.timePerWorkoutMinutes = timePerWorkoutMinutes;
        this.requiresEquipment = requiresEquipment;
        this.workouts = new HashMap<>();
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFitnessLevel() {
        return fitnessLevel;
    }

    public void setFitnessLevel(String fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
    }

    public String getPrimaryGoal() {
        return primaryGoal;
    }

    public void setPrimaryGoal(String primaryGoal) {
        this.primaryGoal = primaryGoal;
    }

    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    public void setDaysPerWeek(int daysPerWeek) {
        this.daysPerWeek = daysPerWeek;
    }

    public int getTimePerWorkoutMinutes() {
        return timePerWorkoutMinutes;
    }

    public void setTimePerWorkoutMinutes(int timePerWorkoutMinutes) {
        this.timePerWorkoutMinutes = timePerWorkoutMinutes;
    }

    public boolean isRequiresEquipment() {
        return requiresEquipment;
    }

    public void setRequiresEquipment(boolean requiresEquipment) {
        this.requiresEquipment = requiresEquipment;
    }

    public Map<String, Workout> getWorkouts() {
        return workouts;
    }

    public void setWorkouts(Map<String, Workout> workouts) {
        this.workouts = workouts;
    }

    public void addWorkout(String day, Workout workout) {
        if (workouts == null) {
            workouts = new HashMap<>();
        }
        workouts.put(day, workout);
    }
} 