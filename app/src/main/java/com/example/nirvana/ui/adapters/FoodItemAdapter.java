// FoodItemAdapter.java in ui.adapters package
package com.example.nirvana.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.FoodItem;
import java.util.List;

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
                .inflate(R.layout.item_food_logged, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem foodItem = foodItems.get(position);

        holder.tvFoodName.setText(foodItem.getFoodName());
        holder.tvServing.setText(foodItem.getServingDescription());
        holder.tvCalories.setText(String.format("%.0f kcal", foodItem.getCalories()));

        holder.itemView.setOnClickListener(v -> listener.onFoodItemClick(foodItem));
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFoodName;
        public TextView tvServing;
        public TextView tvCalories;
        public ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvServing = itemView.findViewById(R.id.tvServing);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}