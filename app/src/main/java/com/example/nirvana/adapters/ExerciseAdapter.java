package com.example.nirvana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nirvana.R;
import com.example.nirvana.models.Exercise;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder> {
    private final OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    private static final DiffUtil.ItemCallback<Exercise> DIFF_CALLBACK = new DiffUtil.ItemCallback<Exercise>() {
        @Override
        public boolean areItemsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getDescription().equals(newItem.getDescription()) &&
                   oldItem.getMuscleGroup().equals(newItem.getMuscleGroup()) &&
                   oldItem.getDifficultyLevel().equals(newItem.getDifficultyLevel()) &&
                   oldItem.getDurationSeconds() == newItem.getDurationSeconds();
        }
    };

    public ExerciseAdapter(OnExerciseClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        Log.d("ExerciseAdapter", "Adapter created with DiffUtil");
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ExerciseAdapter", "Creating new ViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = getItem(position);
        Log.d("ExerciseAdapter", "Binding exercise at position " + position + ": " + exercise.getName());
        holder.bind(exercise, listener);
    }

    /**
     * Sets a new list of exercises and notifies the adapter
     * @param exercises The new list of exercises to display
     */
    public void setExercises(List<Exercise> exercises) {
        Log.d("ExerciseAdapter", "setExercises called with " + (exercises != null ? exercises.size() : 0) + " exercises");
        submitList(exercises);
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgExercise;
        private final TextView txtName;
        private final TextView txtDescription;
        private final TextView txtDetails;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            imgExercise = itemView.findViewById(R.id.imgExercise);
            txtName = itemView.findViewById(R.id.txtName);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtDetails = itemView.findViewById(R.id.txtDetails);
        }

        public void bind(Exercise exercise, OnExerciseClickListener listener) {
            Log.d("ExerciseAdapter", "Binding exercise: " + exercise.getName());
            
            // Set text fields
            txtName.setText(exercise.getName());
            txtDescription.setText(exercise.getDescription());
            
            // Format details string
            String details = String.format("%s • %d min • %s", 
                exercise.getMuscleGroup(),
                exercise.getDurationSeconds() / 60, // Convert seconds to minutes
                exercise.getDifficultyLevel());
            txtDetails.setText(details);

            // Load image using Glide with improved error handling
            String imageUrl = exercise.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d("ExerciseAdapter", "Loading image from URL: " + imageUrl);
                
                // Add request options to prevent 403 errors
                com.bumptech.glide.request.RequestOptions requestOptions = new com.bumptech.glide.request.RequestOptions()
                    .timeout(10000) // 10s timeout
                    .placeholder(R.drawable.ic_exercise_default)
                    .error(getDefaultDrawableForCategory(exercise.getMuscleGroup()));
                
                // Add headers to request to help with some hosting services
                com.bumptech.glide.load.model.GlideUrl glideUrl = new com.bumptech.glide.load.model.GlideUrl(imageUrl, 
                    new com.bumptech.glide.load.model.LazyHeaders.Builder()
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .addHeader("Referer", "https://www.example.com/")
                        .build());
                
                Glide.with(itemView.getContext())
                    .load(glideUrl)
                    .apply(requestOptions)
                    .into(imgExercise);
            } else {
                // Set default image based on category
                Log.d("ExerciseAdapter", "Using default image for category: " + exercise.getMuscleGroup());
                imgExercise.setImageResource(getDefaultDrawableForCategory(exercise.getMuscleGroup()));
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                Log.d("ExerciseAdapter", "Exercise clicked: " + exercise.getName());
                if (listener != null) {
                    listener.onExerciseClick(exercise);
                }
            });
        }
        
        private int getDefaultDrawableForCategory(String muscleGroup) {
            if (muscleGroup == null) return R.drawable.ic_exercise_default;
            
            switch (muscleGroup.toLowerCase()) {
                case "chest":
                    return R.drawable.ic_chest;
                case "back":
                    return R.drawable.ic_back;
                case "shoulders":
                    return R.drawable.ic_shoulders;
                case "arms":
                case "biceps":
                case "triceps":
                    return R.drawable.ic_arms;
                case "legs":
                case "quadriceps":
                case "hamstrings":
                case "calves":
                    return R.drawable.ic_legs;
                case "abs":
                case "core":
                    return R.drawable.ic_core;
                case "cardio":
                    return R.drawable.ic_cardio;
                case "full body":
                    return R.drawable.ic_full_body;
                default:
                    return R.drawable.ic_exercise_default;
            }
        }
    }
} 