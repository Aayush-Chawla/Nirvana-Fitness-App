package com.example.nirvana.utils;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.nirvana.models.FoodItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Helper class for Firestore operations including data migration from Realtime Database
 */
public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final Executor executor = Executors.newSingleThreadExecutor();

    // Listener interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnDataFetchedListener<T> {
        void onDataFetched(T data);
        void onError(String message);
    }

    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String PROFILE_COLLECTION = "profile";
    public static final String MEALS_COLLECTION = "food_logs";
    private static final String WORKOUTS_COLLECTION = "workouts";
    private static final String WORKOUT_HISTORY_COLLECTION = "workoutHistory";
    private static final String DAILY_STATS_COLLECTION = "dailyStats";
    private static final String HEART_RATE_COLLECTION = "heartRate";
    private static final String REWARDS_COLLECTION = "rewards";
    private static final String GYM_MEMBERSHIP_COLLECTION = "gymMembership";
    private static final String CHAT_HISTORY_COLLECTION = "chatHistory";

    /**
     * Get current user ID
     * @return User ID or null if not logged in
     */
    public static String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    /**
     * Get user document reference
     * @return DocumentReference for current user or null if not logged in
     */
    public static DocumentReference getUserDocRef() {
        String userId = getCurrentUserId();
        return userId != null ? db.collection(USERS_COLLECTION).document(userId) : null;
    }

    // ===== PROFILE OPERATIONS =====

    /**
     * Save user profile to Firestore
     * @param profile Map of profile data
     * @param listener Completion listener
     */
    public static void saveUserProfile(Map<String, Object> profile, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        userRef.set(profile)
            .addOnSuccessListener(aVoid -> {
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving profile", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Get user profile from Firestore
     * @param listener Data listener
     */
    public static void getUserProfile(OnDataFetchedListener<Map<String, Object>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onError("User not authenticated");
            return;
        }

        userRef.get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (listener != null) listener.onDataFetched(data != null ? data : new HashMap<>());
                } else {
                    if (listener != null) listener.onDataFetched(new HashMap<>());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting profile", e);
                if (listener != null) listener.onError(e.getMessage());
            });
    }

    // ===== MEAL OPERATIONS =====

    /**
     * Save a food item to meals collection
     * @param mealType Meal type (breakfast, lunch, dinner, snacks)
     * @param foodItem Food item to save
     * @param servingSize Serving size
     * @param listener Completion listener
     */
    public static void logFood(String mealType, FoodItem foodItem, String servingSize, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        // Standardize mealType to lowercase for consistency
        String normalizedMealType = mealType.toLowerCase();
        
        // Get today's date in the format used by the app
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        
        // Log the path where we're saving
        Log.d(TAG, "Saving food to path: users/" + getCurrentUserId() + "/" + MEALS_COLLECTION + 
              "/" + today + "/" + normalizedMealType + " - Food: " + foodItem.getName());

        Map<String, Object> foodLog = new HashMap<>();
        foodLog.put("id", foodItem.getId());
        foodLog.put("name", foodItem.getName());
        foodLog.put("calories", foodItem.getCalories());
        
        // Parse the serving size to extract the numeric value
        float servingSizeValue = 100f; // Default to 100g if parsing fails
        try {
            // Extract numeric part from serving size (e.g., "100g" -> 100)
            String numericPart = servingSize.replaceAll("[^0-9.]", "");
            if (!numericPart.isEmpty()) {
                servingSizeValue = Float.parseFloat(numericPart);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing serving size: " + servingSize, e);
        }
        
        // Store both the original string and the numeric value
        foodLog.put("servingSize", servingSize);
        foodLog.put("servingSizeValue", servingSizeValue);
        
        // Calculate actual values based on serving size
        float servingRatio = servingSizeValue / 100f; // Base values are for 100g
        foodLog.put("protein", foodItem.getProtein() * servingRatio);
        foodLog.put("carbs", foodItem.getCarbs() * servingRatio);
        foodLog.put("fat", foodItem.getFat() * servingRatio);
        foodLog.put("timestamp", System.currentTimeMillis());

        userRef.collection(MEALS_COLLECTION)
            .document(today)
            .collection(normalizedMealType)
            .add(foodLog)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Food logged successfully with ID: " + documentReference.getId() + 
                      " to meal type: " + normalizedMealType);
                if (listener != null) listener.onSuccess();
                
                // Also update daily calories
                int actualCalories = Math.round(foodItem.getCalories() * servingRatio);
                updateDailyCalories(actualCalories, null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error logging food", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Get all meals for the current day
     * @param listener Data listener
     */
    public static void getMeals(OnDataFetchedListener<Map<String, List<Map<String, Object>>>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onError("User not authenticated");
            return;
        }

        // Get today's date in the format used by the app
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        
        // Log the path where we're retrieving from
        Log.d(TAG, "Retrieving meals from path: users/" + getCurrentUserId() + "/" + MEALS_COLLECTION + "/" + today);
        
        // Create result map with standardized keys that match what's used in the MealFragment
        Map<String, List<Map<String, Object>>> meals = new HashMap<>();
        meals.put("Breakfast", new ArrayList<>()); // Note the capitalization to match MealFragment
        meals.put("Lunch", new ArrayList<>());
        meals.put("Dinner", new ArrayList<>());
        meals.put("Snacks", new ArrayList<>());
        
        List<Task<?>> tasks = new ArrayList<>();
        
        // For each meal type, use lowercase version for Firestore path but store with capitalized key for UI
        
        // Get breakfast
        Task<Void> breakfastTask = userRef.collection(MEALS_COLLECTION)
            .document(today)
            .collection("breakfast") // lowercase in storage
            .get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Map<String, Object>> breakfastItems = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> item = document.getData();
                        item.put("docId", document.getId());
                        breakfastItems.add(item);
                    }
                    meals.put("Breakfast", breakfastItems); // Capitalized for UI
                    Log.d(TAG, "Retrieved " + breakfastItems.size() + " breakfast items");
                } else if (task.getException() != null) {
                    Log.e(TAG, "Error retrieving breakfast items", task.getException());
                }
                return null;
            });
        tasks.add(breakfastTask);
        
        // Get lunch
        Task<Void> lunchTask = userRef.collection(MEALS_COLLECTION)
            .document(today)
            .collection("lunch") // lowercase in storage
            .get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Map<String, Object>> lunchItems = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> item = document.getData();
                        item.put("docId", document.getId());
                        lunchItems.add(item);
                    }
                    meals.put("Lunch", lunchItems); // Capitalized for UI
                    Log.d(TAG, "Retrieved " + lunchItems.size() + " lunch items");
                } else if (task.getException() != null) {
                    Log.e(TAG, "Error retrieving lunch items", task.getException());
                }
                return null;
            });
        tasks.add(lunchTask);
        
        // Get dinner
        Task<Void> dinnerTask = userRef.collection(MEALS_COLLECTION)
            .document(today)
            .collection("dinner") // lowercase in storage
            .get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Map<String, Object>> dinnerItems = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> item = document.getData();
                        item.put("docId", document.getId());
                        dinnerItems.add(item);
                    }
                    meals.put("Dinner", dinnerItems); // Capitalized for UI
                    Log.d(TAG, "Retrieved " + dinnerItems.size() + " dinner items");
                } else if (task.getException() != null) {
                    Log.e(TAG, "Error retrieving dinner items", task.getException());
                }
                return null;
            });
        tasks.add(dinnerTask);
        
        // Get snacks
        Task<Void> snacksTask = userRef.collection(MEALS_COLLECTION)
            .document(today)
            .collection("snacks") // lowercase in storage
            .get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Map<String, Object>> snackItems = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> item = document.getData();
                        item.put("docId", document.getId());
                        snackItems.add(item);
                    }
                    meals.put("Snacks", snackItems); // Capitalized for UI
                    Log.d(TAG, "Retrieved " + snackItems.size() + " snack items");
                } else if (task.getException() != null) {
                    Log.e(TAG, "Error retrieving snack items", task.getException());
                }
                return null;
            });
        tasks.add(snacksTask);
        
        // When all tasks complete
        Tasks.whenAll(tasks)
            .addOnSuccessListener(aVoid -> {
                if (listener != null) {
                    Log.d(TAG, "All meal data retrieved: " + 
                          "Breakfast=" + meals.get("Breakfast").size() + ", " +
                          "Lunch=" + meals.get("Lunch").size() + ", " +
                          "Dinner=" + meals.get("Dinner").size() + ", " +
                          "Snacks=" + meals.get("Snacks").size());
                    listener.onDataFetched(meals);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting meals", e);
                if (listener != null) listener.onError(e.getMessage());
            });
    }

    /**
     * Delete a food item from meals
     * @param mealType Meal type
     * @param foodId Food item document ID
     * @param calories Calories to subtract from daily total
     * @param listener Completion listener
     */
    public static void deleteFoodItem(String mealType, String foodId, int calories, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        // Get today's date in the format used by the app
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        userRef.collection(MEALS_COLLECTION)
            .document(today)
            .collection(mealType)
            .document(foodId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                if (listener != null) listener.onSuccess();
                
                // Also update daily calories (subtract)
                updateDailyCalories(-calories, null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting food item", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    // ===== WORKOUT OPERATIONS =====

    /**
     * Log a completed workout
     * @param workoutData Workout data map
     * @param listener Completion listener
     */
    public static void logWorkout(Map<String, Object> workoutData, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        userRef.collection(WORKOUT_HISTORY_COLLECTION)
            .add(workoutData)
            .addOnSuccessListener(documentReference -> {
                if (listener != null) listener.onSuccess();
                
                // Also update daily workout stats
                updateDailyWorkoutStats(workoutData, null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error logging workout", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Get recent workouts
     * @param limit Number of workouts to fetch
     * @param listener Data listener
     */
    public static void getRecentWorkouts(int limit, OnDataFetchedListener<List<Map<String, Object>>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onError("User not authenticated");
            return;
        }

        userRef.collection(WORKOUT_HISTORY_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> workouts = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> workout = document.getData();
                    workout.put("id", document.getId());
                    workouts.add(workout);
                }
                if (listener != null) listener.onDataFetched(workouts);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting recent workouts", e);
                if (listener != null) listener.onError(e.getMessage());
            });
    }

    // ===== DAILY STATS OPERATIONS =====

    /**
     * Update daily calories
     * @param calories Calories to add (can be negative for subtraction)
     * @param listener Completion listener
     */
    public static void updateDailyCalories(int calories, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        // Get today's date in the format used by the app
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        // Create a data update that increments the calories field
        Map<String, Object> update = new HashMap<>();
        update.put("calories", FieldValue.increment(calories));
        update.put("lastUpdated", System.currentTimeMillis());

        userRef.collection(DAILY_STATS_COLLECTION)
            .document(today)
            .set(update, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating daily calories", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Update daily workout stats
     * @param workoutData Workout data
     * @param listener Completion listener
     */
    private static void updateDailyWorkoutStats(Map<String, Object> workoutData, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        // Get today's date in the format used by the app
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        // Update counts
        Map<String, Object> update = new HashMap<>();
        update.put("workoutsCompleted", FieldValue.increment(1));
        
        // If workout has duration, add it to active minutes
        if (workoutData.containsKey("duration")) {
            Object duration = workoutData.get("duration");
            if (duration instanceof Number) {
                update.put("activeMinutes", FieldValue.increment(((Number) duration).intValue()));
            }
        }
        
        // If workout has calories burned, add it to calories burned
        if (workoutData.containsKey("caloriesBurned")) {
            Object caloriesBurned = workoutData.get("caloriesBurned");
            if (caloriesBurned instanceof Number) {
                update.put("caloriesBurned", FieldValue.increment(((Number) caloriesBurned).intValue()));
            }
        }
        
        update.put("lastUpdated", System.currentTimeMillis());

        userRef.collection(DAILY_STATS_COLLECTION)
            .document(today)
            .set(update, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating daily workout stats", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Get daily stats
     * @param listener Data listener
     */
    public static void getDailyStats(OnDataFetchedListener<Map<String, Object>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onError("User not authenticated");
            return;
        }

        // Get today's date in the format used by the app
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        userRef.collection(DAILY_STATS_COLLECTION)
            .document(today)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> stats = new HashMap<>();
                if (documentSnapshot.exists()) {
                    stats = documentSnapshot.getData();
                }
                // Set defaults for missing fields
                if (!stats.containsKey("calories")) stats.put("calories", 0);
                if (!stats.containsKey("caloriesBurned")) stats.put("caloriesBurned", 0);
                if (!stats.containsKey("activeMinutes")) stats.put("activeMinutes", 0);
                if (!stats.containsKey("workoutsCompleted")) stats.put("workoutsCompleted", 0);
                if (!stats.containsKey("steps")) stats.put("steps", 0);
                
                if (listener != null) listener.onDataFetched(stats);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting daily stats", e);
                if (listener != null) listener.onError(e.getMessage());
            });
    }

    // ===== CHAT HISTORY OPERATIONS =====

    /**
     * Save chat message to history
     * @param userMessage User message
     * @param botResponse Bot response
     * @param listener Completion listener
     */
    public static void saveChatMessage(String userMessage, String botResponse, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            Log.e(TAG, "Cannot save chat message - user not authenticated");
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        String userId = getCurrentUserId();
        Log.d(TAG, "Attempting to save chat message for user: " + userId);

        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("userMessage", userMessage);
        chatMessage.put("botResponse", botResponse);
        chatMessage.put("timestamp", System.currentTimeMillis());

        // Log the path where we're saving
        Log.d(TAG, "Saving chat message to path: users/" + userId + "/" + CHAT_HISTORY_COLLECTION);

        userRef.collection(CHAT_HISTORY_COLLECTION)
            .add(chatMessage)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Chat message saved successfully with ID: " + documentReference.getId());
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving chat message", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Get chat history from Firestore
     * @param limit Maximum number of messages to retrieve
     * @param listener Data listener
     */
    public static void getChatHistory(int limit, OnDataFetchedListener<List<Map<String, Object>>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            Log.e(TAG, "Cannot retrieve chat history - user not authenticated");
            if (listener != null) listener.onError("User not authenticated");
            return;
        }

        String userId = getCurrentUserId();
        Log.d(TAG, "Retrieving chat history for user: " + userId);

        userRef.collection(CHAT_HISTORY_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> chatMessages = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> message = document.getData();
                    message.put("id", document.getId());
                    chatMessages.add(message);
                }
                Log.d(TAG, "Retrieved " + chatMessages.size() + " chat messages");
                if (listener != null) listener.onDataFetched(chatMessages);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error retrieving chat history", e);
                if (listener != null) listener.onError(e.getMessage());
            });
    }

    // ===== DATA MIGRATION FUNCTIONS =====

    /**
     * Migrates user data from Firebase Realtime Database to Firestore
     * 
     * @param userId The ID of the user whose data is being migrated
     * @param onSuccess Callback for successful migration
     * @param onFailure Callback for failed migration
     */
    public static void migrateUserData(String userId, Consumer<Void> onSuccess, Consumer<String> onFailure) {
        Log.d(TAG, "Starting migration process for user: " + userId);
        
        // Get reference to the user's data in Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        
        // Get reference to the user's document in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("users").document(userId);
        
        // Check if migration has already been performed
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists() && 
                Boolean.TRUE.equals(task.getResult().getBoolean("migrationCompleted"))) {
                Log.d(TAG, "Migration already completed for user: " + userId);
                onSuccess.accept(null);
                return;
            }
            
            // Read all user data from Realtime Database
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Log.d(TAG, "No data found in Realtime Database for user: " + userId);
                        onSuccess.accept(null);
                        return;
                    }
                    
                    // Convert DataSnapshot to Map
                    Map<String, Object> userData = new HashMap<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        convertDataSnapshotToMap(child, userData, child.getKey());
                    }
                    
                    // Add migration marker
                    userData.put("migrationCompleted", true);
                    userData.put("migrationTimestamp", System.currentTimeMillis());
                    
                    // Write data to Firestore
                    migrateDataToFirestore(userId, userData, onSuccess, onFailure);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    String errorMsg = "Migration cancelled: " + databaseError.getMessage();
                    Log.e(TAG, errorMsg);
                    onFailure.accept(errorMsg);
                }
            });
        });
    }
    
    /**
     * Recursively converts DataSnapshot to Map structure
     */
    private static void convertDataSnapshotToMap(DataSnapshot snapshot, Map<String, Object> result, String key) {
        if (!snapshot.hasChildren()) {
            // Leaf node - add the value directly
            result.put(key, snapshot.getValue());
        } else {
            // This is a nested object
            Map<String, Object> childMap = new HashMap<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                convertDataSnapshotToMap(child, childMap, child.getKey());
            }
            result.put(key, childMap);
        }
    }
    
    /**
     * Writes migrated data to Firestore
     */
    private static void migrateDataToFirestore(String userId, Map<String, Object> userData, 
                                               Consumer<Void> onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("users").document(userId);
        
        // Create subcollections for specific data types
        migrateCollection(userId, "food_logs", userData, "food_logs");
        migrateCollection(userId, "workout_logs", userData, "workout_logs");
        migrateCollection(userId, "water_logs", userData, "water_logs");
        migrateCollection(userId, "weight_logs", userData, "weight_logs");
        
        // Remove migrated collections from main user document
        userData.remove("food_logs");
        userData.remove("workout_logs");
        userData.remove("water_logs");
        userData.remove("weight_logs");
        
        // Write user profile data to main document
        userDocRef.set(userData, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User data successfully migrated to Firestore for user: " + userId);
                onSuccess.accept(null);
            })
            .addOnFailureListener(e -> {
                String errorMsg = "Error migrating user data: " + e.getMessage();
                Log.e(TAG, errorMsg);
                onFailure.accept(errorMsg);
            });
    }
    
    /**
     * Migrates a specific collection (like food_logs) to a Firestore subcollection
     */
    private static void migrateCollection(String userId, String collectionName, 
                                         Map<String, Object> userData, String mapKey) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (userData.containsKey(mapKey) && userData.get(mapKey) instanceof Map) {
            Map<String, Object> itemsMap = (Map<String, Object>) userData.get(mapKey);
            
            for (Map.Entry<String, Object> entry : itemsMap.entrySet()) {
                String docId = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof Map) {
                    db.collection("users").document(userId)
                      .collection(collectionName).document(docId)
                      .set(value)
                      .addOnFailureListener(e -> 
                          Log.e(TAG, "Error migrating " + collectionName + " item " + docId + ": " + e.getMessage()));
                }
            }
        }
    }
} 