package com.example.nirvana.fragments.workout;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.WorkoutCategory;
import com.example.nirvana.ui.adapters.WorkoutCategoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class WorkoutFragment extends Fragment {

    private CardView cardHomeWorkout, cardGymWorkout, cardCustomWorkout;
    private TextView txtWorkoutStreak, txtLastWorkout;
    private ImageView imgWorkoutStreak;
    private RecyclerView recyclerWorkoutCategories;
    private WorkoutCategoryAdapter categoryAdapter;
    private List<WorkoutCategory> workoutCategories;
    private Button btnGoToGymWorkout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        initializeViews(view);

        // Setup RecyclerView
        setupWorkoutCategories();

        // Ensure views are properly laid out before starting animations
        view.post(this::startAnimations);

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews(View view) {
        cardHomeWorkout = view.findViewById(R.id.cardHomeWorkout);
        cardGymWorkout = view.findViewById(R.id.cardGymWorkout);
        cardCustomWorkout = view.findViewById(R.id.cardCustomWorkout);
        txtWorkoutStreak = view.findViewById(R.id.txtWorkoutStreak);
        txtLastWorkout = view.findViewById(R.id.txtLastWorkout);
        imgWorkoutStreak = view.findViewById(R.id.imgWorkoutStreak);
        recyclerWorkoutCategories = view.findViewById(R.id.recyclerWorkoutCategories);
        btnGoToGymWorkout = view.findViewById(R.id.btn_go_to_gymWorkout);

        if (btnGoToGymWorkout != null) {
            btnGoToGymWorkout.setOnClickListener(v -> openFragment(new GymWorkoutFragment()));
        }
    }

    private void setupWorkoutCategories() {
        if (getContext() == null) return; // Prevent null pointer crashes

        workoutCategories = new ArrayList<>();
        workoutCategories.add(new WorkoutCategory("Strength", R.drawable.ic_gym_workout));
        workoutCategories.add(new WorkoutCategory("Cardio", R.drawable.ic_gym_workout));
        workoutCategories.add(new WorkoutCategory("Flexibility", R.drawable.ic_gym_workout));
        workoutCategories.add(new WorkoutCategory("HIIT", R.drawable.ic_gym_workout));

        categoryAdapter = new WorkoutCategoryAdapter(workoutCategories);
        recyclerWorkoutCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerWorkoutCategories.setAdapter(categoryAdapter);
    }

    private void startAnimations() {
        if (getView() == null || getContext() == null || imgWorkoutStreak == null) return;

        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
        cardHomeWorkout.startAnimation(fadeIn);
        cardGymWorkout.startAnimation(fadeIn);
        cardCustomWorkout.startAnimation(fadeIn);
        imgWorkoutStreak.startAnimation(fadeIn);

        ObjectAnimator bounce = ObjectAnimator.ofFloat(imgWorkoutStreak, "translationY", 0f, -20f, 0f);
        bounce.setDuration(800);
        bounce.setRepeatCount(ObjectAnimator.INFINITE);
        bounce.start();
    }

    private void setupClickListeners() {
        if (cardHomeWorkout != null) {
            cardHomeWorkout.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Navigating to Home Workout", Toast.LENGTH_SHORT).show()
            );
        }

        if (cardGymWorkout != null) {
            cardGymWorkout.setOnClickListener(v -> openFragment(new GymWorkoutFragment()));
        }

        if (cardCustomWorkout != null) {
            cardCustomWorkout.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Custom Workout Clicked", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void openFragment(Fragment fragment) {
        if (isAdded() && getActivity() != null) {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(requireContext(), "Error: Fragment not attached!", Toast.LENGTH_SHORT).show();
        }
    }
}
