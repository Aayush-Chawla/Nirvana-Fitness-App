package com.example.nirvana.fragments.workout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.nirvana.R;
import com.example.nirvana.models.Exercise;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ExerciseDetailFragment extends Fragment {
    private Exercise exercise;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isWorkoutActive = false;
    private MaterialButton btnStartExercise;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = (Exercise) getArguments().getSerializable("exercise");
        }
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercise_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews(view);
        setupClickListeners(view);
    }

    private void setupViews(View view) {
        ImageView imgExercise = view.findViewById(R.id.imgExercise);
        TextView txtExerciseName = view.findViewById(R.id.txtExerciseName);
        TextView txtExerciseDescription = view.findViewById(R.id.txtExerciseDescription);
        TextView txtDuration = view.findViewById(R.id.txtDuration);
        TextView txtDifficulty = view.findViewById(R.id.txtDifficulty);
        btnStartExercise = view.findViewById(R.id.btnStartExercise);

        if (exercise != null) {
            // Load exercise image
            Glide.with(this)
                .load(exercise.getImageUrl())
                .placeholder(R.drawable.placeholder_exercise)
                .error(R.drawable.error_exercise)
                .into(imgExercise);

            txtExerciseName.setText(exercise.getName());
            txtExerciseDescription.setText(exercise.getDescription());
            txtDuration.setText(String.format("%d minutes", exercise.getDurationSeconds() / 60));
            txtDifficulty.setText(exercise.getDifficultyLevel());
        }
    }

    private void setupClickListeners(View view) {
        // Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigateUp()
        );

        // Watch video button
        view.findViewById(R.id.btnWatchVideo).setOnClickListener(v -> {
            if (exercise != null && exercise.getVideoUrl() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(exercise.getVideoUrl()));
                startActivity(intent);
            }
        });

        // Start exercise button
        btnStartExercise.setOnClickListener(v -> {
            if (!isWorkoutActive) {
                startWorkout();
            } else {
                finishWorkout();
            }
        });
    }

    private void startWorkout() {
        isWorkoutActive = true;
        btnStartExercise.setText("Finish Exercise");
        btnStartExercise.setIcon(getResources().getDrawable(R.drawable.ic_check, requireContext().getTheme()));
        
        // Add to workout history
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> workoutEntry = new HashMap<>();
        workoutEntry.put("exerciseId", exercise.getId());
        workoutEntry.put("exerciseName", exercise.getName());
        workoutEntry.put("startTime", System.currentTimeMillis());
        workoutEntry.put("muscleGroup", exercise.getMuscleGroup());
        
        db.collection("users")
            .document(userId)
            .collection("workoutHistory")
            .add(workoutEntry)
            .addOnSuccessListener(documentReference -> {
                // Store the document reference for updating later
                workoutEntry.put("historyId", documentReference.getId());
            });
    }

    private void finishWorkout() {
        isWorkoutActive = false;
        btnStartExercise.setText("Start Exercise");
        btnStartExercise.setIcon(getResources().getDrawable(R.drawable.ic_play, requireContext().getTheme()));
        
        // Update workout history
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("endTime", System.currentTimeMillis());
        updates.put("completed", true);
        
        db.collection("users")
            .document(userId)
            .collection("workoutHistory")
            .document(exercise.getHistoryId())
            .update(updates);
    }
} 