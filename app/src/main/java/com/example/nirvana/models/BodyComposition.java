package com.example.nirvana.models;

/**
 * Model class representing body composition analysis results
 */
public class BodyComposition {
    private double bodyFatPercentage;
    private double muscleMassPercentage;
    private double bmr; // Basal Metabolic Rate
    private double visceralFat;
    private String bodyType; // Ectomorph, Mesomorph, Endomorph
    
    /**
     * Constructor for body composition results
     */
    public BodyComposition(double bodyFatPercentage, double muscleMassPercentage, 
                          double bmr, double visceralFat, String bodyType) {
        this.bodyFatPercentage = bodyFatPercentage;
        this.muscleMassPercentage = muscleMassPercentage;
        this.bmr = bmr;
        this.visceralFat = visceralFat;
        this.bodyType = bodyType;
    }
    
    // Getters and setters
    
    public double getBodyFatPercentage() {
        return bodyFatPercentage;
    }
    
    public void setBodyFatPercentage(double bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }
    
    public double getMuscleMassPercentage() {
        return muscleMassPercentage;
    }
    
    public void setMuscleMassPercentage(double muscleMassPercentage) {
        this.muscleMassPercentage = muscleMassPercentage;
    }
    
    public double getBmr() {
        return bmr;
    }
    
    public void setBmr(double bmr) {
        this.bmr = bmr;
    }
    
    public double getVisceralFat() {
        return visceralFat;
    }
    
    public void setVisceralFat(double visceralFat) {
        this.visceralFat = visceralFat;
    }
    
    public String getBodyType() {
        return bodyType;
    }
    
    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }
    
    /**
     * Get body fat category based on percentage
     */
    public String getBodyFatCategory() {
        if (bodyFatPercentage < 10) {
            return "Very Low";
        } else if (bodyFatPercentage < 20) {
            return "Fit";
        } else if (bodyFatPercentage < 25) {
            return "Average";
        } else if (bodyFatPercentage < 30) {
            return "High";
        } else {
            return "Very High";
        }
    }
    
    /**
     * Calculate daily calorie needs based on BMR and activity level
     * @param activityLevel Activity level (1.2=sedentary, 1.375=light, 1.55=moderate, 1.725=very active, 1.9=extra active)
     * @return Estimated daily calorie needs for maintenance
     */
    public double calculateDailyCalorieNeeds(double activityLevel) {
        return bmr * activityLevel;
    }
    
    /**
     * Get body analysis summary as human-readable text
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Body Fat: ").append(String.format("%.1f", bodyFatPercentage)).append("% (")
               .append(getBodyFatCategory()).append(")\n");
        
        summary.append("Muscle Mass: ").append(String.format("%.1f", muscleMassPercentage)).append("%\n");
        
        summary.append("Body Type: ").append(bodyType).append("\n");
        
        summary.append("BMR: ").append(String.format("%.0f", bmr)).append(" calories/day\n");
        
        summary.append("Visceral Fat Level: ").append(String.format("%.1f", visceralFat)).append("\n");
        
        summary.append("Daily Calorie Needs:\n")
               .append("- Sedentary: ").append(String.format("%.0f", calculateDailyCalorieNeeds(1.2))).append(" cal\n")
               .append("- Moderate: ").append(String.format("%.0f", calculateDailyCalorieNeeds(1.55))).append(" cal\n")
               .append("- Very Active: ").append(String.format("%.0f", calculateDailyCalorieNeeds(1.725))).append(" cal\n");
        
        return summary.toString();
    }
} 