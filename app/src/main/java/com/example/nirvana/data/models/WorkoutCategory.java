package com.example.nirvana.data.models;

import com.google.gson.annotations.SerializedName;

public class WorkoutCategory {
    @SerializedName("name")
    private String name;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("description")
    private String description;
    
    private int imageResource; // Used for local resources

    public WorkoutCategory() {
        // Required for Gson
    }

    public WorkoutCategory(String name, int imageResource, String description) {
        this.name = name;
        this.imageResource = imageResource;
        this.description = description;
    }
    
    public WorkoutCategory(String name, String imageUrl, String description) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
