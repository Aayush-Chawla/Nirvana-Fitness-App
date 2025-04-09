package com.example.nirvana.fragments.diet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.data.models.FoodItem;
import com.example.nirvana.ui.adapters.FoodLogAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LogFoodFragment extends Fragment implements FoodSelectionDialog.OnFoodSelectedListener {
    private static final String TAG = "LogFoodFragment";
    
    private MaterialButtonToggleGroup toggleGroup;
    private Button btnAddFood;
    private RecyclerView rvFoodLog;
    private FoodLogAdapter adapter;
    
    private DatabaseReference userRef;
    private String userId;
    private String currentDate;
    private String selectedMealType = "Breakfast";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_food, container, false);
        
        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        
        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentDate = dateFormat.format(new Date());
        
        // Initialize views
        initializeViews(view);
        initializeRecyclerView(view);
        setupListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        toggleGroup = view.findViewById(R.id.toggleGroup);
        btnAddFood = view.findViewById(R.id.btnAddFood);
        rvFoodLog = view.findViewById(R.id.rvFoodLog);
        
        // Set default selection
        MaterialButton breakfastButton = view.findViewById(R.id.btnBreakfast);
        breakfastButton.setChecked(true);
    }

    private void initializeRecyclerView(View view) {
        rvFoodLog.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FoodLogAdapter(new ArrayList<>());  // Initialize with empty list
        rvFoodLog.setAdapter(adapter);
    }

    private void setupListeners() {
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnBreakfast) {
                    selectedMealType = "Breakfast";
                } else if (checkedId == R.id.btnLunch) {
                    selectedMealType = "Lunch";
                } else if (checkedId == R.id.btnDinner) {
                    selectedMealType = "Dinner";
                } else if (checkedId == R.id.btnSnacks) {
                    selectedMealType = "Snacks";
                }
            }
        });

        btnAddFood.setOnClickListener(v -> showFoodSelectionDialog());
    }

    private void showFoodSelectionDialog() {
        FoodSelectionDialog dialog = FoodSelectionDialog.newInstance(selectedMealType);
        dialog.setOnFoodSelectedListener(this);
        dialog.show(getChildFragmentManager(), "food_selection");
    }

    @Override
    public void onFoodSelected(FoodItem foodItem) {
        if (foodItem != null) {
            saveFood(foodItem);
            // Update UI to show the selected food item
            adapter.notifyDataSetChanged();
        }
    }

    private void saveFood(FoodItem foodItem) {
        // Save to Firebase
        String foodId = userRef.child("food_logs").child(currentDate).push().getKey();
        if (foodId != null) {
            userRef.child("food_logs").child(currentDate).child(foodId).setValue(foodItem)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Food item saved successfully");
                    Toast.makeText(getContext(), "Food logged successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving food item", e);
                    Toast.makeText(getContext(), "Error saving food item", Toast.LENGTH_SHORT).show();
                });

            // Also save to meals for the meal type
            userRef.child("meals").child(selectedMealType).child(currentDate).child(foodId).setValue(foodItem)
                .addOnFailureListener(e -> Log.e(TAG, "Error saving to meals", e));
        }
    }
} 