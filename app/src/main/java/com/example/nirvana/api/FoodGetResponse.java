// FoodGetResponse.java
package com.example.nirvana.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FoodGetResponse {
    @SerializedName("food")
    public Food food;

    public static class Food {
        @SerializedName("food_id")
        public String foodId;
        @SerializedName("food_name")
        public String foodName;
        @SerializedName("servings")
        public Servings servings;
    }

    public static class Servings {
        @SerializedName("serving")
        public List<Serving> servingList;
    }

    public static class Serving {
        @SerializedName("serving_id")
        public String servingId;
        @SerializedName("serving_description")
        public String description;
        @SerializedName("calories")
        public String calories;
        @SerializedName("protein")
        public String protein;
        @SerializedName("carbohydrate")
        public String carbs;
        @SerializedName("fat")
        public String fat;
    }
}