package com.example.nirvana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.models.Workout;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Adapter for displaying workouts in a RecyclerView
 */
public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    
    private List<Workout> workouts;
    private OnWorkoutClickListener listener;
    
    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
    }
    
    public WorkoutAdapter() {
        this.workouts = new ArrayList<>();
    }
    
    public WorkoutAdapter(OnWorkoutClickListener listener) {
        this.workouts = new ArrayList<>();
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
    
    public void setWorkouts(Collection<Workout> workouts) {
        this.workouts.clear();
        this.workouts.addAll(workouts);
        notifyDataSetChanged();
    }
    
    public void addWorkout(Workout workout) {
        workouts.add(workout);
        notifyItemInserted(workouts.size() - 1);
    }
    
    public void clearWorkouts() {
        workouts.clear();
        notifyDataSetChanged();
    }
    
    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardWorkout;
        private TextView tvWorkoutName;
        private TextView tvWorkoutDay;
        private TextView tvWorkoutDuration;
        private TextView tvWorkoutFocusArea;
        private TextView tvWorkoutDescription;
        
        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            cardWorkout = itemView.findViewById(R.id.cardWorkout);
            tvWorkoutName = itemView.findViewById(R.id.tvWorkoutName);
            tvWorkoutDay = itemView.findViewById(R.id.tvWorkoutDay);
            tvWorkoutDuration = itemView.findViewById(R.id.tvWorkoutDuration);
            tvWorkoutFocusArea = itemView.findViewById(R.id.tvWorkoutFocusArea);
            tvWorkoutDescription = itemView.findViewById(R.id.tvWorkoutDescription);
        }
        
        public void bind(Workout workout) {
            tvWorkoutName.setText(workout.getName());
            tvWorkoutDay.setText(workout.getDay());
            tvWorkoutDuration.setText(workout.getDurationMinutes() + " min");
            tvWorkoutFocusArea.setText(workout.getFocusArea());
            tvWorkoutDescription.setText(workout.getDescription());
            
            if (listener != null) {
                cardWorkout.setOnClickListener(v -> listener.onWorkoutClick(workout));
            }
        }
    }
} 