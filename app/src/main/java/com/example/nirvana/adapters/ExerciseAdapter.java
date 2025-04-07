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
                   oldItem.getCategory().equals(newItem.getCategory()) &&
                   oldItem.getDifficulty().equals(newItem.getDifficulty()) &&
                   oldItem.getDuration() == newItem.getDuration();
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
                exercise.getCategory(),
                exercise.getDuration(),
                exercise.getDifficulty());
            txtDetails.setText(details);

            // Load image using Glide
            String imageUrl = exercise.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.contains("example.com")) {
                Log.d("ExerciseAdapter", "Loading image from URL: " + imageUrl);
                Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_exercise_default)
                    .error(getDefaultDrawableForCategory(exercise.getCategory()))
                    .fallback(getDefaultDrawableForCategory(exercise.getCategory()))
                    .into(imgExercise);
            } else {
                // Set default image based on category
                Log.d("ExerciseAdapter", "Using default image for category: " + exercise.getCategory());
                imgExercise.setImageResource(getDefaultDrawableForCategory(exercise.getCategory()));
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                Log.d("ExerciseAdapter", "Exercise clicked: " + exercise.getName());
                if (listener != null) {
                    listener.onExerciseClick(exercise);
                }
            });
        }
        
        private int getDefaultDrawableForCategory(String category) {
            if (category == null) {
                return R.drawable.ic_exercise_default;
            }
            
            String lowerCategory = category.toLowerCase();
            if (lowerCategory.contains("chest")) {
                return R.drawable.ic_exercise_default;
            } else if (lowerCategory.contains("back")) {
                return R.drawable.ic_exercise_default;
            } else if (lowerCategory.contains("leg")) {
                return R.drawable.ic_exercise_default;
            } else if (lowerCategory.contains("arm")) {
                return R.drawable.ic_exercise_default;
            } else if (lowerCategory.contains("shoulder")) {
                return R.drawable.ic_exercise_default;
            } else if (lowerCategory.contains("core") || lowerCategory.contains("abs")) {
                return R.drawable.ic_exercise_default;
            } else if (lowerCategory.contains("cardio")) {
                return R.drawable.ic_exercise_default;
            } else {
                return R.drawable.ic_exercise_default;
            }
        }
    }
} 