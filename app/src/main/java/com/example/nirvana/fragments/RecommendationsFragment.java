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
import com.example.nirvana.services.NutritionAnalysisService;
import com.example.nirvana.ui.adapters.RecommendationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecommendationsFragment extends Fragment {
    private static final String TAG = "RecommendationsFragment";
    private RecyclerView rvRecommendations;
    private TextView tvNoRecommendations;
    private RecommendationAdapter adapter;
    private NutritionAnalysisService nutritionAnalysisService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);
        
        // Initialize views
        rvRecommendations = view.findViewById(R.id.rvRecommendations);
        tvNoRecommendations = view.findViewById(R.id.tvNoRecommendations);
        
        // Setup RecyclerView
        adapter = new RecommendationAdapter();
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
                    String[] recommendations = analysis.getRecommendations();
                    if (recommendations != null && recommendations.length > 0) {
                        List<String> recommendationList = Arrays.asList(recommendations);
                        adapter.setRecommendations(recommendationList);
                        
                        // Show the recommendations
                        tvNoRecommendations.setVisibility(View.GONE);
                        rvRecommendations.setVisibility(View.VISIBLE);
                        
                        Log.d(TAG, "Loaded " + recommendationList.size() + " recommendations");
                    } else {
                        // No recommendations available
                        tvNoRecommendations.setText("No recommendations available");
                        tvNoRecommendations.setVisibility(View.VISIBLE);
                        rvRecommendations.setVisibility(View.GONE);
                        
                        Log.d(TAG, "No recommendations available");
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null || !isAdded()) return;
                
                getActivity().runOnUiThread(() -> {
                    Log.e(TAG, "Error loading recommendations: " + error);
                    tvNoRecommendations.setText("Error loading recommendations");
                    tvNoRecommendations.setVisibility(View.VISIBLE);
                    rvRecommendations.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading recommendations", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (nutritionAnalysisService != null) {
            nutritionAnalysisService.stopRealtimeUpdates();
        }
    }
} 