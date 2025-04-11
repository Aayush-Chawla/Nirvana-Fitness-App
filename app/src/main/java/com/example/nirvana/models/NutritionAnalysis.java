package com.example.nirvana.models;

import java.util.ArrayList;
import java.util.List;

public class NutritionAnalysis {
    private double averageCalories;
    private double averageProtein;
    private double averageCarbs;
    private double averageFat;
    
    private double calorieGoal;
    private double proteinGoal;
    private double carbsGoal;
    private double fatGoal;
    
    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    
    private double recommendedCalories;
    private double recommendedProtein;
    private double recommendedCarbs;
    private double recommendedFat;
    
    private List<Double> dailyCalories;
    private List<Double> dailyProtein;
    private List<Double> dailyCarbs;
    private List<Double> dailyFat;
    
    private String calorieStatus;
    private String proteinStatus;
    private String carbsStatus;
    private String fatStatus;
    
    private String[] recommendations;

    public NutritionAnalysis() {
        // Initialize lists
        dailyCalories = new ArrayList<>();
        dailyProtein = new ArrayList<>();
        dailyCarbs = new ArrayList<>();
        dailyFat = new ArrayList<>();
        
        // Set default goals
        calorieGoal = 2000;
        proteinGoal = 150;
        carbsGoal = 250;
        fatGoal = 70;
        
        // Set recommended values same as goals initially
        recommendedCalories = calorieGoal;
        recommendedProtein = proteinGoal;
        recommendedCarbs = carbsGoal;
        recommendedFat = fatGoal;
        
        // Initialize totals and averages
        totalCalories = 0;
        totalProtein = 0;
        totalCarbs = 0;
        totalFat = 0;
        averageCalories = 0;
        averageProtein = 0;
        averageCarbs = 0;
        averageFat = 0;
        
        // Initialize status
        calorieStatus = "Low";
        proteinStatus = "Low";
        carbsStatus = "Low";
        fatStatus = "Low";
    }

    // Getters for average values
    public double getAverageCalories() {
        return averageCalories;
    }

    public double getAverageProtein() {
        return averageProtein;
    }

    public double getAverageCarbs() {
        return averageCarbs;
    }

    public double getAverageFat() {
        return averageFat;
    }

    // Getters for goals
    public double getCalorieGoal() {
        return calorieGoal;
    }

    public double getProteinGoal() {
        return proteinGoal;
    }

    public double getCarbsGoal() {
        return carbsGoal;
    }

    public double getFatGoal() {
        return fatGoal;
    }

    // Getters for recommended values
    public double getRecommendedCalories() {
        return recommendedCalories;
    }

    public double getRecommendedProtein() {
        return recommendedProtein;
    }

    public double getRecommendedCarbs() {
        return recommendedCarbs;
    }

    public double getRecommendedFat() {
        return recommendedFat;
    }

    // Setters for recommended values
    public void setRecommendedCalories(double recommendedCalories) {
        this.recommendedCalories = recommendedCalories;
        updateCalorieStatus();
    }

    public void setRecommendedProtein(double recommendedProtein) {
        this.recommendedProtein = recommendedProtein;
        updateProteinStatus();
    }

    public void setRecommendedCarbs(double recommendedCarbs) {
        this.recommendedCarbs = recommendedCarbs;
        updateCarbsStatus();
    }

    public void setRecommendedFat(double recommendedFat) {
        this.recommendedFat = recommendedFat;
        updateFatStatus();
    }

    // Getters for total values
    public double getTotalCalories() {
        return totalCalories;
    }

    public double getTotalProtein() {
        return totalProtein;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public double getTotalFat() {
        return totalFat;
    }

    // Setters for total values
    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
        updateAverages();
    }

    public void setTotalProtein(double totalProtein) {
        this.totalProtein = totalProtein;
        updateAverages();
    }

    public void setTotalCarbs(double totalCarbs) {
        this.totalCarbs = totalCarbs;
        updateAverages();
    }

    public void setTotalFat(double totalFat) {
        this.totalFat = totalFat;
        updateAverages();
    }

    // Getters for daily values
    public double getDailyCalories(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < dailyCalories.size()) {
            return dailyCalories.get(dayIndex);
        }
        return 0.0;
    }

    public double getDailyProtein(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < dailyProtein.size()) {
            return dailyProtein.get(dayIndex);
        }
        return 0.0;
    }

    public double getDailyCarbs(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < dailyCarbs.size()) {
            return dailyCarbs.get(dayIndex);
        }
        return 0.0;
    }

    public double getDailyFat(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < dailyFat.size()) {
            return dailyFat.get(dayIndex);
        }
        return 0.0;
    }

    // Add getters for daily value lists
    public List<Double> getDailyCalories() {
        return dailyCalories;
    }

    public List<Double> getDailyProtein() {
        return dailyProtein;
    }

    public List<Double> getDailyCarbs() {
        return dailyCarbs;
    }

    public List<Double> getDailyFat() {
        return dailyFat;
    }

    // Getters for status
    public String getCalorieStatus() {
        return calorieStatus;
    }

    public String getProteinStatus() {
        return proteinStatus;
    }

    public String getCarbsStatus() {
        return carbsStatus;
    }

    public String getFatStatus() {
        return fatStatus;
    }

    // Getter for recommendations
    public String[] getRecommendations() {
        return recommendations;
    }

    // Setters for averages
    public void setAverageCalories(double averageCalories) {
        this.averageCalories = averageCalories;
        updateCalorieStatus();
    }

    public void setAverageProtein(double averageProtein) {
        this.averageProtein = averageProtein;
        updateProteinStatus();
    }

    public void setAverageCarbs(double averageCarbs) {
        this.averageCarbs = averageCarbs;
        updateCarbsStatus();
    }

    public void setAverageFat(double averageFat) {
        this.averageFat = averageFat;
        updateFatStatus();
    }

    // Setters for goals
    public void setCalorieGoal(double calorieGoal) {
        this.calorieGoal = calorieGoal;
        updateCalorieStatus();
    }

    public void setProteinGoal(double proteinGoal) {
        this.proteinGoal = proteinGoal;
        updateProteinStatus();
    }

    public void setCarbsGoal(double carbsGoal) {
        this.carbsGoal = carbsGoal;
        updateCarbsStatus();
    }

    public void setFatGoal(double fatGoal) {
        this.fatGoal = fatGoal;
        updateFatStatus();
    }

    public void addDailyValues(double calories, double protein, double carbs, double fat) {
        dailyCalories.add(calories);
        dailyProtein.add(protein);
        dailyCarbs.add(carbs);
        dailyFat.add(fat);
        
        // Keep only last 7 days
        if (dailyCalories.size() > 7) {
            dailyCalories.remove(0);
            dailyProtein.remove(0);
            dailyCarbs.remove(0);
            dailyFat.remove(0);
        }
        
        // Update totals and averages
        updateTotals();
        updateAverages();
    }

    public void setRecommendations(String[] recommendations) {
        this.recommendations = recommendations;
    }

    // Private helper methods to update status
    private void updateCalorieStatus() {
        double percentage = (averageCalories / recommendedCalories) * 100;
        if (percentage < 90) {
            calorieStatus = "Low";
        } else if (percentage > 110) {
            calorieStatus = "High";
        } else {
            calorieStatus = "Optimal";
        }
    }

    private void updateProteinStatus() {
        double percentage = (averageProtein / recommendedProtein) * 100;
        if (percentage < 90) {
            proteinStatus = "Low";
        } else if (percentage > 110) {
            proteinStatus = "High";
        } else {
            proteinStatus = "Optimal";
        }
    }

    private void updateCarbsStatus() {
        double percentage = (averageCarbs / recommendedCarbs) * 100;
        if (percentage < 90) {
            carbsStatus = "Low";
        } else if (percentage > 110) {
            carbsStatus = "High";
        } else {
            carbsStatus = "Optimal";
        }
    }

    private void updateFatStatus() {
        double percentage = (averageFat / recommendedFat) * 100;
        if (percentage < 90) {
            fatStatus = "Low";
        } else if (percentage > 110) {
            fatStatus = "High";
        } else {
            fatStatus = "Optimal";
        }
    }

    private void updateTotals() {
        totalCalories = sum(dailyCalories);
        totalProtein = sum(dailyProtein);
        totalCarbs = sum(dailyCarbs);
        totalFat = sum(dailyFat);
    }

    private void updateAverages() {
        averageCalories = calculateAverage(dailyCalories);
        averageProtein = calculateAverage(dailyProtein);
        averageCarbs = calculateAverage(dailyCarbs);
        averageFat = calculateAverage(dailyFat);
        
        // Update all statuses
        updateCalorieStatus();
        updateProteinStatus();
        updateCarbsStatus();
        updateFatStatus();
    }

    private double calculateAverage(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return sum(values) / values.size();
    }

    private double sum(List<Double> values) {
        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }
        return sum;
    }
} 