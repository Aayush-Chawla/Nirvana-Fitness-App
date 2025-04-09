package com.example.nirvana.services;

import android.util.Log;
import com.example.nirvana.data.models.FoodItem;
import com.example.nirvana.models.PredefinedFoodItem;
import java.util.*;

/**
 * Service to generate food recommendations based on user nutritional data
 */
public class RecommendationService {
    private static final String TAG = "RecommendationService";
    
    // Weights for different factors in recommendation
    private static final double FREQUENCY_WEIGHT = 0.4;
    private static final double TIME_WEIGHT = 0.3;
    private static final double NUTRITION_WEIGHT = 0.3;
    
    // Time windows for meals (24-hour format)
    private static final Map<String, int[]> MEAL_TIMES = new HashMap<String, int[]>() {{
        put("Breakfast", new int[]{6, 11});  // 6 AM - 11 AM
        put("Lunch", new int[]{11, 15});     // 11 AM - 3 PM
        put("Dinner", new int[]{17, 22});    // 5 PM - 10 PM
        put("Snacks", new int[]{0, 23});     // Any time
    }};
    
    /**
     * Get food recommendations based on user's recent food items, meal type, and remaining calories
     * 
     * @param recentFoodItems List of recently consumed food item names
     * @param mealType Current meal type (Breakfast, Lunch, Dinner, Snack)
     * @param remainingCalories Remaining calories for the day
     * @param predefinedItems List of all available predefined food items
     * @return A filtered list of recommended food items
     */
    public static List<PredefinedFoodItem> getRecommendations(
            List<String> recentFoodItems, 
            String mealType, 
            double remainingCalories,
            List<PredefinedFoodItem> predefinedItems) {
        
        Log.d(TAG, "Generating recommendations for meal: " + mealType + 
              ", remaining calories: " + remainingCalories);
        
        List<PredefinedFoodItem> recommendations = new ArrayList<>();
        
        // Return empty list if no predefined items or negative remaining calories
        if (predefinedItems == null || predefinedItems.isEmpty() || remainingCalories <= 0) {
            Log.d(TAG, "No recommendations possible: empty items or no remaining calories");
            return recommendations;
        }
        
        // Filter by meal type and calories
        for (PredefinedFoodItem item : predefinedItems) {
            // Skip if the item has been recently consumed
            if (recentFoodItems != null && recentFoodItems.contains(item.getName())) {
                continue;
            }
            
            // Calculate calories for one serving
            double itemCalories = item.calculateCalories(item.getServingSize());
            
            // Add if the item fits within remaining calories and is appropriate for the meal type
            if (itemCalories <= remainingCalories && isSuitableForMealType(item, mealType)) {
                recommendations.add(item);
            }
        }
        
        // Limit to 10 recommendations maximum
        if (recommendations.size() > 10) {
            recommendations = recommendations.subList(0, 10);
        }
        
        Log.d(TAG, "Generated " + recommendations.size() + " recommendations");
        return recommendations;
    }
    
    /**
     * Check if a food item is suitable for the given meal type
     */
    private static boolean isSuitableForMealType(PredefinedFoodItem item, String mealType) {
        if (mealType == null) return true;
        
        String category = item.getCategory();
        if (category == null) return true;
        
        category = category.toLowerCase();
        mealType = mealType.toLowerCase();
        
        switch (mealType) {
            case "breakfast":
                return category.contains("breakfast") || 
                       category.contains("grain") || 
                       category.contains("dairy") || 
                       category.contains("fruit");
            case "lunch":
            case "dinner":
                return category.contains("protein") || 
                       category.contains("vegetable") || 
                       category.contains("grain");
            case "snack":
                return category.contains("snack") || 
                       category.contains("fruit") || 
                       category.contains("nut");
            default:
                return true;
        }
    }
    
    private static Map<String, Integer> calculateFoodFrequency(List<FoodItem> recentFoodItems) {
        Map<String, Integer> frequency = new HashMap<>();
        for (FoodItem item : recentFoodItems) {
            frequency.merge(item.getName(), 1, Integer::sum);
        }
        return frequency;
    }
    
    private static boolean isAppropriateTime(String mealType, int currentHour) {
        int[] timeWindow = MEAL_TIMES.get(mealType);
        if (timeWindow == null) return true;
        return currentHour >= timeWindow[0] && currentHour <= timeWindow[1];
    }
    
    private static double calculateNutritionScore(PredefinedFoodItem food, int targetCalories) {
        if (targetCalories <= 0) return 1.0;
        
        // Calculate how well the food's calories fit into the target
        double calorieRatio = food.calculateCalories(food.getServingSize()) / targetCalories;
        
        // Score is highest (1.0) when the food is 15-30% of target calories
        if (calorieRatio >= 0.15 && calorieRatio <= 0.3) {
            return 1.0;
        } else if (calorieRatio < 0.15) {
            return calorieRatio / 0.15; // Score decreases for too few calories
        } else {
            return Math.max(0, 1 - ((calorieRatio - 0.3) * 2)); // Score decreases for too many calories
        }
    }
    
    private static List<PredefinedFoodItem> getTopRecommendations(
            Map<PredefinedFoodItem, Double> foodScores, int limit) {
        return foodScores.entrySet().stream()
                .sorted(Map.Entry.<PredefinedFoodItem, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }
} 