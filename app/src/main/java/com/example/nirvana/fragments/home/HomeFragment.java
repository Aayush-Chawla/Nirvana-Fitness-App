package com.example.nirvana.fragments.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.BlogAdapter;
import com.example.nirvana.adapters.ChatAdapter;
import com.example.nirvana.models.BlogPost;
import com.example.nirvana.models.ChatMessage;
import com.example.nirvana.models.GymMembership;
import com.example.nirvana.services.GeminiService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment implements BlogAdapter.OnBlogClickListener {

    private static final String TAG = "HomeFragment";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView txtWelcome;
    private TextView txtStepCount;
    private TextView txtActiveMinutes;
    private TextView txtWorkoutsCompleted;
    private TextView txtCaloriesBurned;
    private TextView txtCurrentHeartRate;
    private TextView txtRewardPoints;
    private TextView txtNextReward;
    private ProgressBar progressCalories;
    private ProgressBar progressRewards;
    private LineChart chartHeartRate;
    private RecyclerView recyclerBlogs;
    private BlogAdapter blogAdapter;
    private DatabaseReference userRef;
    private String currentUserId;
    private CardView cardGymMembership;
    private TextView tvGymName, tvMembershipType, tvMembershipStatus;

    // Add these variables to keep track of listeners
    private ValueEventListener dailyStatsListener;
    private ValueEventListener heartRateListener;
    private ValueEventListener rewardsListener;
    private ValueEventListener gymMembershipListener;
    
    // Chatbot related fields
    private RecyclerView recyclerViewChat;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private GeminiService geminiService;
    private Map<String, Object> userProfile = new HashMap<>();
    private Map<String, Object> userDietData = new HashMap<>();
    private Map<String, Object> userExerciseData = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize all views
        initializeViews(view);
        
        // Initialize Firebase
        setupFirebase();
        
        // Setup UI components
        setupRecyclerView();
        setupHeartRateChart();
        setupDietaryDashboardCard(view);
        setupGymMembershipCard(view);
        setupChatbot(view);
        
        // Load data
        loadDashboardData();
        loadBlogs();
        loadGymMembership();
        loadUserDataForChatbot();
        
        if (checkLocationPermission()) {
            loadNearbyGyms();
        }
    }

    private void initializeViews(View view) {
        txtWelcome = view.findViewById(R.id.txtWelcome);
        txtStepCount = view.findViewById(R.id.txtStepCount);
        txtActiveMinutes = view.findViewById(R.id.txtActiveMinutes);
        txtWorkoutsCompleted = view.findViewById(R.id.txtWorkoutsCompleted);
        txtCaloriesBurned = view.findViewById(R.id.txtCaloriesBurned);
        txtCurrentHeartRate = view.findViewById(R.id.txtCurrentHeartRate);
        txtRewardPoints = view.findViewById(R.id.txtRewardPoints);
        txtNextReward = view.findViewById(R.id.txtNextReward);
        progressCalories = view.findViewById(R.id.progressCalories);
        progressRewards = view.findViewById(R.id.progressRewards);
        chartHeartRate = view.findViewById(R.id.chartHeartRate);
        recyclerBlogs = view.findViewById(R.id.recyclerBlogs);
        cardGymMembership = view.findViewById(R.id.cardGymMembership);
        tvGymName = view.findViewById(R.id.tvGymName);
        tvMembershipType = view.findViewById(R.id.tvMembershipType);
        tvMembershipStatus = view.findViewById(R.id.tvMembershipStatus);
        
        // Initialize chatbot views
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        messageInput = view.findViewById(R.id.editTextMessage);
        sendButton = view.findViewById(R.id.buttonSend);
    }

    private void setupFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId);
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerBlogs.setLayoutManager(layoutManager);
        blogAdapter = new BlogAdapter(new ArrayList<>(), this);
        recyclerBlogs.setAdapter(blogAdapter);
    }

    private void setupHeartRateChart() {
        chartHeartRate.getDescription().setEnabled(false);
        chartHeartRate.setTouchEnabled(true);
        chartHeartRate.setDragEnabled(true);
        chartHeartRate.setScaleEnabled(true);
        chartHeartRate.setPinchZoom(true);
        chartHeartRate.setDrawGridBackground(false);
    }

    private void loadDashboardData() {
        if (userRef == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // Load daily stats
        dailyStatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Skip if fragment is not attached
                
                if (snapshot.exists()) {
                    txtStepCount.setText(String.format(Locale.getDefault(), "%d steps", 
                        snapshot.child("steps").getValue(Integer.class)));
                    txtActiveMinutes.setText(String.format(Locale.getDefault(), "%d mins", 
                        snapshot.child("active_minutes").getValue(Integer.class)));
                    txtWorkoutsCompleted.setText(String.format(Locale.getDefault(), "%d workouts", 
                        snapshot.child("workouts").getValue(Integer.class)));
                    txtCaloriesBurned.setText(String.format(Locale.getDefault(), "%d kcal", 
                        snapshot.child("calories").getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return; // Skip if fragment is not attached
                Log.e(TAG, "Failed to load daily stats", error.toException());
            }
        };
        userRef.child("daily_stats").child(today).addValueEventListener(dailyStatsListener);

        // Load heart rate data
        heartRateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Skip if fragment is not attached
                
                List<Entry> entries = new ArrayList<>();
                float index = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Integer heartRate = dataSnapshot.getValue(Integer.class);
                    if (heartRate != null) {
                        entries.add(new Entry(index++, heartRate));
                        if (index == entries.size()) { // Last entry
                            txtCurrentHeartRate.setText(String.format(Locale.getDefault(), "%d bpm", heartRate));
                        }
                    }
                }
                updateHeartRateChart(entries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return; // Skip if fragment is not attached
                Log.e(TAG, "Failed to load heart rate data", error.toException());
            }
        };
        userRef.child("heart_rate").limitToLast(7).addValueEventListener(heartRateListener);

        // Load rewards data
        rewardsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Skip if fragment is not attached
                
                if (snapshot.exists()) {
                    int points = snapshot.child("points").getValue(Integer.class);
                    int nextReward = snapshot.child("next_reward").getValue(Integer.class);
                    txtRewardPoints.setText(String.format(Locale.getDefault(), "%d pts", points));
                    txtNextReward.setText(String.format(Locale.getDefault(), "%d pts to next reward", nextReward - points));
                    progressRewards.setProgress((points * 100) / nextReward);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return; // Skip if fragment is not attached
                Log.e(TAG, "Failed to load rewards data", error.toException());
            }
        };
        userRef.child("rewards").addValueEventListener(rewardsListener);
    }

    private void updateHeartRateChart(List<Entry> entries) {
        // Safety check to prevent crashes
        if (!isAdded()) return;
        
        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chartHeartRate.setData(lineData);
        chartHeartRate.invalidate();
    }

    private void loadBlogs() {
        // Using mock data for now
        List<BlogPost> mockPosts = new ArrayList<>();
        
        BlogPost post1 = new BlogPost();
        post1.setId("1");
        post1.setTitle("10 Essential Workout Tips for Beginners");
        post1.setDescription("Starting your fitness journey? Here are the key things you need to know.");
        post1.setAuthor("John Smith");
        post1.setImageUrl("https://cdn.pixabay.com/photo/2017/08/07/14/02/man-2604149_960_720.jpg");
        post1.setPublishedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        mockPosts.add(post1);

        BlogPost post2 = new BlogPost();
        post2.setId("2");
        post2.setTitle("Nutrition Guide: Eating for Muscle Growth");
        post2.setDescription("Learn about the best foods and timing for optimal muscle development.");
        post2.setAuthor("Sarah Johnson");
        post2.setImageUrl("https://cdn.pixabay.com/photo/2017/03/26/11/53/hors-doeuvre-2175326_960_720.jpg");
        post2.setPublishedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        mockPosts.add(post2);

        BlogPost post3 = new BlogPost();
        post3.setId("3");
        post3.setTitle("The Benefits of Morning Workouts");
        post3.setDescription("Discover why exercising in the morning can boost your productivity.");
        post3.setAuthor("Mike Wilson");
        post3.setImageUrl("https://cdn.pixabay.com/photo/2017/07/02/19/24/dumbbells-2465478_960_720.jpg");
        post3.setPublishedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        mockPosts.add(post3);

        blogAdapter.updateBlogs(mockPosts);
    }

    private void loadGymMembership() {
        if (userRef == null) return;

        gymMembershipListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Skip if fragment is not attached
                
                if (snapshot.exists()) {
                    GymMembership membership = snapshot.getValue(GymMembership.class);
                    if (membership != null) {
                        tvGymName.setText(membership.getGymName());
                        tvMembershipType.setText(membership.getMembershipType());
                        tvMembershipStatus.setText(membership.getStatus());
                        cardGymMembership.setVisibility(View.VISIBLE);
                    }
                } else {
                    // No active membership, show nearby gyms option
                    tvGymName.setText("Find a Gym");
                    tvMembershipType.setText("Tap to discover nearby gyms");
                    tvMembershipStatus.setText("No Active Membership");
                    cardGymMembership.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return; // Skip if fragment is not attached
                Log.e(TAG, "Error loading gym membership: " + error.getMessage());
            }
        };
        userRef.child("gymMembership").addValueEventListener(gymMembershipListener);

        // Set click listener for gym membership card
        cardGymMembership.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_homeFragment_to_gymListFragment);
        });
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load gym locations
                loadNearbyGyms();
            } else {
                Toast.makeText(requireContext(), "Location permission is required to show nearby gyms", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadNearbyGyms() {
        // TODO: Implement gym location loading
    }

    @Override
    public void onBlogClick(BlogPost blogPost) {
        // Navigate to blog detail screen
        Bundle args = new Bundle();
        args.putParcelable("blog_post", (Parcelable) blogPost);
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_blogDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Remove all listeners to prevent memory leaks
        removeListeners();
        
        // Clear chatbot resources
        chatMessages.clear();
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove listeners again for safety
        removeListeners();
    }

    private void removeListeners() {
        if (userRef != null) {
            // Remove daily stats listener
            if (dailyStatsListener != null) {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                userRef.child("daily_stats").child(today).removeEventListener(dailyStatsListener);
            }
            
            // Remove heart rate listener
            if (heartRateListener != null) {
                userRef.child("heart_rate").removeEventListener(heartRateListener);
            }
            
            // Remove rewards listener
            if (rewardsListener != null) {
                userRef.child("rewards").removeEventListener(rewardsListener);
            }
            
            // Remove gym membership listener
            if (gymMembershipListener != null) {
                userRef.child("gymMembership").removeEventListener(gymMembershipListener);
            }
        }
    }

    private void setupDietaryDashboardCard(View view) {
        View dietaryDashboardCard = view.findViewById(R.id.cardDietaryDashboard);
        if (dietaryDashboardCard != null) {
            dietaryDashboardCard.setOnClickListener(v -> {
                // Navigate to DietaryDashboardFragment
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_homeFragment_to_dietaryDashboardFragment);
            });
        }
    }

    private void setupGymMembershipCard(View view) {
        cardGymMembership = view.findViewById(R.id.cardGymMembership);
        if (cardGymMembership != null) {
            cardGymMembership.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_homeFragment_to_gymListFragment);
            });
        }
    }

    private void setupChatbot(View view) {
        // Initialize ChatAdapter and set up RecyclerView
        chatAdapter = new ChatAdapter(chatMessages, requireContext());
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewChat.setAdapter(chatAdapter);
        
        // Initialize Gemini Service
        geminiService = new GeminiService(requireContext());
        
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

    private void loadUserDataForChatbot() {
        if (userRef == null) return;
        
        // Load profile data
        userRef.child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        userProfile.put(child.getKey(), child.getValue());
                    }
                    Log.d(TAG, "User profile loaded for chatbot: " + userProfile.toString());
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading profile data for chatbot: " + databaseError.getMessage());
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
                    Log.d(TAG, "User diet data loaded for chatbot (last 7 days)");
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading diet data for chatbot: " + databaseError.getMessage());
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
                    Log.d(TAG, "User workout data loaded for chatbot (last 7 days)");
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading workout data for chatbot: " + databaseError.getMessage());
            }
        });
    }

    private void sendMessage(String message) {
        // Don't process empty messages
        if (TextUtils.isEmpty(message)) return;
        
        // Add message to chat
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
                    // The service will provide a meaningful response based on the message content
                    addBotMessage("I'll provide you with the best information I can without accessing the AI service.");
                    
                    // Call generateContent again - the fallback system will automatically kick in
                    // since the error has already been logged
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

