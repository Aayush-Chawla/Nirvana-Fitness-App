package com.example.nirvana.fragments.workout;

import android.os.Bundle;
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
import com.example.nirvana.ui.adapters.WorkoutCategoryAdapter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class HomeWorkoutFragment extends Fragment {
    private RecyclerView recyclerHomeWorkouts;
    private WorkoutCategoryAdapter categoryAdapter;
    private MaterialButton btnBeginnerWorkout, btnIntermediateWorkout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
    }

    private void initializeViews(View view) {
        recyclerHomeWorkouts = view.findViewById(R.id.recyclerHomeWorkouts);
        btnBeginnerWorkout = view.findViewById(R.id.btnBeginnerWorkout);
        btnIntermediateWorkout = view.findViewById(R.id.btnIntermediateWorkout);
    }

    private void setupRecyclerView() {
        recyclerHomeWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoryAdapter = new WorkoutCategoryAdapter(getHomeWorkoutCategories(), category -> {
            // Navigate to workout list with selected category
            Bundle args = new Bundle();
            args.putString("category", category.getName());
            Navigation.findNavController(requireView())
                .navigate(R.id.action_homeWorkoutFragment_to_workoutListFragment, args);
        });
        recyclerHomeWorkouts.setAdapter(categoryAdapter);
    }

    private List<WorkoutCategory> getHomeWorkoutCategories() {
        List<WorkoutCategory> categories = new ArrayList<>();
        categories.add(new WorkoutCategory("Full Body", R.drawable.ic_exercise_placeholder, "Complete body workout with no equipment"));
        categories.add(new WorkoutCategory("Upper Body", R.drawable.ic_exercise_placeholder, "Focus on chest, shoulders, and arms"));
        categories.add(new WorkoutCategory("Lower Body", R.drawable.ic_exercise_placeholder, "Target legs and glutes"));
        categories.add(new WorkoutCategory("Core", R.drawable.ic_exercise_placeholder, "Strengthen your core and abs"));
        categories.add(new WorkoutCategory("Cardio", R.drawable.ic_exercise_placeholder, "High-intensity cardio workouts"));
        categories.add(new WorkoutCategory("Stretching", R.drawable.ic_exercise_placeholder, "Improve flexibility and recovery"));
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
