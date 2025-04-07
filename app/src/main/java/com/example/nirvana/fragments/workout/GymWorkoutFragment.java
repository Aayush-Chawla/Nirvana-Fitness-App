package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
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
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GymWorkoutFragment extends Fragment {
    private static final String TAG = "GymWorkoutFragment";
    private RecyclerView recyclerGymWorkouts;
    private RadioGroup radioGroupExperience;
    private WorkoutCategoryAdapter categoryAdapter;
    private String selectedExperience = "Beginner";
    private WorkoutApiService workoutApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gym_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        workoutApiService = ApiClient.getWorkoutApiService();
        initializeViews(view);
        setupRecyclerView();
        setupExperienceSelection();
        fetchGymWorkoutCategories();
    }

    private void initializeViews(View view) {
        recyclerGymWorkouts = view.findViewById(R.id.recyclerGymWorkouts);
        radioGroupExperience = view.findViewById(R.id.radioGroupExperience);
    }

    private void setupRecyclerView() {
        recyclerGymWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Initialize with empty list, will be populated from API
        categoryAdapter = new WorkoutCategoryAdapter(new ArrayList<>(), category -> {
            // Get selected experience level
            String experience = "Beginner";
            int selectedId = radioGroupExperience.getCheckedRadioButtonId();
            if (selectedId == R.id.radioIntermediate) {
                experience = "Intermediate";
            } else if (selectedId == R.id.radioAdvanced) {
                experience = "Advanced";
            }

            // Navigate to workout list with selected category and experience
            Bundle args = new Bundle();
            args.putString("category", category.getName());
            args.putString("experience", experience);
            Navigation.findNavController(requireView())
                .navigate(R.id.action_gymWorkoutFragment_to_workoutListFragment, args);
        });
        recyclerGymWorkouts.setAdapter(categoryAdapter);
    }

    private void fetchGymWorkoutCategories() {
        showLoading(true);
        
        // First try to fetch from API
        workoutApiService.getGymWorkoutCategories().enqueue(new Callback<WorkoutCategoryResponse>() {
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
                    categoryAdapter.updateCategories(getFallbackGymWorkoutCategories());
                }
            }

            @Override
            public void onFailure(Call<WorkoutCategoryResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "API call failed", t);
                // Use fallback data on network error
                categoryAdapter.updateCategories(getFallbackGymWorkoutCategories());
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

    private List<WorkoutCategory> getFallbackGymWorkoutCategories() {
        List<WorkoutCategory> categories = new ArrayList<>();
        
        // Using image URLs that allow hotlinking instead of local resources
        categories.add(new WorkoutCategory(
            "Free Weights", 
            "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=600&auto=format", 
            "Dumbbells, barbells, and weight plates"));
            
        categories.add(new WorkoutCategory(
            "Machines", 
            "https://images.unsplash.com/photo-1593079831268-3381b0db4a77?w=600&auto=format", 
            "Weight machines and cable equipment"));
            
        categories.add(new WorkoutCategory(
            "Bodyweight", 
            "https://images.unsplash.com/photo-1597076545399-91a3ff0e71b3?w=600&auto=format", 
            "Using gym equipment for bodyweight exercises"));
            
        categories.add(new WorkoutCategory(
            "Cardio Equipment", 
            "https://images.unsplash.com/photo-1596357395217-80de13130e92?w=600&auto=format", 
            "Treadmill, bike, elliptical, and more"));
            
        categories.add(new WorkoutCategory(
            "Resistance Bands", 
            "https://images.unsplash.com/photo-1598632640487-6ea4a5e8bf9b?w=600&auto=format", 
            "Band-based strength training"));
            
        categories.add(new WorkoutCategory(
            "Olympic Lifts", 
            "https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?w=600&auto=format", 
            "Advanced compound movements"));
            
        return categories;
    }

    private void setupExperienceSelection() {
        radioGroupExperience.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioBeginner) {
                selectedExperience = "Beginner";
            } else if (checkedId == R.id.radioIntermediate) {
                selectedExperience = "Intermediate";
            } else if (checkedId == R.id.radioAdvanced) {
                selectedExperience = "Advanced";
            }
            // Refresh workout suggestions based on experience level
            updateWorkoutSuggestions();
        });
    }

    private void updateWorkoutSuggestions() {
        // TODO: Update workout suggestions based on selected experience level
        Toast.makeText(requireContext(), 
            "Updating workouts for " + selectedExperience + " level", 
            Toast.LENGTH_SHORT).show();
    }
}
