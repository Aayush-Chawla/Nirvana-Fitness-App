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

/**
 * Adapter for displaying food items in diet fragments
 */
public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {
    
    private List<FoodItem> foodItems;
    private OnFoodItemClickListener listener;
    
    public interface OnFoodItemClickListener {
        void onFoodItemClick(FoodItem foodItem);
    }
    
    public FoodItemAdapter(List<FoodItem> foodItems, OnFoodItemClickListener listener) {
        this.foodItems = foodItems;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_search, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = foodItems.get(position);
        
        holder.txtFoodName.setText(item.getName());
        holder.txtCalories.setText(String.format("%d calories", item.getCalories()));
        holder.txtServingSize.setText(item.getServingSize());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodItemClick(item);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return foodItems != null ? foodItems.size() : 0;
    }
    
    public void updateItems(List<FoodItem> newItems) {
        this.foodItems = newItems;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtFoodName;
        TextView txtCalories;
        TextView txtServingSize;
        
        ViewHolder(View itemView) {
            super(itemView);
            txtFoodName = itemView.findViewById(R.id.txtFoodName);
            txtCalories = itemView.findViewById(R.id.txtCalories);
            txtServingSize = itemView.findViewById(R.id.txtServingSize);
        }
    }
} 