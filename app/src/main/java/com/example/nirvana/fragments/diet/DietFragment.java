package com.example.nirvana.fragments.diet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nirvana.R;
import com.example.nirvana.models.FoodItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.example.nirvana.utils.FirestoreHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class DietFragment extends Fragment {
    private static final String TAG = "DietFragment";

    private PieChart macrosChart;
    private MaterialButton btnLogDiet;
    private TextView tvCalories, tvRemaining, tvGoal;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private ListenerRegistration mealsListener;
    private ListenerRegistration goalListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        initializeViews(view);
        initializeFirebase();
        setupButtonListeners();
        setupRealTimeListeners();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listeners to prevent memory leaks
        if (mealsListener != null) {
            mealsListener.remove();
        }
        if (goalListener != null) {
            goalListener.remove();
        }
    }

    private void initializeViews(View view) {
        macrosChart = view.findViewById(R.id.macrosChart);
        btnLogDiet = view.findViewById(R.id.btnLogDiet);
        tvCalories = view.findViewById(R.id.tvCalories);
        tvRemaining = view.findViewById(R.id.tvRemaining);
        tvGoal = view.findViewById(R.id.tvGoal);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
    }

    private void setupButtonListeners() {
        btnLogDiet.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dietFragment_to_logDietFragment));
    }

    private void setupRealTimeListeners() {
        // Get today's date in the format used by the app
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        DocumentReference userRef = FirestoreHelper.getUserDocRef();
        
        if (userRef == null) {
            Log.e(TAG, "User not authenticated");
            return;
        }
        
        // First, check if calorie goal exists and create it if it doesn't
        checkAndCreateCalorieGoal(userRef);
        
        // Listen for changes to all meal collections
        setupMealListener(userRef, today);
        
        // Listen for changes to the calorie goal
        setupCalorieGoalListener(userRef);
    }
    
    private void setupMealListener(DocumentReference userRef, String today) {
        // We need to listen to all meal types separately
        List<String> mealTypes = List.of("breakfast", "lunch", "dinner", "snacks");
        
        for (String mealType : mealTypes) {
            userRef.collection(FirestoreHelper.MEALS_COLLECTION)
                .document(today)
                .collection(mealType)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for " + mealType, error);
                        return;
                    }

                    if (snapshots == null) {
                        return;
                    }
                    
                    // When any meal collection changes, recalculate all totals
                    calculateNutrientTotals();
                });
        }
    }
    
    private void setupCalorieGoalListener(DocumentReference userRef) {
        goalListener = userRef.collection("diet")
            .document("goals")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Listen failed for calorie goal", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Long goal = snapshot.getLong("calories");
                    if (goal != null) {
                        updateCalorieGoal(goal);
                    }
                }
            });
    }

    private void checkAndCreateCalorieGoal(DocumentReference userRef) {
        // Check if diet goals document exists
        userRef.collection("diet").document("goals").get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists() || document.getLong("calories") == null || document.getLong("calories") == 0) {
                        // No calorie goal exists, fetch user profile to calculate it
                        userRef.collection("profile").document("details").get()
                            .addOnSuccessListener(profileDoc -> {
                                if (profileDoc.exists()) {
                                    // Calculate calorie goal from profile data
                                    long calorieGoal = calculateCalorieGoal(profileDoc);
                                    
                                    // Save the calculated goal
                                    Map<String, Object> goals = new HashMap<>();
                                    goals.put("calories", calorieGoal);
                                    
                                    userRef.collection("diet").document("goals").set(goals, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Calorie goal saved: " + calorieGoal);
                                            // Update UI immediately
                                            updateCalorieGoal(calorieGoal);
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Error saving calorie goal", e));
                                } else {
                                    // No profile exists, set a default goal
                                    setDefaultCalorieGoal(userRef);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error fetching user profile", e);
                                // Set default goal on failure
                                setDefaultCalorieGoal(userRef);
                            });
                    }
                } else {
                    Log.e(TAG, "Error checking calorie goal", task.getException());
                }
            });
    }

    private long calculateCalorieGoal(DocumentSnapshot profileDoc) {
        // Extract profile data with safe defaults
        double weight = profileDoc.getDouble("weight") != null ? profileDoc.getDouble("weight") : 70.0;
        double height = profileDoc.getDouble("height") != null ? profileDoc.getDouble("height") : 170.0;
        long age = profileDoc.getLong("age") != null ? profileDoc.getLong("age") : 30;
        String gender = profileDoc.getString("gender") != null ? profileDoc.getString("gender") : "Male";
        String activityLevel = profileDoc.getString("activityLevel") != null ? profileDoc.getString("activityLevel") : "Moderately Active";
        String fitnessGoal = profileDoc.getString("fitnessGoal") != null ? profileDoc.getString("fitnessGoal") : "Maintain current fitness";
        
        // Calculate BMR using Mifflin-St Jeor Equation
        double bmr;
        if (gender.equals("Male")) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }
        
        // Calculate TDEE based on activity level
        double tdee = calculateTDEE(bmr, activityLevel);
        
        // Adjust based on fitness goal
        if (fitnessGoal.contains("Lose weight")) {
            tdee = tdee * 0.85; // 15% deficit for weight loss
        } else if (fitnessGoal.contains("Build muscle")) {
            tdee = tdee * 1.1; // 10% surplus for muscle gain
        }
        
        return Math.round(tdee);
    }

    private double calculateTDEE(double bmr, String activityLevel) {
        if (activityLevel.contains("Sedentary")) {
            return bmr * 1.2;
        } else if (activityLevel.contains("Lightly Active")) {
            return bmr * 1.375;
        } else if (activityLevel.contains("Moderately Active")) {
            return bmr * 1.55;
        } else if (activityLevel.contains("Very Active")) {
            return bmr * 1.725;
        } else if (activityLevel.contains("Extra Active")) {
            return bmr * 1.9;
        } else {
            return bmr * 1.2;
        }
    }

    private void setDefaultCalorieGoal(DocumentReference userRef) {
        long defaultGoal = 2000; // Default daily calorie goal
        Map<String, Object> goals = new HashMap<>();
        goals.put("calories", defaultGoal);
        
        userRef.collection("diet").document("goals").set(goals, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Default calorie goal saved: " + defaultGoal);
                // Update UI immediately
                updateCalorieGoal(defaultGoal);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error saving default calorie goal", e));
    }

    private void calculateNutrientTotals() {
        FirestoreHelper.getMeals(new FirestoreHelper.OnDataFetchedListener<Map<String, List<Map<String, Object>>>>() {
            @Override
            public void onDataFetched(Map<String, List<Map<String, Object>>> meals) {
                double totalCalories = 0;
                double totalProtein = 0;
                double totalCarbs = 0;
                double totalFat = 0;

                // Process each meal type
                for (List<Map<String, Object>> mealItems : meals.values()) {
                    for (Map<String, Object> foodMap : mealItems) {
                        // Extract numeric values safely
                        if (foodMap.containsKey("calories")) {
                            Object cal = foodMap.get("calories");
                            if (cal instanceof Number) {
                                totalCalories += ((Number) cal).doubleValue();
                            }
                        }
                        
                        if (foodMap.containsKey("protein")) {
                            Object prot = foodMap.get("protein");
                            if (prot instanceof Number) {
                                totalProtein += ((Number) prot).doubleValue();
                            }
                        }
                        
                        if (foodMap.containsKey("carbs")) {
                            Object carb = foodMap.get("carbs");
                            if (carb instanceof Number) {
                                totalCarbs += ((Number) carb).doubleValue();
                            }
                        }
                        
                        if (foodMap.containsKey("fat")) {
                            Object fat = foodMap.get("fat");
                            if (fat instanceof Number) {
                                totalFat += ((Number) fat).doubleValue();
                            }
                        }
                    }
                }

                // Update UI with the totals
                updateUI(totalCalories, totalProtein, totalCarbs, totalFat);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching meals: " + message);
            }
        });
    }

    private void updateUI(double calories, double protein, double carbs, double fat) {
        tvCalories.setText(String.format("%.0f", calories));
        setupMacrosChart(protein, carbs, fat);
        
        // Update remaining calories if goal is available
        String goalText = tvGoal.getText().toString();
        if (!goalText.isEmpty() && !goalText.equals("0")) {
            try {
                long goal = Long.parseLong(goalText);
                long remaining = goal - (long)calories;
                tvRemaining.setText(String.valueOf(remaining));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing goal value", e);
            }
        }
    }

    private void updateCalorieGoal(long goal) {
        tvGoal.setText(String.valueOf(goal));
        
        // Update remaining calories
        String caloriesText = tvCalories.getText().toString();
        if (!caloriesText.isEmpty()) {
            try {
                long calories = Long.parseLong(caloriesText);
                long remaining = goal - calories;
                tvRemaining.setText(String.valueOf(remaining));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing calories value", e);
            }
        }
    }

    private void setupMacrosChart(double protein, double carbs, double fat) {
        List<PieEntry> entries = new ArrayList<>();
        if (protein > 0) entries.add(new PieEntry((float)protein, "Protein"));
        if (carbs > 0) entries.add(new PieEntry((float)carbs, "Carbs"));
        if (fat > 0) entries.add(new PieEntry((float)fat, "Fat"));

        PieDataSet dataSet = new PieDataSet(entries, "Macros Breakdown");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        macrosChart.setData(new PieData(dataSet));
        macrosChart.getDescription().setEnabled(false);
        macrosChart.setCenterText("Macros");
        macrosChart.setEntryLabelColor(getResources().getColor(android.R.color.black));
        macrosChart.animateY(1000);
        macrosChart.invalidate();
    }
}