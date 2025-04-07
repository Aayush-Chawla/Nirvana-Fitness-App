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
import com.bumptech.glide.Glide;

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
        
        // Handle both local resource images and remote URLs
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            // Load image from URL using Glide with improved error handling
            try {
                // Create request options
                com.bumptech.glide.request.RequestOptions requestOptions = new com.bumptech.glide.request.RequestOptions()
                    .timeout(10000) // 10s timeout
                    .placeholder(R.drawable.ic_exercise_placeholder)
                    .error(R.drawable.ic_exercise_placeholder);
                
                // Create Glide URL with headers
                com.bumptech.glide.load.model.GlideUrl glideUrl = new com.bumptech.glide.load.model.GlideUrl(
                    category.getImageUrl(),
                    new com.bumptech.glide.load.model.LazyHeaders.Builder()
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .addHeader("Referer", "https://www.example.com/")
                        .build()
                );
                
                // Load the image
                Glide.with(holder.itemView.getContext())
                    .load(glideUrl)
                    .apply(requestOptions)
                    .into(holder.imgCategory);
            } catch (Exception e) {
                // Fallback to local resource in case of any error
                android.util.Log.e("WorkoutCategoryAdapter", "Error loading image", e);
                holder.imgCategory.setImageResource(R.drawable.ic_exercise_placeholder);
            }
        } else {
            // Use local resource
            holder.imgCategory.setImageResource(category.getImageResource());
        }
        
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
