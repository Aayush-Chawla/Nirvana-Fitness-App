package com.example.nirvana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.models.Exercise;
import com.example.nirvana.models.Workout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Adapter for displaying workouts in a RecyclerView
 */
public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    private List<Workout> workouts = new ArrayList<>();
    private OnWorkoutClickListener listener;

    /**
     * Interface for workout item click events
     */
    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onStartWorkoutClick(Workout workout);
    }

    /**
     * Default constructor
     */
    public WorkoutAdapter() {
    }

    /**
     * Constructor with listener
     */
    public WorkoutAdapter(OnWorkoutClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    /**
     * Set new workouts list and notify adapter
     */
    public void setWorkouts(List<Workout> workouts) {
        this.workouts.clear();
        if (workouts != null) {
            this.workouts.addAll(workouts);
        }
        notifyDataSetChanged();
    }
    
    /**
     * Set new workouts from a map and notify adapter
     */
    public void setWorkouts(Collection<Workout> workouts) {
        this.workouts.clear();
        if (workouts != null) {
            this.workouts.addAll(workouts);
        }
        notifyDataSetChanged();
    }

    /**
     * Add a workout to the list and notify adapter
     */
    public void addWorkout(Workout workout) {
        if (workout != null && !workouts.contains(workout)) {
            workouts.add(workout);
            notifyItemInserted(workouts.size() - 1);
        }
    }

    /**
     * ViewHolder for workout items
     */
    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvWorkoutName;
        private final TextView tvWorkoutDescription;
        private final TextView tvExerciseCount;
        private final MaterialButton btnStartWorkout;
        private final RecyclerView rvExercises;
        private ExerciseAdapter exerciseAdapter;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvWorkoutName = itemView.findViewById(R.id.tvWorkoutName);
            tvWorkoutDescription = itemView.findViewById(R.id.tvWorkoutDescription);
            tvExerciseCount = itemView.findViewById(R.id.tvExerciseCount);
            btnStartWorkout = itemView.findViewById(R.id.btnStartWorkout);
            rvExercises = itemView.findViewById(R.id.rvExercises);

            // Set up exercise recycler view
            exerciseAdapter = new ExerciseAdapter();
            rvExercises.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvExercises.setAdapter(exerciseAdapter);
            rvExercises.setNestedScrollingEnabled(false);
            
            // Set click listeners
            setupClickListeners();
        }
        
        /**
         * Setup click listeners for the ViewHolder
         */
        private void setupClickListeners() {
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onWorkoutClick(workouts.get(position));
                }
            });
            
            btnStartWorkout.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onStartWorkoutClick(workouts.get(position));
                }
            });
        }

        /**
         * Bind workout data to views
         */
        void bind(Workout workout) {
            if (workout == null) return;
            
            // Set workout data
            tvWorkoutName.setText(workout.getTitle());
            tvWorkoutDescription.setText(workout.getDescription());
            
            // Set exercise count
            List<Exercise> exercises = workout.getExercises();
            int exerciseCount = exercises != null ? exercises.size() : 0;
            tvExerciseCount.setText(String.format("%d exercises", exerciseCount));
            
            // Set exercises in adapter
            exerciseAdapter.setExercises(exercises);
            
            // Show or hide start button based on listener
            btnStartWorkout.setVisibility(listener != null ? View.VISIBLE : View.GONE);
        }
    }
} 