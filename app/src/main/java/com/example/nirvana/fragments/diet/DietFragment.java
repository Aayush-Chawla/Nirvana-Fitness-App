package com.example.nirvana.fragments.diet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nirvana.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DietFragment extends Fragment {
    private TextView remainingCaloriesText;
    private TextView baseGoalText;
    private TextView foodCaloriesText;
    private TextView exerciseCaloriesText;
    private TextView proteinText;
    private TextView carbsText;
    private TextView fatText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        // Initialize views
        remainingCaloriesText = view.findViewById(R.id.remainingCaloriesText);
        baseGoalText = view.findViewById(R.id.baseGoalText);
        foodCaloriesText = view.findViewById(R.id.foodCaloriesText);
        exerciseCaloriesText = view.findViewById(R.id.exerciseCaloriesText);
        proteinText = view.findViewById(R.id.proteinText);
        carbsText = view.findViewById(R.id.carbsText);
        fatText = view.findViewById(R.id.fatText);

        // Update UI with sample data
        updateCalorieData();
        updateMacronutrientsData();

        return view;
    }

    private void updateCalorieData() {
        // Example data - replace with your actual calorie tracking logic
        int baseGoal = 1500;
        int foodCalories = 650;
        int exerciseCalories = 400;
        int remainingCalories = baseGoal - foodCalories + exerciseCalories;

        baseGoalText.setText(String.format("Base Goal: %d", baseGoal));
        foodCaloriesText.setText(String.format("Food: %d", foodCalories));
        exerciseCaloriesText.setText(String.format("Exercise: %d", exerciseCalories));
        remainingCaloriesText.setText(String.valueOf(remainingCalories));
    }

    private void updateMacronutrientsData() {
        // Example data - replace with your actual macronutrient tracking logic
        proteinText.setText("120g");
        carbsText.setText("180g");
        fatText.setText("50g");
    }

    // Method to update data from your tracking system
    public void updateDietData(int baseGoal, int foodCalories, int exerciseCalories,
                               String protein, String carbs, String fat) {
        int remainingCalories = baseGoal - foodCalories + exerciseCalories;

        baseGoalText.setText(String.format("Base Goal: %d", baseGoal));
        foodCaloriesText.setText(String.format("Food: %d", foodCalories));
        exerciseCaloriesText.setText(String.format("Exercise: %d", exerciseCalories));
        remainingCaloriesText.setText(String.valueOf(remainingCalories));

        proteinText.setText(protein);
        carbsText.setText(carbs);
        fatText.setText(fat);
    }
}