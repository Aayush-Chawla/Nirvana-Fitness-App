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
import com.bumptech.glide.Glide;
import com.example.nirvana.R;
import com.example.nirvana.models.Exercise;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class ExerciseDetailsDialog extends BottomSheetDialogFragment {
    private Exercise exercise;

    public static ExerciseDetailsDialog newInstance(Exercise exercise) {
        ExerciseDetailsDialog dialog = new ExerciseDetailsDialog();
        Bundle args = new Bundle();
        args.putSerializable("exercise", exercise);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = (Exercise) getArguments().getSerializable("exercise");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_exercise_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imgExercise = view.findViewById(R.id.imgExercise);
        TextView txtExerciseName = view.findViewById(R.id.txtExerciseName);
        TextView txtExerciseDescription = view.findViewById(R.id.txtExerciseDescription);
        TextView txtCategory = view.findViewById(R.id.txtCategory);
        TextView txtDuration = view.findViewById(R.id.txtDuration);
        TextView txtDifficulty = view.findViewById(R.id.txtDifficulty);
        MaterialButton btnWatchVideo = view.findViewById(R.id.btnWatchVideo);
        MaterialButton btnClose = view.findViewById(R.id.btnClose);

        if (exercise != null) {
            // Load exercise image
            if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
                Glide.with(this)
                    .load(exercise.getImageUrl())
                    .placeholder(R.drawable.placeholder_exercise)
                    .error(R.drawable.error_exercise)
                    .into(imgExercise);
            } else {
                // Set a default image based on category
                int defaultImage = getDefaultImageForCategory(exercise.getCategory());
                imgExercise.setImageResource(defaultImage);
            }

            txtExerciseName.setText(exercise.getName());
            txtExerciseDescription.setText(exercise.getDescription());
            txtCategory.setText(String.format("Category: %s", exercise.getCategory()));
            txtDuration.setText(String.format("Duration: %d minutes", exercise.getDuration()));
            txtDifficulty.setText(String.format("Difficulty: %s", exercise.getDifficulty()));

            // Watch video button
            if (exercise.getVideoUrl() != null && !exercise.getVideoUrl().isEmpty()) {
                btnWatchVideo.setVisibility(View.VISIBLE);
                btnWatchVideo.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(exercise.getVideoUrl()));
                    startActivity(intent);
                });
            } else {
                btnWatchVideo.setVisibility(View.GONE);
            }
        }

        // Close button
        btnClose.setOnClickListener(v -> dismiss());
    }

    private int getDefaultImageForCategory(String category) {
        switch (category.toLowerCase()) {
            case "chest":
                return R.drawable.ic_chest;
            case "back":
                return R.drawable.ic_back;
            case "legs":
                return R.drawable.ic_legs;
            case "shoulders":
                return R.drawable.ic_shoulders;
            case "arms":
                return R.drawable.ic_arms;
            case "core":
                return R.drawable.ic_core;
            case "cardio":
                return R.drawable.ic_cardio;
            case "hiit":
                return R.drawable.ic_hiit;
            default:
                return R.drawable.ic_exercise_default;
        }
    }
} 