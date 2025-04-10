package com.example.nirvana.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.nirvana.models.Exercise;
import com.example.nirvana.models.UserProfile;
import com.example.nirvana.models.Workout;
import com.example.nirvana.models.WorkoutPlan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Service to generate personalized workout plans based on user preferences
 */
public class WorkoutPlanGenerator {
    private static final String TAG = "WorkoutPlanGenerator";
    
    private final Context context;
    private final Executor executor;
    private final Handler mainHandler;
    private final Random random;
    
    // Sample exercise data
    private static final List<String> MUSCLE_GROUPS = Arrays.asList(
            "Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Full Body");
    
    private static final List<String> BODY_WEIGHT_EXERCISES = Arrays.asList(
            "Push-Ups", "Pull-Ups", "Squats", "Lunges", "Planks", "Mountain Climbers", 
            "Burpees", "Sit-Ups", "Crunches", "Jumping Jacks", "Tricep Dips", 
            "Glute Bridges", "Superman", "Bird Dog", "Bicycle Crunches");
    
    private static final List<String> EQUIPMENT_EXERCISES = Arrays.asList(
            "Bench Press", "Deadlift", "Barbell Squat", "Lat Pulldown", "Shoulder Press", 
            "Bicep Curls", "Tricep Extensions", "Leg Press", "Leg Extensions", 
            "Hamstring Curls", "Cable Rows", "Dumbbell Flyes", "Kettlebell Swings", 
            "Kettlebell Goblet Squats", "Resistance Band Rows");
    
    public interface PlanGenerationCallback {
        void onPlanGenerated(WorkoutPlan plan);
        void onError(String message);
    }
    
    public WorkoutPlanGenerator(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.random = new Random();
    }
    
    /**
     * Generate a personalized workout plan based on user profile and preferences
     */
    public void generatePersonalizedPlan(UserProfile userProfile, Map<String, Object> preferences, 
                                         PlanGenerationCallback callback) {
        executor.execute(() -> {
            try {
                // Extract preferences
                int daysPerWeek = (int) preferences.get("daysPerWeek");
                int timePerWorkout = (int) preferences.get("timePerWorkout");
                boolean hasEquipment = (boolean) preferences.get("hasEquipment");
                String primaryGoal = (String) preferences.get("primaryGoal");
                
                // For now, create a demo plan
                WorkoutPlan plan = createDemoPlan(userProfile, daysPerWeek, timePerWorkout, 
                                                 hasEquipment, primaryGoal);
                
                // Notify callback on main thread
                mainHandler.post(() -> callback.onPlanGenerated(plan));
            } catch (Exception e) {
                Log.e(TAG, "Error generating workout plan", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    /**
     * Create a demo workout plan with sample data
     */
    private WorkoutPlan createDemoPlan(UserProfile profile, int daysPerWeek, 
                                      int timePerWorkout, boolean hasEquipment, 
                                      String primaryGoal) {
        // Create plan name and summary
        String planName = profile.getName() + "'s " + daysPerWeek + "-Day " + 
                         profile.getFitnessLevel() + " " + primaryGoal + " Plan";
        
        StringBuilder summary = new StringBuilder();
        summary.append("This personalized plan is designed for a ").append(profile.getFitnessLevel())
                .append(" with a primary goal of ").append(primaryGoal).append(".\n\n");
        summary.append("• Workout Days: ").append(daysPerWeek).append(" days per week\n");
        summary.append("• Duration: ").append(timePerWorkout).append(" minutes per session\n");
        summary.append("• Equipment: ").append(hasEquipment ? "Yes" : "No").append("\n\n");
        summary.append("Follow this plan consistently for 4-6 weeks for best results.");
        
        // Create the plan
        WorkoutPlan plan = new WorkoutPlan(planName, summary.toString(), profile.getFitnessLevel(), 
                                          primaryGoal, daysPerWeek, timePerWorkout, hasEquipment);
        
        // Create workouts for each day
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < daysPerWeek; i++) {
            String day = days[i];
            String focusArea = getFocusAreaForDay(i, daysPerWeek);
            
            // Create workout
            Workout workout = createWorkoutForDay(day, focusArea, timePerWorkout, hasEquipment, 
                                                 profile.getFitnessLevel());
            plan.addWorkout(day, workout);
        }
        
        return plan;
    }
    
    /**
     * Determine focus area for each day based on number of days per week
     */
    private String getFocusAreaForDay(int dayIndex, int totalDays) {
        if (totalDays <= 3) {
            // Full body workouts for 3 or fewer days
            return "Full Body";
        } else if (totalDays == 4) {
            // Upper/Lower split for 4 days
            return dayIndex % 2 == 0 ? "Upper Body" : "Lower Body";
        } else {
            // Push/Pull/Legs for 5+ days
            switch (dayIndex % 5) {
                case 0: return "Chest";
                case 1: return "Back";
                case 2: return "Legs";
                case 3: return "Shoulders";
                case 4: return "Arms";
                default: return "Core";
            }
        }
    }
    
    /**
     * Create a workout for a specific day
     */
    private Workout createWorkoutForDay(String day, String focusArea, int durationMinutes, 
                                       boolean hasEquipment, String fitnessLevel) {
        // Create workout name and description
        String name = focusArea + " Workout";
        String description = "A " + durationMinutes + "-minute " + focusArea.toLowerCase() + 
                            " workout designed for " + fitnessLevel.toLowerCase() + " fitness levels.";
        
        // Create workout
        Workout workout = new Workout(name, description, day, durationMinutes, focusArea, 
                                     getIntensityForLevel(fitnessLevel));
        
        // Add exercises
        int numExercises = durationMinutes / 5; // Approximately one exercise per 5 minutes
        for (int i = 0; i < numExercises; i++) {
            Exercise exercise = createExercise(focusArea, hasEquipment, fitnessLevel);
            workout.addExercise(exercise);
        }
        
        return workout;
    }
    
    /**
     * Create a random exercise
     */
    private Exercise createExercise(String focusArea, boolean hasEquipment, String fitnessLevel) {
        // Get a exercise name based on equipment availability
        List<String> exerciseList = hasEquipment ? EQUIPMENT_EXERCISES : BODY_WEIGHT_EXERCISES;
        String exerciseName = exerciseList.get(random.nextInt(exerciseList.size()));
        
        // Create basic information
        String description = "Perform this exercise with proper form, focusing on controlled movements.";
        
        // Sets and reps based on fitness level
        int sets = getSetsForLevel(fitnessLevel);
        int reps = getRepsForLevel(fitnessLevel);
        
        // Create and return exercise
        Exercise exercise = new Exercise(exerciseName, description, focusArea, sets, reps, hasEquipment);
        exercise.setDifficultyLevel(fitnessLevel);
        
        return exercise;
    }
    
    /**
     * Get intensity based on fitness level
     */
    private String getIntensityForLevel(String fitnessLevel) {
        switch (fitnessLevel) {
            case "Beginner":
                return "Light";
            case "Intermediate":
                return "Moderate";
            case "Advanced":
                return "High";
            default:
                return "Moderate";
        }
    }
    
    /**
     * Get number of sets based on fitness level
     */
    private int getSetsForLevel(String fitnessLevel) {
        switch (fitnessLevel) {
            case "Beginner":
                return 2 + random.nextInt(2); // 2-3 sets
            case "Intermediate":
                return 3 + random.nextInt(2); // 3-4 sets
            case "Advanced":
                return 4 + random.nextInt(2); // 4-5 sets
            default:
                return 3;
        }
    }
    
    /**
     * Get number of reps based on fitness level
     */
    private int getRepsForLevel(String fitnessLevel) {
        switch (fitnessLevel) {
            case "Beginner":
                return 8 + random.nextInt(5); // 8-12 reps
            case "Intermediate":
                return 10 + random.nextInt(5); // 10-14 reps
            case "Advanced":
                return 12 + random.nextInt(5); // 12-16 reps
            default:
                return 10;
        }
    }
} 