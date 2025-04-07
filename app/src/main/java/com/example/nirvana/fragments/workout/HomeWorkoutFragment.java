package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.WorkoutCategory;
import com.example.nirvana.data.models.WorkoutCategoryResponse;
import com.example.nirvana.network.ApiClient;
import com.example.nirvana.network.WorkoutApiService;
import com.example.nirvana.ui.adapters.WorkoutCategoryAdapter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeWorkoutFragment extends Fragment {
    private static final String TAG = "HomeWorkoutFragment";
    private RecyclerView recyclerHomeWorkouts;
    private WorkoutCategoryAdapter categoryAdapter;
    private MaterialButton btnBeginnerWorkout, btnIntermediateWorkout;
    private WorkoutApiService workoutApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        workoutApiService = ApiClient.getWorkoutApiService();
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        fetchHomeWorkoutCategories();
    }

    private void initializeViews(View view) {
        recyclerHomeWorkouts = view.findViewById(R.id.recyclerHomeWorkouts);
        btnBeginnerWorkout = view.findViewById(R.id.btnBeginnerWorkout);
        btnIntermediateWorkout = view.findViewById(R.id.btnIntermediateWorkout);
    }

    private void setupRecyclerView() {
        recyclerHomeWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Initialize with empty list, will be populated from API
        categoryAdapter = new WorkoutCategoryAdapter(new ArrayList<>(), category -> {
            // Navigate to workout list with selected category
            Bundle args = new Bundle();
            args.putString("category", category.getName());
            Navigation.findNavController(requireView())
                .navigate(R.id.action_homeWorkoutFragment_to_workoutListFragment, args);
        });
        recyclerHomeWorkouts.setAdapter(categoryAdapter);
    }

    private void fetchHomeWorkoutCategories() {
        showLoading(true);
        
        // First try to fetch from API
        workoutApiService.getHomeWorkoutCategories().enqueue(new Callback<WorkoutCategoryResponse>() {
            @Override
            public void onResponse(Call<WorkoutCategoryResponse> call, Response<WorkoutCategoryResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getCategories() != null) {
                    // Update the adapter with categories from API
                    categoryAdapter.updateCategories(response.body().getCategories());
                    Log.d(TAG, "Successfully loaded " + response.body().getCategories().size() + " workout categories from API");
                } else {
                    // If API call fails, use fallback data
                    Log.w(TAG, "API call failed, using fallback data. Response code: " + response.code());
                    categoryAdapter.updateCategories(getFallbackHomeWorkoutCategories());
                }
            }

            @Override
            public void onFailure(Call<WorkoutCategoryResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "API call failed", t);
                // Use fallback data on network error
                categoryAdapter.updateCategories(getFallbackHomeWorkoutCategories());
                Toast.makeText(requireContext(), "Failed to load workout data. Using offline data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        // Implement loading indicator logic here
        // For example, show/hide a ProgressBar
        if (getView() != null) {
            View loadingView = getView().findViewById(R.id.progressBar);
            if (loadingView != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        }
    }

    private List<WorkoutCategory> getFallbackHomeWorkoutCategories() {
        List<WorkoutCategory> categories = new ArrayList<>();
        
        // Using image URLs that allow hotlinking instead of local resources
        categories.add(new WorkoutCategory(
            "Full Body", 
            "https://images.unsplash.com/photo-1599058917765-a780eda07a3e?w=600&auto=format", 
            "Complete body workout with no equipment"));
            
        categories.add(new WorkoutCategory(
            "Upper Body", 
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=600&auto=format", 
            "Focus on chest, shoulders, and arms"));
            
        categories.add(new WorkoutCategory(
            "Lower Body", 
            "https://images.unsplash.com/photo-1434608519344-49d77a699e1d?w=600&auto=format", 
            "Target legs and glutes"));
            
        categories.add(new WorkoutCategory(
            "Core", 
            "https://images.unsplash.com/photo-1616803689943-5601631c7fec?w=600&auto=format", 
            "Strengthen your core and abs"));
            
        categories.add(new WorkoutCategory(
            "Cardio", 
            "https://images.unsplash.com/photo-1538805060514-97d9cc17730c?w=600&auto=format", 
            "High-intensity cardio workouts"));
            
        categories.add(new WorkoutCategory(
            "Stretching", 
            "https://images.unsplash.com/photo-1552196563-55cd4e45efb3?w=600&auto=format", 
            "Improve flexibility and recovery"));
            
        return categories;
    }

    private void setupClickListeners() {
        btnBeginnerWorkout.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("category", "Quick Start");
            args.putString("experience", "Beginner");
            Navigation.findNavController(requireView())
                .navigate(R.id.action_homeWorkoutFragment_to_workoutListFragment, args);
        });

        btnIntermediateWorkout.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("category", "Quick Start");
            args.putString("experience", "Intermediate");
            Navigation.findNavController(requireView())
                .navigate(R.id.action_homeWorkoutFragment_to_workoutListFragment, args);
        });
    }
}
