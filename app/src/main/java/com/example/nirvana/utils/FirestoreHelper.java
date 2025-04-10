package com.example.nirvana.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nirvana.models.FoodItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
 * Helper class for Firestore operations related to the app's data
 */
public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    public static final String USERS_COLLECTION = "users";
    public static final String MEALS_COLLECTION = "meals";
    
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    /**
     * Callback interface for data fetch operations
     */
    public interface OnDataFetchedListener<T> {
        void onDataFetched(T data);
        void onError(String message);
    }

    /**
     * Simple callback interface for operations
     */
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Gets the current user's document reference or null if not logged in
     */
    @Nullable
    public static DocumentReference getUserDocRef() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        return db.collection(USERS_COLLECTION).document(currentUser.getUid());
    }

    /**
     * Gets the current user ID or null if not logged in
     */
    @Nullable
    public static String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
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
     * Gets the user's meals for today
     */
    public static void getMeals(OnDataFetchedListener<Map<String, List<Map<String, Object>>>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            listener.onError("User not authenticated");
            return;
        }

        String today = getTodayDate();
        Map<String, List<Map<String, Object>>> meals = new HashMap<>();
        List<String> mealTypes = List.of("breakfast", "lunch", "dinner", "snacks");
        
        // Track completion of all meal type fetches
        final int[] remaining = {mealTypes.size()};
        
        for (String mealType : mealTypes) {
            userRef.collection(MEALS_COLLECTION)
                .document(today)
                .collection(mealType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> foodItems = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Map<String, Object> foodData = doc.getData();
                        if (foodData != null) {
                            foodData.put("docId", doc.getId());
                            foodItems.add(foodData);
                        }
                    }
                    
                    meals.put(mealType, foodItems);
                    
                    // Check if all meal types have been fetched
                    synchronized (remaining) {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            listener.onDataFetched(meals);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting meals for " + mealType, e);
                    
                    // Even on failure, mark this meal type as processed
                    synchronized (remaining) {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            listener.onDataFetched(meals);
                        }
                    }
                });
        }
    }

    /**
     * Gets the user's profile data
     */
    public static void getUserProfile(OnDataFetchedListener<Map<String, Object>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            listener.onError("User not authenticated");
            return;
        }

        userRef.collection("profile").document("details")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> profileData = documentSnapshot.getData();
                if (profileData == null) {
                    profileData = new HashMap<>();
                }
                listener.onDataFetched(profileData);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user profile", e);
                listener.onError(e.getMessage());
            });
    }

    /**
     * Log a food item to the user's meals
     */
    public static void logFood(String mealType, FoodItem foodItem, String servingSize, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        String today = getTodayDate();
        
        Map<String, Object> foodData = new HashMap<>();
        foodData.put("name", foodItem.getName());
        foodData.put("calories", foodItem.getCalories());
        foodData.put("protein", foodItem.getProtein());
        foodData.put("carbs", foodItem.getCarbs());
        foodData.put("fat", foodItem.getFat());
        foodData.put("servingSize", servingSize);
        foodData.put("timestamp", FieldValue.serverTimestamp());
        
        userRef.collection(MEALS_COLLECTION)
            .document(today)
            .collection(mealType.toLowerCase())
            .add(foodData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Food logged successfully");
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error logging food", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Log a food item to the user's meals (data.models.FoodItem version)
     */
    public static void logFood(String mealType, com.example.nirvana.data.models.FoodItem foodItem, String servingSize, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        String today = getTodayDate();
        
        // Get food name from either property
        String foodName = foodItem.getName() != null ? foodItem.getName() : foodItem.getFoodName();
        
        // Get calories from either property
        double calories = foodItem.getCaloriesInt() > 0 ? foodItem.getCaloriesInt() : foodItem.getCalories();
        
        Map<String, Object> foodData = new HashMap<>();
        foodData.put("name", foodName);
        foodData.put("calories", calories);
        foodData.put("protein", foodItem.getProtein());
        foodData.put("carbs", foodItem.getCarbs());
        foodData.put("fat", foodItem.getFat());
        foodData.put("servingSize", servingSize);
        foodData.put("timestamp", FieldValue.serverTimestamp());
        
        userRef.collection(MEALS_COLLECTION)
            .document(today)
            .collection(mealType.toLowerCase())
            .add(foodData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Food logged successfully");
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error logging food", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Delete a food item from the user's meals
     */
    public static void deleteFoodItem(String mealType, String foodItemId, int calories, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        String today = getTodayDate();
        
        userRef.collection(MEALS_COLLECTION)
            .document(today)
            .collection(mealType.toLowerCase())
            .document(foodItemId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Food item deleted successfully");
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting food item", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Update the user's daily caloric intake
     */
    public static void updateDailyCalories(int calories, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        String today = getTodayDate();
        
        Map<String, Object> data = new HashMap<>();
        data.put("calories", FieldValue.increment(calories));
        data.put("date", today);
        data.put("lastUpdated", FieldValue.serverTimestamp());
        
        userRef.collection("diet")
            .document(today)
            .set(data, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Daily calories updated successfully");
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating daily calories", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Get recent workouts
     */
    public static void getRecentWorkouts(int limit, OnDataFetchedListener<List<Map<String, Object>>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            listener.onError("User not authenticated");
            return;
        }

        userRef.collection("workouts")
            .orderBy("timestamp")
            .limit(limit)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> workouts = new ArrayList<>();
                
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    Map<String, Object> workoutData = doc.getData();
                    if (workoutData != null) {
                        workoutData.put("id", doc.getId());
                        workouts.add(workoutData);
                    }
                }
                
                listener.onDataFetched(workouts);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting workouts", e);
                listener.onError(e.getMessage());
            });
    }

    /**
     * Save chat message
     */
    public static void saveChatMessage(String userMessage, String botResponse, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }
        
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("userMessage", userMessage);
        chatData.put("botResponse", botResponse);
        chatData.put("timestamp", FieldValue.serverTimestamp());
        
        userRef.collection("chats")
            .add(chatData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Chat message saved successfully");
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving chat message", e);
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    /**
     * Get chat history
     */
    public static void getChatHistory(int limit, OnDataFetchedListener<List<Map<String, Object>>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            listener.onError("User not authenticated");
            return;
        }

        userRef.collection("chats")
            .orderBy("timestamp")
            .limit(limit)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> chatHistory = new ArrayList<>();
                
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    Map<String, Object> chatData = doc.getData();
                    if (chatData != null) {
                        chatHistory.add(chatData);
                    }
                }
                
                listener.onDataFetched(chatHistory);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting chat history", e);
                listener.onError(e.getMessage());
            });
    }
    
    /**
     * Migrate user data from legacy storage to Firestore
     */
    public static void migrateUserData(String userId, Consumer<Void> onSuccess, Consumer<String> onFailure) {
        // Mark as migrated
        Map<String, Object> data = new HashMap<>();
        data.put("migrated", true);
        data.put("migratedAt", FieldValue.serverTimestamp());
        
        db.collection(USERS_COLLECTION).document(userId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User data migrated successfully");
                onSuccess.accept(null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error migrating user data", e);
                onFailure.accept(e.getMessage());
            });
    }
} 