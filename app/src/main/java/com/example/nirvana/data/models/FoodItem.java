package com.example.nirvana.data.models;

import androidx.annotation.NonNull;

public class FoodItem {
    private String foodId;
    private String foodName;
    private String servingId;
    private String servingDescription;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private String mealType;

    // Default constructor (required for Firebase)
    public FoodItem() {}

    // Parameterized constructor
    public FoodItem(@NonNull String foodId, @NonNull String foodName,
                    @NonNull String servingId, String servingDescription,
                    double calories, double protein,
                    double carbs, double fat, @NonNull String mealType) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.servingId = servingId;
        this.servingDescription = servingDescription;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.mealType = mealType;
    }

    // Getters and Setters
    @NonNull
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(@NonNull String foodId) {
        this.foodId = foodId;
    }

    @NonNull
    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(@NonNull String foodName) {
        this.foodName = foodName;
    }

    @NonNull
    public String getServingId() {
        return servingId;
    }

    public void setServingId(@NonNull String servingId) {
        this.servingId = servingId;
    }

    public String getServingDescription() {
        return servingDescription;
    }

    public void setServingDescription(String servingDescription) {
        this.servingDescription = servingDescription;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    @NonNull
    public String getMealType() {
        return mealType;
    }

    public void setMealType(@NonNull String mealType) {
        this.mealType = mealType;
    }

    // Optional: toString() method for debugging
    @Override
    @NonNull
    public String toString() {
        return "FoodItem{" +
                "foodId='" + foodId + '\'' +
                ", foodName='" + foodName + '\'' +
                ", servingId='" + servingId + '\'' +
                ", servingDescription='" + servingDescription + '\'' +
                ", calories=" + calories +
                ", protein=" + protein +
                ", carbs=" + carbs +
                ", fat=" + fat +
                ", mealType='" + mealType + '\'' +
                '}';
    }
}