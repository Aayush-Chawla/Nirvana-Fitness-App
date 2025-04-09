package com.example.nirvana.fragments.diet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.data.models.FoodItem;
import com.example.nirvana.models.PredefinedFoodItem;
import com.example.nirvana.services.FoodItemService;
import com.example.nirvana.ui.adapters.FoodSelectionAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class FoodSelectionDialog extends DialogFragment {
    private RecyclerView rvFoodItems;
    private ChipGroup chipGroupCategories;
    private FoodSelectionAdapter adapter;
    private OnFoodSelectedListener listener;
    private String mealType;

    public interface OnFoodSelectedListener {
        void onFoodSelected(FoodItem foodItem);
    }

    public static FoodSelectionDialog newInstance(String mealType) {
        FoodSelectionDialog dialog = new FoodSelectionDialog();
        Bundle args = new Bundle();
        args.putString("meal_type", mealType);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnFoodSelectedListener(OnFoodSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_MaterialComponents_Dialog);
        if (getArguments() != null) {
            mealType = getArguments().getString("meal_type", "Snacks");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_food_selection, container, false);
        
        rvFoodItems = view.findViewById(R.id.rvFoodItems);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        
        setupRecyclerView();
        setupChipGroup();
        
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new FoodSelectionAdapter();
        rvFoodItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFoodItems.setAdapter(adapter);
        
        // Load all food items initially
        List<PredefinedFoodItem> foodItems = FoodItemService.loadFoodItems(requireContext());
        adapter.setFoodItems(foodItems);
        
        adapter.setOnFoodItemSelectedListener((item, quantity) -> {
            if (listener != null) {
                // Convert PredefinedFoodItem to FoodItem
                FoodItem foodItem = new FoodItem(
                    item.getId(),
                    item.getName(),
                    String.valueOf(quantity), // servingId
                    item.getServingUnit(), // servingDescription
                    item.calculateCalories(quantity),
                    item.calculateProtein(quantity),
                    item.calculateCarbs(quantity),
                    item.calculateFat(quantity),
                    mealType
                );
                listener.onFoodSelected(foodItem);
                dismiss();
            }
        });
    }

    private void setupChipGroup() {
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedCategory = null;
            if (checkedId != View.NO_ID) {
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    selectedCategory = chip.getText().toString();
                    if (selectedCategory.equals("All")) {
                        selectedCategory = null;
                    }
                }
            }
            
            List<PredefinedFoodItem> filteredItems;
            if (selectedCategory == null) {
                filteredItems = FoodItemService.loadFoodItems(requireContext());
            } else {
                filteredItems = FoodItemService.getFoodItemsByCategory(selectedCategory);
            }
            adapter.setFoodItems(filteredItems);
        });
    }
} 