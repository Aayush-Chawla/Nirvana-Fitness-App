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
        FoodItem foodItem = foodItems.get(position);
        Log.d(TAG, "onBindViewHolder: binding item at position " + position + 
            " - name: " + foodItem.getName() + 
            ", calories: " + foodItem.getCaloriesInt() + 
            ", time: " + foodItem.getTime());
        holder.bind(foodItem);
    }

    @Override
    public int getItemCount() {
        int count = foodItems.size();
        Log.d(TAG, "getItemCount: returning " + count);
        return count;
    }

    public void updateFoodItems(List<FoodItem> newFoodItems) {
        Log.d(TAG, "updateFoodItems: received " + (newFoodItems != null ? newFoodItems.size() : "null") + " items");
        for (FoodItem item : newFoodItems) {
            Log.d(TAG, "  - Item: " + item.getName() + ", " + item.getCaloriesInt() + " calories, time: " + item.getTime());
        }
        this.foodItems = newFoodItems;
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
            try {
                txtFoodName.setText(foodItem.getName());
                txtCalories.setText(foodItem.getCaloriesInt() + " kcal");
                txtTime.setText(foodItem.getTime());
                Log.d("FoodViewHolder", "Item bound successfully: " + foodItem.getName());
            } catch (Exception e) {
                Log.e("FoodViewHolder", "Error binding item: " + e.getMessage(), e);
            }
        }
    }
} 