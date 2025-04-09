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
import com.example.nirvana.services.RecommendationService;
import com.example.nirvana.ui.adapters.RecommendationsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsFragment extends Fragment implements RecommendationsAdapter.OnRecommendationClickListener {
    private static final String TAG = "RecommendationsFragment";
    private RecyclerView rvRecommendations;
    private TextView tvNoRecommendations;
    private RecommendationsAdapter adapter;
    private NutritionAnalysisService nutritionAnalysisService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);
        
        // Initialize views
        rvRecommendations = view.findViewById(R.id.rvRecommendations);
        tvNoRecommendations = view.findViewById(R.id.tvNoRecommendations);
        
        // Setup RecyclerView
        adapter = new RecommendationsAdapter(this);
        rvRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecommendations.setAdapter(adapter);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize nutrition analysis service with current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            nutritionAnalysisService = new NutritionAnalysisService(userId);
            loadRecommendationsFromFirebase();
        } else {
            Log.e(TAG, "No user is signed in");
            tvNoRecommendations.setText("Please sign in to view recommendations");
            tvNoRecommendations.setVisibility(View.VISIBLE);
            rvRecommendations.setVisibility(View.GONE);
        }
    }

    private void loadRecommendationsFromFirebase() {
        // Show loading
        tvNoRecommendations.setText("Loading recommendations...");
        tvNoRecommendations.setVisibility(View.VISIBLE);
        rvRecommendations.setVisibility(View.GONE);
        
        nutritionAnalysisService.startRealtimeUpdates(new NutritionAnalysisService.NutritionAnalysisCallback() {
            @Override
            public void onAnalysisComplete(NutritionAnalysis analysis) {
                if (getActivity() == null || !isAdded()) return;
                
                getActivity().runOnUiThread(() -> {
                    try {
                        // Get recommendations based on analysis
                        // Using dummy values if methods are not available
                        List<String> recentFoodItems = new ArrayList<>(); 
                        String currentMealType = "Lunch";
                        double remainingCalories = 500;
                        
                        List<PredefinedFoodItem> recommendations = RecommendationService.getRecommendations(
                            recentFoodItems, 
                            currentMealType,
                            remainingCalories,
                            getPredefinedFoodItems()
                        );
                        
                        if (!recommendations.isEmpty()) {
                            adapter.updateRecommendations(recommendations);
                            
                            // Show the recommendations
                            tvNoRecommendations.setVisibility(View.GONE);
                            rvRecommendations.setVisibility(View.VISIBLE);
                            
                            Log.d(TAG, "Loaded " + recommendations.size() + " recommendations");
                        } else {
                            // No recommendations available
                            tvNoRecommendations.setText("No recommendations available");
                            tvNoRecommendations.setVisibility(View.VISIBLE);
                            rvRecommendations.setVisibility(View.GONE);
                            
                            Log.d(TAG, "No recommendations available");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing recommendations: " + e.getMessage());
                        tvNoRecommendations.setText("Error loading recommendations");
                        tvNoRecommendations.setVisibility(View.VISIBLE);
                        rvRecommendations.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null || !isAdded()) return;
                
                getActivity().runOnUiThread(() -> {
                    Log.e(TAG, "Error loading nutrition data: " + error);
                    tvNoRecommendations.setText("Error: " + error);
                    tvNoRecommendations.setVisibility(View.VISIBLE);
                    rvRecommendations.setVisibility(View.GONE);
                });
            }
        });
    }

    private List<PredefinedFoodItem> getPredefinedFoodItems() {
        // TODO: Load this from Firebase or local database
        // For now, return a sample list
        List<PredefinedFoodItem> items = new ArrayList<>();
        items.add(new PredefinedFoodItem("Oatmeal", 150, 27, 6, 3, 40, "g"));
        items.add(new PredefinedFoodItem("Greek Yogurt", 130, 6, 22, 0, 170, "g"));
        items.add(new PredefinedFoodItem("Banana", 105, 27, 1, 0, 118, "g"));
        items.add(new PredefinedFoodItem("Almonds", 162, 6, 6, 14, 28, "g"));
        return items;
    }

    @Override
    public void onRecommendationClick(PredefinedFoodItem food) {
        // TODO: Handle food selection
        Toast.makeText(getContext(), "Selected: " + food.getName(), Toast.LENGTH_SHORT).show();
    }
} 