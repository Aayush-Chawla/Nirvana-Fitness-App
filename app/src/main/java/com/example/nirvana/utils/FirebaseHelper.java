package com.example.nirvana.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.nirvana.models.FoodItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Legacy helper class for Firebase operations
 * Now delegates to FirestoreHelper for all operations
 * @deprecated Use FirestoreHelper directly for new code
 */
@Deprecated
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static final String USERS_COLLECTION = "users";
    
    private static final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface OnFoodLoggedListener {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Checks if the user data has been migrated to Firestore
     *
     * @param userId    The user ID to check
     * @param callback  Callback that receives true if migrated, false otherwise
     */
    public static void isMigrated(String userId, Consumer<Boolean> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION).document(userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    Boolean migrated = document.exists() && 
                                      document.contains("migrated") && 
                                      Boolean.TRUE.equals(document.getBoolean("migrated"));
                    callback.accept(migrated);
                } else {
                    Log.e(TAG, "Error checking migration status", task.getException());
                    callback.accept(false);
                }
            });
    }

    /**
     * Gets the current user ID or null if not logged in
     */
    public static String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    /**
     * Gets today's date formatted as yyyy-MM-dd
     */
    public static String getTodayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    /**
     * Log a food item to the user's meals
     * Now delegates to FirestoreHelper
     */
    public static void logFood(String mealType, FoodItem foodItem, String servingSize, OnFoodLoggedListener listener) {
        // Delegate to FirestoreHelper
        FirestoreHelper.logFood(mealType, foodItem, servingSize, new FirestoreHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                if (listener != null) listener.onSuccess();
            }
            
            @Override
            public void onFailure(String error) {
                if (listener != null) listener.onFailure(error);
            }
        });
    }

    /**
     * Update the user's daily caloric intake
     * Now delegates to FirestoreHelper
     */
    public static void updateDailyCalories(int calories, OnFoodLoggedListener listener) {
        // Delegate to FirestoreHelper
        FirestoreHelper.updateDailyCalories(calories, new FirestoreHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                if (listener != null) listener.onSuccess();
            }
            
            @Override
            public void onFailure(String error) {
                if (listener != null) listener.onFailure(error);
            }
        });
    }

    /**
     * Migrate user data from Realtime Database to Firestore
     * This should be called once per user to ensure all data is moved to Firestore
     */
    public static void migrateDataToFirestore(OnFoodLoggedListener listener) {
        // Get the current user ID
        String userId = getCurrentUserId();
        if (userId == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        // Delegate to FirestoreHelper with the correct parameters
        FirestoreHelper.migrateUserData(
            userId,
            unused -> {
                if (listener != null) listener.onSuccess();
            },
            error -> {
                if (listener != null) listener.onFailure(error);
            }
        );
    }

    // Food logging methods
    public static void logFood(String userId, FoodItem foodItem, String mealType, Consumer<Void> onSuccess, Consumer<String> onFailure) {
        // First check if migration occurred
        isMigrated(userId, migrated -> {
            if (migrated) {
                logFoodFirestore(userId, foodItem, mealType, onSuccess, onFailure);
            } else {
                logFoodRealtimeDb(userId, foodItem, mealType, onSuccess, onFailure);
            }
        });
    }
    
    private static void logFoodFirestore(String userId, FoodItem foodItem, String mealType, Consumer<Void> onSuccess, Consumer<String> onFailure) {
        String date = getTodayDate();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> foodData = new HashMap<>();
        foodData.put("name", foodItem.getName());
        foodData.put("calories", foodItem.getCalories());
        foodData.put("protein", foodItem.getProtein());
        foodData.put("carbs", foodItem.getCarbs());
        foodData.put("fat", foodItem.getFat());
        foodData.put("servingSize", foodItem.getServingSize());
        foodData.put("timestamp", FieldValue.serverTimestamp());
        
        db.collection(USERS_COLLECTION)
            .document(userId)
            .collection("food_logs")
            .document(date)
            .collection(mealType)
            .add(foodData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Food logged to Firestore successfully");
                // Get numeric value from serving size for calorie calculation
                float servingSizeValue = extractServingSizeValue(foodItem.getServingSize());
                int actualCalories = Math.round(foodItem.getCalories() * servingSizeValue / 100f);
                updateDailyCaloriesFirestore(userId, actualCalories);
                onSuccess.accept(null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error logging food to Firestore", e);
                onFailure.accept(e.getMessage());
            });
    }
    
    private static void logFoodRealtimeDb(String userId, FoodItem foodItem, String mealType, Consumer<Void> onSuccess, Consumer<String> onFailure) {
        String date = getTodayDate();
        DatabaseReference foodLogRef = FirebaseDatabase.getInstance()
                .getReference()
            .child("users")
            .child(userId)
            .child("food_logs")
            .child(date)
            .child(mealType)
            .push();

        Map<String, Object> foodData = new HashMap<>();
        foodData.put("name", foodItem.getName());
        foodData.put("calories", foodItem.getCalories());
        foodData.put("protein", foodItem.getProtein());
        foodData.put("carbs", foodItem.getCarbs());
        foodData.put("fat", foodItem.getFat());
        foodData.put("servingSize", foodItem.getServingSize());
        foodData.put("timestamp", ServerValue.TIMESTAMP);

        foodLogRef.setValue(foodData)
            .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Food logged successfully");
                    // Get numeric value from serving size for calorie calculation
                    float servingSizeValue = extractServingSizeValue(foodItem.getServingSize());
                    int actualCalories = Math.round(foodItem.getCalories() * servingSizeValue / 100f);
                    updateDailyCalories(userId, actualCalories);
                    onSuccess.accept(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error logging food", e);
                    onFailure.accept(e.getMessage());
                });
    }
    
    // Helper method to extract numeric value from serving size
    private static float extractServingSizeValue(String servingSize) {
        if (servingSize == null || servingSize.isEmpty()) {
            return 100f; // Default to 100g
        }
        
        try {
            String numericPart = servingSize.replaceAll("[^0-9.]", "");
            if (!numericPart.isEmpty()) {
                return Float.parseFloat(numericPart);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing serving size: " + servingSize, e);
        }
        
        return 100f; // Default to 100g if parsing fails
    }
    
    // User food logs retrieval
    public static void getUserFoodLogs(String userId, String date, Consumer<Map<String, List<FoodItem>>> onSuccess, Consumer<String> onFailure) {
        // First check if migration occurred
        isMigrated(userId, migrated -> {
            if (migrated) {
                getUserFoodLogsFirestore(userId, date, onSuccess, onFailure);
            } else {
                getUserFoodLogsRealtimeDb(userId, date, onSuccess, onFailure);
            }
        });
    }
    
    private static void getUserFoodLogsFirestore(String userId, String date, Consumer<Map<String, List<FoodItem>>> onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, List<FoodItem>> mealMap = new HashMap<>();
        List<String> mealTypes = List.of("breakfast", "lunch", "dinner", "snacks");
        
        // Initialize empty lists for each meal type
        for (String mealType : mealTypes) {
            mealMap.put(mealType, new ArrayList<>());
        }
        
        // Process each meal type
        for (String mealType : mealTypes) {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .collection("food_logs")
                .document(date)
                .collection(mealType)
                .get()
                .addOnSuccessListener(queryDocuments -> {
                    List<FoodItem> foodItems = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocuments.getDocuments()) {
                        Map<String, Object> data = document.getData();
                        if (data != null) {
                            FoodItem foodItem = new FoodItem();
                            foodItem.setName((String) data.get("name"));
                            foodItem.setCalories(((Number) data.get("calories")).intValue());
                            foodItem.setProtein(((Number) data.get("protein")).floatValue());
                            foodItem.setCarbs(((Number) data.get("carbs")).floatValue());
                            foodItem.setFat(((Number) data.get("fat")).floatValue());
                            if (data.containsKey("servingSize")) {
                                if (data.get("servingSize") instanceof Number) {
                                    // Old format stored as number
                                    Number servingSizeNum = (Number) data.get("servingSize");
                                    foodItem.setServingSize(servingSizeNum.toString() + "g");
                                } else if (data.get("servingSize") instanceof String) {
                                    // New format stored as string
                                    foodItem.setServingSize((String) data.get("servingSize"));
                                }
                            } else {
                                foodItem.setServingSize("100g"); // Default
                            }
                            foodItems.add(foodItem);
                        }
                    }
                    
                    mealMap.put(mealType, foodItems);
                    
                    // Check if we've processed all meal types
                    boolean allProcessed = true;
                    for (String meal : mealTypes) {
                        if (mealMap.get(meal) == null) {
                            allProcessed = false;
                            break;
                        }
                    }
                    
                    if (allProcessed) {
                        onSuccess.accept(mealMap);
                    }
            })
            .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting food logs for " + mealType, e);
                    onFailure.accept(e.getMessage());
                });
        }
    }
    
    private static void getUserFoodLogsRealtimeDb(String userId, String date, Consumer<Map<String, List<FoodItem>>> onSuccess, Consumer<String> onFailure) {
        DatabaseReference foodLogRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(userId)
                .child("food_logs")
                .child(date);

        foodLogRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Map<String, List<FoodItem>> mealMap = new HashMap<>();
                    List<String> mealTypes = List.of("breakfast", "lunch", "dinner", "snacks");
                    
                    // Initialize mealMap with empty lists for each meal type
                    for (String mealType : mealTypes) {
                        mealMap.put(mealType, new ArrayList<>());
                    }
                    
                    // Process each meal type found in the database
                    for (DataSnapshot mealSnapshot : dataSnapshot.getChildren()) {
                        String mealType = mealSnapshot.getKey();
                        List<FoodItem> foodItems = new ArrayList<>();
                        
                        for (DataSnapshot foodSnapshot : mealSnapshot.getChildren()) {
                            Map<String, Object> foodData = (Map<String, Object>) foodSnapshot.getValue();
                            if (foodData != null) {
                                FoodItem foodItem = new FoodItem();
                                foodItem.setName((String) foodData.get("name"));
                                foodItem.setCalories(((Number) foodData.get("calories")).intValue());
                                foodItem.setProtein(((Number) foodData.get("protein")).floatValue());
                                foodItem.setCarbs(((Number) foodData.get("carbs")).floatValue());
                                foodItem.setFat(((Number) foodData.get("fat")).floatValue());
                                if (foodData.containsKey("servingSize")) {
                                    if (foodData.get("servingSize") instanceof Number) {
                                        // Old format stored as number
                                        Number servingSizeNum = (Number) foodData.get("servingSize");
                                        foodItem.setServingSize(servingSizeNum.toString() + "g");
                                    } else if (foodData.get("servingSize") instanceof String) {
                                        // New format stored as string
                                        foodItem.setServingSize((String) foodData.get("servingSize"));
                                    }
                                } else {
                                    foodItem.setServingSize("100g"); // Default
                                }
                                foodItems.add(foodItem);
                            }
                        }
                        
                        mealMap.put(mealType, foodItems);
                    }
                    
                    onSuccess.accept(mealMap);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing food log data", e);
                    onFailure.accept("Error parsing food log data: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error when getting food logs", databaseError.toException());
                onFailure.accept(databaseError.getMessage());
            }
        });
    }
    
    // Daily calorie update methods
    private static void updateDailyCalories(String userId, float calories) {
        String date = getTodayDate();
        DatabaseReference calorieRef = FirebaseDatabase.getInstance()
                .getReference()
            .child("users")
            .child(userId)
            .child("daily_calories")
            .child(date);

        calorieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float currentCalories = 0;
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    currentCalories = ((Number) dataSnapshot.getValue()).floatValue();
                }
                
                float newCalories = currentCalories + calories;
                calorieRef.setValue(newCalories)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Daily calories updated: " + newCalories))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating daily calories", e));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error when updating daily calories", databaseError.toException());
            }
        });
    }
    
    private static void updateDailyCaloriesFirestore(String userId, float calories) {
        String date = getTodayDate();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference calorieRef = db.collection(USERS_COLLECTION)
            .document(userId)
            .collection("daily_calories")
            .document(date);
            
        // First get current value
        calorieRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                float currentCalories = 0;
                if (document.exists() && document.contains("calories")) {
                    currentCalories = document.getDouble("calories").floatValue();
                }
                
                float newCalories = currentCalories + calories;
                Map<String, Object> calorieData = new HashMap<>();
                calorieData.put("calories", newCalories);
                calorieData.put("last_updated", FieldValue.serverTimestamp());
                
                calorieRef.set(calorieData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Daily calories updated in Firestore: " + newCalories))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating daily calories in Firestore", e));
            } else {
                Log.e(TAG, "Error getting current calorie data", task.getException());
            }
        });
    }
    
    // User profile data methods
    public static void getUserProfile(String userId, Consumer<Map<String, Object>> onSuccess, Consumer<String> onFailure) {
        // First check if migration occurred
        isMigrated(userId, migrated -> {
            if (migrated) {
                getUserProfileFirestore(userId, onSuccess, onFailure);
            } else {
                getUserProfileRealtimeDb(userId, onSuccess, onFailure);
            }
        });
    }
    
    private static void getUserProfileFirestore(String userId, Consumer<Map<String, Object>> onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION).document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> userData = documentSnapshot.getData();
                    onSuccess.accept(userData != null ? userData : new HashMap<>());
                } else {
                    Log.d(TAG, "User profile not found in Firestore");
                    onSuccess.accept(new HashMap<>());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user profile from Firestore", e);
                onFailure.accept(e.getMessage());
            });
    }
    
    private static void getUserProfileRealtimeDb(String userId, Consumer<Map<String, Object>> onSuccess, Consumer<String> onFailure) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference()
            .child("users")
            .child(userId)
                .child("profile");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();
                    onSuccess.accept(userData != null ? userData : new HashMap<>());
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing user profile data", e);
                    onFailure.accept("Error parsing user profile data: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error when getting user profile", databaseError.toException());
                onFailure.accept(databaseError.getMessage());
            }
        });
    }
    
    // Update user profile methods
    public static void updateUserProfile(String userId, Map<String, Object> profileData, Consumer<Void> onSuccess, Consumer<String> onFailure) {
        // First check if migration occurred
        isMigrated(userId, migrated -> {
            if (migrated) {
                updateUserProfileFirestore(userId, profileData, onSuccess, onFailure);
            } else {
                updateUserProfileRealtimeDb(userId, profileData, onSuccess, onFailure);
            }
        });
    }
    
    private static void updateUserProfileFirestore(String userId, Map<String, Object> profileData, Consumer<Void> onSuccess, Consumer<String> onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection(USERS_COLLECTION).document(userId);
        
        // Add last_updated timestamp
        profileData.put("last_updated", FieldValue.serverTimestamp());
        
        userDocRef.set(profileData, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User profile updated in Firestore");
                onSuccess.accept(null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating user profile in Firestore", e);
                onFailure.accept(e.getMessage());
            });
    }
    
    private static void updateUserProfileRealtimeDb(String userId, Map<String, Object> profileData, Consumer<Void> onSuccess, Consumer<String> onFailure) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(userId)
                .child("profile");

        userRef.updateChildren(profileData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile updated successfully");
                    onSuccess.accept(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user profile", e);
                    onFailure.accept(e.getMessage());
                });
    }
    
    // Other methods follow the same pattern - add Firestore versions and the selector method
    // ...
} 