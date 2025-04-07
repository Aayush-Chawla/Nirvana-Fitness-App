package com.example.nirvana.fragments.workout;

import android.os.Bundle;
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
import com.example.nirvana.ui.adapters.WorkoutCategoryAdapter;
import java.util.ArrayList;
import java.util.List;

public class GymWorkoutFragment extends Fragment {
    private RecyclerView recyclerGymWorkouts;
    private RadioGroup radioGroupExperience;
    private WorkoutCategoryAdapter categoryAdapter;
    private String selectedExperience = "Beginner";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gym_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRecyclerView();
        setupExperienceSelection();
    }

    private void initializeViews(View view) {
        recyclerGymWorkouts = view.findViewById(R.id.recyclerGymWorkouts);
        radioGroupExperience = view.findViewById(R.id.radioGroupExperience);
    }

    private void setupRecyclerView() {
        recyclerGymWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoryAdapter = new WorkoutCategoryAdapter(getGymWorkoutCategories(), category -> {
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

    private List<WorkoutCategory> getGymWorkoutCategories() {
        List<WorkoutCategory> categories = new ArrayList<>();
        categories.add(new WorkoutCategory("Free Weights", R.drawable.ic_exercise_placeholder, "Dumbbells, barbells, and weight plates"));
        categories.add(new WorkoutCategory("Machines", R.drawable.ic_exercise_placeholder, "Weight machines and cable equipment"));
        categories.add(new WorkoutCategory("Bodyweight", R.drawable.ic_exercise_placeholder, "Using gym equipment for bodyweight exercises"));
        categories.add(new WorkoutCategory("Cardio Equipment", R.drawable.ic_exercise_placeholder, "Treadmill, bike, elliptical, and more"));
        categories.add(new WorkoutCategory("Resistance Bands", R.drawable.ic_exercise_placeholder, "Band-based strength training"));
        categories.add(new WorkoutCategory("Olympic Lifts", R.drawable.ic_exercise_placeholder, "Advanced compound movements"));
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
