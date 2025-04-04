package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.os.Handler;
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
import com.example.nirvana.data.models.Exercise;
import com.example.nirvana.ui.adapters.ExerciseAdapter;
import java.util.ArrayList;
import java.util.List;

public class WorkoutDetailFragment extends Fragment {

    private TextView txtWorkoutTitle, txtWorkoutInstructions, txtTimer;
    private RecyclerView recyclerExerciseList;
    private ProgressBar progressWorkout;
    private Button btnStartWorkout, btnPreviousWorkout, btnNextWorkout;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList;
    private Handler timerHandler = new Handler();
    private int timeElapsed = 0;
    private boolean isWorkoutRunning = false;
    private String category;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category", "Workout");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        updateUI();
    }

    private void initializeViews(View view) {
        txtWorkoutTitle = view.findViewById(R.id.txtWorkoutTitle);
        txtWorkoutInstructions = view.findViewById(R.id.txtWorkoutInstructions);
        txtTimer = view.findViewById(R.id.txtTimer);
        recyclerExerciseList = view.findViewById(R.id.recyclerExerciseList);
        progressWorkout = view.findViewById(R.id.progressWorkout);
        btnStartWorkout = view.findViewById(R.id.btnStartWorkout);
        btnPreviousWorkout = view.findViewById(R.id.btnPreviousWorkout);
        btnNextWorkout = view.findViewById(R.id.btnNextWorkout);
    }

    private void setupRecyclerView() {
        exerciseList = getExercisesForCategory();
        exerciseAdapter = new ExerciseAdapter(exerciseList);
        recyclerExerciseList.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerExerciseList.setAdapter(exerciseAdapter);
    }

    private List<Exercise> getExercisesForCategory() {
        List<Exercise> exercises = new ArrayList<>();
        switch (category) {
            // Home workout exercises
            case "Upper Body":
                exercises.add(new Exercise("Push-ups", R.drawable.ic_exercise));
                exercises.add(new Exercise("Pull-ups", R.drawable.ic_exercise));
                exercises.add(new Exercise("Dips", R.drawable.ic_exercise));
                exercises.add(new Exercise("Diamond Push-ups", R.drawable.ic_exercise));
                break;
            case "Lower Body":
                exercises.add(new Exercise("Squats", R.drawable.ic_exercise));
                exercises.add(new Exercise("Lunges", R.drawable.ic_exercise));
                exercises.add(new Exercise("Calf Raises", R.drawable.ic_exercise));
                exercises.add(new Exercise("Jump Squats", R.drawable.ic_exercise));
                break;
            case "Abs":
                exercises.add(new Exercise("Crunches", R.drawable.ic_exercise));
                exercises.add(new Exercise("Plank", R.drawable.ic_exercise));
                exercises.add(new Exercise("Russian Twists", R.drawable.ic_exercise));
                exercises.add(new Exercise("Leg Raises", R.drawable.ic_exercise));
                break;
            case "Legs":
                exercises.add(new Exercise("Squats", R.drawable.ic_exercise));
                exercises.add(new Exercise("Lunges", R.drawable.ic_exercise));
                exercises.add(new Exercise("Jump Squats", R.drawable.ic_exercise));
                exercises.add(new Exercise("Wall Sit", R.drawable.ic_exercise));
                break;
            case "Calisthenics":
                exercises.add(new Exercise("Burpees", R.drawable.ic_exercise));
                exercises.add(new Exercise("Mountain Climbers", R.drawable.ic_exercise));
                exercises.add(new Exercise("Jump Rope", R.drawable.ic_exercise));
                exercises.add(new Exercise("High Knees", R.drawable.ic_exercise));
                break;

            // Gym workout exercises
            case "Chest":
                exercises.add(new Exercise("Bench Press", R.drawable.ic_exercise));
                exercises.add(new Exercise("Incline Dumbbell Press", R.drawable.ic_exercise));
                exercises.add(new Exercise("Chest Flyes", R.drawable.ic_exercise));
                exercises.add(new Exercise("Cable Crossovers", R.drawable.ic_exercise));
                exercises.add(new Exercise("Decline Bench Press", R.drawable.ic_exercise));
                break;
            case "Back":
                exercises.add(new Exercise("Lat Pulldowns", R.drawable.ic_exercise));
                exercises.add(new Exercise("Barbell Rows", R.drawable.ic_exercise));
                exercises.add(new Exercise("Seated Cable Rows", R.drawable.ic_exercise));
                exercises.add(new Exercise("T-Bar Rows", R.drawable.ic_exercise));
                exercises.add(new Exercise("Face Pulls", R.drawable.ic_exercise));
                break;
            case "Shoulders":
                exercises.add(new Exercise("Military Press", R.drawable.ic_exercise));
                exercises.add(new Exercise("Lateral Raises", R.drawable.ic_exercise));
                exercises.add(new Exercise("Front Raises", R.drawable.ic_exercise));
                exercises.add(new Exercise("Reverse Flyes", R.drawable.ic_exercise));
                exercises.add(new Exercise("Shrugs", R.drawable.ic_exercise));
                break;
            case "Arms":
                exercises.add(new Exercise("Bicep Curls", R.drawable.ic_exercise));
                exercises.add(new Exercise("Tricep Pushdowns", R.drawable.ic_exercise));
                exercises.add(new Exercise("Hammer Curls", R.drawable.ic_exercise));
                exercises.add(new Exercise("Skull Crushers", R.drawable.ic_exercise));
                exercises.add(new Exercise("Preacher Curls", R.drawable.ic_exercise));
                break;
            case "Core":
                exercises.add(new Exercise("Cable Crunches", R.drawable.ic_exercise));
                exercises.add(new Exercise("Hanging Leg Raises", R.drawable.ic_exercise));
                exercises.add(new Exercise("Ab Wheel Rollouts", R.drawable.ic_exercise));
                exercises.add(new Exercise("Decline Sit-ups", R.drawable.ic_exercise));
                exercises.add(new Exercise("Wood Choppers", R.drawable.ic_exercise));
                break;
            default:
                exercises.add(new Exercise("Jumping Jacks", R.drawable.ic_exercise));
                exercises.add(new Exercise("Push-ups", R.drawable.ic_exercise));
                exercises.add(new Exercise("Squats", R.drawable.ic_exercise));
                exercises.add(new Exercise("Lunges", R.drawable.ic_exercise));
        }
        return exercises;
    }

    private void setupClickListeners() {
        btnStartWorkout.setOnClickListener(v -> toggleWorkout());

        btnPreviousWorkout.setOnClickListener(v -> {
            // TODO: Navigate to previous workout in the same category
        });

        btnNextWorkout.setOnClickListener(v -> {
            // TODO: Navigate to next workout in the same category
        });
    }

    private void updateUI() {
        txtWorkoutTitle.setText(category + " Workout");
        txtWorkoutInstructions.setText("Follow the exercises below to complete your " + category.toLowerCase() + " workout.");
    }

    private void toggleWorkout() {
        if (isWorkoutRunning) {
            isWorkoutRunning = false;
            btnStartWorkout.setText("Start Workout");
            timerHandler.removeCallbacks(timerRunnable);
        } else {
            isWorkoutRunning = true;
            btnStartWorkout.setText("Stop Workout");
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isWorkoutRunning) {
                timeElapsed++;
                txtTimer.setText(formatTime(timeElapsed));
                progressWorkout.setProgress(timeElapsed % 100);
                timerHandler.postDelayed(this, 1000);
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
        timerHandler.removeCallbacks(timerRunnable);
    }
} 