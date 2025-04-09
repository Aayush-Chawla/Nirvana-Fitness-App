package com.example.nirvana.services;

import android.content.Context;
import android.util.Log;

import com.example.nirvana.models.PredefinedFoodItem;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FoodItemService {
    private static final String TAG = "FoodItemService";
    private static List<PredefinedFoodItem> foodItems;

    public static List<PredefinedFoodItem> loadFoodItems(Context context) {
        if (foodItems != null && !foodItems.isEmpty()) {
            Log.d(TAG, "Returning cached food items: " + foodItems.size());
            return foodItems;
        }

        try {
            String jsonString = loadJSONFromAsset(context);
            if (jsonString == null || jsonString.isEmpty()) {
                Log.e(TAG, "JSON string is empty or null");
                // Add some default items for testing if JSON is empty
                foodItems = createDefaultItems();
                return foodItems;
            }
            
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
            if (!jsonObject.has("food_items")) {
                Log.e(TAG, "JSON missing 'food_items' key");
                foodItems = createDefaultItems();
                return foodItems;
            }
            
            Type listType = new TypeToken<List<PredefinedFoodItem>>() {}.getType();
            foodItems = gson.fromJson(jsonObject.get("food_items"), listType);
            
            Log.d(TAG, "Successfully loaded " + foodItems.size() + " food items");
            // Log first item as example
            if (!foodItems.isEmpty()) {
                Log.d(TAG, "First item: " + foodItems.get(0).toString());
            }
            
            return foodItems;
        } catch (Exception e) {
            Log.e(TAG, "Error loading food items: " + e.getMessage(), e);
            foodItems = createDefaultItems();
            return foodItems;
        }
    }

    private static String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("food_items.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            Log.d(TAG, "JSON loaded successfully, size: " + json.length() + " bytes");
            return json;
        } catch (IOException ex) {
            Log.e(TAG, "Error reading JSON file: " + ex.getMessage(), ex);
            return null;
        }
    }
    
    private static List<PredefinedFoodItem> createDefaultItems() {
        Log.d(TAG, "Creating default food items");
        List<PredefinedFoodItem> items = new ArrayList<>();
        
        // Create chicken breast
        PredefinedFoodItem chicken = new PredefinedFoodItem();
        chicken.setId("1");
        chicken.setName("Chicken Breast");
        chicken.setCategory("Protein");
        chicken.setServingSize(100);
        chicken.setServingUnit("g");
        chicken.setMaxQuantity(500);
        
        PredefinedFoodItem.NutritionPer100g chickenNutrition = new PredefinedFoodItem.NutritionPer100g();
        chickenNutrition.setCalories(165);
        chickenNutrition.setProtein(31);
        chickenNutrition.setCarbs(0);
        chickenNutrition.setFat(3.6);
        chicken.setPer100g(chickenNutrition);
        
        items.add(chicken);
        
        // Create rice
        PredefinedFoodItem rice = new PredefinedFoodItem();
        rice.setId("2");
        rice.setName("Brown Rice");
        rice.setCategory("Grains");
        rice.setServingSize(100);
        rice.setServingUnit("g");
        rice.setMaxQuantity(400);
        
        PredefinedFoodItem.NutritionPer100g riceNutrition = new PredefinedFoodItem.NutritionPer100g();
        riceNutrition.setCalories(111);
        riceNutrition.setProtein(2.6);
        riceNutrition.setCarbs(23);
        riceNutrition.setFat(0.9);
        rice.setPer100g(riceNutrition);
        
        items.add(rice);
        
        return items;
    }

    public static PredefinedFoodItem getFoodItemById(String id) {
        if (foodItems == null || foodItems.isEmpty()) return null;
        
        for (PredefinedFoodItem item : foodItems) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public static List<PredefinedFoodItem> getFoodItemsByCategory(String category) {
        if (foodItems == null || foodItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<PredefinedFoodItem> categoryItems = new ArrayList<>();
        for (PredefinedFoodItem item : foodItems) {
            if (item.getCategory().equals(category)) {
                categoryItems.add(item);
            }
        }
        return categoryItems;
    }
} 