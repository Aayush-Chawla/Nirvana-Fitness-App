package com.example.nirvana.fragments.diet;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.models.NutritionAnalysis;
import com.example.nirvana.models.PredefinedFoodItem;
import com.example.nirvana.services.NutritionAnalysisService;
import com.example.nirvana.ui.adapters.RecommendationAdapter;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DietaryDashboardFragment extends Fragment implements RecommendationAdapter.OnRecommendationClickListener {
    private static final String TAG = "DietaryDashboardFragment";
    
    // UI elements
    private TextView tvCalories, tvProtein, tvCarbs, tvFat;
    private TextView tvCaloriesStatus, tvProteinStatus, tvCarbsStatus, tvFatStatus;
    private TextView tvWeeklySummary, tvErrorState;
    private RecyclerView rvRecommendations;
    private RecommendationAdapter recommendationAdapter;
    private CircularProgressIndicator progressIndicator;
    private View contentContainer;
    private LineChart weeklyChart;
    private HorizontalBarChart nutrientProgressChart;
    
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private ListenerRegistration mealsListener;
    private ListenerRegistration goalListener;
    
    // Service
    private NutritionAnalysisService nutritionAnalysisService;
    
    // Formatting
    private DecimalFormat decimalFormat = new DecimalFormat("#.#");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dietary_dashboard, container, false);
        
        // Initialize UI elements
        initializeViews(view);
        initializeFirebase();
        setupCharts();
        
        return view;
    }

    private void initializeViews(View view) {
        try {
            // Summary values
            tvCalories = view.findViewById(R.id.tvCalories);
            tvProtein = view.findViewById(R.id.tvProtein);
            tvCarbs = view.findViewById(R.id.tvCarbs);
            tvFat = view.findViewById(R.id.tvFat);
            
            // Status values
            tvCaloriesStatus = view.findViewById(R.id.tvCaloriesStatus);
            tvProteinStatus = view.findViewById(R.id.tvProteinStatus);
            tvCarbsStatus = view.findViewById(R.id.tvCarbsStatus);
            tvFatStatus = view.findViewById(R.id.tvFatStatus);
            
            // Weekly summary
            tvWeeklySummary = view.findViewById(R.id.tvWeeklySummary);
            
            // Charts
            weeklyChart = view.findViewById(R.id.weeklyChart);
            nutrientProgressChart = view.findViewById(R.id.nutrientProgressChart);
            
            // Progress and error states
            progressIndicator = view.findViewById(R.id.progressIndicator);
            tvErrorState = view.findViewById(R.id.tvErrorState);
            contentContainer = view.findViewById(R.id.contentContainer);
            
            // Recommendations
            rvRecommendations = view.findViewById(R.id.rvRecommendations);
            rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
            recommendationAdapter = new RecommendationAdapter();
            recommendationAdapter.setOnRecommendationClickListener(this);
            rvRecommendations.setAdapter(recommendationAdapter);
            
            // Show loading state initially
            showLoadingState();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            showErrorState("Error initializing dashboard");
        }
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            showErrorState("Please sign in to view your nutrition data");
        }
    }

    private void setupCharts() {
        try {
            // Setup Weekly Chart
            weeklyChart.getDescription().setEnabled(false);
            weeklyChart.setTouchEnabled(true);
            weeklyChart.setDragEnabled(true);
            weeklyChart.setScaleEnabled(true);
            weeklyChart.setPinchZoom(true);
            weeklyChart.setDrawGridBackground(false);
            
            XAxis xAxis = weeklyChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setLabelRotationAngle(-45f);
            
            YAxis leftAxis = weeklyChart.getAxisLeft();
            leftAxis.setDrawGridLines(true);
            leftAxis.setSpaceTop(35f);
            leftAxis.setAxisMinimum(0f);
            
            weeklyChart.getAxisRight().setEnabled(false);
            weeklyChart.getLegend().setEnabled(true);
            weeklyChart.getLegend().setTextSize(12f);
            
            // Setup Nutrient Progress Chart
            nutrientProgressChart.getDescription().setEnabled(false);
            nutrientProgressChart.setTouchEnabled(false);
            nutrientProgressChart.setDragEnabled(false);
            nutrientProgressChart.setScaleEnabled(false);
            nutrientProgressChart.setPinchZoom(false);
            nutrientProgressChart.setDrawGridBackground(false);
            
            XAxis xAxis2 = nutrientProgressChart.getXAxis();
            xAxis2.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis2.setDrawGridLines(false);
            xAxis2.setGranularity(1f);
            xAxis2.setLabelRotationAngle(-45f);
            
            YAxis leftAxis2 = nutrientProgressChart.getAxisLeft();
            leftAxis2.setDrawGridLines(true);
            leftAxis2.setSpaceTop(35f);
            leftAxis2.setAxisMinimum(0f);
            leftAxis2.setAxisMaximum(100f);
            
            nutrientProgressChart.getAxisRight().setEnabled(false);
            nutrientProgressChart.getLegend().setEnabled(true);
            nutrientProgressChart.getLegend().setTextSize(12f);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up charts: " + e.getMessage());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (userId != null) {
            setupRealTimeListeners();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mealsListener != null) {
            mealsListener.remove();
        }
        if (goalListener != null) {
            goalListener.remove();
        }
        if (nutritionAnalysisService != null) {
            nutritionAnalysisService.stopRealtimeUpdates();
        }
    }

    private void setupRealTimeListeners() {
        // Get today's date in the format used by the app
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        DocumentReference userRef = db.collection("users").document(userId);
        
        // First, check if nutrition goals exist and create them if they don't
        checkAndCreateNutritionGoals(userRef);
        
        // Listen for changes to all meal collections
        setupMealListener(userRef, today);
        
        // Listen for changes to the nutrition goals
        setupNutritionGoalListener(userRef);
    }
    
    private void setupMealListener(DocumentReference userRef, String today) {
        // Listen to all meal types
        List<String> mealTypes = List.of("breakfast", "lunch", "dinner", "snacks");
        
        for (String mealType : mealTypes) {
            userRef.collection("meals")
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
    
    private void setupNutritionGoalListener(DocumentReference userRef) {
        goalListener = userRef.collection("diet")
            .document("goals")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Listen failed for nutrition goals", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> goals = snapshot.getData();
                    if (goals != null) {
                        updateNutritionGoals(goals);
                    }
                }
            });
    }

    private void calculateNutrientTotals() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        DocumentReference userRef = db.collection("users").document(userId);
        
        // Track totals for each nutrient
        AtomicReference<Double> totalCalories = new AtomicReference<>((double) 0);
        AtomicReference<Double> totalProtein = new AtomicReference<>((double) 0);
        AtomicReference<Double> totalCarbs = new AtomicReference<>((double) 0);
        AtomicReference<Double> totalFat = new AtomicReference<>((double) 0);
        
        // Get all meal types
        List<String> mealTypes = List.of("breakfast", "lunch", "dinner", "snacks");
        AtomicInteger completedQueries = new AtomicInteger();
        
        for (String mealType : mealTypes) {
            userRef.collection("meals")
                .document(today)
                .collection(mealType)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Map<String, Object> foodData = doc.getData();
                        if (foodData != null) {
                            // Extract numeric values safely
                            if (foodData.containsKey("calories")) {
                                Object cal = foodData.get("calories");
                                if (cal instanceof Number) {
                                    totalCalories.updateAndGet(v -> new Double((double) (v + ((Number) cal).doubleValue())));
                                }
                            }
                            
                            if (foodData.containsKey("protein")) {
                                Object prot = foodData.get("protein");
                                if (prot instanceof Number) {
                                    totalProtein.updateAndGet(v -> new Double((double) (v + ((Number) prot).doubleValue())));
                                }
                            }
                            
                            if (foodData.containsKey("carbs")) {
                                Object carb = foodData.get("carbs");
                                if (carb instanceof Number) {
                                    totalCarbs.updateAndGet(v -> new Double((double) (v + ((Number) carb).doubleValue())));
                                }
                            }
                            
                            if (foodData.containsKey("fat")) {
                                Object fat = foodData.get("fat");
                                if (fat instanceof Number) {
                                    totalFat.updateAndGet(v -> new Double((double) (v + ((Number) fat).doubleValue())));
                                }
                            }
                        }
                    }
                    
                    completedQueries.getAndIncrement();
                    
                    // When all queries are complete, update the UI
                    if (completedQueries.get() == mealTypes.size()) {
                        updateUI(totalCalories.get(), totalProtein.get(), totalCarbs.get(), totalFat.get());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting meals for " + mealType, e);
                    showErrorState("Error loading nutrition data");
                });
        }
    }

    private void updateUI(double calories, double protein, double carbs, double fat) {
        if (getActivity() == null || !isAdded()) return;
        
        getActivity().runOnUiThread(() -> {
            try {
                // Update summary values with units
                tvCalories.setText(String.format("%.0f kcal", calories));
                tvProtein.setText(String.format("%.0fg", protein));
                tvCarbs.setText(String.format("%.0fg", carbs));
                tvFat.setText(String.format("%.0fg", fat));

                // Update weekly summary text
                updateWeeklySummary(calories, protein, carbs, fat);

                // Update charts
                updateWeeklyChart(calories, protein, carbs, fat);
                updateNutrientProgressChart(calories, protein, carbs, fat);

                // Show content and hide loading
                hideLoadingState();
                contentContainer.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.e(TAG, "Error updating UI: " + e.getMessage());
                showErrorState("Error updating nutrition data");
            }
        });
    }

    private void updateWeeklySummary(double calories, double protein, double carbs, double fat) {
        StringBuilder summary = new StringBuilder();
        summary.append("Today's nutrition overview:\n\n");
        
        // Add calorie summary
        summary.append(String.format("• Calories: %.0f kcal\n", calories));
        summary.append(String.format("• Protein: %.0fg\n", protein));
        summary.append(String.format("• Carbs: %.0fg\n", carbs));
        summary.append(String.format("• Fat: %.0fg", fat));
        
        tvWeeklySummary.setText(summary.toString());
    }

    private void updateWeeklyChart(double calories, double protein, double carbs, double fat) {
        try {
            List<Entry> calorieEntries = new ArrayList<>();
            List<Entry> proteinEntries = new ArrayList<>();
            List<Entry> carbEntries = new ArrayList<>();
            List<Entry> fatEntries = new ArrayList<>();
            
            // Create entries for today
            calorieEntries.add(new Entry((float) 0, (float) calories));
            proteinEntries.add(new Entry((float) 0, (float) protein));
            carbEntries.add(new Entry((float) 0, (float) carbs));
            fatEntries.add(new Entry((float) 0, (float) fat));
            
            // Create datasets with colors and styling
            LineDataSet calorieDataSet = new LineDataSet(calorieEntries, "Calories");
            calorieDataSet.setColor(Color.RED);
            calorieDataSet.setCircleColor(Color.RED);
            calorieDataSet.setLineWidth(2f);
            calorieDataSet.setCircleRadius(3f);
            calorieDataSet.setDrawCircleHole(false);
            calorieDataSet.setValueTextSize(9f);
            calorieDataSet.setDrawFilled(true);
            calorieDataSet.setFillColor(Color.argb(50, 255, 0, 0));
            
            LineDataSet proteinDataSet = new LineDataSet(proteinEntries, "Protein");
            proteinDataSet.setColor(Color.BLUE);
            proteinDataSet.setCircleColor(Color.BLUE);
            proteinDataSet.setLineWidth(2f);
            proteinDataSet.setCircleRadius(3f);
            proteinDataSet.setDrawCircleHole(false);
            proteinDataSet.setValueTextSize(9f);
            proteinDataSet.setDrawFilled(true);
            proteinDataSet.setFillColor(Color.argb(50, 0, 0, 255));
            
            LineDataSet carbDataSet = new LineDataSet(carbEntries, "Carbs");
            carbDataSet.setColor(Color.GREEN);
            carbDataSet.setCircleColor(Color.GREEN);
            carbDataSet.setLineWidth(2f);
            carbDataSet.setCircleRadius(3f);
            carbDataSet.setDrawCircleHole(false);
            carbDataSet.setValueTextSize(9f);
            carbDataSet.setDrawFilled(true);
            carbDataSet.setFillColor(Color.argb(50, 0, 255, 0));
            
            LineDataSet fatDataSet = new LineDataSet(fatEntries, "Fat");
            fatDataSet.setColor(Color.YELLOW);
            fatDataSet.setCircleColor(Color.YELLOW);
            fatDataSet.setLineWidth(2f);
            fatDataSet.setCircleRadius(3f);
            fatDataSet.setDrawCircleHole(false);
            fatDataSet.setValueTextSize(9f);
            fatDataSet.setDrawFilled(true);
            fatDataSet.setFillColor(Color.argb(50, 255, 255, 0));
            
            // Create LineData object with all datasets
            LineData lineData = new LineData(calorieDataSet, proteinDataSet, carbDataSet, fatDataSet);
            weeklyChart.setData(lineData);
            
            // Set X-axis labels for today
            String[] labels = new String[]{"Today"};
            weeklyChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            
            // Refresh chart
            weeklyChart.invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Error updating weekly chart: " + e.getMessage());
        }
    }

    private void updateNutrientProgressChart(double calories, double protein, double carbs, double fat) {
        try {
            List<BarEntry> entries = new ArrayList<>();
            
            // Calculate progress percentages
            float calorieProgress = (float) ((calories / calories) * 100);
            float proteinProgress = (float) ((protein / protein) * 100);
            float carbProgress = (float) ((carbs / carbs) * 100);
            float fatProgress = (float) ((fat / fat) * 100);
            
            // Add entries
            entries.add(new BarEntry(0, calorieProgress));
            entries.add(new BarEntry(1, proteinProgress));
            entries.add(new BarEntry(2, carbProgress));
            entries.add(new BarEntry(3, fatProgress));
            
            // Create dataset with colors
            BarDataSet dataSet = new BarDataSet(entries, "Progress (%)");
            dataSet.setColors(
                Color.rgb(255, 0, 0),    // Red for calories
                Color.rgb(0, 0, 255),    // Blue for protein
                Color.rgb(0, 255, 0),    // Green for carbs
                Color.rgb(255, 255, 0)   // Yellow for fat
            );
            dataSet.setValueTextSize(12f);
            
            // Create BarData object
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            
            // Set data to chart
            nutrientProgressChart.setData(barData);
            
            // Set X-axis labels
            String[] labels = new String[]{"Calories", "Protein", "Carbs", "Fat"};
            nutrientProgressChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            
            // Refresh chart
            nutrientProgressChart.invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Error updating nutrient progress chart: " + e.getMessage());
        }
    }

    private void updateNutritionStatus(NutritionAnalysis analysis) {
        try {
            // Update status indicators with percentages
            String calorieStatus = String.format("%s (%.0f%%)",
                    analysis.getCalorieStatus(),
                    (analysis.getAverageCalories() / analysis.getCalorieGoal()) * 100);
            
            String proteinStatus = String.format("%s (%.0f%%)",
                    analysis.getProteinStatus(),
                    (analysis.getAverageProtein() / analysis.getProteinGoal()) * 100);
            
            String carbsStatus = String.format("%s (%.0f%%)",
                    analysis.getCarbsStatus(),
                    (analysis.getAverageCarbs() / analysis.getCarbsGoal()) * 100);
            
            String fatStatus = String.format("%s (%.0f%%)",
                    analysis.getFatStatus(),
                    (analysis.getAverageFat() / analysis.getFatGoal()) * 100);
            
            tvCaloriesStatus.setText(calorieStatus);
            tvProteinStatus.setText(proteinStatus);
            tvCarbsStatus.setText(carbsStatus);
            tvFatStatus.setText(fatStatus);
            
            // Set status colors
            setStatusColor(tvCaloriesStatus, analysis.getCalorieStatus());
            setStatusColor(tvProteinStatus, analysis.getProteinStatus());
            setStatusColor(tvCarbsStatus, analysis.getCarbsStatus());
            setStatusColor(tvFatStatus, analysis.getFatStatus());
        } catch (Exception e) {
            Log.e(TAG, "Error updating nutrition status: " + e.getMessage());
            showErrorState("Error updating nutrition status");
        }
    }

    private void setStatusColor(TextView textView, String status) {
        try {
            if (status.equals("Optimal")) {
                textView.setTextColor(getResources().getColor(R.color.green_500));
            } else if (status.equals("Low")) {
                textView.setTextColor(getResources().getColor(R.color.orange_500));
            } else if (status.equals("High")) {
                textView.setTextColor(getResources().getColor(R.color.red_500));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting status color: " + e.getMessage());
        }
    }

    private void updateRecommendations(NutritionAnalysis analysis) {
        try {
            String[] recommendations = analysis.getRecommendations();
            if (recommendations != null && recommendations.length > 0) {
                List<PredefinedFoodItem> foodItems = new ArrayList<>();
                for (String recommendation : recommendations) {
                    PredefinedFoodItem foodItem = new PredefinedFoodItem();
                    foodItem.setName(recommendation);
                    foodItem.setCategory("Other");
                    foodItem.setServingSize(100);
                    foodItems.add(foodItem);
                }
                recommendationAdapter.updateRecommendations(foodItems);
                Log.d(TAG, "Loaded " + foodItems.size() + " recommendations");
            } else {
                Log.d(TAG, "No recommendations available");
                recommendationAdapter.updateRecommendations(new ArrayList<>());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating recommendations: " + e.getMessage());
            showErrorState("Error loading recommendations");
        }
    }

    private void showLoadingState() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
        if (contentContainer != null) {
            contentContainer.setVisibility(View.GONE);
        }
        if (tvErrorState != null) {
            tvErrorState.setVisibility(View.GONE);
        }
    }

    private void hideLoadingState() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
    }

    private void showErrorState(String message) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
        if (contentContainer != null) {
            contentContainer.setVisibility(View.GONE);
        }
        if (tvErrorState != null) {
            tvErrorState.setText(message);
            tvErrorState.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRecommendationClick(PredefinedFoodItem food) {
        if (food != null) {
            Toast.makeText(requireContext(), 
                "Selected: " + food.getName(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndCreateNutritionGoals(DocumentReference userRef) {
        userRef.collection("diet").document("goals").get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        // No goals exist, fetch user profile to calculate them
                        userRef.collection("profile").document("details").get()
                            .addOnSuccessListener(profileDoc -> {
                                if (profileDoc.exists()) {
                                    // Calculate goals from profile data
                                    Map<String, Object> goals = calculateNutritionGoals(profileDoc);
                                    
                                    // Save the calculated goals
                                    userRef.collection("diet").document("goals")
                                        .set(goals)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Nutrition goals saved");
                                            updateNutritionGoals(goals);
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Error saving nutrition goals", e));
                                } else {
                                    // No profile exists, set default goals
                                    setDefaultNutritionGoals(userRef);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error fetching user profile", e);
                                setDefaultNutritionGoals(userRef);
                            });
                    }
                } else {
                    Log.e(TAG, "Error checking nutrition goals", task.getException());
                }
            });
    }

    private Map<String, Object> calculateNutritionGoals(DocumentSnapshot profileDoc) {
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
        
        // Calculate macronutrient goals
        double proteinGoal = weight * 2.0; // 2g per kg of body weight
        double fatGoal = (tdee * 0.25) / 9; // 25% of calories from fat
        double carbsGoal = (tdee - (proteinGoal * 4 + fatGoal * 9)) / 4; // Remaining calories from carbs
        
        Map<String, Object> goals = new HashMap<>();
        goals.put("calories", Math.round(tdee));
        goals.put("protein", Math.round(proteinGoal));
        goals.put("carbs", Math.round(carbsGoal));
        goals.put("fat", Math.round(fatGoal));
        
        return goals;
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

    private void setDefaultNutritionGoals(DocumentReference userRef) {
        Map<String, Object> goals = new HashMap<>();
        goals.put("calories", 2000);
        goals.put("protein", 150);
        goals.put("carbs", 250);
        goals.put("fat", 70);
        
        userRef.collection("diet").document("goals")
            .set(goals)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Default nutrition goals saved");
                updateNutritionGoals(goals);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error saving default nutrition goals", e));
    }

    private void updateNutritionGoals(Map<String, Object> goals) {
        if (getActivity() == null || !isAdded()) return;
        
        getActivity().runOnUiThread(() -> {
            try {
                // Update progress calculations in charts
                if (goals.containsKey("calories")) {
                    double calorieGoal = ((Number) goals.get("calories")).doubleValue();
                    // Update calorie goal in UI
                }
                
                if (goals.containsKey("protein")) {
                    double proteinGoal = ((Number) goals.get("protein")).doubleValue();
                    // Update protein goal in UI
                }
                
                if (goals.containsKey("carbs")) {
                    double carbsGoal = ((Number) goals.get("carbs")).doubleValue();
                    // Update carbs goal in UI
                }
                
                if (goals.containsKey("fat")) {
                    double fatGoal = ((Number) goals.get("fat")).doubleValue();
                    // Update fat goal in UI
                }
                
                // Recalculate nutrient totals to update progress bars
                calculateNutrientTotals();
            } catch (Exception e) {
                Log.e(TAG, "Error updating nutrition goals in UI: " + e.getMessage());
            }
        });
    }
} 