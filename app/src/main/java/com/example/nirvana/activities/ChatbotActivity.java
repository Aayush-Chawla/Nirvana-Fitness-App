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
import com.example.nirvana.adapters.SuggestionAdapter;
import com.example.nirvana.models.ChatMessage;
import com.example.nirvana.services.EnhancedFitnessAssistant;
import com.example.nirvana.services.GeminiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotActivity extends AppCompatActivity implements SuggestionAdapter.OnSuggestionClickListener {
    private static final String TAG = "ChatbotActivity";
    private static final String BASIC_SYSTEM_PROMPT = "You are a fitness assistant. Keep responses brief.";
    private static final int MAX_RETRIES = 2;

    private RecyclerView recyclerView;
    private RecyclerView rvSuggestions;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private SuggestionAdapter suggestionAdapter;
    private List<ChatMessage> chatMessages;
    private List<String> suggestionList;
    
    private EnhancedFitnessAssistant fitnessAssistant;
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
        rvSuggestions = findViewById(R.id.rvSuggestions);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Setup RecyclerView for chat messages
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
        
        // Setup suggestions
        setupSuggestionList();
        
        // Initialize Enhanced Fitness Assistant
        try {
            fitnessAssistant = new EnhancedFitnessAssistant(this);
            Log.d(TAG, "EnhancedFitnessAssistant initialized successfully");
            
            // Test message - send directly to the service to verify it's working
            testFitnessAssistant();
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing EnhancedFitnessAssistant", e);
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
        addBotMessage("Hello! I'm your fitness assistant. Choose from the suggestions below or ask me anything about workouts, nutrition, and fitness goals.");

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
    
    private void setupSuggestionList() {
        suggestionList = Arrays.asList(
            "What is my BMI?",
            "Home chest workout",
            "Gym chest workout",
            "Home back workout",
            "Gym back workout",
            "Core strengthening",
            "Abs workout routine",
            "Leg day exercises",
            "Shoulder exercises",
            "Protein intake guide",
            "Calorie calculation",
            "Weight loss tips",
            "Muscle gain diet",
            "Cardio routine",
            "Stretching guide",
            "Recovery tips",
            "Workout schedule"
        );
        
        suggestionAdapter = new SuggestionAdapter(suggestionList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvSuggestions.setLayoutManager(layoutManager);
        rvSuggestions.setAdapter(suggestionAdapter);
    }
    
    @Override
    public void onSuggestionClick(String suggestion) {
        messageInput.setText(suggestion);
        sendMessage(suggestion);
        messageInput.setText("");
    }
    
    private void testFitnessAssistant() {
        String testPrompt = "Say hello";
        Log.d(TAG, "Testing Fitness Assistant with prompt: " + testPrompt);
        
        fitnessAssistant.generateContent(testPrompt, new GeminiService.GeminiResponseCallback() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Fitness Assistant test successful! Response: " + response);
                Toast.makeText(ChatbotActivity.this, "AI connection successful!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Fitness Assistant test failed: " + errorMessage);
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
        
        // Build a more specific prompt based on the type of question
        String enhancedPrompt = buildEnhancedPrompt(message);
        
        // Send to Enhanced Fitness Assistant
        sendPromptToGemini(enhancedPrompt, message);
    }
    
    private String buildEnhancedPrompt(String userMessage) {
        StringBuilder prompt = new StringBuilder();
        
        // Add user context if available
        if (!userProfile.isEmpty()) {
            prompt.append("USER CONTEXT: ");
            for (Map.Entry<String, Object> entry : userProfile.entrySet()) {
                prompt.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
            prompt.append("\n\n");
        }
        
        // Add specialized instructions based on query type
        String lowerCaseMessage = userMessage.toLowerCase();
        
        if (lowerCaseMessage.contains("bmi")) {
            prompt.append("Answer the user's BMI question. If they're asking to calculate BMI, explain that you need their height and weight, and provide the formula. If they've provided height and weight, calculate their BMI value and explain what the value means for health.");
        } 
        else if (lowerCaseMessage.contains("chest") || lowerCaseMessage.contains("back") || 
                 lowerCaseMessage.contains("leg") || lowerCaseMessage.contains("abs") || 
                 lowerCaseMessage.contains("core") || lowerCaseMessage.contains("shoulder")) {
            
            prompt.append("Provide a focused workout routine for the specific body part mentioned (");
            
            if (lowerCaseMessage.contains("chest")) prompt.append("chest");
            else if (lowerCaseMessage.contains("back")) prompt.append("back");
            else if (lowerCaseMessage.contains("leg")) prompt.append("legs");
            else if (lowerCaseMessage.contains("abs")) prompt.append("abs");
            else if (lowerCaseMessage.contains("core")) prompt.append("core");
            else if (lowerCaseMessage.contains("shoulder")) prompt.append("shoulders");
            
            prompt.append("). ");
            
            if (lowerCaseMessage.contains("home")) {
                prompt.append("Focus on bodyweight or minimal equipment exercises that can be done at home. ");
            } else if (lowerCaseMessage.contains("gym")) {
                prompt.append("Include gym equipment-based exercises. ");
            }
            
            prompt.append("Include 4-6 exercises with sets and reps, proper form cues, and progression tips. Keep it concise.");
        }
        else if (lowerCaseMessage.contains("protein") || lowerCaseMessage.contains("calorie") || 
                 lowerCaseMessage.contains("diet") || lowerCaseMessage.contains("nutrition")) {
            prompt.append("Provide nutritional guidance related to fitness. Include scientific rationale but keep it practical and actionable.");
        }
        else {
            // General fitness query
            prompt.append("Answer the user's fitness question with accurate, practical advice. Be concise but thorough.");
        }
        
        prompt.append("\n\nUSER QUESTION: ").append(userMessage);
        
        return prompt.toString();
    }
    
    private void sendPromptToGemini(String prompt, String originalMessage) {
        fitnessAssistant.generateContent(prompt, new GeminiService.GeminiResponseCallback() {
            @Override
            public void onResponse(String response) {
                addBotMessage(response);
                
                // Save to chat history
                saveChatHistory(originalMessage, response);
                
                Log.d(TAG, "Received response from AI: Length=" + response.length());
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error generating response: " + errorMessage);
                
                if (retryCount < MAX_RETRIES) {
                    // Retry with a simpler prompt
                    retryCount++;
                    String retryPrompt = originalMessage + " (please keep it brief)";
                    Log.d(TAG, "Retrying with simplified prompt. Attempt " + retryCount);
                    sendPromptToGemini(retryPrompt, originalMessage);
                } else {
                    // Fall back to basic responses after retries
                    fallbackResponse(originalMessage);
                }
            }
        });
    }

    private void fallbackResponse(String userMessage) {
        // This is a fallback if Enhanced Fitness Assistant fails
        String response;
        userMessage = userMessage.toLowerCase();
        
        if (userMessage.contains("bmi")) {
            response = "BMI (Body Mass Index) is calculated as weight(kg) / height²(m). A healthy BMI range is typically between 18.5-24.9. For personalized advice, please consult a healthcare professional.";
        }
        else if (userMessage.contains("chest") && userMessage.contains("workout")) {
            if (userMessage.contains("home")) {
                response = "Home chest workout: 1) Push-ups (3x10-15) 2) Decline push-ups (3x10) 3) Diamond push-ups (3x8-12) 4) Chest dips using chairs (3x8-10). Rest 60-90 seconds between sets.";
            } else {
                response = "Gym chest workout: 1) Bench press (4x8-12) 2) Incline dumbbell press (3x10-12) 3) Chest flys (3x12-15) 4) Cable crossovers (3x12-15). Focus on proper form and full range of motion.";
            }
        }
        else if (userMessage.contains("back") && userMessage.contains("workout")) {
            if (userMessage.contains("home")) {
                response = "Home back workout: 1) Superman holds (3x30s) 2) Doorway rows (3x10-15) 3) Reverse snow angels (3x12) 4) Dolphin kicks (3x15). Focus on squeezing your shoulder blades together.";
            } else {
                response = "Gym back workout: 1) Lat pulldowns (4x10-12) 2) Seated rows (3x10-12) 3) Dumbbell rows (3x10-12 each side) 4) Face pulls (3x15). Focus on pulling with your back, not arms.";
            }
        }
        else if (userMessage.contains("leg") && userMessage.contains("workout")) {
            response = "Leg workout: 1) Squats or goblet squats (4x10-15) 2) Lunges (3x10-12 each leg) 3) Romanian deadlifts (3x10-12) 4) Calf raises (3x15-20). Ensure proper form to prevent knee injuries.";
        }
        else if (userMessage.contains("core") || userMessage.contains("abs")) {
            response = "Core/Abs workout: 1) Plank (3x30-60s) 2) Bicycle crunches (3x15-20 each side) 3) Russian twists (3x20) 4) Leg raises (3x12-15). Train abs 2-3 times per week for best results.";
        }
        else if (userMessage.contains("shoulder")) {
            response = "Shoulder workout: 1) Overhead press or pike push-ups (3x10-12) 2) Lateral raises (3x12-15) 3) Front raises (3x12) 4) Reverse flys (3x15). Maintain a neutral spine and avoid shrugging.";
        }
        else if (userMessage.contains("protein") || userMessage.contains("diet")) {
            response = "For muscle building, aim for 1.6-2.2g of protein per kg of bodyweight daily. Good sources include lean meats, eggs, dairy, legumes, and plant-based options like tofu and tempeh. Spread intake throughout the day for optimal muscle protein synthesis.";
        }
        else if (userMessage.contains("calorie")) {
            response = "Basic calorie calculation: BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age + 5 (men) or -161 (women). Multiply by activity factor: 1.2 (sedentary), 1.375 (light active), 1.55 (moderate), 1.725 (very active).";
        }
        else {
            response = "As your fitness assistant, I can help with workout plans, nutrition advice, and healthy lifestyle tips. Please ask specific questions about body parts, workout types, or nutrition goals.";
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