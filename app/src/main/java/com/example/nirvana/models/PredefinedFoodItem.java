package com.example.nirvana.models;

import com.google.gson.annotations.SerializedName;

public class PredefinedFoodItem {
    private String id;
    private String name;
    private String category;
    
    @SerializedName("per_100g")
    private NutritionPer100g per100g;
    
    @SerializedName("serving_size")
    private int servingSize;
    
    @SerializedName("serving_unit")
    private String servingUnit;
    
    @SerializedName("max_quantity")
    private int maxQuantity;

    public static class NutritionPer100g {
        private double calories;
        private double protein;
        private double carbs;
        private double fat;

        public NutritionPer100g() {
            // Required empty constructor for Firebase
        }

        public double getCalories() { return calories; }
        public void setCalories(double calories) { this.calories = calories; }

        public double getProtein() { return protein; }
        public void setProtein(double protein) { this.protein = protein; }

        public double getCarbs() { return carbs; }
        public void setCarbs(double carbs) { this.carbs = carbs; }

        public double getFat() { return fat; }
        public void setFat(double fat) { this.fat = fat; }
        
        @Override
        public String toString() {
            return String.format("NutritionPer100g{calories=%.1f, protein=%.1f, carbs=%.1f, fat=%.1f}",
                calories, protein, carbs, fat);
        }
    }

    public PredefinedFoodItem() {
        // Required empty constructor for Firebase
    }

    /**
     * Constructor for creating a food item with all necessary nutritional information
     * @param name Food name
     * @param calories Calories per 100g
     * @param carbs Carbohydrates per 100g
     * @param protein Protein per 100g
     * @param fat Fat per 100g
     * @param servingSize Standard serving size
     * @param servingUnit Unit for serving (g, ml, etc)
     */
    public PredefinedFoodItem(String name, double calories, double carbs, double protein, double fat, 
                             int servingSize, String servingUnit) {
        this.name = name;
        this.servingSize = servingSize;
        this.servingUnit = servingUnit;
        this.maxQuantity = 10; // Default max quantity
        this.category = "Other"; // Default category
        
        // Create the per100g nutritional values
        NutritionPer100g nutrition = new NutritionPer100g();
        nutrition.setCalories(calories);
        nutrition.setCarbs(carbs);
        nutrition.setProtein(protein);
        nutrition.setFat(fat);
        this.per100g = nutrition;
    }

    // Calculate nutrition values based on quantity in grams
    public double calculateCalories(int quantity) {
        if (per100g == null) {
            return 0.0;
        }
        return (per100g.getCalories() * quantity) / 100.0;
    }

    public double calculateProtein(int quantity) {
        if (per100g == null) {
            return 0.0;
        }
        return (per100g.getProtein() * quantity) / 100.0;
    }

    public double calculateCarbs(int quantity) {
        if (per100g == null) {
            return 0.0;
        }
        return (per100g.getCarbs() * quantity) / 100.0;
    }

    public double calculateFat(int quantity) {
        if (per100g == null) {
            return 0.0;
        }
        return (per100g.getFat() * quantity) / 100.0;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public NutritionPer100g getPer100g() { return per100g; }
    public void setPer100g(NutritionPer100g per100g) { this.per100g = per100g; }

    public int getServingSize() { return servingSize; }
    public void setServingSize(int servingSize) { this.servingSize = servingSize; }

    public String getServingUnit() { return servingUnit; }
    public void setServingUnit(String servingUnit) { this.servingUnit = servingUnit; }

    public int getMaxQuantity() { return maxQuantity; }
    public void setMaxQuantity(int maxQuantity) { this.maxQuantity = maxQuantity; }
    
    @Override
    public String toString() {
        return String.format("PredefinedFoodItem{id='%s', name='%s', category='%s', servingSize=%d, servingUnit='%s', maxQuantity=%d, per100g=%s}",
            id, name, category, servingSize, servingUnit, maxQuantity, per100g);
    }
} 