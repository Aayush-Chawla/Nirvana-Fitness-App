package com.example.nirvana.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.adapters.ChatAdapter;
import com.example.nirvana.models.ChatMessage;
import com.example.nirvana.services.GeminiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotActivity extends AppCompatActivity {
    private static final String TAG = "ChatbotActivity";
    private static final String BASIC_SYSTEM_PROMPT = "You are a fitness assistant. Keep responses brief.";
    private static final int MAX_RETRIES = 2;

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    
    private GeminiService geminiService;
    private DatabaseReference userRef;
    private Map<String, Object> userProfile = new HashMap<>();
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Fitness Assistant");

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Setup RecyclerView
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
        
        // Initialize Gemini Service
        try {
            geminiService = new GeminiService(this);
            Log.d(TAG, "GeminiService initialized successfully");
            
            // Test message - send directly to the service to verify it's working
            testGeminiService();
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing GeminiService", e);
            Toast.makeText(this, "AI assistant features may be limited", Toast.LENGTH_SHORT).show();
        }
        
        // Initialize Firebase reference if user is logged in
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            loadUserProfile();
        } catch (Exception e) {
            Log.w(TAG, "User not logged in or Firebase error: " + e.getMessage());
            // Continue without Firebase - chatbot will still work
        }

        // Add welcome message
        addBotMessage("Hello! I'm your fitness assistant. How can I help you today?");

        // Setup send button
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText("");
                
                // Disable the send button temporarily to prevent multiple quick requests
                sendButton.setEnabled(false);
                sendButton.postDelayed(() -> sendButton.setEnabled(true), 1000);
            }
        });

        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void testGeminiService() {
        String testPrompt = "Say hello";
        Log.d(TAG, "Testing Gemini API with prompt: " + testPrompt);
        
        geminiService.generateContent(testPrompt, new GeminiService.GeminiResponseCallback() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Gemini API test successful! Response: " + response);
                Toast.makeText(ChatbotActivity.this, "AI connection successful!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Gemini API test failed: " + errorMessage);
                Toast.makeText(ChatbotActivity.this, "AI connection issues: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadUserProfile() {
        if (userRef == null) return;
        
        userRef.child("profile").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for (com.google.firebase.database.DataSnapshot child : dataSnapshot.getChildren()) {
                    userProfile.put(child.getKey(), child.getValue());
                }
                Log.d(TAG, "Loaded user profile with " + userProfile.size() + " fields");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading user profile", e);
        });
    }

    private void sendMessage(String message) {
        retryCount = 0;
        // Add user message to chat
        addUserMessage(message);
        
        // Just try with the raw message first - that's the simplest approach
        sendPromptToGemini(message, message);
    }
    
    private void sendPromptToGemini(String prompt, String originalMessage) {
        geminiService.generateContent(prompt, new GeminiService.GeminiResponseCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    addBotMessage(response);
                    
                    // Save chat history if Firebase is available
                    saveChatHistory(originalMessage, response);
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Gemini API error: " + errorMessage);
                    
                    // Retry with simpler prompt if this was a complex one
                    if (retryCount < MAX_RETRIES) {
                        retryCount++;
                        Log.d(TAG, "Retry attempt " + retryCount);
                        
                        // Just use progressively shorter versions of the original message
                        String retryPrompt = originalMessage;
                        if (retryPrompt.length() > 20 * retryCount) {
                            retryPrompt = retryPrompt.substring(0, Math.min(retryPrompt.length(), 20 * (MAX_RETRIES - retryCount + 1)));
                        }
                        
                        // Try again with simpler prompt
                        sendPromptToGemini(retryPrompt, originalMessage);
                    } else {
                        // If all retries failed, use fallback response
                        addBotMessage("I'm having trouble connecting to my knowledge base right now. Here's a basic response:");
                        fallbackResponse(originalMessage);
                    }
                });
            }
        });
    }

    private void fallbackResponse(String userMessage) {
        // This is a fallback if Gemini API fails
        String response;
        userMessage = userMessage.toLowerCase();
        
        if (userMessage.contains("workout") || userMessage.contains("exercise") || userMessage.contains("training")) {
            response = "For a balanced workout routine, aim for 3-5 sessions per week including cardio, strength training, and flexibility exercises. Start with 30-minute sessions and gradually increase as your fitness improves.";
        } else if (userMessage.contains("diet") || userMessage.contains("nutrition") || userMessage.contains("food") || userMessage.contains("eat")) {
            response = "A balanced diet should include proteins (lean meats, fish, beans), complex carbs (whole grains, vegetables), and healthy fats (avocados, nuts, olive oil). Try to eat 3-5 servings of vegetables and 2-3 servings of fruit daily.";
        } else if (userMessage.contains("hello") || userMessage.contains("hi") || userMessage.contains("hey")) {
            response = "Hello! How can I assist you with your fitness journey today? Feel free to ask about workouts, nutrition plans, or general fitness advice.";
        } else if (userMessage.contains("weight") || userMessage.contains("lose") || userMessage.contains("gain") || userMessage.contains("fat")) {
            response = "Healthy weight management combines regular exercise with proper nutrition. For weight loss, aim for a calorie deficit through both diet and exercise. For muscle gain, incorporate strength training and ensure adequate protein intake.";
        } else if (userMessage.contains("muscle") || userMessage.contains("strength") || userMessage.contains("lifting")) {
            response = "To build muscle, focus on progressive overload in your strength training, adequate protein intake (1.6-2.2g per kg of bodyweight), and proper recovery. Include compound exercises like squats, deadlifts, and bench press in your routine.";
        } else if (userMessage.contains("cardio") || userMessage.contains("running") || userMessage.contains("jogging")) {
            response = "Cardiovascular exercise improves heart health, endurance, and can help with weight management. Aim for 150 minutes of moderate-intensity or 75 minutes of high-intensity cardio per week. Mix in interval training for maximum benefits.";
        } else {
            response = "As your fitness assistant, I can help with workout plans, nutrition advice, and healthy lifestyle tips. What specific fitness goal are you working toward right now?";
        }
        
        addBotMessage(response);
    }
    
    private void addUserMessage(String message) {
        ChatMessage userMessage = new ChatMessage(message, true);
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
    }
    
    private void addBotMessage(String message) {
        ChatMessage botMessage = new ChatMessage(message, false);
        chatMessages.add(botMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
    }
    
    private void saveChatHistory(String userMessage, String botResponse) {
        try {
            if (userRef != null) {
                String chatId = userRef.child("chat_history").push().getKey();
                if (chatId != null) {
                    Map<String, Object> chatEntry = new HashMap<>();
                    chatEntry.put("timestamp", System.currentTimeMillis());
                    chatEntry.put("userMessage", userMessage);
                    chatEntry.put("botResponse", botResponse);
                    
                    userRef.child("chat_history").child(chatId).setValue(chatEntry)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat history saved successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error saving chat history", e));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving chat history: " + e.getMessage());
        }
    }
} 