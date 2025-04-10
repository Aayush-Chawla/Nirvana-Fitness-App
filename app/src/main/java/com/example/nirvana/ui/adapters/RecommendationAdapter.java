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

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {
    private static final String TAG = "RecommendationAdapter";
    private List<PredefinedFoodItem> recommendations = new ArrayList<>();
    private OnRecommendationClickListener listener;

    public interface OnRecommendationClickListener {
        void onRecommendationClick(PredefinedFoodItem food);
    }

    // Constructor with listener
    public RecommendationAdapter(OnRecommendationClickListener listener) {
        this.listener = listener;
    }

    // Empty constructor - no listener
    public RecommendationAdapter() {
        // Empty constructor
    }

    // Setter method for the listener
    public void setOnRecommendationClickListener(OnRecommendationClickListener listener) {
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
        if (recommendations.isEmpty()) {
            Log.w(TAG, "No recommendations to bind");
            return;
        }
        
        if (position >= recommendations.size()) {
            Log.e(TAG, "Position out of bounds: " + position + " size: " + recommendations.size());
            return;
        }
        
        PredefinedFoodItem food = recommendations.get(position);
        holder.bind(food);
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    public void updateRecommendations(List<PredefinedFoodItem> newRecommendations) {
        if (newRecommendations == null) {
            Log.e(TAG, "Attempted to update with null recommendations");
            this.recommendations = new ArrayList<>();
        } else {
            Log.d(TAG, "Updating recommendations: " + newRecommendations.size() + " items");
            this.recommendations = new ArrayList<>(newRecommendations);
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFoodName;
        private TextView tvNutritionInfo;
        private MaterialButton btnAdd;
        private ImageView ivRecommendationIcon;

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
            if (tvFoodName != null) {
                tvFoodName.setText(food.getName());
            }

            // Set nutrition info
            if (tvNutritionInfo != null) {
                try {
                    // Check if this is a recommendation text or actual food item
                    if (food.getCategory() != null && food.getCategory().equals("Recommendation")) {
                        // For recommendation text items, hide or set empty nutrition info
                        tvNutritionInfo.setVisibility(View.GONE);
                    } else {
                        tvNutritionInfo.setVisibility(View.VISIBLE);
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
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error calculating nutrition info", e);
                    tvNutritionInfo.setText("Nutrition info not available");
                }
            }

            // Set icon
            if (ivRecommendationIcon != null) {
                String category = food.getCategory() != null ? food.getCategory().toLowerCase() : "";
                int iconResId;
                
                if (category.contains("recommendation")) {
                    // Use a specific icon for recommendation items
                    iconResId = R.drawable.ic_food;
                } else if (category.contains("protein") || category.contains("meat")) {
                    iconResId = R.drawable.ic_protein;
                } else if (category.contains("fruit")) {
                    iconResId = R.drawable.ic_fruits;
                } else if (category.contains("vegetable")) {
                    iconResId = R.drawable.ic_vegetables;
                } else if (category.contains("grain") || category.contains("carb")) {
                    iconResId = R.drawable.ic_food;
                } else if (category.contains("dairy")) {
                    iconResId = R.drawable.ic_food;
                } else {
                    iconResId = R.drawable.ic_food;
                }
                ivRecommendationIcon.setImageResource(iconResId);
            }

            // Set click listener
            if (btnAdd != null) {
                btnAdd.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRecommendationClick(food);
                    } else {
                        Log.w(TAG, "No click listener set for recommendation item");
                    }
                });
            }

            Log.d(TAG, "Bound food item: " + food.getName());
        }
    }
} 