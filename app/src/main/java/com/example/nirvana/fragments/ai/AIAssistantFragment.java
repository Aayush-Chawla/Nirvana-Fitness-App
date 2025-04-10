package com.example.nirvana.fragments.ai;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import com.example.nirvana.R;
import com.example.nirvana.adapters.ChatAdapter;
import com.example.nirvana.models.ChatMessage;
import com.example.nirvana.services.GeminiService;
import com.example.nirvana.utils.FirebaseHelper;
import com.example.nirvana.utils.FirestoreHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIAssistantFragment extends Fragment {
    private static final String TAG = "AIAssistantFragment";
    
    // UI Elements
    private RecyclerView recyclerViewChat;
    private EditText messageInput;
    private ImageButton sendButton;
    private Button btnTryMe;
    private CardView cardFitnessGoals, cardNutritionAdvice, cardWorkoutPlans, cardHealthTracking;
    
    // Data
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private GeminiService geminiService;
    private String currentUserId;
    
    // Context data for AI
    private Map<String, Object> userProfile = new HashMap<>();
    private Map<String, Object> userDietData = new HashMap<>();
    private Map<String, Object> userExerciseData = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_assistant, container, false);
        
        // Initialize UI components
        initializeViews(view);
        
        // Initialize Firebase and services
        setupFirebase();
        
        // Setup Gemini service
        geminiService = new GeminiService(requireContext());
        
        // Initialize chat recycler view
        setupChatRecyclerView();
        
        // Setup card click listeners
        setupCardClickListeners();
        
        // Setup input and send button
        setupChatControls();
        
        // Setup try me button
        setupDemoButton();
        
        // Load previous chat history and user data
        loadChatHistory();
        loadUserDataForChatbot();
        
        return view;
    }
    
    private void initializeViews(View view) {
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        messageInput = view.findViewById(R.id.editTextMessage);
        sendButton = view.findViewById(R.id.buttonSend);
        btnTryMe = view.findViewById(R.id.btnTryMe);
        
        // AI Feature Cards
        cardFitnessGoals = view.findViewById(R.id.cardFitnessGoals);
        cardNutritionAdvice = view.findViewById(R.id.cardNutritionAdvice);
        cardWorkoutPlans = view.findViewById(R.id.cardWorkoutPlans);
        cardHealthTracking = view.findViewById(R.id.cardHealthTracking);
    }
    
    private void setupFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }
    }
    
    private void setupChatRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); // Scroll to bottom on new messages
        recyclerViewChat.setLayoutManager(layoutManager);
        
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerViewChat.setAdapter(chatAdapter);
    }
    
    private void setupCardClickListeners() {
        // Fitness Goals card
        cardFitnessGoals.setOnClickListener(v -> {
            String prompt = "What are some realistic fitness goals I can set for myself?";
            messageInput.setText(prompt);
            sendMessage(prompt);
        });
        
        // Nutrition Advice card
        cardNutritionAdvice.setOnClickListener(v -> {
            String prompt = "Can you give me nutrition advice based on my diet logs?";
            messageInput.setText(prompt);
            sendMessage(prompt);
        });
        
        // Workout Plans card
        cardWorkoutPlans.setOnClickListener(v -> {
            String prompt = "What workouts would you recommend based on my activity history?";
            messageInput.setText(prompt);
            sendMessage(prompt);
        });
        
        // Health Tracking card
        cardHealthTracking.setOnClickListener(v -> {
            String prompt = "How can I better track my overall health and fitness progress?";
            messageInput.setText(prompt);
            sendMessage(prompt);
        });
    }
    
    private void setupChatControls() {
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
            }
        });
    }
    
    private void setupDemoButton() {
        btnTryMe.setOnClickListener(v -> {
            // Cycle through demo questions
            String[] demoQuestions = {
                "What is my name?",
                "How old am I?",
                "What is my weight?",
                "What are my recent meals?",
                "Tell me about my workouts"
            };
            
            // Use a tag to cycle through questions
            int currentQuestion = 0;
            if (btnTryMe.getTag() != null) {
                currentQuestion = (int) btnTryMe.getTag();
                currentQuestion = (currentQuestion + 1) % demoQuestions.length;
            }
            btnTryMe.setTag(currentQuestion);
            
            // Send the selected demo question
            String question = demoQuestions[currentQuestion];
            messageInput.setText(question);
            sendMessage(question);
        });
    }
    
    private void loadChatHistory() {
        if (currentUserId == null) return;
        
        FirebaseHelper.getChatHistory(10, new FirestoreHelper.OnDataFetchedListener<List<Map<String, Object>>>() {
            @Override
            public void onDataFetched(List<Map<String, Object>> messages) {
                Log.d(TAG, "Loading " + messages.size() + " chat messages from history");
                
                // Clear existing messages first
                chatMessages.clear();
                
                // Messages come in chronological order, show oldest first
                for (int i = 0; i < messages.size(); i++) {
                    Map<String, Object> messageData = messages.get(i);
                    String userMessage = (String) messageData.get("userMessage");
                    String botResponse = (String) messageData.get("botResponse");
                    
                    // Add user message first
                    chatMessages.add(new ChatMessage(userMessage, true));
                    
                    // Then add bot response
                    chatMessages.add(new ChatMessage(botResponse, false));
                }
                
                chatAdapter.notifyDataSetChanged();
                
                // Scroll to the bottom if there are messages
                if (!chatMessages.isEmpty()) {
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                }
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading chat history: " + message);
                // Continue without history
            }
        });
    }
    
    private void loadUserDataForChatbot() {
        if (currentUserId == null) return;
        
        // Load profile data using FirestoreHelper
        FirestoreHelper.getUserProfile(new FirestoreHelper.OnDataFetchedListener<Map<String, Object>>() {
            @Override
            public void onDataFetched(Map<String, Object> profile) {
                userProfile.clear();
                userProfile.putAll(profile);
                Log.d(TAG, "User profile loaded for chatbot: " + userProfile.toString());
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading profile data for chatbot: " + message);
            }
        });
        
        // Load meal data
        loadMealDataForChatbot();
        
        // Load exercise data
        loadExerciseDataForChatbot();
    }
    
    private void loadMealDataForChatbot() {
        // Use FirestoreHelper to get meals
        Log.d(TAG, "Starting to fetch meal data for chatbot context");
        FirestoreHelper.getMeals(new FirestoreHelper.OnDataFetchedListener<Map<String, List<Map<String, Object>>>>() {
            @Override
            public void onDataFetched(Map<String, List<Map<String, Object>>> meals) {
                // Process meals data for chatbot context
                if (meals != null) {
                    int totalMeals = 0;
                    StringBuilder mealInfo = new StringBuilder("Meals loaded: ");
                    
                    for (String mealType : meals.keySet()) {
                        if (meals.get(mealType) != null) {
                            int count = meals.get(mealType).size();
                            totalMeals += count;
                            mealInfo.append(mealType).append("=").append(count).append(", ");
                        }
                    }
                    
                    Log.d(TAG, mealInfo.toString());
                    userDietData.put("meal_count", totalMeals);
                    Log.d(TAG, "Meal data loaded for chatbot: " + userDietData.toString());
                } else {
                    Log.d(TAG, "No meal data returned from Firestore");
                }
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading meal data for chatbot: " + message);
            }
        });
    }
    
    private void loadExerciseDataForChatbot() {
        // Use FirestoreHelper to get workouts
        FirestoreHelper.getRecentWorkouts(10, new FirestoreHelper.OnDataFetchedListener<List<Map<String, Object>>>() {
            @Override
            public void onDataFetched(List<Map<String, Object>> workouts) {
                // Process workout data for chatbot context
                if (workouts != null) {
                    userExerciseData.put("workout_count", workouts.size());
                    Log.d(TAG, "Exercise data loaded for chatbot: " + userExerciseData.toString());
                }
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading exercise data for chatbot: " + message);
            }
        });
    }
    
    private void sendMessage(String message) {
        // Don't process empty messages
        if (TextUtils.isEmpty(message)) return;
        
        // Add message to chat
        addUserMessage(message);
        
        // Clear input
        messageInput.setText("");
        
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
                if (getActivity() == null || !isAdded()) return;
                
                requireActivity().runOnUiThread(() -> {
                    addBotMessage(response);
                    
                    // Save this chat to Firebase
                    saveChatHistory(message, response);
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null || !isAdded()) return;
                
                requireActivity().runOnUiThread(() -> {
                    Log.e(TAG, "Gemini API error: " + errorMessage);
                    // Instead of showing a generic error message, let the GeminiService handle it with a fallback
                    addBotMessage("I'll provide you with the best information I can without accessing the AI service.");
                    
                    // Call generateContent again - the fallback system will automatically kick in
                    geminiService.generateContent(message, new GeminiService.GeminiResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            if (getActivity() == null || !isAdded()) return;
                            addBotMessage(response);
                            saveChatHistory(message, response);
                        }
                        
                        @Override
                        public void onError(String secondError) {
                            // This should not happen since we're already using the fallback
                            Log.e(TAG, "Double error in Gemini service: " + secondError);
                        }
                    });
                });
            }
        });
    }
    
    private void addUserMessage(String message) {
        ChatMessage userMessage = new ChatMessage(message, true);
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.smoothScrollToPosition(chatMessages.size() - 1);
    }
    
    private void addBotMessage(String message) {
        ChatMessage botMessage = new ChatMessage(message, false);
        chatMessages.add(botMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.smoothScrollToPosition(chatMessages.size() - 1);
    }
    
    private void saveChatHistory(String userMessage, String botResponse) {
        Log.d(TAG, "User message: " + (userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage));
        Log.d(TAG, "Bot response: " + (botResponse.length() > 50 ? botResponse.substring(0, 50) + "..." : botResponse));
        
        // Use FirestoreHelper to save chat history
        FirestoreHelper.saveChatMessage(userMessage, botResponse, new FirestoreHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Chat history saved successfully");
            }
            
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error saving chat history: " + error);
            }
        });
    }
} 