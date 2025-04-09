package com.example.nirvana.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.adapters.ChatAdapter;
import com.example.nirvana.models.ChatMessage;
import com.example.nirvana.services.GeminiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotFragment extends Fragment {
    private static final String TAG = "ChatbotFragment";
    
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    
    private GeminiService geminiService;
    private DatabaseReference userRef;
    private Map<String, Object> userProfile = new HashMap<>();
    private Map<String, Object> userDietData = new HashMap<>();
    private Map<String, Object> userExerciseData = new HashMap<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewChat);
        messageInput = view.findViewById(R.id.editTextMessage);
        sendButton = view.findViewById(R.id.buttonSend);
        
        // Set up RecyclerView
        chatAdapter = new ChatAdapter(chatMessages, requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(chatAdapter);
        
        // Initialize Gemini Service
        geminiService = new GeminiService(requireContext());
        
        // Initialize Firebase and load user data
        initializeFirebase();
        
        // Add welcome message
        addBotMessage("Hello! I'm your Nirvana Fitness assistant. How can I help you today? You can ask me about exercises, nutrition, or any fitness challenges you're facing.");
        
        // Set up send button
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
                messageInput.setText("");
            }
        });
    }
    
    private void initializeFirebase() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            
            // Load user profile data
            loadUserData();
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
            addBotMessage("I'm having trouble connecting to your profile. Some personalized features may not be available.");
        }
    }
    
    private void loadUserData() {
        // Load profile data
        userRef.child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        userProfile.put(child.getKey(), child.getValue());
                    }
                    Log.d(TAG, "User profile loaded: " + userProfile.toString());
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading profile data: " + databaseError.getMessage());
            }
        });
        
        // Load food log data
        userRef.child("food_logs").limitToLast(7).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey();
                        List<Map<String, Object>> dailyFoods = new ArrayList<>();
                        
                        for (DataSnapshot foodSnapshot : dateSnapshot.getChildren()) {
                            Map<String, Object> foodItem = new HashMap<>();
                            for (DataSnapshot foodDetail : foodSnapshot.getChildren()) {
                                foodItem.put(foodDetail.getKey(), foodDetail.getValue());
                            }
                            dailyFoods.add(foodItem);
                        }
                        
                        userDietData.put(date, dailyFoods);
                    }
                    Log.d(TAG, "User diet data loaded for last 7 days");
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading diet data: " + databaseError.getMessage());
            }
        });
        
        // Load workout data if available
        userRef.child("workouts").limitToLast(7).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey();
                        List<Map<String, Object>> dailyWorkouts = new ArrayList<>();
                        
                        for (DataSnapshot workoutSnapshot : dateSnapshot.getChildren()) {
                            Map<String, Object> workoutItem = new HashMap<>();
                            for (DataSnapshot workoutDetail : workoutSnapshot.getChildren()) {
                                workoutItem.put(workoutDetail.getKey(), workoutDetail.getValue());
                            }
                            dailyWorkouts.add(workoutItem);
                        }
                        
                        userExerciseData.put(date, dailyWorkouts);
                    }
                    Log.d(TAG, "User workout data loaded for last 7 days");
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading workout data: " + databaseError.getMessage());
            }
        });
    }
    
    private void sendMessage(String message) {
        // Add user message to chat
        addUserMessage(message);
        
        // Build context for Gemini API
        StringBuilder context = new StringBuilder();
        
        // Add user profile data
        if (!userProfile.isEmpty()) {
            context.append("USER PROFILE:\n");
            for (Map.Entry<String, Object> entry : userProfile.entrySet()) {
                context.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            context.append("\n");
        }
        
        // Add recent diet data summary
        if (!userDietData.isEmpty()) {
            context.append("RECENT DIET SUMMARY:\n");
            context.append("User has logged food for ").append(userDietData.size()).append(" days recently.\n\n");
        }
        
        // Add recent exercise data summary
        if (!userExerciseData.isEmpty()) {
            context.append("RECENT EXERCISE SUMMARY:\n");
            context.append("User has logged workouts for ").append(userExerciseData.size()).append(" days recently.\n\n");
        }
        
        // Add user query
        context.append("USER QUERY: ").append(message);
        
        // Process with Gemini API
        geminiService.generateContent(context.toString(), new GeminiService.GeminiResponseCallback() {
            @Override
            public void onResponse(String response) {
                requireActivity().runOnUiThread(() -> {
                    addBotMessage(response);
                    
                    // Save this chat to Firebase
                    saveChatHistory(message, response);
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Log.e(TAG, "Gemini API error: " + errorMessage);
                    addBotMessage("I'm sorry, I'm having trouble processing your request right now. Please try again later.");
                });
            }
        });
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
                Map<String, Object> chatEntry = new HashMap<>();
                chatEntry.put("timestamp", System.currentTimeMillis());
                chatEntry.put("userMessage", userMessage);
                chatEntry.put("botResponse", botResponse);
                
                userRef.child("chat_history").child(chatId).setValue(chatEntry)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat history saved successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving chat history", e));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving chat history: " + e.getMessage());
        }
    }
} 