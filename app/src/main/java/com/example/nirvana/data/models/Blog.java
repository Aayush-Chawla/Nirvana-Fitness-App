package com.example.nirvana.data.models;

public class Blog {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String author;
    private String date;
    private String content;

    public Blog(String id, String title, String description, String imageUrl, String author, String date, String content) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.author = author;
        this.date = date;
        this.content = content;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getAuthor() { return author; }
    public String getDate() { return date; }
    public String getContent() { return content; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setAuthor(String author) { this.author = author; }
    public void setDate(String date) { this.date = date; }
    public void setContent(String content) { this.content = content; }
} 