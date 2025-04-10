package com.example.nirvana.fragments.diet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.models.FoodItem;

import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {

    private final List<FoodItem> foodItems;
    private final OnFoodItemClickListener listener;

    public FoodItemAdapter(List<FoodItem> foodItems, OnFoodItemClickListener listener) {
        this.foodItems = foodItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem foodItem = foodItems.get(position);
        holder.bind(foodItem, listener);
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFoodName;
        private final TextView tvCalories;
        private final TextView tvTime;
        private final TextView tvMacros;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.txtFoodName);
            tvCalories = itemView.findViewById(R.id.txtCalories);
            tvTime = itemView.findViewById(R.id.txtTime);
            tvMacros = itemView.findViewById(R.id.txtMacros);
        }

        public void bind(FoodItem foodItem, OnFoodItemClickListener listener) {
            tvFoodName.setText(foodItem.getName());
            tvCalories.setText(String.format("%d cal", foodItem.getCalories()));
            
            // Display serving size in the time TextView
            tvTime.setText(foodItem.getServingSize());
            
            // Format macros as P/C/F
            String macros = String.format("P: %.1fg • C: %.1fg • F: %.1fg",
                   foodItem.getProtein(), foodItem.getCarbs(), foodItem.getFat());
            tvMacros.setText(macros);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFoodItemClick(foodItem);
                }
            });
        }
    }

    public interface OnFoodItemClickListener {
        void onFoodItemClick(FoodItem foodItem);
    }
} 