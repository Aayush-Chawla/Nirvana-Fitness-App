package com.example.nirvana.fragments;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DietaryDashboardFragment extends Fragment {
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
        recommendationAdapter = new RecommendationAdapter();
        rvRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecommendations.setAdapter(recommendationAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            nutritionAnalysisService = new NutritionAnalysisService(userId);
            loadNutritionData();
        } else {
            Log.e(TAG, "No user is signed in");
            Toast.makeText(getContext(), "Please sign in to view your nutrition data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (nutritionAnalysisService != null) {
            nutritionAnalysisService.stopRealtimeUpdates();
        }
    }

    private void loadNutritionData() {
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
        // Update average values
        tvCalories.setText(decimalFormat.format(analysis.getAverageCalories()));
        tvProtein.setText(decimalFormat.format(analysis.getAverageProtein()) + "g");
        tvCarbs.setText(decimalFormat.format(analysis.getAverageCarbs()) + "g");
        tvFat.setText(decimalFormat.format(analysis.getAverageFat()) + "g");
    }

    private void updateNutritionStatus(NutritionAnalysis analysis) {
        // Update status indicators
        tvCaloriesStatus.setText(analysis.getCalorieStatus());
        tvProteinStatus.setText(analysis.getProteinStatus());
        tvCarbsStatus.setText(analysis.getCarbsStatus());
        tvFatStatus.setText(analysis.getFatStatus());
        
        // Set status colors
        setStatusColor(tvCaloriesStatus, analysis.getCalorieStatus());
        setStatusColor(tvProteinStatus, analysis.getProteinStatus());
        setStatusColor(tvCarbsStatus, analysis.getCarbsStatus());
        setStatusColor(tvFatStatus, analysis.getFatStatus());
    }

    private void setStatusColor(TextView textView, String status) {
        if (status.equals("Optimal")) {
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
            // Convert String recommendations to PredefinedFoodItem objects
            List<PredefinedFoodItem> foodItems = new ArrayList<>();
            for (String recommendation : recommendations) {
                PredefinedFoodItem foodItem = new PredefinedFoodItem();
                foodItem.setName(recommendation);
                foodItem.setCategory("Other"); // Default category
                foodItem.setServingSize(100); // Default serving size in grams
                foodItems.add(foodItem);
            }
            recommendationAdapter.updateRecommendations(foodItems);
            Log.d(TAG, "Loaded " + recommendations.length + " recommendations");
        } else {
            Log.d(TAG, "No recommendations available");
            recommendationAdapter.updateRecommendations(new ArrayList<>());
        }
    }
} 