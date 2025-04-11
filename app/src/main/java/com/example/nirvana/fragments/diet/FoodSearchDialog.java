package com.example.nirvana.fragments.diet;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.FoodSearchAdapter;
import com.example.nirvana.models.FoodItem;
import com.example.nirvana.models.PredefinedFoodItem;
import com.example.nirvana.services.FoodItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog for searching and selecting food items.
 * Note: This dialog works with two different FoodItem classes:
 * - com.example.nirvana.models.FoodItem (used by the adapter)
 * - com.example.nirvana.data.models.FoodItem (used by Firestore)
 */
public class FoodSearchDialog extends DialogFragment {
    private static final String TAG = "FoodSearchDialog";
    private EditText editSearch;
    private RecyclerView recyclerResults;
    private FoodSearchAdapter adapter;
    private OnFoodSelectedListener listener;

    public static FoodSearchDialog newInstance(Fragment fragment) {
        FoodSearchDialog dialog = new FoodSearchDialog();
        if (fragment instanceof OnFoodSelectedListener) {
            dialog.setOnFoodSelectedListener((OnFoodSelectedListener) fragment);
        }
        return dialog;
    }

    /**
     * Interface for notifying when a food item has been selected
     */
    public interface OnFoodSelectedListener {
        void onFoodSelected(com.example.nirvana.data.models.FoodItem foodItem, String servingSize);
    }

    public void setOnFoodSelectedListener(OnFoodSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_food_search, null);

        editSearch = view.findViewById(R.id.editSearch);
        recyclerResults = view.findViewById(R.id.recyclerResults);
        recyclerResults.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FoodSearchAdapter(new ArrayList<>(), this::showServingSelectionDialog);
        recyclerResults.setAdapter(adapter);

        builder.setView(view)
               .setTitle("Search Food")
               .setPositiveButton("Close", null);

        // Setup search functionality with TextWatcher for real-time search
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load initial data
        performSearch("");

        return builder.create();
    }

    private void performSearch(String query) {
        // Get food items from assets/food_items.json through FoodItemService
        List<PredefinedFoodItem> predefinedItems = FoodItemService.loadFoodItems(requireContext());
        List<FoodItem> modelResults = new ArrayList<>();
        
        // Filter items based on search query
        List<PredefinedFoodItem> filteredItems;
        if (query.isEmpty()) {
            filteredItems = predefinedItems;
        } else {
            filteredItems = predefinedItems.stream()
                .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        // Convert PredefinedFoodItem to FoodItem
        for (PredefinedFoodItem item : filteredItems) {
            FoodItem foodItem = new FoodItem(
                item.getId(),
                item.getName(),
                (int) item.getPer100g().getCalories(),
                item.getServingSize() + item.getServingUnit(),
                item.getPer100g().getProtein(),
                item.getPer100g().getCarbs(),
                item.getPer100g().getFat()
            );
            modelResults.add(foodItem);
        }
        
        Log.d(TAG, "Found " + modelResults.size() + " food items matching '" + query + "'");
        adapter.updateFoodItems(modelResults);
    }

    private void showServingSelectionDialog(FoodItem foodItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_serving_size, null);
        
        EditText editServingSize = view.findViewById(R.id.editServingSize);
        TextView tvNutritionInfo = view.findViewById(R.id.tvNutritionInfo);
        
        // Set default serving size
        editServingSize.setText("100");
        
        // Calculate and display initial nutrition info
        updateNutritionInfo(tvNutritionInfo, foodItem, 100);
        
        // Add text change listener to update nutrition info in real time
        editServingSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int servingSize = s.length() > 0 ? Integer.parseInt(s.toString()) : 0;
                    updateNutritionInfo(tvNutritionInfo, foodItem, servingSize);
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    tvNutritionInfo.setText("Please enter a valid number");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        builder.setView(view)
               .setTitle("Select Serving Size")
               .setPositiveButton("Confirm", (dialog, which) -> {
                   String servingSizeText = editServingSize.getText().toString();
                   if (servingSizeText.isEmpty()) {
                       servingSizeText = "100"; // Default if empty
                   }
                   String servingSize = servingSizeText + "g";
                   if (listener != null) {
                       com.example.nirvana.data.models.FoodItem dataFoodItem = convertToDataFoodItem(foodItem, servingSize);
                       listener.onFoodSelected(dataFoodItem, servingSize);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    /**
     * Converts a FoodItem from models package to a FoodItem from data.models package
     * @param foodItem The source FoodItem from models package
     * @param servingSize The selected serving size (e.g. "100g")
     * @return A new FoodItem from data.models package with values from the source
     */
    private com.example.nirvana.data.models.FoodItem convertToDataFoodItem(FoodItem foodItem, String servingSize) {
        com.example.nirvana.data.models.FoodItem dataFoodItem = new com.example.nirvana.data.models.FoodItem();
        
        // Extract numeric part from serving size
        float servingSizeValue = 100f; // Default value
        try {
            String numericPart = servingSize.replaceAll("[^0-9.]", "");
            if (!numericPart.isEmpty()) {
                servingSizeValue = Float.parseFloat(numericPart);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing serving size: " + servingSize, e);
        }
        
        // Calculate the ratio for nutrition values
        float ratio = servingSizeValue / 100f; // Base values are for 100g
        
        // Set all properties
        dataFoodItem.setFoodId(foodItem.getId());
        dataFoodItem.setName(foodItem.getName());
        dataFoodItem.setFoodName(foodItem.getName());
        
        // Calculate actual calories based on serving size
        int adjustedCalories = Math.round(foodItem.getCalories() * ratio);
        dataFoodItem.setCaloriesInt(adjustedCalories);
        dataFoodItem.setCalories(adjustedCalories);
        
        // Calculate actual macros based on serving size
        dataFoodItem.setProtein(foodItem.getProtein() * ratio);
        dataFoodItem.setCarbs(foodItem.getCarbs() * ratio);
        dataFoodItem.setFat(foodItem.getFat() * ratio);
        
        // Set serving information
        dataFoodItem.setServingDescription(servingSize);
        dataFoodItem.setServingId("custom");
        
        // Default meal type (will be updated by LogDietFragment based on selected tab)
        dataFoodItem.setMealType("snack");
        
        return dataFoodItem;
    }
    
    private void updateNutritionInfo(TextView tvNutritionInfo, FoodItem foodItem, int servingSize) {
        // Calculate nutrition based on serving size
        double ratio = servingSize / 100.0; // Base values are for 100g
        
        int calories = (int) (foodItem.getCalories() * ratio);
        double protein = foodItem.getProtein() * ratio;
        double carbs = foodItem.getCarbs() * ratio;
        double fat = foodItem.getFat() * ratio;
        
        String info = String.format(
                "Nutrition for %dg:\n\nCalories: %d\nProtein: %.1fg\nCarbs: %.1fg\nFat: %.1fg",
                servingSize, calories, protein, carbs, fat);
        tvNutritionInfo.setText(info);
    }
}