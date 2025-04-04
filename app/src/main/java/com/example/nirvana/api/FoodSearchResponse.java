package com.example.nirvana.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FoodSearchResponse {
    @SerializedName("foods")
    public List<FoodItem> foods;

    public static class FoodItem {
        @SerializedName("food_id")
        public String food_id;

        @SerializedName("food_name")
        public String food_name;

        @SerializedName("food_description")
        public String food_description;

        @SerializedName("calories")
        public int calories;

        @SerializedName("protein")
        public float protein;

        @SerializedName("carbohydrate")
        public float carbs;

        @SerializedName("fat")
        public float fat;

        // Getter methods
        public String getName() {
            return food_name;
        }

        public int getCalories() {
            return calories;
        }

        public float getProtein() {
            return protein;
        }

        public float getCarbs() {
            return carbs;
        }

        public float getFat() {
            return fat;
        }
    }
}