package com.example.nirvana.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.WorkoutCategory;

import java.util.List;

public class WorkoutCategoryAdapter extends RecyclerView.Adapter<WorkoutCategoryAdapter.ViewHolder> {
    private List<WorkoutCategory> workoutCategories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(WorkoutCategory category);
    }

    // Constructor
    public WorkoutCategoryAdapter(List<WorkoutCategory> workoutCategories, OnCategoryClickListener listener) {
        this.workoutCategories = workoutCategories;
        this.listener = listener;
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
        holder.txtCategoryName.setText(category.getName());
        holder.imgCategory.setImageResource(category.getImageResource());
        
        if (holder.txtCategoryDescription != null && category.getDescription() != null) {
            holder.txtCategoryDescription.setText(category.getDescription());
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workoutCategories != null ? workoutCategories.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryName;
        TextView txtCategoryDescription;
        ImageView imgCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            txtCategoryDescription = itemView.findViewById(R.id.txtCategoryDescription);
            imgCategory = itemView.findViewById(R.id.imgCategory);
        }
    }

    public void updateCategories(List<WorkoutCategory> newCategories) {
        this.workoutCategories = newCategories;
        notifyDataSetChanged();
    }
}
