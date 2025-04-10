package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.adapters.ExerciseAdapter;
import com.example.nirvana.models.CustomWorkout;
import com.example.nirvana.models.Exercise;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomWorkoutDetailFragment extends Fragment implements ExerciseAdapter.ExerciseListener {

    private EditText etWorkoutName;
    private EditText etWorkoutDescription;
    private RecyclerView rvExercises;
    private LinearLayout emptyStateView;
    private ExerciseAdapter adapter;
    private List<Exercise> exerciseList;
    private String workoutId;
    private CustomWorkout workout;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Check if we're editing an existing workout
        if (getArguments() != null) {
            workoutId = getArguments().getString("workoutId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_workout_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        etWorkoutName = view.findViewById(R.id.etWorkoutName);
        etWorkoutDescription = view.findViewById(R.id.etWorkoutDescription);
        rvExercises = view.findViewById(R.id.rvExercises);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        FloatingActionButton fabAddExercise = view.findViewById(R.id.fabAddExercise);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // Setup RecyclerView
        setupRecyclerView();

        // If we have a workout ID, load the workout details
        if (workoutId != null) {
            loadWorkoutDetails();
        } else {
            workout = new CustomWorkout();
            workout.setExercises(new ArrayList<>());
        }

        // Save button
        btnSave.setOnClickListener(v -> saveWorkout());

        // Cancel button
        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // Add exercise button
        fabAddExercise.setOnClickListener(v -> showAddExerciseDialog());
    }

    private void setupRecyclerView() {
        exerciseList = new ArrayList<>();
        adapter = new ExerciseAdapter(requireContext(), exerciseList, this, true); // true = editable mode
        rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExercises.setAdapter(adapter);
    }

    private void loadWorkoutDetails() {
        if (userId == null || workoutId == null) return;

        db.collection("users").document(userId)
                .collection("customWorkouts")
                .document(workoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        workout = documentSnapshot.toObject(CustomWorkout.class);
                        if (workout != null) {
                            workout.setId(documentSnapshot.getId());
                            populateWorkoutDetails();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Workout not found", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load workout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                });
    }

    private void populateWorkoutDetails() {
        if (workout == null) return;

        etWorkoutName.setText(workout.getName());
        etWorkoutDescription.setText(workout.getDescription());

        if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
            exerciseList.clear();
            exerciseList.addAll(workout.getExercises());
            adapter.notifyDataSetChanged();
            updateEmptyState(false);
        } else {
            updateEmptyState(true);
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvExercises.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            rvExercises.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void showAddExerciseDialog() {
        // We'll use a simple dialog to add an exercise
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_exercise, null);
        
        EditText etExerciseName = dialogView.findViewById(R.id.etExerciseName);
        EditText etExerciseSets = dialogView.findViewById(R.id.etExerciseSets);
        EditText etExerciseReps = dialogView.findViewById(R.id.etExerciseReps);
        EditText etExerciseRest = dialogView.findViewById(R.id.etExerciseRest);
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Exercise")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etExerciseName.getText().toString().trim();
                    String setsStr = etExerciseSets.getText().toString().trim();
                    String repsStr = etExerciseReps.getText().toString().trim();
                    String restStr = etExerciseRest.getText().toString().trim();
                    
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(requireContext(), "Exercise name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    int sets = TextUtils.isEmpty(setsStr) ? 3 : Integer.parseInt(setsStr);
                    int reps = TextUtils.isEmpty(repsStr) ? 10 : Integer.parseInt(repsStr);
                    int rest = TextUtils.isEmpty(restStr) ? 60 : Integer.parseInt(restStr);
                    
                    Exercise exercise = new Exercise();
                    exercise.setId(UUID.randomUUID().toString());
                    exercise.setName(name);
                    exercise.setSets(sets);
                    exercise.setReps(reps);
                    exercise.setRestInSeconds(rest);
                    
                    // Add to our list
                    if (workout.getExercises() == null) {
                        workout.setExercises(new ArrayList<>());
                    }
                    workout.getExercises().add(exercise);
                    exerciseList.add(exercise);
                    adapter.notifyDataSetChanged();
                    updateEmptyState(false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveWorkout() {
        String name = etWorkoutName.getText().toString().trim();
        String description = etWorkoutDescription.getText().toString().trim();
        
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Workout name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (userId == null) {
            Toast.makeText(requireContext(), "You must be logged in to save workouts", Toast.LENGTH_SHORT).show();
            return;
        }
        
        workout.setName(name);
        workout.setDescription(description);
        workout.setExercises(exerciseList);
        
        DocumentReference workoutRef;
        if (workoutId != null) {
            // Update existing workout
            workoutRef = db.collection("users").document(userId)
                    .collection("customWorkouts").document(workoutId);
        } else {
            // Create new workout
            workoutRef = db.collection("users").document(userId)
                    .collection("customWorkouts").document();
            workout.setId(workoutRef.getId());
        }
        
        workoutRef.set(workout)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Workout saved successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save workout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onExerciseEdit(int position, Exercise exercise) {
        // Show edit dialog similar to add but with pre-filled values
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_exercise, null);
        
        EditText etExerciseName = dialogView.findViewById(R.id.etExerciseName);
        EditText etExerciseSets = dialogView.findViewById(R.id.etExerciseSets);
        EditText etExerciseReps = dialogView.findViewById(R.id.etExerciseReps);
        EditText etExerciseRest = dialogView.findViewById(R.id.etExerciseRest);
        
        // Pre-fill with existing values
        etExerciseName.setText(exercise.getName());
        etExerciseSets.setText(String.valueOf(exercise.getSets()));
        etExerciseReps.setText(String.valueOf(exercise.getReps()));
        etExerciseRest.setText(String.valueOf(exercise.getRestInSeconds()));
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Exercise")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etExerciseName.getText().toString().trim();
                    String setsStr = etExerciseSets.getText().toString().trim();
                    String repsStr = etExerciseReps.getText().toString().trim();
                    String restStr = etExerciseRest.getText().toString().trim();
                    
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(requireContext(), "Exercise name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    int sets = TextUtils.isEmpty(setsStr) ? 3 : Integer.parseInt(setsStr);
                    int reps = TextUtils.isEmpty(repsStr) ? 10 : Integer.parseInt(repsStr);
                    int rest = TextUtils.isEmpty(restStr) ? 60 : Integer.parseInt(restStr);
                    
                    exercise.setName(name);
                    exercise.setSets(sets);
                    exercise.setReps(reps);
                    exercise.setRestInSeconds(rest);
                    
                    adapter.notifyItemChanged(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onExerciseDelete(int position, Exercise exercise) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Exercise")
                .setMessage("Are you sure you want to delete this exercise?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    exerciseList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateEmptyState(exerciseList.isEmpty());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
} 