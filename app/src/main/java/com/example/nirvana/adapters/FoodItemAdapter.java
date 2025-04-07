package com.example.nirvana.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.FoodItem;

public class FoodItemAdapter extends ListAdapter<FoodItem, FoodItemAdapter.FoodItemViewHolder> {
    private static final String TAG = "FoodItemAdapter";

    public FoodItemAdapter() {
        super(new DiffUtil.ItemCallback<FoodItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull FoodItem oldItem, @NonNull FoodItem newItem) {
                // If items have unique IDs, compare them
                if (oldItem.getTime() != null && newItem.getTime() != null) {
                    return oldItem.getTime().equals(newItem.getTime());
                }
                // Fallback comparison
                return oldItem == newItem;
            }

            @Override
            public boolean areContentsTheSame(@NonNull FoodItem oldItem, @NonNull FoodItem newItem) {
                // Compare relevant fields
                boolean sameNames = false;
                if (oldItem.getName() != null && newItem.getName() != null) {
                    sameNames = oldItem.getName().equals(newItem.getName());
                } else if (oldItem.getFoodName() != null && newItem.getFoodName() != null) {
                    sameNames = oldItem.getFoodName().equals(newItem.getFoodName());
                }
                
                // Compare times
                boolean sameTimes = false;
                if (oldItem.getTime() != null && newItem.getTime() != null) {
                    sameTimes = oldItem.getTime().equals(newItem.getTime());
                }
                
                return sameNames && sameTimes;
            }
        });
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {
        FoodItem item = getItem(position);
        holder.bind(item);
    }

    static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtFoodName;
        private final TextView txtCalories;
        private final TextView txtTime;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFoodName = itemView.findViewById(R.id.txtFoodName);
            txtCalories = itemView.findViewById(R.id.txtCalories);
            txtTime = itemView.findViewById(R.id.txtTime);
        }

        public void bind(FoodItem item) {
            Log.d(TAG, "Binding food item: " + item);
            
            // Set food name (handle different property names)
            if (item.getName() != null && !item.getName().isEmpty()) {
                txtFoodName.setText(item.getName());
            } else if (item.getFoodName() != null && !item.getFoodName().isEmpty()) {
                txtFoodName.setText(item.getFoodName());
            } else {
                txtFoodName.setText("Unknown Food");
            }
            
            // Set calories (handle different property types)
            if (item.getCaloriesInt() > 0) {
                txtCalories.setText(String.format("%d cal", item.getCaloriesInt()));
            } else {
                int calories = (int) item.getCalories();
                txtCalories.setText(String.format("%d cal", calories));
            }
            
            // Set time
            if (item.getTime() != null && !item.getTime().isEmpty()) {
                txtTime.setText(item.getTime());
            } else {
                txtTime.setText("Today");
            }
        }
    }
} 