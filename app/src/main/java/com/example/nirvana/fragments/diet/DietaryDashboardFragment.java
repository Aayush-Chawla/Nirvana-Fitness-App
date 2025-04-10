package com.example.nirvana.fragments.diet;

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
import com.example.nirvana.utils.FirestoreHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DietaryDashboardFragment extends Fragment implements RecommendationAdapter.OnRecommendationClickListener {
    private static final String TAG = "DietaryDashboardFragment";
    
    // UI elements
    private TextView tvCalories, tvProtein, tvCarbs, tvFat;
    private TextView tvCaloriesStatus, tvProteinStatus, tvCarbsStatus, tvFatStatus;
    private RecyclerView rvRecommendations;
    private RecommendationAdapter recommendationAdapter;
    
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
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            nutritionAnalysisService = new NutritionAnalysisService(userId);
            startRealtimeUpdates();
            
            // Also directly fetch today's food logs to ensure we have the latest data
            fetchTodaysFoodLogs();
        } else {
            Log.e(TAG, "No user is signed in");
            Toast.makeText(getContext(), "Please sign in to view your nutrition data", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews(View view) {
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
        
        // Recommendations
        rvRecommendations = view.findViewById(R.id.rvRecommendations);
        rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recommendationAdapter = new RecommendationAdapter(); // Use no-arg constructor
        recommendationAdapter.setOnRecommendationClickListener(this); // Set the listener explicitly
        rvRecommendations.setAdapter(recommendationAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (nutritionAnalysisService != null) {
            nutritionAnalysisService.stopRealtimeUpdates();
        }
    }

    private void startRealtimeUpdates() {
        nutritionAnalysisService.startRealtimeUpdates(new NutritionAnalysisService.NutritionAnalysisCallback() {
            @Override
            public void onAnalysisComplete(NutritionAnalysis analysis) {
                if (getActivity() == null || !isAdded()) return;
                
                getActivity().runOnUiThread(() -> {
                    updateNutritionSummary(analysis);
                    updateNutritionStatus(analysis);
                    updateRecommendations(analysis);
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null || !isAdded()) return;
                
                getActivity().runOnUiThread(() -> {
                    Log.e(TAG, "Error loading nutrition data: " + error);
                    Toast.makeText(getContext(), "Error loading nutrition data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateNutritionSummary(NutritionAnalysis analysis) {
        tvCalories.setText(decimalFormat.format(analysis.getAverageCalories()));
        tvProtein.setText(decimalFormat.format(analysis.getAverageProtein()) + "g");
        tvCarbs.setText(decimalFormat.format(analysis.getAverageCarbs()) + "g");
        tvFat.setText(decimalFormat.format(analysis.getAverageFat()) + "g");
    }

    private void updateNutritionStatus(NutritionAnalysis analysis) {
        tvCaloriesStatus.setText(analysis.getCalorieStatus());
        tvProteinStatus.setText(analysis.getProteinStatus());
        tvCarbsStatus.setText(analysis.getCarbsStatus());
        tvFatStatus.setText(analysis.getFatStatus());
        
        setStatusColor(tvCaloriesStatus, analysis.getCalorieStatus());
        setStatusColor(tvProteinStatus, analysis.getProteinStatus());
        setStatusColor(tvCarbsStatus, analysis.getCarbsStatus());
        setStatusColor(tvFatStatus, analysis.getFatStatus());
    }

    private void setStatusColor(TextView textView, String status) {
        if (status.equals("Optimal") || status.equals("Tracking")) {
            textView.setTextColor(getResources().getColor(R.color.green_500));
        } else if (status.equals("Low")) {
            textView.setTextColor(getResources().getColor(R.color.orange_500));
        } else if (status.equals("High")) {
            textView.setTextColor(getResources().getColor(R.color.red_500));
        }
    }

    private void updateRecommendations(NutritionAnalysis analysis) {
        String[] recommendations = analysis.getRecommendations();
        if (recommendations != null && recommendations.length > 0) {
            List<PredefinedFoodItem> foodItems = new ArrayList<>();
            for (String recommendation : recommendations) {
                try {
                    // Create a properly initialized PredefinedFoodItem from the recommendation string
                    PredefinedFoodItem foodItem = new PredefinedFoodItem();
                    foodItem.setId("rec_" + foodItems.size()); // Generate a dummy ID
                    foodItem.setName(recommendation); // Use the recommendation text as the name
                    foodItem.setCategory("Recommendation"); // Set a category to ensure proper icon display
                    foodItem.setServingSize(100); // Default serving size in grams
                    foodItem.setServingUnit("g"); // Set serving unit
                    
                    // Set nutritional values to 0 as this is just a recommendation text
                    PredefinedFoodItem.NutritionPer100g nutrition = new PredefinedFoodItem.NutritionPer100g();
                    nutrition.setCalories(0);
                    nutrition.setProtein(0);
                    nutrition.setCarbs(0);
                    nutrition.setFat(0);
                    foodItem.setPer100g(nutrition);
                    
                    foodItems.add(foodItem);
                    Log.d(TAG, "Added recommendation: " + recommendation);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating recommendation item", e);
                }
            }
            
            if (!foodItems.isEmpty()) {
                recommendationAdapter.updateRecommendations(foodItems);
                Log.d(TAG, "Loaded " + foodItems.size() + " recommendations");
            } else {
                Log.d(TAG, "No valid recommendations to display");
                recommendationAdapter.updateRecommendations(new ArrayList<>());
            }
        } else {
            Log.d(TAG, "No recommendations available");
            recommendationAdapter.updateRecommendations(new ArrayList<>());
        }
    }

    @Override
    public void onRecommendationClick(PredefinedFoodItem food) {
        // Handle the food item click
        if (food != null) {
            Toast.makeText(requireContext(), 
                "Selected: " + food.getName(), 
                Toast.LENGTH_SHORT).show();
            // TODO: Implement your logic for handling the selected food item
        }
    }

    /**
     * Fetch today's food logs directly from Firestore
     */
    private void fetchTodaysFoodLogs() {
        FirestoreHelper.fetchTodayFoodLogs(new FirestoreHelper.OnNutritionDataFetchedListener() {
            @Override
            public void onDataFetched(double calories, double protein, double carbs, double fat) {
                if (getActivity() == null || !isAdded()) return;
                
                Log.d(TAG, "Direct fetch - Today's nutrients: calories=" + calories + 
                      ", protein=" + protein + ", carbs=" + carbs + ", fat=" + fat);
                
                // If we have data, update the UI immediately
                if (calories > 0 || protein > 0 || carbs > 0 || fat > 0) {
                    getActivity().runOnUiThread(() -> {
                        updateTodayNutritionUI(calories, protein, carbs, fat);
                    });
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error fetching today's food logs: " + errorMessage);
            }
        });
    }
    
    /**
     * Update UI with today's nutrition data
     */
    private void updateTodayNutritionUI(double calories, double protein, double carbs, double fat) {
        // Only update if we aren't already showing non-zero values (to avoid overwriting weekly data)
        if (tvCalories.getText().toString().equals("0") || tvCalories.getText().toString().equals("0.0")) {
            tvCalories.setText(decimalFormat.format(calories));
            tvProtein.setText(decimalFormat.format(protein) + "g");
            tvCarbs.setText(decimalFormat.format(carbs) + "g");
            tvFat.setText(decimalFormat.format(fat) + "g");
            
            Log.d(TAG, "Updated UI with today's nutrition data");
            
            // Generate status based on today's values
            // We'll use simplified status here - just Low if zero
            String calorieStatus = calories > 0 ? "Tracking" : "Low";
            String proteinStatus = protein > 0 ? "Tracking" : "Low";
            String carbsStatus = carbs > 0 ? "Tracking" : "Low";
            String fatStatus = fat > 0 ? "Tracking" : "Low";
            
            tvCaloriesStatus.setText(calorieStatus);
            tvProteinStatus.setText(proteinStatus);
            tvCarbsStatus.setText(carbsStatus);
            tvFatStatus.setText(fatStatus);
            
            // Set colors
            setStatusColor(tvCaloriesStatus, calorieStatus);
            setStatusColor(tvProteinStatus, proteinStatus);
            setStatusColor(tvCarbsStatus, carbsStatus);
            setStatusColor(tvFatStatus, fatStatus);
        }
    }
} 