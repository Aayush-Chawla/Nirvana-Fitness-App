package com.example.nirvana.services;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.example.nirvana.models.Exercise;
import com.example.nirvana.models.UserProfile;
import com.example.nirvana.models.Workout;
import com.example.nirvana.models.WorkoutPlan;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service that generates personalized workout plans based on user profile, goals, and preferences
 */
public class WorkoutPlanGenerator {
    private static final String TAG = "WorkoutPlanGenerator";
    private static final String MODEL_PATH = "workout_plan_recommender.tflite";
    
    private Context context;
    private Interpreter planGeneratorModel;
    private DatabaseReference database;
    private Map<String, List<Exercise>> exerciseDatabase;
    
    public interface PlanGenerationCallback {
        void onPlanGenerated(WorkoutPlan plan);
        void onError(String message);
    }
    
    public WorkoutPlanGenerator(Context context) {
        this.context = context;
        
        // Initialize Firebase
        try {
            database = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
        
        // Try to load TensorFlow Lite model
        try {
            planGeneratorModel = new Interpreter(loadModelFile(MODEL_PATH));
            Log.d(TAG, "Workout plan model loaded successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error loading workout plan model", e);
            // Will use rule-based generation as fallback
        }
        
        // Initialize exercise database
        initializeExerciseDatabase();
    }
    
    /**
     * Generate a personalized workout plan based on user profile and preferences
     */
    public void generatePersonalizedPlan(UserProfile userProfile, Map<String, Object> preferences, 
                                        PlanGenerationCallback callback) {
        if (userProfile == null) {
            callback.onError("User profile is required");
            return;
        }
        
        try {
            WorkoutPlan plan;
            
            // Use ML model if available
            if (planGeneratorModel != null) {
                plan = generatePlanWithModel(userProfile, preferences);
            } else {
                // Fallback to rule-based generation
                plan = generatePlanWithRules(userProfile, preferences);
            }
            
            // Save plan to Firebase if user is logged in
            savePlanToFirebase(plan, userProfile.getUserId());
            
            // Return plan through callback
            callback.onPlanGenerated(plan);
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating workout plan", e);
            callback.onError("Failed to generate workout plan: " + e.getMessage());
        }
    }
    
    /**
     * Generate workout plan using TensorFlow Lite model
     */
    private WorkoutPlan generatePlanWithModel(UserProfile userProfile, Map<String, Object> preferences) {
        // Prepare input features for the model
        float[] features = prepareModelFeatures(userProfile, preferences);
        
        // Run inference
        float[][] output = new float[1][20]; // Example output size
        planGeneratorModel.run(features, output);
        
        // Process output and create a plan
        return decodePlanFromOutput(output[0], userProfile, preferences);
    }
    
    /**
     * Generate workout plan using rule-based approach (fallback method)
     */
    private WorkoutPlan generatePlanWithRules(UserProfile userProfile, Map<String, Object> preferences) {
        WorkoutPlan plan = new WorkoutPlan();
        
        // Extract preferences
        int daysPerWeek = preferences.containsKey("daysPerWeek") ? 
                (int) preferences.get("daysPerWeek") : 3;
        boolean hasEquipment = preferences.containsKey("hasEquipment") ? 
                (boolean) preferences.get("hasEquipment") : false;
        int timePerWorkout = preferences.containsKey("timePerWorkout") ? 
                (int) preferences.get("timePerWorkout") : 45;
        String primaryGoal = preferences.containsKey("primaryGoal") ? 
                (String) preferences.get("primaryGoal") : "General Fitness";
        
        // Set plan metadata
        plan.setTitle(userProfile.getName() + "'s " + primaryGoal + " Plan");
        plan.setDescription("Custom workout plan designed for your " + primaryGoal + " goals");
        plan.setDaysPerWeek(daysPerWeek);
        plan.setWeeks(8); // Default to 8-week plan
        
        // Determine focus areas based on goal
        List<String> focusAreas = new ArrayList<>();
        if (primaryGoal.contains("Strength") || primaryGoal.contains("Muscle")) {
            focusAreas.add("Strength");
            plan.setFocus("Strength Training");
        } else if (primaryGoal.contains("Weight") || primaryGoal.contains("Fat")) {
            focusAreas.add("Cardio");
            focusAreas.add("HIIT");
            plan.setFocus("Weight Loss");
        } else if (primaryGoal.contains("Endurance")) {
            focusAreas.add("Cardio");
            focusAreas.add("Endurance");
            plan.setFocus("Cardiovascular Endurance");
        } else {
            focusAreas.add("FullBody");
            plan.setFocus("General Fitness");
        }
        
        // Create workouts for each day
        for (int day = 1; day <= daysPerWeek; day++) {
            Workout workout = createWorkout(day, focusAreas, hasEquipment, timePerWorkout, userProfile.getFitnessLevel());
            plan.addWorkout(day, workout);
        }
        
        return plan;
    }
    
    /**
     * Create a single workout for a specific day
     */
    private Workout createWorkout(int day, List<String> focusAreas, boolean hasEquipment, 
                                int timePerWorkout, String fitnessLevel) {
        Workout workout = new Workout();
        
        // Determine workout type based on day and focus areas
        String workoutType;
        if (focusAreas.contains("Strength")) {
            switch (day % 3) {
                case 1: workoutType = "Push"; break;
                case 2: workoutType = "Pull"; break;
                case 0: workoutType = "Legs"; break;
                default: workoutType = "FullBody";
            }
        } else if (focusAreas.contains("Cardio") && focusAreas.contains("HIIT")) {
            workoutType = day % 2 == 0 ? "HIIT" : "Cardio";
        } else if (focusAreas.contains("Cardio")) {
            workoutType = "Cardio";
        } else {
            workoutType = "FullBody";
        }
        
        // Set workout metadata
        workout.setTitle(workoutType + " Workout");
        workout.setType(workoutType);
        workout.setDuration(timePerWorkout);
        
        // Determine exercise count based on workout duration
        int exerciseCount = Math.max(4, timePerWorkout / 10);
        
        // Get suitable exercises for this workout type
        List<Exercise> suitableExercises = getExercisesForType(workoutType, hasEquipment, fitnessLevel);
        
        // Select random exercises from the suitable ones
        Random random = new Random();
        List<Exercise> selectedExercises = new ArrayList<>();
        for (int i = 0; i < exerciseCount && !suitableExercises.isEmpty(); i++) {
            int index = random.nextInt(suitableExercises.size());
            selectedExercises.add(suitableExercises.get(index));
            suitableExercises.remove(index);
        }
        
        // Add exercises to workout
        workout.setExercises(selectedExercises);
        
        return workout;
    }
    
    /**
     * Get suitable exercises for a specific workout type
     */
    private List<Exercise> getExercisesForType(String workoutType, boolean hasEquipment, String fitnessLevel) {
        List<Exercise> exercises = new ArrayList<>();
        
        // Get exercises from the database by type
        List<Exercise> typeExercises = exerciseDatabase.get(workoutType);
        if (typeExercises != null) {
            for (Exercise exercise : typeExercises) {
                // Filter based on equipment availability
                if (!hasEquipment && exercise.isRequiresEquipment()) {
                    continue;
                }
                
                // Filter based on fitness level
                if (fitnessLevel.equals("Beginner") && exercise.getDifficulty() > 3) {
                    continue;
                }
                
                exercises.add(exercise);
            }
        }
        
        // If not enough exercises for this type, add some generic ones
        if (exercises.size() < 5) {
            List<Exercise> genericExercises = exerciseDatabase.get("Generic");
            if (genericExercises != null) {
                for (Exercise exercise : genericExercises) {
                    if (!hasEquipment && exercise.isRequiresEquipment()) {
                        continue;
                    }
                    exercises.add(exercise);
                }
            }
        }
        
        return exercises;
    }
    
    /**
     * Prepare feature vector for the model input
     */
    private float[] prepareModelFeatures(UserProfile userProfile, Map<String, Object> preferences) {
        float[] features = new float[15]; // Example size
        
        // User basic info (normalize to 0-1 range)
        features[0] = userProfile.getAge() / 100f;
        features[1] = userProfile.getGender().equalsIgnoreCase("male") ? 1.0f : 0.0f;
        features[2] = userProfile.getWeight() / 150f;
        features[3] = userProfile.getHeight() / 200f;
        
        // Fitness level
        String fitnessLevel = userProfile.getFitnessLevel();
        if (fitnessLevel.equalsIgnoreCase("Beginner")) {
            features[4] = 0.0f;
        } else if (fitnessLevel.equalsIgnoreCase("Intermediate")) {
            features[4] = 0.5f;
        } else {
            features[4] = 1.0f;
        }
        
        // Goal encoding
        String goal = preferences.containsKey("primaryGoal") ? 
                (String) preferences.get("primaryGoal") : "General Fitness";
        
        features[5] = goal.contains("Strength") ? 1.0f : 0.0f;
        features[6] = goal.contains("Weight Loss") ? 1.0f : 0.0f;
        features[7] = goal.contains("Endurance") ? 1.0f : 0.0f;
        features[8] = goal.contains("Muscle") ? 1.0f : 0.0f;
        
        // Equipment availability
        boolean hasEquipment = preferences.containsKey("hasEquipment") ? 
                (boolean) preferences.get("hasEquipment") : false;
        features[9] = hasEquipment ? 1.0f : 0.0f;
        
        // Time availability
        int daysPerWeek = preferences.containsKey("daysPerWeek") ? 
                (int) preferences.get("daysPerWeek") : 3;
        int timePerWorkout = preferences.containsKey("timePerWorkout") ? 
                (int) preferences.get("timePerWorkout") : 45;
        
        features[10] = daysPerWeek / 7.0f;
        features[11] = timePerWorkout / 120.0f;
        
        // Injury status
        boolean hasInjury = preferences.containsKey("hasInjury") ? 
                (boolean) preferences.get("hasInjury") : false;
        features[12] = hasInjury ? 1.0f : 0.0f;
        
        // Fill in remaining features with defaults
        features[13] = 0.5f;
        features[14] = 0.5f;
        
        return features;
    }
    
    /**
     * Decode model output into a workout plan
     */
    private WorkoutPlan decodePlanFromOutput(float[] output, UserProfile userProfile, Map<String, Object> preferences) {
        // This would convert model output to an actual plan
        // For now, we'll use the rule-based approach as a placeholder
        return generatePlanWithRules(userProfile, preferences);
    }
    
    /**
     * Save workout plan to Firebase
     */
    private void savePlanToFirebase(WorkoutPlan plan, String userId) {
        if (database == null || userId == null || userId.isEmpty()) {
            return;
        }
        
        try {
            DatabaseReference plansRef = database.child("users").child(userId).child("workout_plans");
            
            // Generate unique ID for the plan
            String planId = plansRef.push().getKey();
            plan.setId(planId);
            
            // Save plan metadata
            Map<String, Object> planData = new HashMap<>();
            planData.put("title", plan.getTitle());
            planData.put("description", plan.getDescription());
            planData.put("focus", plan.getFocus());
            planData.put("daysPerWeek", plan.getDaysPerWeek());
            planData.put("weeks", plan.getWeeks());
            planData.put("createdAt", System.currentTimeMillis());
            
            plansRef.child(planId).setValue(planData);
            
            // Save workouts for each day
            Map<String, Workout> workouts = plan.getWorkouts();
            for (Map.Entry<String, Workout> entry : workouts.entrySet()) {
                String day = entry.getKey();
                Workout workout = entry.getValue();
                
                // Save workout data
                Map<String, Object> workoutData = new HashMap<>();
                workoutData.put("title", workout.getTitle());
                workoutData.put("type", workout.getType());
                workoutData.put("duration", workout.getDuration());
                
                plansRef.child(planId).child("workouts").child(day).setValue(workoutData);
                
                // Save exercises for this workout
                List<Exercise> exercises = workout.getExercises();
                for (int i = 0; i < exercises.size(); i++) {
                    Exercise exercise = exercises.get(i);
                    
                    Map<String, Object> exerciseData = new HashMap<>();
                    exerciseData.put("name", exercise.getName());
                    exerciseData.put("description", exercise.getDescription());
                    exerciseData.put("sets", exercise.getSets());
                    exerciseData.put("reps", exercise.getReps());
                    exerciseData.put("duration", exercise.getDuration());
                    exerciseData.put("requiresEquipment", exercise.isRequiresEquipment());
                    exerciseData.put("difficulty", exercise.getDifficulty());
                    exerciseData.put("muscleGroup", exercise.getMuscleGroup());
                    exerciseData.put("imageUrl", exercise.getImageUrl());
                    exerciseData.put("videoUrl", exercise.getVideoUrl());
                    
                    plansRef.child(planId).child("workouts").child(day)
                            .child("exercises").child(String.valueOf(i)).setValue(exerciseData);
                }
            }
            
            Log.d(TAG, "Workout plan saved to Firebase: " + planId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving workout plan to Firebase", e);
        }
    }
    
    /**
     * Initialize the exercise database with predefined exercises
     */
    private void initializeExerciseDatabase() {
        exerciseDatabase = new HashMap<>();
        
        // Push exercises
        List<Exercise> pushExercises = Arrays.asList(
            new Exercise("p1", "Push-ups", "Standard push-ups", "Chest", "beginner", 0, "", ""),
            new Exercise("p2", "Bench Press", "Barbell bench press", "Chest", "intermediate", 0, "", ""),
            new Exercise("p3", "Shoulder Press", "Dumbbell overhead press", "Shoulders", "intermediate", 0, "", ""),
            new Exercise("p4", "Tricep Dips", "Bodyweight tricep dips", "Triceps", "beginner", 0, "", ""),
            new Exercise("p5", "Incline Push-ups", "Elevated feet push-ups", "Upper Chest", "intermediate", 0, "", ""),
            new Exercise("p6", "Chest Flyes", "Dumbbell chest flyes", "Chest", "beginner", 0, "", "")
        );
        exerciseDatabase.put("Push", pushExercises);
        
        // Pull exercises
        List<Exercise> pullExercises = Arrays.asList(
            new Exercise("pl1", "Pull-ups", "Standard pull-ups", "Back", "advanced", 0, "", ""),
            new Exercise("pl2", "Bent-over Rows", "Barbell bent-over rows", "Back", "intermediate", 0, "", ""),
            new Exercise("pl3", "Bicep Curls", "Dumbbell bicep curls", "Biceps", "beginner", 0, "", ""),
            new Exercise("pl4", "Australian Pull-ups", "Row with elevated body", "Back", "intermediate", 0, "", ""),
            new Exercise("pl5", "Face Pulls", "Cable face pulls", "Rear Deltoids", "beginner", 0, "", ""),
            new Exercise("pl6", "Inverted Rows", "Bodyweight rows", "Back", "intermediate", 0, "", "")
        );
        exerciseDatabase.put("Pull", pullExercises);
        
        // Legs exercises
        List<Exercise> legsExercises = Arrays.asList(
            new Exercise("l1", "Squats", "Bodyweight squats", "Quads", "beginner", 0, "", ""),
            new Exercise("l2", "Lunges", "Walking lunges", "Quads", "beginner", 0, "", ""),
            new Exercise("l3", "Deadlifts", "Barbell deadlifts", "Hamstrings", "advanced", 0, "", ""),
            new Exercise("l4", "Calf Raises", "Standing calf raises", "Calves", "beginner", 0, "", ""),
            new Exercise("l5", "Glute Bridges", "Hip thrusts", "Glutes", "beginner", 0, "", ""),
            new Exercise("l6", "Leg Press", "Machine leg press", "Quads", "intermediate", 0, "", "")
        );
        exerciseDatabase.put("Legs", legsExercises);
        
        // Cardio exercises
        List<Exercise> cardioExercises = Arrays.asList(
            new Exercise("c1", "Running", "Steady state running", "Cardio", "intermediate", 20, "", ""),
            new Exercise("c2", "Cycling", "Stationary bike", "Cardio", "beginner", 20, "", ""),
            new Exercise("c3", "Jump Rope", "Standard jump rope", "Cardio", "intermediate", 5, "", ""),
            new Exercise("c4", "Swimming", "Freestyle swimming", "Cardio", "intermediate", 20, "", ""),
            new Exercise("c5", "Rowing", "Rowing machine", "Cardio", "intermediate", 15, "", ""),
            new Exercise("c6", "Elliptical", "Elliptical trainer", "Cardio", "beginner", 20, "", "")
        );
        exerciseDatabase.put("Cardio", cardioExercises);
        
        // HIIT exercises
        List<Exercise> hiitExercises = Arrays.asList(
            new Exercise("h1", "Burpees", "Full burpees", "Full Body", "advanced", 1, "", ""),
            new Exercise("h2", "Mountain Climbers", "Quick mountain climbers", "Core", "intermediate", 1, "", ""),
            new Exercise("h3", "Jump Squats", "Explosive squats", "Legs", "intermediate", 1, "", ""),
            new Exercise("h4", "High Knees", "Running in place", "Cardio", "beginner", 1, "", ""),
            new Exercise("h5", "Battle Ropes", "Alternating waves", "Upper Body", "intermediate", 1, "", ""),
            new Exercise("h6", "Box Jumps", "Explosive box jumps", "Legs", "advanced", 1, "", "")
        );
        exerciseDatabase.put("HIIT", hiitExercises);
        
        // Full Body exercises
        List<Exercise> fullBodyExercises = Arrays.asList(
            new Exercise("f1", "Burpees", "Full burpees", "Full Body", "advanced", 0, "", ""),
            new Exercise("f2", "Mountain Climbers", "Quick mountain climbers", "Core", "intermediate", 0, "", ""),
            new Exercise("f3", "Kettlebell Swings", "Two-handed swings", "Full Body", "intermediate", 0, "", ""),
            new Exercise("f4", "Turkish Get-ups", "Full movement", "Full Body", "advanced", 0, "", ""),
            new Exercise("f5", "Plank", "Standard plank", "Core", "beginner", 1, "", ""),
            new Exercise("f6", "Jumping Jacks", "Standard jumping jacks", "Cardio", "beginner", 0, "", "")
        );
        exerciseDatabase.put("FullBody", fullBodyExercises);
        
        // Generic exercises that can fit anywhere
        List<Exercise> genericExercises = Arrays.asList(
            new Exercise("g1", "Push-ups", "Standard push-ups", "Chest", "beginner", 0, "", ""),
            new Exercise("g2", "Squats", "Bodyweight squats", "Quads", "beginner", 0, "", ""),
            new Exercise("g3", "Plank", "Standard plank", "Core", "beginner", 1, "", ""),
            new Exercise("g4", "Crunches", "Standard crunches", "Abs", "beginner", 0, "", ""),
            new Exercise("g5", "Lunges", "Walking lunges", "Quads", "beginner", 0, "", ""),
            new Exercise("g6", "Jumping Jacks", "Standard jumping jacks", "Cardio", "beginner", 0, "", "")
        );
        exerciseDatabase.put("Generic", genericExercises);
    }
    
    /**
     * Load TensorFlow Lite model file
     */
    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
} 