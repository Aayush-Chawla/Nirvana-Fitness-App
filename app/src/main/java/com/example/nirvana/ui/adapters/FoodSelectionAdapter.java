package com.example.nirvana.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.models.PredefinedFoodItem;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodSelectionAdapter extends RecyclerView.Adapter<FoodSelectionAdapter.ViewHolder> {
    private static final String TAG = "FoodSelectionAdapter";
    private static final int MAX_SERVINGS = 10; // Constant for max servings
    private static final int MIN_SERVINGS = 1;  // Constant for min servings
    
    private List<PredefinedFoodItem> foodItems = new ArrayList<>();
    private List<PredefinedFoodItem> filteredItems = new ArrayList<>();
    private OnFoodItemSelectedListener listener;
    private DecimalFormat df = new DecimalFormat("#.#");

    public interface OnFoodItemSelectedListener {
        void onFoodItemSelected(PredefinedFoodItem item, int quantity);
    }

    public void setOnFoodItemSelectedListener(OnFoodItemSelectedListener listener) {
        this.listener = listener;
    }

    public void setFoodItems(List<PredefinedFoodItem> items) {
        if (items == null) {
            this.foodItems = new ArrayList<>();
            this.filteredItems = new ArrayList<>();
        } else {
            this.foodItems = new ArrayList<>(items);
            this.filteredItems = new ArrayList<>(items);
            
            // Debug log of items
            Log.d(TAG, "Loaded " + items.size() + " food items");
            for (PredefinedFoodItem item : items) {
                Log.d(TAG, item.toString());
            }
        }
        notifyDataSetChanged();
    }

    public void filterByCategory(String category) {
        filteredItems.clear();
        if (category == null || category.isEmpty()) {
            filteredItems.addAll(foodItems);
        } else {
            for (PredefinedFoodItem item : foodItems) {
                if (category.equals(item.getCategory())) {
                    filteredItems.add(item);
                }
            }
        }
        Log.d(TAG, "Filtered to " + filteredItems.size() + " items for category: " + category);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PredefinedFoodItem item = filteredItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFoodName;
        private final TextView tvQuantity;
        private final SeekBar seekBarQuantity;
        private final TextView tvCalories;
        private final TextView tvProtein;
        private final TextView tvCarbs;
        private final TextView tvFat;
        private final MaterialButton btnAdd;
        private int currentQuantity = 0;
        private PredefinedFoodItem currentItem;

        ViewHolder(View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            seekBarQuantity = itemView.findViewById(R.id.seekBarQuantity);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvProtein = itemView.findViewById(R.id.tvProtein);
            tvCarbs = itemView.findViewById(R.id.tvCarbs);
            tvFat = itemView.findViewById(R.id.tvFat);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            
            // Set default text for nutrition labels
            tvCalories.setText("Calories: 0");
            tvProtein.setText("Protein: 0g");
            tvCarbs.setText("Carbs: 0g");
            tvFat.setText("Fat: 0g");
            
            setupListeners();
        }

        private void setupListeners() {
            seekBarQuantity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (currentItem != null) {
                        // Ensure progress is at least MIN_SERVINGS
                        currentQuantity = Math.max(MIN_SERVINGS, progress);
                        
                        // Update nutrition info with current quantity
                        updateNutritionInfo(currentItem, currentQuantity);
                        
                        // Always enable the button since we ensure minimum is 1
                        btnAdd.setEnabled(true);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Ensure minimum value is MIN_SERVINGS
                    if (seekBarQuantity.getProgress() < MIN_SERVINGS) {
                        seekBarQuantity.setProgress(MIN_SERVINGS);
                    }
                    Log.d(TAG, "SeekBar stopped at: " + seekBarQuantity.getProgress());
                }
            });

            btnAdd.setOnClickListener(v -> {
                if (listener != null && currentQuantity >= MIN_SERVINGS && currentItem != null) {
                    listener.onFoodItemSelected(currentItem, currentQuantity);
                    
                    // Reset after adding
                    seekBarQuantity.setProgress(MIN_SERVINGS);
                    currentQuantity = MIN_SERVINGS;
                    updateNutritionInfo(currentItem, MIN_SERVINGS);
                    
                    // Show toast for feedback
                    if (itemView.getContext() != null) {
                        Toast.makeText(itemView.getContext(), 
                            "Added " + currentItem.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        void bind(final PredefinedFoodItem item) {
            if (item == null) {
                Log.e(TAG, "Attempting to bind null item");
                return;
            }
            
            currentItem = item;
            
            // Debug nutrition values
            Log.d(TAG, "Binding food item: " + item.getName());
            if (item.getPer100g() != null) {
                Log.d(TAG, "Nutrition info available");
            } else {
                Log.e(TAG, "Nutrition info missing for " + item.getName());
            }
            
            // Set food name with serving size info
            String nameText = String.format(Locale.getDefault(), "%s (%d%s per serving)", 
                item.getName(),
                item.getServingSize(),
                item.getServingUnit());
            tvFoodName.setText(nameText);
            
            // Always set max quantity to MAX_SERVINGS (10)
            seekBarQuantity.setMax(MAX_SERVINGS);
            
            // Set initial quantity to MIN_SERVINGS (1)
            currentQuantity = MIN_SERVINGS;
            seekBarQuantity.setProgress(MIN_SERVINGS);
            btnAdd.setEnabled(true);
            
            // Initialize nutrition display with minimum serving
            updateNutritionInfo(item, MIN_SERVINGS);
        }

        private void updateNutritionInfo(PredefinedFoodItem item, int servings) {
            if (item == null) {
                Log.e(TAG, "Attempting to update nutrition info for null item");
                return;
            }
            
            // Calculate total quantity in grams/ml
            int totalQuantity = servings * item.getServingSize();
            
            // Update quantity display
            String quantityText = String.format(Locale.getDefault(), "%d serving%s (%d%s)", 
                servings,
                servings == 1 ? "" : "s",
                totalQuantity,
                item.getServingUnit());
            tvQuantity.setText(quantityText);
            
            // Calculate nutrition values using the model's methods
            double calories = item.calculateCalories(totalQuantity);
            double protein = item.calculateProtein(totalQuantity);
            double carbs = item.calculateCarbs(totalQuantity);
            double fat = item.calculateFat(totalQuantity);
            
            Log.d(TAG, String.format("Calculated for %d servings (%d%s): cal=%.1f, p=%.1f, c=%.1f, f=%.1f",
                servings, totalQuantity, item.getServingUnit(), calories, protein, carbs, fat));

            // Update nutrition displays with formatted values
            tvCalories.setText(String.format(Locale.getDefault(), "Calories: %s", df.format(calories)));
            tvProtein.setText(String.format(Locale.getDefault(), "Protein: %sg", df.format(protein)));
            tvCarbs.setText(String.format(Locale.getDefault(), "Carbs: %sg", df.format(carbs)));
            tvFat.setText(String.format(Locale.getDefault(), "Fat: %sg", df.format(fat)));
        }
    }
} 