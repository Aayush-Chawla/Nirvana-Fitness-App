package com.example.nirvana.models;

/**
 * Model class for user profile data
 */
public class UserProfile {
    private String userId;
    private String name;
    private int age;
    private String gender;
    private float weight; // in kg
    private float height; // in cm
    private String fitnessLevel; // Beginner, Intermediate, Advanced
    private String fitnessGoal; // Weight Loss, Muscle Gain, etc.
    
    public UserProfile() {
        // Required empty constructor for Firebase
    }
    
    public UserProfile(String userId, String name, int age, String gender, float weight, float height, 
                      String fitnessLevel, String fitnessGoal) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.fitnessLevel = fitnessLevel;
        this.fitnessGoal = fitnessGoal;
    }
    
    // Getters and setters
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public float getWeight() {
        return weight;
    }
    
    public void setWeight(float weight) {
        this.weight = weight;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    public String getFitnessLevel() {
        return fitnessLevel;
    }
    
    public void setFitnessLevel(String fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
    }
    
    public String getFitnessGoal() {
        return fitnessGoal;
    }
    
    public void setFitnessGoal(String fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }
    
    /**
     * Calculate Body Mass Index (BMI)
     * @return BMI value
     */
    public float calculateBMI() {
        if (height <= 0) return 0;
        float heightInMeters = height / 100f;
        return weight / (heightInMeters * heightInMeters);
    }
    
    /**
     * Get BMI category
     * @return BMI category as string
     */
    public String getBMICategory() {
        float bmi = calculateBMI();
        if (bmi < 18.5f) {
            return "Underweight";
        } else if (bmi < 25f) {
            return "Normal";
        } else if (bmi < 30f) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
    
    /**
     * Calculate estimated daily calorie needs
     * @param activityMultiplier Activity level multiplier (1.2=sedentary, 1.375=light, 1.55=moderate, 1.725=very active, 1.9=extra active)
     * @return Estimated daily calorie needs
     */
    public double calculateCalorieNeeds(double activityMultiplier) {
        // Mifflin-St Jeor Equation
        double bmr;
        if (gender.equalsIgnoreCase("male")) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }
        return bmr * activityMultiplier;
    }
} 