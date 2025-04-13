package com.example.nirvana.ml;

import java.util.HashMap;
import java.util.Map;

public class FoodNutritionData {
    private static final Map<String, NutritionInfo> FOOD_NUTRITION_DATABASE = new HashMap<>();

    static {
        // Initialize with common food items and their nutritional values
        // Values are per 100g serving unless specified otherwise
        
        // Fast Foods
        addFood("hotdog", 290, 11.7, 18.8, 24.3);  // Standard hot dog with bun
        addFood("hamburger", 295, 17.0, 30.3, 14.0);
        addFood("pizza", 266, 11.0, 33.0, 10.0);
        addFood("french_fries", 312, 3.4, 41.0, 15.0);
        
        // Proteins
        addFood("chicken", 165, 31.0, 0.0, 3.6);
        addFood("beef", 250, 26.0, 0.0, 17.0);
        addFood("fish", 206, 22.0, 0.0, 12.0);
        addFood("eggs", 155, 13.0, 1.1, 11.0);
        
        // Vegetables
        addFood("broccoli", 34, 2.8, 7.0, 0.4);
        addFood("carrot", 41, 0.9, 10.0, 0.2);
        addFood("tomato", 18, 0.9, 3.9, 0.2);
        addFood("salad", 15, 1.4, 2.9, 0.2);
        
        // Fruits
        addFood("apple", 52, 0.3, 14.0, 0.2);
        addFood("banana", 89, 1.1, 23.0, 0.3);
        addFood("orange", 47, 0.9, 12.0, 0.1);
        
        // Grains
        addFood("rice", 130, 2.7, 28.0, 0.3);
        addFood("bread", 265, 9.0, 49.0, 3.2);
        addFood("pasta", 158, 5.8, 31.0, 0.9);
        
        // Dairy
        addFood("milk", 42, 3.4, 5.0, 1.0);
        addFood("cheese", 402, 25.0, 1.3, 33.0);
        addFood("yogurt", 59, 3.5, 4.7, 3.3);
        
        // Snacks
        addFood("chips", 536, 7.0, 53.0, 34.0);
        addFood("popcorn", 375, 11.0, 74.0, 4.3);
        addFood("chocolate", 545, 4.9, 60.0, 31.0);
        
        // Beverages
        addFood("soda", 41, 0.0, 10.4, 0.0);
        addFood("juice", 45, 0.5, 10.8, 0.1);
        
        // Additional common items
        addFood("sandwich", 250, 12.0, 28.0, 9.0);
        addFood("burrito", 206, 6.8, 25.8, 9.3);
        addFood("taco", 217, 8.7, 20.7, 12.1);
        addFood("sushi", 150, 5.6, 30.0, 0.7);
        addFood("noodles", 138, 4.5, 25.0, 2.1);
    }

    private static void addFood(String name, double calories, double protein, double carbs, double fat) {
        FOOD_NUTRITION_DATABASE.put(name.toLowerCase(), new NutritionInfo(calories, protein, carbs, fat));
    }

    public static NutritionInfo getNutritionInfo(String foodName) {
        // Convert food name to lowercase and remove any special characters
        String normalizedName = foodName.toLowerCase().replaceAll("[^a-z0-9]", "");
        
        // Try exact match first
        NutritionInfo info = FOOD_NUTRITION_DATABASE.get(normalizedName);
        if (info != null) {
            return info;
        }
        
        // If no exact match, try to find partial matches
        for (Map.Entry<String, NutritionInfo> entry : FOOD_NUTRITION_DATABASE.entrySet()) {
            if (normalizedName.contains(entry.getKey()) || entry.getKey().contains(normalizedName)) {
                return entry.getValue();
            }
        }
        
        return null;
    }

    public static class NutritionInfo {
        private final double calories;
        private final double protein;
        private final double carbs;
        private final double fat;

        public NutritionInfo(double calories, double protein, double carbs, double fat) {
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
        }

        public double getCalories() { return calories; }
        public double getProtein() { return protein; }
        public double getCarbs() { return carbs; }
        public double getFat() { return fat; }
    }
} 