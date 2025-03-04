package com.example.nirvana;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import java.util.List;

public class WorkoutCategoryAdapter extends RecyclerView.Adapter<WorkoutCategoryAdapter.ViewHolder> {
    private List<WorkoutCategory> workoutCategories;  // Use WorkoutCategory list

    // Constructor
    public WorkoutCategoryAdapter(List<WorkoutCategory> workoutCategories) {
        this.workoutCategories = workoutCategories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutCategory category = workoutCategories.get(position);
        holder.txtCategoryName.setText(category.getName());  // Set category name
        holder.imgCategoryIcon.setImageResource(category.getIconResId());  // Set category icon
    }

    @Override
    public int getItemCount() {
        return workoutCategories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryName;
        ImageView imgCategoryIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            imgCategoryIcon = itemView.findViewById(R.id.imgCategoryIcon);
        }
    }
}
