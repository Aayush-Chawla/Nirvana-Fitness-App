package com.example.nirvana.models;

public class ChatMessage {
    private String message;
    private boolean isUser; // true if message is from user, false if from bot
    private long timestamp;
    
    public ChatMessage() {
        // Required empty constructor for Firebase
        this.timestamp = System.currentTimeMillis();
    }
    
    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isUser() {
        return isUser;
    }
    
    public void setUser(boolean user) {
        isUser = user;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 