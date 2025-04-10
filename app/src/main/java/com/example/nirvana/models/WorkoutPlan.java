package com.example.nirvana.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class for workout plan data
 */
public class WorkoutPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String description;
    private String focus; // Main focus area
    private int daysPerWeek;
    private int weeks;
    private Map<String, Workout> workouts; // Key is the day number (1-7) as String
    
    public WorkoutPlan() {
        workouts = new HashMap<>();
    }
    
    public WorkoutPlan(String id, String title, String description, String focus, int daysPerWeek, int weeks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.focus = focus;
        this.daysPerWeek = daysPerWeek;
        this.weeks = weeks;
        this.workouts = new HashMap<>();
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFocus() {
        return focus;
    }
    
    public void setFocus(String focus) {
        this.focus = focus;
    }
    
    public int getDaysPerWeek() {
        return daysPerWeek;
    }
    
    public void setDaysPerWeek(int daysPerWeek) {
        this.daysPerWeek = daysPerWeek;
    }
    
    public int getWeeks() {
        return weeks;
    }
    
    public void setWeeks(int weeks) {
        this.weeks = weeks;
    }
    
    public Map<String, Workout> getWorkouts() {
        return workouts;
    }
    
    public void setWorkouts(Map<String, Workout> workouts) {
        this.workouts = workouts;
    }
    
    /**
     * Add a workout for a specific day
     * @param day Day number (1-7)
     * @param workout Workout object
     */
    public void addWorkout(int day, Workout workout) {
        if (workouts == null) {
            workouts = new HashMap<>();
        }
        workouts.put(String.valueOf(day), workout);
    }
    
    /**
     * Get workout for a specific day
     * @param day Day number (1-7)
     * @return Workout object or null if not found
     */
    public Workout getWorkout(int day) {
        if (workouts == null) {
            return null;
        }
        return workouts.get(String.valueOf(day));
    }
    
    /**
     * Get workout for a specific day by name
     * @param dayName Name of the day (e.g., "Monday")
     * @return Workout object or null if not found
     */
    public Workout getWorkoutByDay(String dayName) {
        if (workouts == null) {
            return null;
        }
        
        int dayNum;
        switch (dayName.toLowerCase()) {
            case "monday": dayNum = 1; break;
            case "tuesday": dayNum = 2; break;
            case "wednesday": dayNum = 3; break;
            case "thursday": dayNum = 4; break;
            case "friday": dayNum = 5; break;
            case "saturday": dayNum = 6; break;
            case "sunday": dayNum = 7; break;
            default: return null;
        }
        
        return workouts.get(String.valueOf(dayNum));
    }
    
    /**
     * Get total workouts in the plan
     * @return Number of workouts
     */
    public int getTotalWorkouts() {
        return workouts != null ? workouts.size() : 0;
    }
    
    /**
     * Get plan summary as formatted text
     * @return Formatted plan summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(title).append("\n");
        
        if (description != null && !description.isEmpty()) {
            summary.append(description).append("\n\n");
        }
        
        summary.append("Focus: ").append(focus).append("\n");
        summary.append("Duration: ").append(weeks).append(" weeks\n");
        summary.append("Workout Days: ").append(daysPerWeek).append(" days per week\n\n");
        
        // Add brief info about each workout
        if (workouts != null && !workouts.isEmpty()) {
            for (Map.Entry<String, Workout> entry : workouts.entrySet()) {
                int day = Integer.parseInt(entry.getKey());
                String dayName;
                switch (day) {
                    case 1: dayName = "Monday"; break;
                    case 2: dayName = "Tuesday"; break;
                    case 3: dayName = "Wednesday"; break;
                    case 4: dayName = "Thursday"; break;
                    case 5: dayName = "Friday"; break;
                    case 6: dayName = "Saturday"; break;
                    case 7: dayName = "Sunday"; break;
                    default: dayName = "Day " + day;
                }
                
                Workout workout = entry.getValue();
                summary.append("- ").append(dayName).append(": ")
                       .append(workout.getTitle()).append(" (")
                       .append(workout.getDuration()).append(" min)\n");
            }
        }
        
        return summary.toString();
    }
} 