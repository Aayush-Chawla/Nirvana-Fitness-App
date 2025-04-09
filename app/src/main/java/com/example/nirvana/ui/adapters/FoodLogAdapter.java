package com.example.nirvana.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.FoodItem;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class FoodLogAdapter extends RecyclerView.Adapter<FoodLogAdapter.FoodViewHolder> {
    private static final String TAG = "FoodLogAdapter";
    private List<FoodItem> foodItems;

    public FoodLogAdapter(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
        Log.d(TAG, "Constructor called with " + (foodItems != null ? foodItems.size() : "null") + " items");
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_log, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        if (foodItems == null || position >= foodItems.size()) {
            Log.e(TAG, "onBindViewHolder: Invalid position " + position + " or null foodItems");
            return;
        }
        
        FoodItem foodItem = foodItems.get(position);
        if (foodItem == null) {
            Log.e(TAG, "onBindViewHolder: Null food item at position " + position);
            return;
        }
        
        Log.d(TAG, "onBindViewHolder: binding item at position " + position + 
            " - name: " + foodItem.getName() + 
            ", calories: " + foodItem.getCaloriesInt() + 
            ", time: " + (foodItem.getTime() != null ? foodItem.getTime() : "null"));
        
        holder.bind(foodItem);
    }

    @Override
    public int getItemCount() {
        int count = (foodItems != null) ? foodItems.size() : 0;
        Log.d(TAG, "getItemCount: returning " + count);
        return count;
    }

    public void updateFoodItems(List<FoodItem> newFoodItems) {
        Log.d(TAG, "updateFoodItems: received " + (newFoodItems != null ? newFoodItems.size() : "null") + " items");
        
        if (newFoodItems != null) {
            for (FoodItem item : newFoodItems) {
                if (item != null) {
                    Log.d(TAG, "  - Item: " + item.getName() + 
                        ", " + item.getCaloriesInt() + " calories" + 
                        ", time: " + (item.getTime() != null ? item.getTime() : "null") + 
                        ", mealType: " + (item.getMealType() != null ? item.getMealType() : "null"));
                } else {
                    Log.e(TAG, "  - Null item in the list");
                }
            }
            this.foodItems = new ArrayList<>(newFoodItems); // Create a new copy
        } else {
            this.foodItems = new ArrayList<>(); // Empty list instead of null
            Log.w(TAG, "updateFoodItems: Null list passed, using empty list instead");
        }
        
        notifyDataSetChanged();
        Log.d(TAG, "updateFoodItems: called notifyDataSetChanged");
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtFoodName;
        private final TextView txtCalories;
        private final TextView txtTime;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFoodName = itemView.findViewById(R.id.txtFoodName);
            txtCalories = itemView.findViewById(R.id.txtCalories);
            txtTime = itemView.findViewById(R.id.txtTime);
            
            if (txtFoodName == null) {
                Log.e("FoodViewHolder", "txtFoodName view not found!");
            }
            if (txtCalories == null) {
                Log.e("FoodViewHolder", "txtCalories view not found!");
            }
            if (txtTime == null) {
                Log.e("FoodViewHolder", "txtTime view not found!");
            }
        }

        public void bind(FoodItem foodItem) {
            if (foodItem == null) {
                Log.e("FoodViewHolder", "Cannot bind null food item");
                return;
            }
            
            try {
                // Check if all views are available before binding
                if (txtFoodName != null && txtCalories != null && txtTime != null) {
                    // Safely set food name
                    String name = foodItem.getName();
                    txtFoodName.setText(name != null ? name : "Unknown Food");
                    
                    // Format calories with locale for proper number formatting
                    String calories = String.format(Locale.getDefault(), "%d kcal", foodItem.getCaloriesInt());
                    txtCalories.setText(calories);
                    
                    // Safely set time
                    String time = foodItem.getTime();
                    txtTime.setText(time != null ? time : "");
                    
                    Log.d("FoodViewHolder", "Item bound successfully: " + foodItem.getName());
                } else {
                    Log.e("FoodViewHolder", "One or more views are null, cannot bind item");
                }
            } catch (Exception e) {
                Log.e("FoodViewHolder", "Error binding item: " + e.getMessage(), e);
            }
        }
    }
} 