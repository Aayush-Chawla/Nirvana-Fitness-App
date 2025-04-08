package com.example.nirvana.models;

public class NutritionAnalysis {
    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    private double averageCalories;
    private double averageProtein;
    private double averageCarbs;
    private double averageFat;
    private double recommendedCalories;
    private double recommendedProtein;
    private double recommendedCarbs;
    private double recommendedFat;
    private String calorieStatus;
    private String proteinStatus;
    private String carbsStatus;
    private String fatStatus;
    private String[] recommendations;

    public NutritionAnalysis() {
        // Default constructor
    }

    // Getters and setters
    public double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(double totalCalories) { this.totalCalories = totalCalories; }

    public double getTotalProtein() { return totalProtein; }
    public void setTotalProtein(double totalProtein) { this.totalProtein = totalProtein; }

    public double getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(double totalCarbs) { this.totalCarbs = totalCarbs; }

    public double getTotalFat() { return totalFat; }
    public void setTotalFat(double totalFat) { this.totalFat = totalFat; }

    public double getAverageCalories() { return averageCalories; }
    public void setAverageCalories(double averageCalories) { this.averageCalories = averageCalories; }

    public double getAverageProtein() { return averageProtein; }
    public void setAverageProtein(double averageProtein) { this.averageProtein = averageProtein; }

    public double getAverageCarbs() { return averageCarbs; }
    public void setAverageCarbs(double averageCarbs) { this.averageCarbs = averageCarbs; }

    public double getAverageFat() { return averageFat; }
    public void setAverageFat(double averageFat) { this.averageFat = averageFat; }

    public double getRecommendedCalories() { return recommendedCalories; }
    public void setRecommendedCalories(double recommendedCalories) { this.recommendedCalories = recommendedCalories; }

    public double getRecommendedProtein() { return recommendedProtein; }
    public void setRecommendedProtein(double recommendedProtein) { this.recommendedProtein = recommendedProtein; }

    public double getRecommendedCarbs() { return recommendedCarbs; }
    public void setRecommendedCarbs(double recommendedCarbs) { this.recommendedCarbs = recommendedCarbs; }

    public double getRecommendedFat() { return recommendedFat; }
    public void setRecommendedFat(double recommendedFat) { this.recommendedFat = recommendedFat; }

    public String getCalorieStatus() { return calorieStatus; }
    public void setCalorieStatus(String calorieStatus) { this.calorieStatus = calorieStatus; }

    public String getProteinStatus() { return proteinStatus; }
    public void setProteinStatus(String proteinStatus) { this.proteinStatus = proteinStatus; }

    public String getCarbsStatus() { return carbsStatus; }
    public void setCarbsStatus(String carbsStatus) { this.carbsStatus = carbsStatus; }

    public String getFatStatus() { return fatStatus; }
    public void setFatStatus(String fatStatus) { this.fatStatus = fatStatus; }

    public String[] getRecommendations() { return recommendations; }
    public void setRecommendations(String[] recommendations) { this.recommendations = recommendations; }
} 