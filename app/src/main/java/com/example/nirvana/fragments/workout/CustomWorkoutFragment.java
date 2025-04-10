package com.example.nirvana.fragments.workout;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.CustomWorkoutAdapter;
import com.example.nirvana.models.CustomWorkout;
import com.example.nirvana.models.Exercise;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CustomWorkoutFragment extends Fragment implements CustomWorkoutAdapter.CustomWorkoutListener {
    private static final String TAG = "CustomWorkoutFragment";
    
    private RecyclerView recyclerView;
    private CustomWorkoutAdapter adapter;
    private List<CustomWorkout> workoutList;
    private LinearLayout emptyStateView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private FloatingActionButton fabAddWorkout;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerCustomWorkouts);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        fabAddWorkout = view.findViewById(R.id.fabAddWorkout);

        // Setup RecyclerView
        setupRecyclerView();
        
        // Load workouts
        loadCustomWorkouts();

        // Setup FAB
        fabAddWorkout.setOnClickListener(v -> {
            navigateToWorkoutDetail(null);
        });
    }
    
    private void setupRecyclerView() {
        workoutList = new ArrayList<>();
        adapter = new CustomWorkoutAdapter(requireContext(), workoutList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadCustomWorkouts() {
        if (userId == null) {
            updateEmptyState(true);
            return;
        }

        db.collection("users").document(userId)
                .collection("customWorkouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    workoutList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CustomWorkout workout = document.toObject(CustomWorkout.class);
                        // Ensure workout has an ID
                        if (workout.getId() == null) {
                            workout.setId(document.getId());
                        }
                        workoutList.add(workout);
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyState(workoutList.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load workouts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateEmptyState(true);
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void navigateToWorkoutDetail(CustomWorkout workout) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        if (workout != null) {
            args.putString("workoutId", workout.getId());
        }
        navController.navigate(R.id.action_customWorkoutFragment_to_customWorkoutDetailFragment, args);
    }

    @Override
    public void onWorkoutClicked(CustomWorkout workout) {
        navigateToWorkoutDetail(workout);
    }

    @Override
    public void onWorkoutDeleted(CustomWorkout workout) {
        if (userId == null || workout.getId() == null) return;

        db.collection("users").document(userId)
                .collection("customWorkouts")
                .document(workout.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Workout deleted", Toast.LENGTH_SHORT).show();
                    workoutList.remove(workout);
                    adapter.notifyDataSetChanged();
                    updateEmptyState(workoutList.isEmpty());
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to delete workout", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onWorkoutStarted(CustomWorkout workout) {
        if (workout.getExercises() == null || workout.getExercises().isEmpty()) {
            Toast.makeText(requireContext(), "This workout has no exercises", Toast.LENGTH_SHORT).show();
            return;
        }

        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putString("workoutId", workout.getId());
        args.putBoolean("isCustomWorkout", true);
        navController.navigate(R.id.action_customWorkoutFragment_to_activeWorkoutFragment, args);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload workouts when returning to this fragment
        loadCustomWorkouts();
    }

    private void showCreateWorkoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create Custom Workout");
        
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);
        
        EditText nameInput = new EditText(requireContext());
        nameInput.setHint("Workout Name");
        layout.addView(nameInput);
        
        EditText descriptionInput = new EditText(requireContext());
        descriptionInput.setHint("Description");
        layout.addView(descriptionInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(requireContext(), "Please enter a workout name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            createCustomWorkout(name, description);
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void createCustomWorkout(String name, String description) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in");
            return;
        }
        
        String userId = currentUser.getUid();
        CustomWorkout workout = new CustomWorkout(null, name, description, userId);
        
        db.collection("users")
            .document(userId)
            .collection("custom_workouts")
            .add(workout)
            .addOnSuccessListener(documentReference -> {
                workout.setId(documentReference.getId());
                adapter.addWorkout(workout);
                Toast.makeText(requireContext(), "Custom workout created", Toast.LENGTH_SHORT).show();
                
                // Navigate to workout detail/edit screen
                navigateToWorkoutDetail(workout);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating custom workout", e);
                Toast.makeText(requireContext(), "Failed to create workout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadExercisesForWorkout(CustomWorkout workout) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || workout == null || workout.getId() == null) {
            return;
        }
        
        String userId = currentUser.getUid();
        
        db.collection("users")
            .document(userId)
            .collection("custom_workouts")
            .document(workout.getId())
            .collection("exercises")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Exercise> exercises = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Exercise exercise = document.toObject(Exercise.class);
                    exercise.setId(document.getId());
                    exercises.add(exercise);
                }
                
                workout.setExercises(exercises);
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading exercises for workout: " + workout.getId(), e);
            });
    }
    
    public void onCustomWorkoutLongClick(CustomWorkout workout, int position) {
        CharSequence[] options = {"Edit", "Start Workout", "Delete"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(workout.getName());
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Edit
                    navigateToWorkoutDetail(workout);
                    break;
                case 1: // Start Workout
                    startWorkout(workout);
                    break;
                case 2: // Delete
                    showDeleteConfirmation(workout, position);
                    break;
            }
        });
        builder.show();
    }
    
    private void startWorkout(CustomWorkout workout) {
        if (workout.getExercises() == null || workout.getExercises().isEmpty()) {
            Toast.makeText(requireContext(), "This workout has no exercises", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Bundle args = new Bundle();
        args.putString("category", workout.getName());
        args.putString("experience", "Custom");
        args.putSerializable("exercises", new ArrayList<>(workout.getExercises()));
        Navigation.findNavController(requireView())
            .navigate(R.id.action_customWorkoutFragment_to_activeWorkoutFragment, args);
    }
    
    private void showDeleteConfirmation(CustomWorkout workout, int position) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete " + workout.getName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> deleteWorkout(workout, position))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteWorkout(CustomWorkout workout, int position) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || workout == null || workout.getId() == null) {
            return;
        }
        
        String userId = currentUser.getUid();
        DocumentReference workoutRef = db.collection("users")
            .document(userId)
            .collection("custom_workouts")
            .document(workout.getId());
        
        // First delete all exercises
        workoutRef.collection("exercises")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    document.getReference().delete();
                }
                
                // Then delete the workout document
                workoutRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        adapter.removeWorkout(position);
                        Toast.makeText(requireContext(), "Workout deleted", Toast.LENGTH_SHORT).show();
                        updateEmptyState(adapter.getItemCount() == 0);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting workout", e);
                        Toast.makeText(requireContext(), "Failed to delete workout", Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting exercises to delete", e);
                Toast.makeText(requireContext(), "Failed to delete workout", Toast.LENGTH_SHORT).show();
            });
    }
}
