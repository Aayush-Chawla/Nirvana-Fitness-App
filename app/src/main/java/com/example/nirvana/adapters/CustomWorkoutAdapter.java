package com.example.nirvana.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.models.CustomWorkout;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.content.Context;

public class CustomWorkoutAdapter extends RecyclerView.Adapter<CustomWorkoutAdapter.CustomWorkoutViewHolder> {
    private List<CustomWorkout> workouts;
    private OnCustomWorkoutClickListener listener;
    private Context context;
    private CustomWorkoutListener customWorkoutListener;

    // Interface for click listener
    public interface OnCustomWorkoutClickListener {
        void onCustomWorkoutClick(CustomWorkout workout);
        void onCustomWorkoutLongClick(CustomWorkout workout, int position);
    }
    
    // Interface for workout operations
    public interface CustomWorkoutListener {
        void onWorkoutClicked(CustomWorkout workout);
        void onWorkoutDeleted(CustomWorkout workout);
        void onWorkoutStarted(CustomWorkout workout);
    }

    // New constructor with CustomWorkoutListener
    public CustomWorkoutAdapter(Context context, List<CustomWorkout> workouts, CustomWorkoutListener listener) {
        this.context = context;
        this.workouts = workouts != null ? workouts : new ArrayList<>();
        this.customWorkoutListener = listener;
        Log.d("CustomWorkoutAdapter", "Adapter created with CustomWorkoutListener");
    }

    public CustomWorkoutAdapter(OnCustomWorkoutClickListener listener) {
        this.workouts = new ArrayList<>();
        this.listener = listener;
        Log.d("CustomWorkoutAdapter", "Adapter created");
    }

    @NonNull
    @Override
    public CustomWorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_custom_workout, parent, false);
        return new CustomWorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomWorkoutViewHolder holder, int position) {
        CustomWorkout workout = workouts.get(position);
        holder.bind(workout, listener, customWorkoutListener);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void setWorkouts(List<CustomWorkout> workouts) {
        this.workouts = workouts;
        notifyDataSetChanged();
        Log.d("CustomWorkoutAdapter", "Set workouts: " + workouts.size());
    }

    public void addWorkout(CustomWorkout workout) {
        workouts.add(workout);
        notifyItemInserted(workouts.size() - 1);
        Log.d("CustomWorkoutAdapter", "Added workout: " + workout.getName());
    }

    public void removeWorkout(int position) {
        if (position >= 0 && position < workouts.size()) {
            workouts.remove(position);
            notifyItemRemoved(position);
            Log.d("CustomWorkoutAdapter", "Removed workout at position: " + position);
        }
    }

    static class CustomWorkoutViewHolder extends RecyclerView.ViewHolder {
        private TextView txtWorkoutName;
        private TextView txtWorkoutDescription;
        private TextView txtExerciseCount;
        private TextView txtDuration;

        public CustomWorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            txtWorkoutName = itemView.findViewById(R.id.txtWorkoutName);
            txtWorkoutDescription = itemView.findViewById(R.id.txtWorkoutDescription);
            txtExerciseCount = itemView.findViewById(R.id.txtExerciseCount);
            txtDuration = itemView.findViewById(R.id.txtDuration);
        }

        public void bind(CustomWorkout workout, OnCustomWorkoutClickListener clickListener, CustomWorkoutListener workoutListener) {
            txtWorkoutName.setText(workout.getName());
            txtWorkoutDescription.setText(workout.getDescription());
            
            int exerciseCount = workout.getExercises() != null ? workout.getExercises().size() : 0;
            txtExerciseCount.setText(exerciseCount + " Exercises");
            
            int totalMinutes = workout.getTotalDuration() / 60;
            if (totalMinutes < 1) {
                txtDuration.setText(workout.getTotalDuration() + " sec");
            } else {
                txtDuration.setText(totalMinutes + " min");
            }

            // Set click listeners based on which interface is available
            if (clickListener != null) {
                // Set click listeners for the original interface
                itemView.setOnClickListener(v -> clickListener.onCustomWorkoutClick(workout));
                itemView.setOnLongClickListener(v -> {
                    clickListener.onCustomWorkoutLongClick(workout, getAdapterPosition());
                    return true;
                });
            } else if (workoutListener != null) {
                // Set click listener for the CustomWorkoutListener interface
                itemView.setOnClickListener(v -> workoutListener.onWorkoutClicked(workout));
                itemView.setOnLongClickListener(v -> {
                    // Show a popup menu with options
                    showPopupMenu(v, workout, workoutListener);
                    return true;
                });
            }
        }
        
        private void showPopupMenu(View view, CustomWorkout workout, CustomWorkoutListener listener) {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.menu_custom_workout_options, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_start_workout) {
                    listener.onWorkoutStarted(workout);
                    return true;
                } else if (itemId == R.id.action_delete_workout) {
                    listener.onWorkoutDeleted(workout);
                    return true;
                } else if (itemId == R.id.action_edit_workout) {
                    listener.onWorkoutClicked(workout);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }
    }
} 