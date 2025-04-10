package com.example.nirvana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nirvana.R;
import com.example.nirvana.models.Exercise;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying exercises in a RecyclerView
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    private List<Exercise> exercises = new ArrayList<>();
    private OnExerciseClickListener listener;

    /**
     * Interface for exercise item click events
     */
    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    /**
     * Default constructor
     */
    public ExerciseAdapter() {
    }

    /**
     * Constructor with listener
     */
    public ExerciseAdapter(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    /**
     * Set new exercises list and notify adapter
     */
    public void setExercises(List<Exercise> exercises) {
        this.exercises.clear();
        if (exercises != null) {
            this.exercises.addAll(exercises);
        }
        notifyDataSetChanged();
    }

    /**
     * Add an exercise to the list and notify adapter
     */
    public void addExercise(Exercise exercise) {
        if (exercise != null && !exercises.contains(exercise)) {
            exercises.add(exercise);
            notifyItemInserted(exercises.size() - 1);
        }
    }

    /**
     * ViewHolder for exercise items
     */
    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExerciseName;
        private TextView tvExerciseDetails;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseDetails = itemView.findViewById(R.id.tvExerciseDetails);

            // Set click listener if available
            if (listener != null) {
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onExerciseClick(exercises.get(position));
                    }
                });
            }
        }

        /**
         * Bind exercise data to views
         */
        void bind(Exercise exercise) {
            if (exercise == null) return;
            
            // Set exercise name
            tvExerciseName.setText(exercise.getName());
            
            // Format and set exercise details
            StringBuilder details = new StringBuilder();
            
            if (exercise.getSets() > 0 && exercise.getReps() > 0) {
                details.append(exercise.getSets()).append(" sets × ")
                       .append(exercise.getReps()).append(" reps");
            } else if (exercise.getDuration() > 0) {
                details.append(exercise.getDuration()).append(" min");
            }
            
            if (details.length() > 0) {
                details.append(" • ");
            }
            
            details.append(exercise.getMuscleGroup());
            
            if (exercise.getDifficulty() > 0) {
                details.append(" • ");
                for (int i = 0; i < exercise.getDifficulty(); i++) {
                    details.append("★");
                }
                for (int i = exercise.getDifficulty(); i < 5; i++) {
                    details.append("☆");
                }
            }
            
            tvExerciseDetails.setText(details.toString());
        }
    }
} 