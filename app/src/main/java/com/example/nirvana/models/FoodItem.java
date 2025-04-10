package com.example.nirvana.models;

public class FoodItem {
    private String id;
    private String name;
    private int calories;
    private String servingSize;
    private double protein;
    private double carbs;
    private double fat;
    private String docId;

    public FoodItem() {
        // Required empty constructor for Firebase
    }

    public FoodItem(String id, String name, int calories, String servingSize, double protein, double carbs, double fat) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.servingSize = servingSize;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public String getServingSize() { return servingSize; }
    public void setServingSize(String servingSize) { this.servingSize = servingSize; }
    
    // Helper method to get numeric value from serving size
    public float getServingSizeValue() {
        if (servingSize == null || servingSize.isEmpty()) {
            return 100f; // Default to 100g
        }
        
        try {
            String numericPart = servingSize.replaceAll("[^0-9.]", "");
            if (!numericPart.isEmpty()) {
                return Float.parseFloat(numericPart);
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return default
        }
        
        return 100f; // Default to 100g if parsing fails
    }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
    
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
} 