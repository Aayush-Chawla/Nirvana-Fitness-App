package com.example.nirvana.services;

import android.content.Context;
import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced AI assistant specifically focused on fitness and nutrition domain knowledge
 * Extends the base GeminiService with specialized fitness context and prompting
 */
public class EnhancedFitnessAssistant extends GeminiService {
    private static final String TAG = "EnhancedFitnessAssistant";
    
    // System prompt to guide the model with domain-specific knowledge
    private static final String SYSTEM_PROMPT = 
        "You are a professional fitness trainer and nutritionist assistant for the Nirvana Fitness app. " +
        "Use the following structured approach to answer questions:\n" +
        "1. For workout advice: Consider user's fitness level, goals, and any physical limitations\n" +
        "2. For nutrition advice: Base recommendations on user's dietary preferences, caloric needs, and nutritional goals\n" +
        "3. For motivation: Provide evidence-based motivation tailored to the user's specific goals\n" +
        "4. Include specific actionable steps when appropriate\n" +
        "5. When discussing exercises, focus on proper form and safety\n" +
        "Always maintain a supportive and encouraging tone. Keep responses concise and practical.";
    
    private final DatabaseReference userRef;
    private Map<String, Object> userProfile;
    private Map<String, Object> nutritionData;
    private Map<String, Object> activityData;
    
    public EnhancedFitnessAssistant(Context context) {
        super(context);
        
        // Initialize with empty maps
        userProfile = new HashMap<>();
        nutritionData = new HashMap<>();
        activityData = new HashMap<>();
        
        // Setup Firebase reference if user is logged in
        DatabaseReference ref = null;
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ref = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            loadUserData(ref);
        } catch (Exception e) {
            Log.w(TAG, "User not logged in or Firebase error: " + e.getMessage());
        }
        
        userRef = ref;
    }
    
    /**
     * Load user data from Firebase to enhance the AI's context awareness
     */
    private void loadUserData(DatabaseReference userRef) {
        if (userRef == null) return;
        
        // Load user profile
        userRef.child("profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userProfile.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        userProfile.put(child.getKey(), child.getValue());
                    }
                    Log.d(TAG, "Loaded user profile with " + userProfile.size() + " fields");
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading user profile", databaseError.toException());
            }
        });
        
        // Load nutrition data
        userRef.child("nutrition_summary").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nutritionData.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        nutritionData.put(child.getKey(), child.getValue());
                    }
                    Log.d(TAG, "Loaded nutrition data with " + nutritionData.size() + " fields");
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading nutrition data", databaseError.toException());
            }
        });
        
        // Load activity data
        userRef.child("activity_summary").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activityData.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        activityData.put(child.getKey(), child.getValue());
                    }
                    Log.d(TAG, "Loaded activity data with " + activityData.size() + " fields");
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading activity data", databaseError.toException());
            }
        });
    }
    
    /**
     * Override the generateContent method to enhance prompts with user context and domain knowledge
     */
    @Override
    public void generateContent(String prompt, GeminiResponseCallback callback) {
        // Enhance the prompt with user context and domain knowledge
        String enhancedPrompt = enrichPromptWithContext(prompt);
        
        // Call the parent method with the enhanced prompt
        super.generateContent(enhancedPrompt, callback);
    }
    
    /**
     * Enhance the user prompt with fitness domain knowledge and user context
     */
    private String enrichPromptWithContext(String userPrompt) {
        StringBuilder contextBuilder = new StringBuilder(SYSTEM_PROMPT);
        contextBuilder.append("\n\n");
        
        // Add user profile context if available
        if (!userProfile.isEmpty()) {
            contextBuilder.append("USER PROFILE:\n");
            appendMapData(contextBuilder, userProfile, "Age", "Weight", "Height", "Gender", "FitnessLevel", "FitnessGoal");
            contextBuilder.append("\n");
        }
        
        // Add nutrition context if available and if it's a nutrition-related query
        if (!nutritionData.isEmpty() && isNutritionQuery(userPrompt)) {
            contextBuilder.append("NUTRITION DATA:\n");
            appendMapData(contextBuilder, nutritionData, "AverageCalories", "AverageProtein", 
                          "AverageCarbs", "AverageFat", "CalorieTarget", "ProteinTarget", "DietType");
            contextBuilder.append("\n");
        }
        
        // Add activity context if available and if it's a workout-related query
        if (!activityData.isEmpty() && isWorkoutQuery(userPrompt)) {
            contextBuilder.append("ACTIVITY DATA:\n");
            appendMapData(contextBuilder, activityData, "WorkoutsPerWeek", "AvgDuration", 
                          "PreferredWorkouts", "RecentExercises", "FocusAreas");
            contextBuilder.append("\n");
        }
        
        // Add the actual user query
        contextBuilder.append("USER QUERY: ").append(userPrompt);
        
        return contextBuilder.toString();
    }
    
    private void appendMapData(StringBuilder builder, Map<String, Object> data, String... keys) {
        for (String key : keys) {
            if (data.containsKey(key)) {
                builder.append("- ").append(key).append(": ").append(data.get(key)).append("\n");
            }
        }
    }
    
    private boolean isNutritionQuery(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("diet") || lowerQuery.contains("food") || 
               lowerQuery.contains("nutrition") || lowerQuery.contains("eat") || 
               lowerQuery.contains("meal") || lowerQuery.contains("calorie") || 
               lowerQuery.contains("protein") || lowerQuery.contains("carb") || 
               lowerQuery.contains("fat") || lowerQuery.contains("weight");
    }
    
    private boolean isWorkoutQuery(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("workout") || lowerQuery.contains("exercise") || 
               lowerQuery.contains("training") || lowerQuery.contains("gym") || 
               lowerQuery.contains("run") || lowerQuery.contains("cardio") || 
               lowerQuery.contains("strength") || lowerQuery.contains("muscle") || 
               lowerQuery.contains("stretching") || lowerQuery.contains("routine");
    }
} 