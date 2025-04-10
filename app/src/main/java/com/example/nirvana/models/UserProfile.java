package com.example.nirvana.models;

/**
 * Model class representing a user's profile information
 */
public class UserProfile {
    private String userId;
    private String name;
    private int age;
    private String gender;
    private float weight; // kg
    private float height; // cm
    private String fitnessLevel; // "Beginner", "Intermediate", "Advanced"
    private String fitnessGoal; // "Weight Loss", "Muscle Gain", "General Fitness", etc.

    public UserProfile() {
        // Required empty constructor for Firebase
    }

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
} 