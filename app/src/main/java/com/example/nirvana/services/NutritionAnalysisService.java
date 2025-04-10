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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
        
        // Start listening to profile changes
        setupProfileListener();
        
        // Start listening to food log changes
        setupFoodLogsListener();
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
                    
                    // Update recommended values
                    currentAnalysis.setRecommendedCalories(tdee);
                    currentAnalysis.setRecommendedProtein(weightValue * 1.6);
                    currentAnalysis.setRecommendedCarbs(tdee * 0.45 / 4);
                    currentAnalysis.setRecommendedFat(tdee * 0.25 / 9);
                    
                    // Update analysis with new recommendations
                    updateAnalysis();
                } else {
                    // Use default values if profile doesn't exist
                    setDefaultRecommendations();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (currentCallback != null) {
                    currentCallback.onError("Failed to load profile: " + databaseError.getMessage());
                }
            }
        };

        userRef.child("profile").addValueEventListener(profileListener);
    }

    private void setupFoodLogsListener() {
        List<String> dates = getLastSevenDays();
        final double[] totals = {0, 0, 0, 0}; // calories, protein, carbs, fat

        if (foodLogsListener != null) {
            userRef.child("food_logs").removeEventListener(foodLogsListener);
        }

        foodLogsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    // Reset totals
                    for (int i = 0; i < totals.length; i++) {
                        totals[i] = 0;
                    }

                    int daysWithData = 0;
                    
                    // Calculate totals for each date
                    for (String date : dates) {
                        DataSnapshot dateSnapshot = dataSnapshot.child(date);
                        boolean hasDataForDate = false;
                        
                        if (dateSnapshot.exists()) {
                            // First try to access all meal types (breakfast, lunch, dinner, snacks)
                            for (DataSnapshot mealTypeSnapshot : dateSnapshot.getChildren()) {
                                // For each food item in the meal type
                                for (DataSnapshot foodSnapshot : mealTypeSnapshot.getChildren()) {
                                    hasDataForDate = true;
                                    // Try to get values with null checks
                                    Double calories = foodSnapshot.child("calories").getValue(Double.class);
                                    Double protein = foodSnapshot.child("protein").getValue(Double.class);
                                    Double carbs = foodSnapshot.child("carbs").getValue(Double.class);
                                    Double fat = foodSnapshot.child("fat").getValue(Double.class);
                                    
                                    // Add values with null checks
                                    totals[0] += calories != null ? calories : 0;
                                    totals[1] += protein != null ? protein : 0;
                                    totals[2] += carbs != null ? carbs : 0;
                                    totals[3] += fat != null ? fat : 0;
                                    
                                    Log.d(TAG, "Food item found: " + 
                                        (calories != null ? calories : 0) + " calories, " + 
                                        (protein != null ? protein : 0) + "g protein");
                                }
                            }
                        }
                        
                        // If we found data for this date, increment counter
                        if (hasDataForDate) {
                            daysWithData++;
                        }
                    }

                    Log.d(TAG, "Found data for " + daysWithData + " days");
                    Log.d(TAG, "Total calories: " + totals[0] + ", protein: " + totals[1] + 
                          ", carbs: " + totals[2] + ", fat: " + totals[3]);
                    
                    // Calculate averages - avoid division by zero
                    int divisor = Math.max(1, daysWithData); // Use actual days with data instead of 7
                    
                    // Update analysis with new totals
                    currentAnalysis.setTotalCalories(totals[0]);
                    currentAnalysis.setTotalProtein(totals[1]);
                    currentAnalysis.setTotalCarbs(totals[2]);
                    currentAnalysis.setTotalFat(totals[3]);
                    
                    currentAnalysis.setAverageCalories(totals[0] / divisor);
                    currentAnalysis.setAverageProtein(totals[1] / divisor);
                    currentAnalysis.setAverageCarbs(totals[2] / divisor);
                    currentAnalysis.setAverageFat(totals[3] / divisor);
                    
                    // Update analysis with new values
                    updateAnalysis();

                } catch (Exception e) {
                    Log.e(TAG, "Error processing food logs: " + e.getMessage(), e);
                    if (currentCallback != null) {
                        currentCallback.onError("Error processing food logs: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (currentCallback != null) {
                    currentCallback.onError("Failed to load food logs: " + databaseError.getMessage());
                }
            }
        };

        userRef.child("food_logs").addValueEventListener(foodLogsListener);
        
        // Also fetch data from Firestore as the app may be transitioning between databases
        fetchFirestoreNutritionData(dates);
    }
    
    private void fetchFirestoreNutritionData(List<String> dates) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        final double[] totals = {0, 0, 0, 0}; // calories, protein, carbs, fat
        final int[] processedDates = {0};
        final int[] daysWithData = {0};
        
        // Try direct Firestore path first - this is for logged meals
        db.collection("users").document(userId)
          .collection("meals")
          .get()
          .addOnSuccessListener(querySnapshot -> {
              Log.d(TAG, "Querying Firestore meals collection, found: " + querySnapshot.size() + " documents");
              
              boolean hasData = false;
              for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                  hasData = true;
                  Map<String, Object> data = document.getData();
                  if (data != null) {
                      Log.d(TAG, "Found meal document: " + document.getId() + " with data: " + data);
                      
                      // Extract numeric values with proper type conversion
                      Object calObj = data.get("calories");
                      Object protObj = data.get("protein");
                      Object carbObj = data.get("carbs");
                      Object fatObj = data.get("fat");
                      
                      double calories = extractNumberValue(calObj);
                      double protein = extractNumberValue(protObj);
                      double carbs = extractNumberValue(carbObj);
                      double fat = extractNumberValue(fatObj);
                      
                      totals[0] += calories;
                      totals[1] += protein;
                      totals[2] += carbs;
                      totals[3] += fat;
                      
                      Log.d(TAG, "Added values: calories=" + calories + ", protein=" + protein + 
                            ", carbs=" + carbs + ", fat=" + fat);
                  }
              }
              
              if (hasData) {
                  daysWithData[0]++;
              }
              
              // Now try the structured date approach from the previous implementation
              checkAllDates(dates, db, totals, daysWithData);
          })
          .addOnFailureListener(e -> {
              Log.e(TAG, "Error fetching Firestore meals: " + e.getMessage());
              // Try the structured date approach as a fallback
              checkAllDates(dates, db, totals, daysWithData);
          });
    }
    
    private void checkAllDates(List<String> dates, com.google.firebase.firestore.FirebaseFirestore db, 
                              double[] totals, int[] daysWithData) {
        final int[] processedDates = {0};
        final List<String> mealTypes = Arrays.asList("breakfast", "lunch", "dinner", "snacks");
        
        for (String date : dates) {
            final int[] processedMealTypes = {0};
            final boolean[] dateHasData = {false};
            
            for (String mealType : mealTypes) {
                // Path: users/{userId}/food_logs/{date}/{mealType}
                db.collection("users").document(userId)
                  .collection("food_logs").document(date)
                  .collection(mealType)
                  .get()
                  .addOnSuccessListener(querySnapshot -> {
                      boolean mealHasData = false;
                      
                      for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                          mealHasData = true;
                          dateHasData[0] = true;
                          Map<String, Object> data = document.getData();
                          if (data != null) {
                              Log.d(TAG, "Found food item in " + mealType + " for date " + date + ": " + data);
                              
                              // Extract numeric values with proper type conversion
                              Object calObj = data.get("calories");
                              Object protObj = data.get("protein");
                              Object carbObj = data.get("carbs");
                              Object fatObj = data.get("fat");
                              
                              double calories = extractNumberValue(calObj);
                              double protein = extractNumberValue(protObj);
                              double carbs = extractNumberValue(carbObj);
                              double fat = extractNumberValue(fatObj);
                              
                              totals[0] += calories;
                              totals[1] += protein;
                              totals[2] += carbs;
                              totals[3] += fat;
                          }
                      }
                      
                      if (mealHasData) {
                          Log.d(TAG, "Found data for " + mealType + " on " + date);
                      }
                      
                      processedMealTypes[0]++;
                      
                      // Check if we've processed all meal types for this date
                      if (processedMealTypes[0] >= mealTypes.size()) {
                          if (dateHasData[0]) {
                              daysWithData[0]++;
                          }
                          
                          processedDates[0]++;
                          
                          // If we've processed all dates, update the analysis
                          if (processedDates[0] >= dates.size()) {
                              updateFirestoreAnalysis(totals, daysWithData[0]);
                          }
                      }
                  })
                  .addOnFailureListener(e -> {
                      Log.e(TAG, "Error fetching " + mealType + " for " + date + ": " + e.getMessage());
                      
                      processedMealTypes[0]++;
                      
                      // Still need to check if all meal types are processed
                      if (processedMealTypes[0] >= mealTypes.size()) {
                          processedDates[0]++;
                          
                          if (processedDates[0] >= dates.size()) {
                              updateFirestoreAnalysis(totals, daysWithData[0]);
                          }
                      }
                  });
            }
        }
    }
    
    private void updateFirestoreAnalysis(double[] totals, int daysWithData) {
        Log.d(TAG, "Firestore totals: calories=" + totals[0] + ", protein=" + totals[1] + 
              ", carbs=" + totals[2] + ", fat=" + totals[3] + " over " + daysWithData + " days");
              
        // Only update if we have data and the current analysis is empty
        if (totals[0] > 0 && currentAnalysis.getTotalCalories() == 0) {
            Log.d(TAG, "Updating analysis with Firestore data");
            
            int divisor = Math.max(1, daysWithData);
            
            // Update analysis with Firestore data
            currentAnalysis.setTotalCalories(totals[0]);
            currentAnalysis.setTotalProtein(totals[1]);
            currentAnalysis.setTotalCarbs(totals[2]);
            currentAnalysis.setTotalFat(totals[3]);
            
            currentAnalysis.setAverageCalories(totals[0] / divisor);
            currentAnalysis.setAverageProtein(totals[1] / divisor);
            currentAnalysis.setAverageCarbs(totals[2] / divisor);
            currentAnalysis.setAverageFat(totals[3] / divisor);
            
            updateAnalysis();
        }
    }
    
    private double extractNumberValue(Object value) {
        if (value == null) return 0;
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Could not parse string to number: " + value);
                return 0;
            }
        }
        return 0;
    }

    private void updateAnalysis() {
        if (currentAnalysis != null && currentCallback != null) {
            generateStatusAndRecommendations(currentAnalysis);
            currentCallback.onAnalysisComplete(currentAnalysis);
        }
    }

    private void setDefaultRecommendations() {
        double defaultWeight = 70.0;
        double defaultTDEE = 2000.0;
        
        currentAnalysis.setRecommendedCalories(defaultTDEE);
        currentAnalysis.setRecommendedProtein(defaultWeight * 1.6);
        currentAnalysis.setRecommendedCarbs(defaultTDEE * 0.45 / 4);
        currentAnalysis.setRecommendedFat(defaultTDEE * 0.25 / 9);
        
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

    private void generateStatusAndRecommendations(NutritionAnalysis analysis) {
        // Generate status for each nutrient
        analysis.setCalorieStatus(generateStatus(analysis.getAverageCalories(), analysis.getRecommendedCalories()));
        analysis.setProteinStatus(generateStatus(analysis.getAverageProtein(), analysis.getRecommendedProtein()));
        analysis.setCarbsStatus(generateStatus(analysis.getAverageCarbs(), analysis.getRecommendedCarbs()));
        analysis.setFatStatus(generateStatus(analysis.getAverageFat(), analysis.getRecommendedFat()));
        
        // Generate recommendations
        List<String> recommendations = new ArrayList<>();
        
        // Calorie recommendations
        if (analysis.getAverageCalories() < analysis.getRecommendedCalories() * 0.9) {
            recommendations.add("Your calorie intake is below the recommended amount. Consider increasing your food intake.");
        } else if (analysis.getAverageCalories() > analysis.getRecommendedCalories() * 1.1) {
            recommendations.add("Your calorie intake is above the recommended amount. Consider reducing portion sizes.");
        }
        
        // Protein recommendations
        if (analysis.getAverageProtein() < analysis.getRecommendedProtein() * 0.9) {
            recommendations.add("Your protein intake is low. Include more lean meats, fish, or plant-based protein sources.");
        }
        
        // Carbs recommendations
        if (analysis.getAverageCarbs() < analysis.getRecommendedCarbs() * 0.9) {
            recommendations.add("Your carbohydrate intake is low. Include more whole grains and vegetables.");
        } else if (analysis.getAverageCarbs() > analysis.getRecommendedCarbs() * 1.1) {
            recommendations.add("Your carbohydrate intake is high. Consider reducing refined carbs and sugars.");
        }
        
        // Fat recommendations
        if (analysis.getAverageFat() > analysis.getRecommendedFat() * 1.1) {
            recommendations.add("Your fat intake is high. Focus on healthy fats and reduce saturated fats.");
        }
        
        // Add general recommendations
        recommendations.add("Aim to eat a variety of foods to ensure you get all necessary nutrients.");
        recommendations.add("Stay hydrated by drinking plenty of water throughout the day.");
        
        analysis.setRecommendations(recommendations.toArray(new String[0]));
    }

    private String generateStatus(double actual, double recommended) {
        double percentage = (actual / recommended) * 100;
        if (percentage < 90) return "Low";
        if (percentage > 110) return "High";
        return "Optimal";
    }
} 