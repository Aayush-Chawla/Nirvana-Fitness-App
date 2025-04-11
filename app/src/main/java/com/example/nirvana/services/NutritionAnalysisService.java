package com.example.nirvana.services;

import android.util.Log;
import com.example.nirvana.models.NutritionAnalysis;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NutritionAnalysisService {
    private static final String TAG = "NutritionAnalysisService";
    private final DatabaseReference userRef;
    private final String userId;
    private ValueEventListener foodLogsListener;
    private ValueEventListener profileListener;
    private NutritionAnalysis currentAnalysis;
    private NutritionAnalysisCallback currentCallback;

    public NutritionAnalysisService(String userId) {
        this.userId = userId;
        this.userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId);
    }

    public interface NutritionAnalysisCallback {
        void onAnalysisComplete(NutritionAnalysis analysis);
        void onError(String error);
    }

    public void startRealtimeUpdates(NutritionAnalysisCallback callback) {
        this.currentCallback = callback;
        this.currentAnalysis = new NutritionAnalysis();
        
        // First set up profile to get goals
        setupProfileListener();
        
        // Then set up food logs after a short delay to ensure goals are set
        new android.os.Handler().postDelayed(() -> {
            setupFoodLogsListener();
        }, 500);
    }

    public void stopRealtimeUpdates() {
        if (foodLogsListener != null) {
            userRef.child("food_logs").removeEventListener(foodLogsListener);
        }
        if (profileListener != null) {
            userRef.child("profile").removeEventListener(profileListener);
        }
        currentCallback = null;
    }

    private void setupProfileListener() {
        if (profileListener != null) {
            userRef.child("profile").removeEventListener(profileListener);
        }

        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        // Add null checks for user profile data
                        Double weight = dataSnapshot.child("weight").getValue(Double.class);
                        Double height = dataSnapshot.child("height").getValue(Double.class);
                        Integer age = dataSnapshot.child("age").getValue(Integer.class);
                        String gender = dataSnapshot.child("gender").getValue(String.class);
                        String activityLevel = dataSnapshot.child("activityLevel").getValue(String.class);
                        
                        // Use default values if data is missing
                        double weightValue = weight != null ? weight : 70.0;
                        double heightValue = height != null ? height : 170.0;
                        int ageValue = age != null ? age : 30;
                        String genderValue = gender != null ? gender : "Male";
                        String activityLevelValue = activityLevel != null ? activityLevel : "Moderately Active";
                        
                        // Calculate BMR using Mifflin-St Jeor Equation
                        double bmr;
                        if (genderValue.equals("Male")) {
                            bmr = (10 * weightValue) + (6.25 * heightValue) - (5 * ageValue) + 5;
                        } else {
                            bmr = (10 * weightValue) + (6.25 * heightValue) - (5 * ageValue) - 161;
                        }
                        
                        // Calculate TDEE based on activity level
                        double tdee = calculateTDEE(bmr, activityLevelValue);
                        
                        // Set goals based on calculated values
                        currentAnalysis.setCalorieGoal(tdee);
                        currentAnalysis.setProteinGoal(weightValue * 1.6); // 1.6g protein per kg bodyweight
                        currentAnalysis.setCarbsGoal(tdee * 0.45 / 4); // 45% of calories from carbs
                        currentAnalysis.setFatGoal(tdee * 0.25 / 9); // 25% of calories from fat
                        
                        // Log the goals for debugging
                        Log.d(TAG, String.format("Goals set - Calories: %.0f, Protein: %.0f, Carbs: %.0f, Fat: %.0f",
                            tdee, weightValue * 1.6, tdee * 0.45 / 4, tdee * 0.25 / 9));
                        
                        // Update analysis with new goals
                        updateAnalysis();
                    } else {
                        // Use default values if profile doesn't exist
                        setDefaultGoals();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing profile data: " + e.getMessage());
                    setDefaultGoals();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (currentCallback != null) {
                    currentCallback.onError("Failed to load profile: " + databaseError.getMessage());
                }
                setDefaultGoals();
            }
        };

        userRef.child("profile").addValueEventListener(profileListener);
    }

    private void setupFoodLogsListener() {
        List<String> dates = getLastSevenDays();
        Map<String, double[]> dailyTotals = new HashMap<>();

        if (foodLogsListener != null) {
            userRef.child("food_logs").removeEventListener(foodLogsListener);
        }

        foodLogsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    // Reset daily totals but keep the current analysis object
                    dailyTotals.clear();
                    
                    // Clear previous daily values
                    currentAnalysis.getDailyCalories().clear();
                    currentAnalysis.getDailyProtein().clear();
                    currentAnalysis.getDailyCarbs().clear();
                    currentAnalysis.getDailyFat().clear();
                    
                    for (String date : dates) {
                        dailyTotals.put(date, new double[4]); // calories, protein, carbs, fat
                    }

                    // Calculate totals for each date
                    for (String date : dates) {
                        DataSnapshot dateSnapshot = dataSnapshot.child(date);
                        double[] totals = dailyTotals.get(date);
                        
                        if (dateSnapshot.exists()) {
                            for (DataSnapshot foodSnapshot : dateSnapshot.getChildren()) {
                                Double calories = foodSnapshot.child("calories").getValue(Double.class);
                                Double protein = foodSnapshot.child("protein").getValue(Double.class);
                                Double carbs = foodSnapshot.child("carbs").getValue(Double.class);
                                Double fat = foodSnapshot.child("fat").getValue(Double.class);
                                
                                totals[0] += calories != null ? calories : 0;
                                totals[1] += protein != null ? protein : 0;
                                totals[2] += carbs != null ? carbs : 0;
                                totals[3] += fat != null ? fat : 0;
                            }
                        }
                        
                        // Add daily values to analysis (even if zero)
                        currentAnalysis.addDailyValues(totals[0], totals[1], totals[2], totals[3]);
                        
                        // Log the values for debugging
                        Log.d(TAG, String.format("Date: %s, Calories: %.1f, Protein: %.1f, Carbs: %.1f, Fat: %.1f",
                            date, totals[0], totals[1], totals[2], totals[3]));
                    }

                    // Update analysis with new values
                    updateAnalysis();

                } catch (Exception e) {
                    Log.e(TAG, "Error processing food logs: " + e.getMessage());
                    e.printStackTrace();
                    if (currentCallback != null) {
                        currentCallback.onError("Error processing food logs: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Food logs listener cancelled: " + databaseError.getMessage());
                if (currentCallback != null) {
                    currentCallback.onError("Failed to load food logs: " + databaseError.getMessage());
                }
            }
        };

        userRef.child("food_logs").addValueEventListener(foodLogsListener);
    }

    private void updateAnalysis() {
        if (currentAnalysis != null && currentCallback != null) {
            try {
                generateRecommendations();
                currentCallback.onAnalysisComplete(currentAnalysis);
            } catch (Exception e) {
                Log.e(TAG, "Error in updateAnalysis: " + e.getMessage());
                e.printStackTrace();
                currentCallback.onError("Error updating analysis: " + e.getMessage());
            }
        }
    }

    private void setDefaultGoals() {
        double defaultWeight = 70.0;
        double defaultTDEE = 2000.0;
        
        currentAnalysis.setCalorieGoal(defaultTDEE);
        currentAnalysis.setProteinGoal(defaultWeight * 1.6);
        currentAnalysis.setCarbsGoal(defaultTDEE * 0.45 / 4);
        currentAnalysis.setFatGoal(defaultTDEE * 0.25 / 9);
        
        updateAnalysis();
    }

    private List<String> getLastSevenDays() {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        for (int i = 0; i < 7; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        
        return dates;
    }

    private double calculateTDEE(double bmr, String activityLevel) {
        switch (activityLevel) {
            case "Sedentary":
                return bmr * 1.2;
            case "Lightly Active":
                return bmr * 1.375;
            case "Moderately Active":
                return bmr * 1.55;
            case "Very Active":
                return bmr * 1.725;
            case "Extra Active":
                return bmr * 1.9;
            default:
                return bmr * 1.2;
        }
    }

    private void generateRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        // Check calorie intake
        if (currentAnalysis.getAverageCalories() < currentAnalysis.getCalorieGoal() * 0.9) {
            recommendations.add("Increase your calorie intake by adding healthy snacks between meals.");
        } else if (currentAnalysis.getAverageCalories() > currentAnalysis.getCalorieGoal() * 1.1) {
            recommendations.add("Try to reduce your calorie intake by controlling portion sizes.");
        }
        
        // Check protein intake
        if (currentAnalysis.getAverageProtein() < currentAnalysis.getProteinGoal() * 0.9) {
            recommendations.add("Include more lean protein sources like chicken, fish, or legumes.");
        }
        
        // Check carbs intake
        if (currentAnalysis.getAverageCarbs() < currentAnalysis.getCarbsGoal() * 0.9) {
            recommendations.add("Add more complex carbohydrates like whole grains and vegetables.");
        } else if (currentAnalysis.getAverageCarbs() > currentAnalysis.getCarbsGoal() * 1.1) {
            recommendations.add("Consider reducing refined carbohydrates and sugary foods.");
        }
        
        // Check fat intake
        if (currentAnalysis.getAverageFat() < currentAnalysis.getFatGoal() * 0.9) {
            recommendations.add("Include healthy fats from sources like avocados, nuts, and olive oil.");
        } else if (currentAnalysis.getAverageFat() > currentAnalysis.getFatGoal() * 1.1) {
            recommendations.add("Try to reduce intake of saturated and processed fats.");
        }
        
        currentAnalysis.setRecommendations(recommendations.toArray(new String[0]));
    }
} 