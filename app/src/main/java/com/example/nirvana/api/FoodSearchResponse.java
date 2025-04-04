package com.example.nirvana.api;

import java.util.List;

public class FoodSearchResponse {
    public List<FoodItem> foods;

    public static class FoodItem {
        private String id;
        private String name;
        private int calories;
        private String servingSize;
        private double protein;
        private double carbs;
        private double fat;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }

        public String getServingSize() { return servingSize; }
        public void setServingSize(String servingSize) { this.servingSize = servingSize; }

        public double getProtein() { return protein; }
        public void setProtein(double protein) { this.protein = protein; }

        public double getCarbs() { return carbs; }
        public void setCarbs(double carbs) { this.carbs = carbs; }

        public double getFat() { return fat; }
        public void setFat(double fat) { this.fat = fat; }
    }
}