package com.example.nirvana.ui.adapters;

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

    private final List<FoodItem> foodItems;

    public FoodLogAdapter(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_log, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem foodItem = foodItems.get(position);
        holder.bind(foodItem);
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFoodName;
        private final TextView tvFoodCalories;
        private final TextView tvFoodTime;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodCalories = itemView.findViewById(R.id.tvFoodCalories);
            tvFoodTime = itemView.findViewById(R.id.tvFoodTime);
        }

        public void bind(FoodItem foodItem) {
            tvFoodName.setText(foodItem.getName());
            tvFoodCalories.setText(foodItem.getCalories() + " kcal");
            tvFoodTime.setText(foodItem.getTime());
        }
    }
} 