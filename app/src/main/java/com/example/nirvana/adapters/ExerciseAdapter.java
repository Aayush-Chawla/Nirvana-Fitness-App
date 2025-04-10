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
import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder> {
    private final OnExerciseClickListener listener;
    private final boolean isEditable;
    private final Context context;
    private ExerciseListener exerciseListener;
    
    // Listener interface for edit and delete operations
    public interface ExerciseListener {
        void onExerciseEdit(int position, Exercise exercise);
        void onExerciseDelete(int position, Exercise exercise);
    }

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

    // Constructor with additional parameter for edit mode
    public ExerciseAdapter(Context context, List<Exercise> exercises, ExerciseListener exerciseListener, boolean isEditable) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = null;
        this.exerciseListener = exerciseListener;
        this.isEditable = isEditable;
        submitList(exercises);
        Log.d("ExerciseAdapter", "Adapter created with editable mode: " + isEditable);
    }
    
    // Original constructor
    public ExerciseAdapter(OnExerciseClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.context = null;
        this.isEditable = false;
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
        
        // Set basic exercise details
        holder.tvExerciseName.setText(exercise.getName());
        
        // For editable mode (custom workout)
        if (isEditable) {
            String details = String.format("%d sets x %d reps | %d second rest", 
                exercise.getSets(), exercise.getReps(), exercise.getRestInSeconds());
            holder.tvExerciseDetails.setText(details);
            
            // Show edit and delete buttons in editable mode
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivDelete.setVisibility(View.VISIBLE);
            
            // Set edit click listener
            holder.ivEdit.setOnClickListener(v -> {
                if (exerciseListener != null) {
                    exerciseListener.onExerciseEdit(holder.getAdapterPosition(), exercise);
                }
            });
            
            // Set delete click listener
            holder.ivDelete.setOnClickListener(v -> {
                if (exerciseListener != null) {
                    exerciseListener.onExerciseDelete(holder.getAdapterPosition(), exercise);
                }
            });
        } else {
            // Non-editable mode (regular exercise list)
            String details = String.format("%s • %d min • %s", 
                exercise.getCategory() != null ? exercise.getCategory() : "",
                exercise.getDuration(),
                exercise.getDifficulty() != null ? exercise.getDifficulty() : "");
            holder.tvExerciseDetails.setText(details);
            
            // Hide edit and delete buttons in non-editable mode
            holder.ivEdit.setVisibility(View.GONE);
            holder.ivDelete.setVisibility(View.GONE);
            
            // Set click listener for the whole item
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise);
                }
            });
        }
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
        private final ImageView ivExerciseIcon;
        private final TextView tvExerciseName;
        private final TextView tvExerciseDetails;
        private final ImageView ivEdit;
        private final ImageView ivDelete;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExerciseIcon = itemView.findViewById(R.id.ivExerciseIcon);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseDetails = itemView.findViewById(R.id.tvExerciseDetails);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
} 