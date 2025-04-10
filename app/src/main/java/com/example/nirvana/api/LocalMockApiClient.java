package com.example.nirvana.api;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.nirvana.models.FoodItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Client to access locally stored mock data for development and testing
 */
public class LocalMockApiClient {
    private static final String TAG = "LocalMockApiClient";
    private final Context context;

    public LocalMockApiClient(Context context) {
        this.context = context;
    }

    /**
     * Get list of food items from local mock data
     * @return List of food items
     */
    public List<FoodItem> getFoodItems() {
        List<FoodItem> foodItems = new ArrayList<>();
        
        try {
            String jsonData = loadJsonFromAsset("food_data.json");
            if (jsonData != null) {
                JSONArray jsonArray = new JSONArray(jsonData);
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    
                    FoodItem foodItem = new FoodItem(
                            item.getString("id"),
                            item.getString("name"),
                            item.getInt("calories"),
                            item.getString("servingSize"),
                            item.getDouble("protein"),
                            item.getDouble("carbs"),
                            item.getDouble("fat")
                    );
                    
                    foodItems.add(foodItem);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing food data JSON", e);
        }
        
        // If no data or error, return sample data
        if (foodItems.isEmpty()) {
            foodItems = getSampleFoodItems();
        }
        
        return foodItems;
    }
    
    /**
     * Search food items by name
     * @param query Search query
     * @return List of matching food items
     */
    public List<FoodItem> searchFoodItems(String query) {
        List<FoodItem> allItems = getFoodItems();
        List<FoodItem> results = new ArrayList<>();
        
        String lowerQuery = query.toLowerCase();
        
        for (FoodItem item : allItems) {
            if (item.getName().toLowerCase().contains(lowerQuery)) {
                results.add(item);
            }
        }
        
        return results;
    }
    
    /**
     * Load JSON data from an asset file
     * @param fileName Name of the asset file
     * @return String containing the JSON data
     */
    private String loadJsonFromAsset(String fileName) {
        StringBuilder json = new StringBuilder();
        
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                json.append(line);
            }
            
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error loading JSON from asset: " + fileName, e);
            return null;
        }
        
        return json.toString();
    }
    
    /**
     * Get sample food items for testing
     * @return List of sample food items
     */
    private List<FoodItem> getSampleFoodItems() {
        List<FoodItem> samples = new ArrayList<>();
        
        samples.add(new FoodItem("1", "Greek Yogurt", 150, "100g", 15, 8, 5));
        samples.add(new FoodItem("2", "Chicken Breast", 165, "100g", 31, 0, 3.6));
        samples.add(new FoodItem("3", "Brown Rice", 112, "100g", 2.6, 23, 0.9));
        samples.add(new FoodItem("4", "Banana", 89, "1 medium", 1.1, 23, 0.3));
        samples.add(new FoodItem("5", "Eggs", 155, "100g", 13, 1.1, 11));
        samples.add(new FoodItem("6", "Salmon", 208, "100g", 20, 0, 13));
        samples.add(new FoodItem("7", "Avocado", 160, "100g", 2, 8.5, 14.7));
        samples.add(new FoodItem("8", "Sweet Potato", 86, "100g", 1.6, 20, 0.1));
        samples.add(new FoodItem("9", "Oats", 389, "100g", 16.9, 66, 6.9));
        samples.add(new FoodItem("10", "Spinach", 23, "100g", 2.9, 3.6, 0.4));
        
        return samples;
    }
} 