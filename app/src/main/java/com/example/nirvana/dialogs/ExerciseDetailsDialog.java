package com.example.nirvana.dialogs;

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
        TextView txtDuration = view.findViewById(R.id.txtDuration);
        TextView txtDifficulty = view.findViewById(R.id.txtDifficulty);
        MaterialButton btnWatchVideo = view.findViewById(R.id.btnWatchVideo);
        MaterialButton btnClose = view.findViewById(R.id.btnClose);

        if (exercise != null) {
            // Load exercise image
            if (exercise.getImageUrl() != null) {
                Glide.with(this)
                    .load(exercise.getImageUrl())
                    .placeholder(R.drawable.placeholder_exercise)
                    .error(R.drawable.error_exercise)
                    .into(imgExercise);
            }

            txtExerciseName.setText(exercise.getName());
            txtExerciseDescription.setText(exercise.getDescription());
            txtDuration.setText(String.format("%d minutes", exercise.getDuration()));
            txtDifficulty.setText(exercise.getDifficulty());

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
} 