package com.example.nirvana.fragments.workout;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.WorkoutCategory;
import com.example.nirvana.ui.adapters.WorkoutCategoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class WorkoutFragment extends Fragment {

    private static final String TAG = "WorkoutFragment";

    // Views
    private CardView cardHomeWorkout, cardGymWorkout, cardCustomWorkout, cardQuickWorkout;
    private TextView txtWorkoutStreak, txtLastWorkout;
    private ImageView imgWorkoutStreak;
    private RecyclerView recyclerWorkoutCategories;

    // Adapters
    private WorkoutCategoryAdapter categoryAdapter;
    private List<WorkoutCategory> workoutCategories;

    // Firebase
    private DatabaseReference userRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        setupFirebase();

        // Initialize UI components
        initializeViews(view);

        // Setup RecyclerView
        setupWorkoutCategories();

        // Load data
        loadWorkoutData();

        // Setup click listeners
        setupClickListeners();

        // Start animations
        startAnimations();
    }

    private void setupFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId);
        }
    }

    private void initializeViews(View view) {
        cardHomeWorkout = view.findViewById(R.id.cardHomeWorkout);
        cardGymWorkout = view.findViewById(R.id.cardGymWorkout);
        cardCustomWorkout = view.findViewById(R.id.cardCustomWorkout);
        cardQuickWorkout = view.findViewById(R.id.cardQuickWorkout);
        txtWorkoutStreak = view.findViewById(R.id.txtWorkoutStreak);
        txtLastWorkout = view.findViewById(R.id.txtLastWorkout);
        imgWorkoutStreak = view.findViewById(R.id.imgWorkoutStreak);
        recyclerWorkoutCategories = view.findViewById(R.id.recyclerWorkoutCategories);
    }

    private void setupWorkoutCategories() {
        workoutCategories = getWorkoutCategories();
        categoryAdapter = new WorkoutCategoryAdapter(workoutCategories, category -> {
            // Handle category click
            Toast.makeText(requireContext(), "Selected: " + category.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to category detail or start workout
        });
        recyclerWorkoutCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerWorkoutCategories.setAdapter(categoryAdapter);
    }

    private List<WorkoutCategory> getWorkoutCategories() {
        List<WorkoutCategory> categories = new ArrayList<>();
        categories.add(new WorkoutCategory("Strength", R.drawable.ic_gym_workout, "Build muscle and strength with weight training"));
        categories.add(new WorkoutCategory("Cardio", R.drawable.ic_gym_workout, "Improve cardiovascular fitness and endurance"));
        categories.add(new WorkoutCategory("Flexibility", R.drawable.ic_gym_workout, "Enhance flexibility and mobility"));
        categories.add(new WorkoutCategory("HIIT", R.drawable.ic_gym_workout, "High-intensity interval training for maximum results"));
        return categories;
    }

    private void loadWorkoutData() {
        if (userRef == null) return;

        userRef.child("workoutData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int streak = snapshot.child("streak").getValue(Integer.class);
                    String lastWorkout = snapshot.child("lastWorkout").getValue(String.class);
                    
                    txtWorkoutStreak.setText(streak + " Day Streak");
                    txtLastWorkout.setText("Last workout: " + lastWorkout);
                } else {
                    txtWorkoutStreak.setText("0 Day Streak");
                    txtLastWorkout.setText("No workouts yet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error loading workout data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        cardHomeWorkout.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_workoutFragment_to_homeWorkoutFragment);
        });

        cardGymWorkout.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_workoutFragment_to_gymWorkoutFragment);
        });

        cardCustomWorkout.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Custom Workout Coming Soon", Toast.LENGTH_SHORT).show();
        });

        cardQuickWorkout.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Quick Workout Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void startAnimations() {
        if (getView() == null || getContext() == null || imgWorkoutStreak == null) return;

        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
        cardHomeWorkout.startAnimation(fadeIn);
        cardGymWorkout.startAnimation(fadeIn);
        cardCustomWorkout.startAnimation(fadeIn);
        cardQuickWorkout.startAnimation(fadeIn);
        imgWorkoutStreak.startAnimation(fadeIn);

        ObjectAnimator bounce = ObjectAnimator.ofFloat(imgWorkoutStreak, "translationY", 0f, -20f, 0f);
        bounce.setDuration(800);
        bounce.setRepeatCount(ObjectAnimator.INFINITE);
        bounce.start();
    }
}
