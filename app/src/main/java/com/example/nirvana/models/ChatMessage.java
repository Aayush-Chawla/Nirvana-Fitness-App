package com.example.nirvana.models;

import java.util.Date;

public class ChatMessage {
    private String message;
    private boolean isUserMessage;
    private Date timestamp;
    
    // Empty constructor for Firebase
    public ChatMessage() {
        // Required empty constructor for Firestore
    }
    
    public ChatMessage(String message, boolean isUserMessage) {
        this.message = message;
        this.isUserMessage = isUserMessage;
        this.timestamp = new Date();
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isUserMessage() {
        return isUserMessage;
    }
    
    public void setUserMessage(boolean userMessage) {
        isUserMessage = userMessage;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
} 