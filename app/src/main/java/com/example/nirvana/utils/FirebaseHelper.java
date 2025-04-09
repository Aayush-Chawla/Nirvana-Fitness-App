package com.example.nirvana.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.nirvana.models.FoodItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private static final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void logFood(String mealType, FoodItem foodItem, String servingSize, OnFoodLoggedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) listener.onFailure("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        DatabaseReference mealRef = database
            .child("users")
            .child(userId)
            .child("meals")
            .child(mealType)
            .push();

        Map<String, Object> foodLog = new HashMap<>();
        foodLog.put("id", foodItem.getId());
        foodLog.put("name", foodItem.getName());
        foodLog.put("calories", foodItem.getCalories());
        foodLog.put("servingSize", servingSize);
        foodLog.put("protein", foodItem.getProtein());
        foodLog.put("carbs", foodItem.getCarbs());
        foodLog.put("fat", foodItem.getFat());
        foodLog.put("timestamp", System.currentTimeMillis());

        mealRef.setValue(foodLog)
            .addOnSuccessListener(aVoid -> {
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    private static void updateDailyCalories(String userId, String date, int calories, String servingSize) {
        DatabaseReference caloriesRef = database
            .child("users")
            .child(userId)
            .child("daily_calories")
            .child(date);

        // Parse serving size to calculate actual calories
        int grams = Integer.parseInt(servingSize.replace("g", ""));
        int actualCalories = (calories * grams) / 100; // Assuming base calories are per 100g

        caloriesRef.get().addOnSuccessListener(snapshot -> {
            int currentCalories = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            caloriesRef.setValue(currentCalories + actualCalories);
        });
    }

    public static void resetDailyCalories() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        DatabaseReference caloriesRef = database
            .child("users")
            .child(userId)
            .child("daily_calories")
            .child(date);

        caloriesRef.setValue(0);
    }

    public interface OnFoodLoggedListener {
        void onSuccess();
        void onFailure(String error);
    }
} 