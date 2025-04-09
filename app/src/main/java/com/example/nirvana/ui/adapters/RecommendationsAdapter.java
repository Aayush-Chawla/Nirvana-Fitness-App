package com.example.nirvana.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.models.PredefinedFoodItem;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.ViewHolder> {
    private static final String TAG = "RecommendationsAdapter";
    private List<PredefinedFoodItem> recommendations = new ArrayList<>();
    private OnRecommendationClickListener listener;

    public interface OnRecommendationClickListener {
        void onRecommendationClick(PredefinedFoodItem food);
    }

    public RecommendationsAdapter(OnRecommendationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PredefinedFoodItem food = recommendations.get(position);
        holder.bind(food);
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    public void updateRecommendations(List<PredefinedFoodItem> newRecommendations) {
        Log.d(TAG, "Updating recommendations: " + newRecommendations.size() + " items");
        this.recommendations = new ArrayList<>(newRecommendations);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFoodName;
        private final TextView tvNutritionInfo;
        private final MaterialButton btnAdd;
        private final ImageView ivRecommendationIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvNutritionInfo = itemView.findViewById(R.id.tvNutritionInfo);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            ivRecommendationIcon = itemView.findViewById(R.id.ivRecommendationIcon);
        }

        void bind(PredefinedFoodItem food) {
            if (food == null) {
                Log.e(TAG, "Attempting to bind null food item");
                return;
            }

            // Set food name
            tvFoodName.setText(food.getName());

            // Set appropriate icon based on food category
            String category = food.getCategory().toLowerCase();
            int iconResId;
            if (category.contains("protein") || category.contains("meat")) {
                iconResId = R.drawable.ic_protein;
            } else if (category.contains("fruit")) {
                iconResId = R.drawable.ic_food;
            } else if (category.contains("vegetable")) {
                iconResId = R.drawable.ic_food;
            } else if (category.contains("grain") || category.contains("carb")) {
                iconResId = R.drawable.ic_food;
            } else if (category.contains("dairy")) {
                iconResId = R.drawable.ic_food;
            } else {
                iconResId = R.drawable.ic_food;
            }
            ivRecommendationIcon.setImageResource(iconResId);

            // Calculate nutrition for one serving
            double calories = food.calculateCalories(food.getServingSize());
            double protein = food.calculateProtein(food.getServingSize());
            double carbs = food.calculateCarbs(food.getServingSize());
            double fat = food.calculateFat(food.getServingSize());

            // Format nutrition info
            String nutritionInfo = String.format(Locale.getDefault(),
                "%d cal • %.1fg P • %.1fg C • %.1fg F",
                (int) calories, protein, carbs, fat);
            tvNutritionInfo.setText(nutritionInfo);

            // Set click listener
            btnAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecommendationClick(food);
                }
            });

            Log.d(TAG, "Bound food item: " + food.getName() + " with nutrition: " + nutritionInfo);
        }
    }
} 