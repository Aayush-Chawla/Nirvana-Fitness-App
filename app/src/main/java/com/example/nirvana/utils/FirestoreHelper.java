package com.example.nirvana.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.nirvana.models.WorkoutPlan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Helper class for Firestore operations
 */
public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    // Collection constants
    public static final String USERS_COLLECTION = "users";
    public static final String MEALS_COLLECTION = "meals";
    public static final String DIET_COLLECTION = "diet";

    /**
     * Get the current user's document reference
     *
     * @return DocumentReference for the user, or null if not authenticated
     */
    public static DocumentReference getUserDocRef() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        }
        return db.collection(USERS_COLLECTION).document(user.getUid());
    }

    /**
     * Get meals for the current user for the current day
     *
     * @param listener Callback for the meal data
     */
    public static void getMeals(OnDataFetchedListener<Map<String, List<Map<String, Object>>>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) {
                listener.onError("User not authenticated");
            }
            return;
        }

        // Get today's date
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        
        // We need to query all meal types separately
        List<String> mealTypes = List.of("breakfast", "lunch", "dinner", "snacks");
        Map<String, List<Map<String, Object>>> allMeals = new HashMap<>();
        AtomicInteger completedQueries = new AtomicInteger(0);
        
        for (String mealType : mealTypes) {
            allMeals.put(mealType, new ArrayList<>());
            
            userRef.collection(MEALS_COLLECTION)
                .document(today)
                .collection(mealType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> mealItems = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        mealItems.add(doc.getData());
                    }
                    
                    allMeals.put(mealType, mealItems);
                    
                    // Check if all queries have completed
                    if (completedQueries.incrementAndGet() == mealTypes.size()) {
                        if (listener != null) {
                            listener.onDataFetched(allMeals);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting meals for " + mealType, e);
                    
                    // Even on failure, we need to count this as completed
                    if (completedQueries.incrementAndGet() == mealTypes.size()) {
                        if (listener != null) {
                            listener.onDataFetched(allMeals);
                        }
                    }
                });
        }
    }

    /**
     * Add a new document to a collection
     *
     * @param collection Collection name
     * @param data       Data to add
     * @param callback   Callback for document ID
     */
    public static void addDocument(String collection, Map<String, Object> data, final DocumentCallback callback) {
        db.collection(collection)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    if (callback != null) {
                        callback.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Update an existing document
     *
     * @param collection  Collection name
     * @param documentId  Document ID
     * @param data        Data to update
     * @param callback    Callback for success/failure
     */
    public static void updateDocument(String collection, String documentId, Map<String, Object> data, final OperationCallback callback) {
        db.collection(collection).document(documentId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating document", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Delete a document
     *
     * @param collection  Collection name
     * @param documentId  Document ID
     * @param callback    Callback for success/failure
     */
    public static void deleteDocument(String collection, String documentId, final OperationCallback callback) {
        db.collection(collection).document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting document", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Get a document by ID
     *
     * @param collection  Collection name
     * @param documentId  Document ID
     * @param callback    Callback for document data
     */
    public static void getDocument(String collection, String documentId, final DocumentSnapshotCallback callback) {
        db.collection(collection).document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "DocumentSnapshot successfully retrieved!");
                    if (callback != null) {
                        callback.onSuccess(documentSnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting document", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Get all documents in a collection
     *
     * @param collection  Collection name
     * @param callback    Callback for query results
     */
    public static void getCollection(String collection, final QuerySnapshotCallback callback) {
        db.collection(collection)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Collection successfully retrieved!");
                    if (callback != null) {
                        callback.onSuccess(queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting collection", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Log food item to Firestore for the current user and date
     *
     * @param mealType    Type of meal (Breakfast, Lunch, Dinner, Snacks, Other)
     * @param foodItem    Food item to log
     * @param servingSize Serving size selected
     * @param listener    Callback for completion
     */
    public static void logFood(String mealType, com.example.nirvana.models.FoodItem foodItem, String servingSize, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) {
                listener.onFailure("User not authenticated");
            }
            return;
        }

        // Get today's date
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        // Convert mealType to lowercase for collection name
        String mealCollection = mealType.toLowerCase(Locale.US);
        
        // Calculate nutritional values based on serving size
        Map<String, Object> foodData = new HashMap<>();
        foodData.put("name", foodItem.getName());
        foodData.put("servingSize", servingSize);
        foodData.put("calories", foodItem.getCalories());
        foodData.put("protein", foodItem.getProtein());
        foodData.put("carbs", foodItem.getCarbs());
        foodData.put("fat", foodItem.getFat());
        foodData.put("timestamp", new Date().getTime());
        
        userRef.collection(MEALS_COLLECTION)
                .document(today)
                .collection(mealCollection)
                .add(foodData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Food logged successfully: " + documentReference.getId());
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error logging food", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Migrate user data from Firebase Realtime Database to Firestore
     *
     * @param userId   User ID
     * @param onSuccess Callback for success
     * @param onError  Callback for error
     */
    public static void migrateUserData(String userId, Runnable onSuccess, Consumer<String> onError) {
        // TODO: Implement actual migration logic
        // This is a placeholder that just calls success since we're not implementing actual migration
        
        if (onSuccess != null) {
            onSuccess.run();
        }
    }

    /**
     * Get the current user's profile data
     * 
     * @param listener Callback for the user profile data
     */
    public static void getUserProfile(OnDataFetchedListener<Map<String, Object>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) {
                listener.onError("User not authenticated");
            }
            return;
        }

        userRef.get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> userData = documentSnapshot.getData();
                    if (userData == null) {
                        userData = new HashMap<>();
                    }
                    if (listener != null) {
                        listener.onDataFetched(userData);
                    }
                } else {
                    if (listener != null) {
                        listener.onDataFetched(new HashMap<>());
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error getting user profile", e);
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            });
    }

    /**
     * Get recent workouts for the current user
     * 
     * @param limit     Maximum number of workouts to retrieve
     * @param listener  Callback for the workout data
     */
    public static void getRecentWorkouts(int limit, OnDataFetchedListener<List<Map<String, Object>>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) {
                listener.onError("User not authenticated");
            }
            return;
        }

        userRef.collection("workouts")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> workouts = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    Map<String, Object> workout = doc.getData();
                    if (workout != null) {
                        // Add the document ID to the workout data
                        workout.put("id", doc.getId());
                        workouts.add(workout);
                    }
                }
                if (listener != null) {
                    listener.onDataFetched(workouts);
                }
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error getting recent workouts", e);
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            });
    }

    /**
     * Delete a food item from a meal
     *
     * @param mealType     Type of meal (breakfast, lunch, dinner, snacks)
     * @param foodItemDocId Document ID of the food item to delete
     * @param calories     Calories of the food item (for updating daily totals)
     * @param listener     Callback for delete operation
     */
    public static void deleteFoodItem(String mealType, String foodItemDocId, int calories, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) {
                listener.onFailure("User not authenticated");
            }
            return;
        }

        // Get today's date
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        
        // Convert mealType to lowercase for collection name
        String mealCollection = mealType.toLowerCase(Locale.US);
        
        // Delete the food item document
        userRef.collection(MEALS_COLLECTION)
                .document(today)
                .collection(mealCollection)
                .document(foodItemDocId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Food item successfully deleted: " + foodItemDocId);
                    
                    // Update daily calorie count if needed
                    updateDailyTotals(userRef, today, -calories);
                    
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting food item", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }
    
    /**
     * Update daily nutritional totals after adding or removing food items
     * 
     * @param userRef User document reference
     * @param date Date to update
     * @param caloriesDelta Change in calories (positive for addition, negative for removal)
     */
    private static void updateDailyTotals(DocumentReference userRef, String date, int caloriesDelta) {
        // Get the daily totals document
        DocumentReference dailyTotalsRef = userRef.collection("dailyTotals").document(date);
        
        // Update the calories field using a transaction to avoid race conditions
        db.runTransaction(transaction -> {
            DocumentSnapshot dailyTotalsDoc = transaction.get(dailyTotalsRef);
            
            int currentCalories = 0;
            if (dailyTotalsDoc.exists() && dailyTotalsDoc.contains("calories")) {
                Object calories = dailyTotalsDoc.get("calories");
                if (calories instanceof Number) {
                    currentCalories = ((Number) calories).intValue();
                }
            }
            
            // Calculate new total and ensure it doesn't go below zero
            int newCalories = Math.max(0, currentCalories + caloriesDelta);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("calories", newCalories);
            updates.put("lastUpdated", new Date());
            
            if (dailyTotalsDoc.exists()) {
                transaction.update(dailyTotalsRef, updates);
            } else {
                transaction.set(dailyTotalsRef, updates);
            }
            
            return null;
        }).addOnSuccessListener(result -> {
            Log.d(TAG, "Daily totals updated successfully");
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Error updating daily totals", e);
        });
    }

    /**
     * Update daily calorie count
     *
     * @param calories The amount of calories to add or subtract from the daily total
     * @param listener Callback for the operation
     */
    public static void updateDailyCalories(int calories, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) {
                listener.onFailure("User not authenticated");
            }
            return;
        }

        // Get today's date
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        
        // Get reference to daily totals document
        DocumentReference dailyTotalsRef = userRef.collection("dailyTotals").document(today);
        
        // Update the calories field using a transaction
        db.runTransaction(transaction -> {
            DocumentSnapshot dailyTotalsDoc = transaction.get(dailyTotalsRef);
            
            int currentCalories = 0;
            if (dailyTotalsDoc.exists() && dailyTotalsDoc.contains("calories")) {
                Object currentCal = dailyTotalsDoc.get("calories");
                if (currentCal instanceof Number) {
                    currentCalories = ((Number) currentCal).intValue();
                }
            }
            
            // Calculate new total (can go negative in this case since it's a direct update)
            int newCalories = currentCalories + calories;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("calories", newCalories);
            updates.put("lastUpdated", new Date());
            
            if (dailyTotalsDoc.exists()) {
                transaction.update(dailyTotalsRef, updates);
            } else {
                transaction.set(dailyTotalsRef, updates);
            }
            
            return newCalories;
        }).addOnSuccessListener(result -> {
            Log.d(TAG, "Daily calories updated successfully to: " + result);
            if (listener != null) {
                listener.onSuccess();
            }
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Error updating daily calories", e);
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        });
    }

    public interface DocumentCallback {
        void onSuccess(String documentId);
        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface DocumentSnapshotCallback {
        void onSuccess(DocumentSnapshot document);
        void onError(String errorMessage);
    }

    public interface QuerySnapshotCallback {
        void onSuccess(QuerySnapshot querySnapshot);
        void onError(String errorMessage);
    }
    
    public interface OnDataFetchedListener<T> {
        void onDataFetched(T data);
        void onError(String message);
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Get chat history for the current user
     *
     * @param limit     Maximum number of messages to retrieve
     * @param listener  Callback for the chat history data
     */
    public static void getChatHistory(int limit, OnDataFetchedListener<List<Map<String, Object>>> listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) {
                listener.onError("User not authenticated");
            }
            return;
        }

        userRef.collection("chatHistory")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> chatMessages = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    Map<String, Object> message = doc.getData();
                    if (message != null) {
                        // Add the document ID to the message data
                        message.put("id", doc.getId());
                        chatMessages.add(message);
                    }
                }
                if (listener != null) {
                    listener.onDataFetched(chatMessages);
                }
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error getting chat history", e);
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            });
    }

    /**
     * Save a chat message to the user's chat history
     *
     * @param userMessage  The message from the user
     * @param botResponse  The response from the bot
     * @param listener     Callback for the operation
     */
    public static void saveChatMessage(String userMessage, String botResponse, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) {
                listener.onFailure("User not authenticated");
            }
            return;
        }

        // Create a data object for the chat message
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("userMessage", userMessage);
        chatMessage.put("botResponse", botResponse);
        chatMessage.put("timestamp", new Date());

        // Add to the chatHistory collection
        userRef.collection("chatHistory")
            .add(chatMessage)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Chat message saved with ID: " + documentReference.getId());
                if (listener != null) {
                    listener.onSuccess();
                }
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error saving chat message", e);
                if (listener != null) {
                    listener.onFailure(e.getMessage());
                }
            });
    }

    /**
     * Save a generated workout plan to Firestore for the current user
     * 
     * @param workoutPlan The workout plan to save
     * @param listener    Callback for the operation
     */
    public static void saveWorkoutPlan(WorkoutPlan workoutPlan, OnCompleteListener listener) {
        DocumentReference userRef = getUserDocRef();
        if (userRef == null) {
            if (listener != null) {
                listener.onFailure("User not authenticated");
            }
            return;
        }

        // Create a workout plan document in the "workoutPlans" collection
        Map<String, Object> planData = new HashMap<>();
        planData.put("name", workoutPlan.getName());
        planData.put("description", workoutPlan.getDescription());
        planData.put("fitnessLevel", workoutPlan.getFitnessLevel());
        planData.put("goal", workoutPlan.getGoal());
        planData.put("daysPerWeek", workoutPlan.getDaysPerWeek());
        planData.put("minutesPerWorkout", workoutPlan.getMinutesPerWorkout());
        planData.put("createdAt", new Date());
        
        // Add the plan
        userRef.collection("workoutPlans")
            .add(planData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Workout plan saved with ID: " + documentReference.getId());
                
                // Now save each workout separately
                saveWorkouts(documentReference, workoutPlan, listener);
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error saving workout plan", e);
                if (listener != null) {
                    listener.onFailure(e.getMessage());
                }
            });
    }
    
    /**
     * Save the workouts associated with a workout plan
     * 
     * @param planRef     Reference to the workout plan document
     * @param workoutPlan The workout plan containing workouts
     * @param listener    Callback for the operation
     */
    private static void saveWorkouts(DocumentReference planRef, WorkoutPlan workoutPlan, OnCompleteListener listener) {
        List<Map<String, Object>> workoutsData = new ArrayList<>();
        
        // Get all workouts from the plan
        for (int i = 0; i < workoutPlan.getWorkouts().size(); i++) {
            com.example.nirvana.models.Workout workout = workoutPlan.getWorkouts().get(i);
            
            Map<String, Object> workoutData = new HashMap<>();
            workoutData.put("name", workout.getName());
            workoutData.put("dayNumber", workout.getDayNumber());
            workoutData.put("targetMuscleGroups", workout.getTargetMuscleGroups());
            workoutData.put("durationMinutes", workout.getDurationMinutes());
            
            // Store the exercises for this workout
            List<Map<String, Object>> exercisesData = new ArrayList<>();
            for (com.example.nirvana.models.Exercise exercise : workout.getExercises()) {
                Map<String, Object> exerciseData = new HashMap<>();
                exerciseData.put("name", exercise.getName());
                exerciseData.put("muscleGroup", exercise.getMuscleGroup());
                exerciseData.put("sets", exercise.getSets());
                exerciseData.put("reps", exercise.getReps());
                exerciseData.put("restSeconds", exercise.getRestSeconds());
                exerciseData.put("description", exercise.getDescription());
                exerciseData.put("equipment", exercise.getEquipment());
                
                exercisesData.add(exerciseData);
            }
            
            workoutData.put("exercises", exercisesData);
            workoutsData.add(workoutData);
        }
        
        // Save all workouts to the workouts subcollection
        Map<String, Object> allWorkoutsData = new HashMap<>();
        allWorkoutsData.put("workouts", workoutsData);
        
        planRef.collection("workouts")
            .document("all_workouts")
            .set(allWorkoutsData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Workouts saved successfully");
                if (listener != null) {
                    listener.onSuccess();
                }
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error saving workouts", e);
                if (listener != null) {
                    listener.onFailure(e.getMessage());
                }
            });
    }
} 