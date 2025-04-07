package com.example.nirvana.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class BlogPost {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("content")
    private String content;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("author")
    private String author;

    @SerializedName("published_at")
    private String publishedAt;

    @SerializedName("category")
    private String category;

    public BlogPost() {
        // Required empty constructor for Firebase
    }

    public BlogPost(String id, String title, String description, String author, String imageUrl, String publishedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
} 