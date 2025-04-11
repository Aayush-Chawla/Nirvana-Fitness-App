package com.example.nirvana.fragments.workout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.ExerciseAdapter;
import com.example.nirvana.models.Exercise;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ExerciseSelectionDialog extends BottomSheetDialogFragment implements ExerciseAdapter.OnExerciseClickListener {
    private static final String TAG = "ExerciseSelectionDialog";
    private OnExerciseSelectedListener listener;
    private ExerciseAdapter adapter;
    private View rootView;
    private CircularProgressIndicator progressIndicator;
    private TextView emptyStateText;

    public interface OnExerciseSelectedListener {
        void onExerciseSelected(Exercise exercise);
    }

    public static ExerciseSelectionDialog newInstance() {
        return new ExerciseSelectionDialog();
    }

    public void setOnExerciseSelectedListener(OnExerciseSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        rootView = inflater.inflate(R.layout.dialog_exercise_selection, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupRecyclerView(view);
    }

    private void setupViews(View view) {
        try {
            progressIndicator = view.findViewById(R.id.progressIndicator);
            emptyStateText = view.findViewById(R.id.emptyStateText);
            
            if (progressIndicator != null) {
                progressIndicator.setVisibility(View.VISIBLE);
            }
            
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views", e);
        }
    }

    private void setupRecyclerView(View view) {
        try {
            Log.d(TAG, "Setting up RecyclerView");
            RecyclerView recyclerView = view.findViewById(R.id.exerciseSelectionList);
            
            if (recyclerView == null) {
                Log.e(TAG, "RecyclerView not found! Check the layout ID.");
                return;
            }
            
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            
            adapter = new ExerciseAdapter(this);
            recyclerView.setAdapter(adapter);
            
            // Load exercises with a slight delay to show loading state
            recyclerView.post(() -> {
                List<Exercise> exercises = getDummyExercises();
                Log.d(TAG, "Loading " + exercises.size() + " exercises");
                
                if (exercises.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    adapter.setExercises(exercises);
                }
                
                // Hide progress indicator
                if (progressIndicator != null) {
                    progressIndicator.setVisibility(View.GONE);
                }
            });
            
            Log.d(TAG, "RecyclerView setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
            if (progressIndicator != null) {
                progressIndicator.setVisibility(View.GONE);
            }
            showEmptyState();
        }
    }

    private void showEmptyState() {
        if (rootView == null) return;
        
        try {
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.VISIBLE);
                emptyStateText.setText("No exercises available.\nPlease try again later.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing empty state", e);
        }
    }
    
    private void hideEmptyState() {
        if (rootView == null) return;
        
        try {
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding empty state", e);
        }
    }

    @Override
    public void onExerciseClick(Exercise exercise) {
        Log.d(TAG, "Exercise clicked: " + (exercise != null ? exercise.getName() : "null"));
        if (listener != null && exercise != null) {
            try {
                listener.onExerciseSelected(exercise);
                dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Error handling exercise selection", e);
                Toast.makeText(requireContext(), "Error selecting exercise", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "No listener set for exercise selection or exercise is null");
            Toast.makeText(requireContext(), "Error selecting exercise", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Exercise> getDummyExercises() {
        List<Exercise> exercises = new ArrayList<>();
        
        try {
            // Chest exercises
            exercises.add(new Exercise("c1", "Bench Press", 
                "The bench press is a compound exercise that primarily targets the chest muscles. " +
                "It also engages the shoulders and triceps.", 
                "chest", "intermediate", 45, null, null));
                
            exercises.add(new Exercise("c2", "Push-Ups", 
                "A fundamental bodyweight exercise that works the chest, shoulders, and triceps. " +
                "Great for beginners and can be modified for different difficulty levels.", 
                "chest", "beginner", 30, null, null));
            
            // Back exercises
            exercises.add(new Exercise("b1", "Pull-Ups", 
                "An upper body pulling exercise that targets the back muscles, particularly the lats. " +
                "Also works the biceps and core.", 
                "back", "intermediate", 40, null, null));
                
            exercises.add(new Exercise("b2", "Deadlift", 
                "A compound exercise that works the entire posterior chain. " +
                "Targets the back, glutes, and hamstrings.", 
                "back", "advanced", 50, null, null));
            
            // Legs exercises
            exercises.add(new Exercise("l1", "Squats", 
                "A fundamental lower body exercise that targets the quadriceps, hamstrings, and glutes. " +
                "Essential for building leg strength.", 
                "legs", "intermediate", 45, null, null));
                
            exercises.add(new Exercise("l2", "Lunges", 
                "A unilateral leg exercise that improves balance and strength. " +
                "Works the quadriceps, hamstrings, and glutes.", 
                "legs", "beginner", 35, null, null));
            
            // Shoulders exercises
            exercises.add(new Exercise("s1", "Military Press", 
                "A compound shoulder exercise that builds upper body strength. " +
                "Primary focus on the deltoids and triceps.", 
                "shoulders", "intermediate", 40, null, null));
                
            exercises.add(new Exercise("s2", "Lateral Raises", 
                "An isolation exercise for the lateral deltoids. " +
                "Helps build shoulder width and definition.", 
                "shoulders", "beginner", 30, null, null));
            
            // Arms exercises
            exercises.add(new Exercise("a1", "Bicep Curls", 
                "An isolation exercise targeting the biceps. " +
                "Multiple variations available for different angles.", 
                "arms", "beginner", 30, null, null));
                
            exercises.add(new Exercise("a2", "Tricep Extensions", 
                "Isolation exercise for the triceps muscles. " +
                "Can be performed with various equipment.", 
                "arms", "beginner", 30, null, null));
            
            // Core exercises
            exercises.add(new Exercise("ab1", "Crunches", 
                "A basic abdominal exercise targeting the rectus abdominis. " +
                "Good for beginners to develop core strength.", 
                "core", "beginner", 20, null, null));
                
            exercises.add(new Exercise("ab2", "Planks", 
                "An isometric core exercise that builds stability and endurance. " +
                "Works the entire core, including deep muscles.", 
                "core", "beginner", 25, null, null));
            
            // Cardio exercises
            exercises.add(new Exercise("cd1", "Running", 
                "A fundamental cardio exercise that improves endurance and burns calories. " +
                "Can be done outdoors or on a treadmill.", 
                "cardio", "beginner", 30, null, null));
                
            exercises.add(new Exercise("cd2", "Jump Rope", 
                "A high-intensity cardio exercise that improves coordination and agility. " +
                "Great for warming up or HIIT workouts.", 
                "cardio", "intermediate", 20, null, null));
            
            // HIIT exercises
            exercises.add(new Exercise("h1", "Burpees", 
                "A full-body HIIT exercise combining a push-up, jump, and squat. " +
                "Excellent for burning calories and improving conditioning.", 
                "hiit", "advanced", 20, null, null));
                
            exercises.add(new Exercise("h2", "Mountain Climbers", 
                "A dynamic core exercise that also provides cardio benefits. " +
                "Great for building core strength and endurance.", 
                "hiit", "intermediate", 15, null, null));
        } catch (Exception e) {
            Log.e(TAG, "Error creating dummy exercises", e);
        }
        
        return exercises;
    }
} 