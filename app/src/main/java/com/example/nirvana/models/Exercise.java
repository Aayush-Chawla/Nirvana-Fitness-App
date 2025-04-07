package com.example.nirvana.models;

import java.io.Serializable;

public class Exercise implements Serializable {
    private String id;
    private String name;
    private String description;
    private String category;
    private String difficulty;
    private int duration;
    private String imageUrl;
    private String videoUrl;
    private String historyId;

    public Exercise() {
        // Required empty constructor for Firebase
    }

    public Exercise(String id, String name, String description, String category, 
                   String difficulty, int duration, String imageUrl, String videoUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }
} 