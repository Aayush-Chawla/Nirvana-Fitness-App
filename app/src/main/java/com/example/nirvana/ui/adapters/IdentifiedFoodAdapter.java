package com.example.nirvana.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.ml.FoodRecognitionModel;
import com.example.nirvana.ml.FoodNutritionData;

import java.util.List;

public class IdentifiedFoodAdapter extends RecyclerView.Adapter<IdentifiedFoodAdapter.ViewHolder> {
    private List<FoodRecognitionModel.FoodRecognitionResult> identifiedFoods;

    public IdentifiedFoodAdapter(List<FoodRecognitionModel.FoodRecognitionResult> identifiedFoods) {
        this.identifiedFoods = identifiedFoods;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_identified_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodRecognitionModel.FoodRecognitionResult food = identifiedFoods.get(position);
        FoodNutritionData.NutritionInfo nutritionInfo = FoodNutritionData.getNutritionInfo(food.getFoodName());

        holder.tvFoodName.setText(food.getFoodName());
        holder.tvConfidence.setText(String.format("Confidence: %.1f%%", food.getConfidence() * 100));

        if (nutritionInfo != null) {
            String nutritionText = String.format("Calories: %.0f kcal | Protein: %.1fg | Carbs: %.1fg | Fat: %.1fg",
                    nutritionInfo.getCalories(),
                    nutritionInfo.getProtein(),
                    nutritionInfo.getCarbs(),
                    nutritionInfo.getFat());
            holder.tvNutritionInfo.setText(nutritionText);
        } else {
            holder.tvNutritionInfo.setText("Nutritional information not available");
        }
    }

    @Override
    public int getItemCount() {
        return identifiedFoods.size();
    }

    public void updateFoods(List<FoodRecognitionModel.FoodRecognitionResult> newFoods) {
        this.identifiedFoods = newFoods;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName;
        TextView tvConfidence;
        TextView tvNutritionInfo;

        ViewHolder(View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvConfidence = itemView.findViewById(R.id.tvConfidence);
            tvNutritionInfo = itemView.findViewById(R.id.tvNutritionInfo);
        }
    }
} 