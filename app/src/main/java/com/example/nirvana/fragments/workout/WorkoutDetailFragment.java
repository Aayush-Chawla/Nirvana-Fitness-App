package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.models.Exercise;
import com.example.nirvana.adapters.ExerciseAdapter;
import java.util.ArrayList;
import java.util.List;

public class WorkoutDetailFragment extends Fragment implements ExerciseAdapter.OnExerciseClickListener {

    private TextView txtWorkoutTitle, txtWorkoutInstructions, txtTimer;
    private RecyclerView recyclerExerciseList;
    private ProgressBar progressWorkout;
    private Button btnStartWorkout, btnPreviousWorkout, btnNextWorkout;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList;
    private Handler timerHandler;
    private int timeElapsed = 0;
    private boolean isWorkoutRunning = false;
    private String category;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category", "Default");
            Log.d("WorkoutDetail", "Category from arguments: " + category);
        } else {
            category = "Default";
            Log.d("WorkoutDetail", "No arguments, using default category");
        }
        timerHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("WorkoutDetail", "onCreateView called");
        return inflater.inflate(R.layout.fragment_workout_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("WorkoutDetail", "onViewCreated called");
        
        // Initialize views first
        initializeViews(view);
        
        // Create adapter and get exercises
        exerciseAdapter = new ExerciseAdapter(this);
        exerciseList = getExercisesForCategory();
        
        // Set up RecyclerView
        setupRecyclerView();
        
        // Set up other UI elements
        setupClickListeners();
        updateUI();
    }

    private void initializeViews(View view) {
        try {
            Log.d("WorkoutDetail", "Initializing views");
            txtWorkoutTitle = view.findViewById(R.id.txtWorkoutTitle);
            txtWorkoutInstructions = view.findViewById(R.id.txtWorkoutInstructions);
            txtTimer = view.findViewById(R.id.txtTimer);
            recyclerExerciseList = view.findViewById(R.id.recyclerExerciseList);
            progressWorkout = view.findViewById(R.id.progressWorkout);
            btnStartWorkout = view.findViewById(R.id.btnStartWorkout);
            btnPreviousWorkout = view.findViewById(R.id.btnPreviousWorkout);
            btnNextWorkout = view.findViewById(R.id.btnNextWorkout);
            Log.d("WorkoutDetail", "Views initialized successfully");
        } catch (Exception e) {
            Log.e("WorkoutDetail", "Error initializing views", e);
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        try {
            Log.d("WorkoutDetail", "Setting up RecyclerView");
            
            // Get exercises for the category
            exerciseList = getExercisesForCategory();
            
            // Initialize adapter
            exerciseAdapter = new ExerciseAdapter(this);
            recyclerExerciseList.setAdapter(exerciseAdapter);
            
            // Submit list if available
            if (exerciseList != null && !exerciseList.isEmpty()) {
                Log.d("WorkoutDetail", "Submitting exercises list of size: " + exerciseList.size());
                exerciseAdapter.setExercises(exerciseList);
            } else {
                Log.w("WorkoutDetail", "Exercise list is null or empty!");
            }
            
            Log.d("WorkoutDetail", "RecyclerView setup complete");
        } catch (Exception e) {
            Log.e("WorkoutDetail", "Error setting up RecyclerView", e);
            e.printStackTrace();
        }
    }

    private List<Exercise> getExercisesForCategory() {
        List<Exercise> exercises = new ArrayList<>();
        
        try {
            Log.d("WorkoutDetail", "Loading exercises for category: " + category);
            
            if (category == null || category.isEmpty()) {
                category = "Default";
                Log.d("WorkoutDetail", "Using default category");
            }

            // Create some test exercises to verify the adapter is working
            exercises.add(new Exercise(
                "test1",
                "Test Exercise 1",
                "This is a test exercise description",
                category,
                "Beginner",
                10,
                null,
                null
            ));
            
            exercises.add(new Exercise(
                "test2",
                "Test Exercise 2",
                "Another test exercise description",
                category,
                "Intermediate",
                15,
                null,
                null
            ));
            
            // Add the regular exercises based on category
            switch (category) {
                case "Upper Body":
                    Log.d("WorkoutDetail", "Adding Upper Body exercises");
                    exercises.add(new Exercise(
                        "ub1", "Push-ups", 
                        "A fundamental bodyweight exercise that works the chest, shoulders, and triceps.",
                        "Upper Body", "Beginner", 10,
                        "https://tse1.mm.bing.net/th?id=OIP._hkOZJF7_1c-hP9tjPxlzwHaFY&pid=Api&P=0&h=180", null
                    ));
                    // ... rest of upper body exercises
                    break;

                case "Lower Body":
                    Log.d("WorkoutDetail", "Adding Lower Body exercises");
                    exercises.add(new Exercise(
                        "lb1", "Squats", 
                        "A fundamental lower body exercise targeting quadriceps, hamstrings, and glutes.",
                        "Lower Body", "Beginner", 15,
                        null, null
                    ));
                    // ... rest of lower body exercises
                    break;

                default:
                    Log.d("WorkoutDetail", "Adding default exercises");
                    exercises.add(new Exercise(
                        "def1", "Jumping Jacks", 
                        "A full-body cardio exercise that raises heart rate and improves coordination.",
                        "Full Body", "Beginner", 10,
                        null, null
                    ));
                    // ... rest of default exercises
                    break;
            }
            
            Log.d("WorkoutDetail", "Created " + exercises.size() + " exercises");
        } catch (Exception e) {
            Log.e("WorkoutDetail", "Error creating exercises", e);
            e.printStackTrace();
        }
        
        return exercises;
    }

    private void setupClickListeners() {
        try {
            btnStartWorkout.setOnClickListener(v -> toggleWorkout());
            btnPreviousWorkout.setOnClickListener(v -> {
                // TODO: Navigate to previous workout in the same category
            });
            btnNextWorkout.setOnClickListener(v -> {
                // TODO: Navigate to next workout in the same category
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        try {
            Log.d("WorkoutDetail", "Updating UI with category: " + category);
            if (txtWorkoutTitle != null && txtWorkoutInstructions != null) {
                String title = (category != null ? category : "Default") + " Workout";
                txtWorkoutTitle.setText(title);
                txtWorkoutInstructions.setText("Follow the exercises below to complete your " + 
                    (category != null ? category.toLowerCase() : "default") + " workout.");
            }
        } catch (Exception e) {
            Log.e("WorkoutDetail", "Error updating UI", e);
            e.printStackTrace();
        }
    }

    private void toggleWorkout() {
        try {
            if (isWorkoutRunning) {
                isWorkoutRunning = false;
                btnStartWorkout.setText("Start Workout");
                timerHandler.removeCallbacks(timerRunnable);
            } else {
                isWorkoutRunning = true;
                btnStartWorkout.setText("Stop Workout");
                timerHandler.postDelayed(timerRunnable, 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onExerciseClick(Exercise exercise) {
        // Handle exercise click
        // TODO: Show exercise details or start specific exercise
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (isWorkoutRunning && isAdded()) {
                    timeElapsed++;
                    if (txtTimer != null) {
                        txtTimer.setText(formatTime(timeElapsed));
                    }
                    if (progressWorkout != null) {
                        progressWorkout.setProgress(timeElapsed % 100);
                    }
                    timerHandler.postDelayed(this, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
} 