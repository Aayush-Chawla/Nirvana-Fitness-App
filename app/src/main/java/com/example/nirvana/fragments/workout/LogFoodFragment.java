package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.ui.adapters.FoodLogAdapter;
import com.example.nirvana.data.models.FoodItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.firebase.database.Logger;
import java.util.Map;
import java.lang.NumberFormatException;

public class LogFoodFragment extends Fragment {
    private static final String TAG = "LogFoodFragment";

    private TextInputEditText edtFoodName;
    private TextInputEditText edtCalories;
    private MaterialButton btnSave;
    private RecyclerView recyclerFoodItems;
    private TextView txtNoItems;
    private FoodLogAdapter foodItemAdapter;
    private DatabaseReference userRef;
    private String today;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_food, container, false);
        Log.d(TAG, "onCreateView called");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        // Initialize views
        edtFoodName = view.findViewById(R.id.edtFoodName);
        edtCalories = view.findViewById(R.id.edtCalories);
        btnSave = view.findViewById(R.id.btnSave);
        recyclerFoodItems = view.findViewById(R.id.recyclerFoodItems);
        txtNoItems = view.findViewById(R.id.txtNoItems);

        // Setup keyboard actions
        setupKeyboardActions();

        // Initialize Firebase
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .child("food_logs");
            today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Log.d(TAG, "Firebase path: " + userRef.toString());
            Log.d(TAG, "Today's date: " + today);
            
            // Test write to confirm we have write access
            DatabaseReference testRef = userRef.child("test");
            testRef.setValue("test_value")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Test write succeeded");
                    // Remove the test data
                    testRef.removeValue();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Test write failed: " + e.getMessage(), e);
                });
            
            Toast.makeText(requireContext(), "Firebase connected!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error connecting to Firebase", Toast.LENGTH_LONG).show();
            txtNoItems.setText("Error connecting to database: " + e.getMessage());
            txtNoItems.setVisibility(View.VISIBLE);
            return;
        }

        // Set initial status
        txtNoItems.setText("Initializing...");
        txtNoItems.setVisibility(View.VISIBLE);
        
        // Setup RecyclerView
        setupRecyclerView();

        // Setup click listener
        btnSave.setOnClickListener(v -> saveFood());

        // Load today's food items
        loadTodaysFoodItems();
    }

    private void setupKeyboardActions() {
        // When "Next" is pressed on food name, focus moves to calories
        edtFoodName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtCalories.requestFocus();
                return true;
            }
            return false;
        });

        // When "Done" is pressed on calories, save the food
        edtCalories.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveFood();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        try {
            foodItemAdapter = new FoodLogAdapter(new ArrayList<>());
            recyclerFoodItems.setAdapter(foodItemAdapter);
            recyclerFoodItems.setLayoutManager(new LinearLayoutManager(requireContext()));
            Log.d(TAG, "RecyclerView setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error setting up list view", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTodaysFoodItems() {
        Log.d(TAG, "Loading food items for date: " + today);
        
        if (userRef == null) {
            Log.e(TAG, "Database reference is null");
            txtNoItems.setText("Error: Could not connect to the database");
            txtNoItems.setVisibility(View.VISIBLE);
            recyclerFoodItems.setVisibility(View.GONE);
            return;
        }
        
        txtNoItems.setText("Loading food items...");
        txtNoItems.setVisibility(View.VISIBLE);
        
        // Add more detailed logging for the Firebase path
        DatabaseReference todayRef = userRef.child(today);
        Log.d(TAG, "Firebase query path: " + todayRef.toString());
        
        // Directly check the structure of the data at users/userId
        FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Root user data exists: " + snapshot.exists());
                    if (snapshot.exists()) {
                        Log.d(TAG, "Root user data children count: " + snapshot.getChildrenCount());
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Log.d(TAG, "Root user data child key: " + child.getKey());
                        }
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Root user data check cancelled: " + error.getMessage());
                }
            });
        
        // Add a single value listener to check if data exists before setting up the value listener
        todayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Initial check - Data exists: " + snapshot.exists());
                Log.d(TAG, "Initial check - Children count: " + snapshot.getChildrenCount());
                
                if (snapshot.exists()) {
                    // Log all the keys at this level
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Log.d(TAG, "Child key: " + child.getKey());
                    }
                }
                
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    Log.d(TAG, "No food items found for today");
                    txtNoItems.setText("No food items logged yet for today (" + today + ")");
                    txtNoItems.setVisibility(View.VISIBLE);
                    recyclerFoodItems.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "Found food items, setting up value listener");
                    txtNoItems.setVisibility(View.GONE);
                    recyclerFoodItems.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Initial check cancelled: " + error.getMessage());
            }
        });
        
        todayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Data snapshot exists: " + snapshot.exists());
                Log.d(TAG, "Number of children: " + snapshot.getChildrenCount());
                
                List<FoodItem> foodItems = new ArrayList<>();
                StringBuilder debugInfo = new StringBuilder();
                debugInfo.append("Firebase path: ").append(userRef.child(today).toString())
                      .append("\nData exists: ").append(snapshot.exists())
                      .append("\nChildren count: ").append(snapshot.getChildrenCount());
                
                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    try {
                        String key = foodSnapshot.getKey();
                        Log.d(TAG, "Processing food item with key: " + key);
                        debugInfo.append("\n\nItem key: ").append(key);
                        
                        // Log the raw JSON for debugging
                        Object value = foodSnapshot.getValue();
                        Log.d(TAG, "Raw food item data: " + (value != null ? value.toString() : "null"));
                        debugInfo.append("\nRaw value: ").append(value);
                        
                        // Try to get values directly
                        String name = null;
                        Integer calories = null;
                        String time = null;
                        
                        // First try to get fields from the direct child nodes
                        if (foodSnapshot.hasChild("name")) {
                            name = foodSnapshot.child("name").getValue(String.class);
                            Log.d(TAG, "Found name field: " + name);
                        }
                        
                        if (foodSnapshot.hasChild("calories")) {
                            // Try as Integer first
                            calories = foodSnapshot.child("calories").getValue(Integer.class);
                            if (calories == null) {
                                // Try as Long
                                Long caloriesLong = foodSnapshot.child("calories").getValue(Long.class);
                                if (caloriesLong != null) {
                                    calories = caloriesLong.intValue();
                                } else {
                                    // Try as Double
                                    Double caloriesDouble = foodSnapshot.child("calories").getValue(Double.class);
                                    if (caloriesDouble != null) {
                                        calories = caloriesDouble.intValue();
                                    }
                                }
                            }
                            Log.d(TAG, "Found calories field: " + calories);
                        }
                        
                        if (foodSnapshot.hasChild("time")) {
                            time = foodSnapshot.child("time").getValue(String.class);
                            Log.d(TAG, "Found time field: " + time);
                        }
                        
                        // If any fields are still missing, try to handle the object as a map
                        if (name == null || calories == null || time == null) {
                            if (value instanceof Map) {
                                Map<String, Object> foodMap = (Map<String, Object>) value;
                                Log.d(TAG, "Processing as Map with keys: " + foodMap.keySet());
                                
                                if (name == null && foodMap.containsKey("name")) {
                                    name = foodMap.get("name").toString();
                                }
                                
                                if (calories == null && foodMap.containsKey("calories")) {
                                    Object cal = foodMap.get("calories");
                                    if (cal instanceof Integer) {
                                        calories = (Integer) cal;
                                    } else if (cal instanceof Long) {
                                        calories = ((Long) cal).intValue();
                                    } else if (cal instanceof Double) {
                                        calories = ((Double) cal).intValue();
                                    } else if (cal != null) {
                                        try {
                                            calories = Integer.parseInt(cal.toString());
                                        } catch (NumberFormatException e) {
                                            Log.e(TAG, "Could not parse calories: " + cal);
                                        }
                                    }
                                }
                                
                                if (time == null && foodMap.containsKey("time")) {
                                    time = foodMap.get("time").toString();
                                }
                            }
                        }
                        
                        Log.d(TAG, String.format("Final Food data - Name: %s, Calories: %s, Time: %s", 
                            name, calories, time));
                        
                        debugInfo.append("\nName: ").append(name)
                               .append("\nCalories: ").append(calories)
                               .append("\nTime: ").append(time);
                        
                        if (name != null && calories != null && time != null) {
                            FoodItem foodItem = new FoodItem();
                            foodItem.setName(name);
                            foodItem.setCaloriesInt(calories);
                            foodItem.setTime(time);
                            foodItems.add(foodItem);
                            Log.d(TAG, "Added food item to list: " + name);
                        } else {
                            debugInfo.append("\nSkipped (missing data)");
                            Log.w(TAG, "Skipped food item due to missing data - name: " + name + 
                                ", calories: " + calories + ", time: " + time);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing food item: " + e.getMessage(), e);
                        debugInfo.append("\nError: ").append(e.getMessage());
                    }
                }
                
                Log.d(TAG, "Total food items loaded: " + foodItems.size());
                
                // Update UI based on whether there are items
                if (foodItems.isEmpty()) {
                    Log.d(TAG, "No food items found, showing empty state");
                    txtNoItems.setText("No food items logged yet\n\n" + debugInfo);
                    txtNoItems.setVisibility(View.VISIBLE);
                    recyclerFoodItems.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "Found food items, showing list");
                    // Still show the list, even if we have debug info
                    txtNoItems.setVisibility(View.GONE);
                    recyclerFoodItems.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Found " + foodItems.size() + " food items", Toast.LENGTH_SHORT).show();
                }
                
                // Update the adapter with the loaded items
                foodItemAdapter.updateFoodItems(foodItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage(), error.toException());
                Toast.makeText(requireContext(), "Failed to load food items: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                txtNoItems.setText("Error loading food items:\n" + error.getMessage());
                txtNoItems.setVisibility(View.VISIBLE);
                recyclerFoodItems.setVisibility(View.GONE);
            }
        });
    }

    private void saveFood() {
        String foodName = edtFoodName.getText().toString().trim();
        String caloriesStr = edtCalories.getText().toString().trim();

        if (foodName.isEmpty()) {
            edtFoodName.setError("Please enter food name");
            return;
        }

        if (caloriesStr.isEmpty()) {
            edtCalories.setError("Please enter calories");
            return;
        }

        int calories = Integer.parseInt(caloriesStr);
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        Log.d(TAG, String.format("Saving food - Name: %s, Calories: %d, Time: %s", 
            foodName, calories, currentTime));

        if (userRef == null) {
            Log.e(TAG, "Cannot save: Database reference is null");
            Toast.makeText(requireContext(), "Error: Database reference is null", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a map with the exact field names that match the adapter's expectations
        java.util.Map<String, Object> foodValues = new java.util.HashMap<>();
        foodValues.put("name", foodName);
        foodValues.put("calories", calories);
        foodValues.put("time", currentTime);

        // Save directly to today's date node
        DatabaseReference newFoodRef = userRef.child(today).push();
        Log.d(TAG, "New food reference: " + newFoodRef.toString());

        // Save to both locations
        newFoodRef.setValue(foodValues)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Food item saved successfully to food_logs");
                
                // Also save to the meals path (for MealFragment display)
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("meals")
                    .child("Snacks"); // Default to Snacks category
                
                // Use the same map for meals
                DatabaseReference newMealRef = mealsRef.push();
                newMealRef.setValue(foodValues)
                    .addOnSuccessListener(aVoid2 -> {
                        Log.d(TAG, "Food item saved successfully to meals");
                        Toast.makeText(requireContext(), "Food logged successfully", Toast.LENGTH_SHORT).show();
                        
                        // Force reload the food items
                        loadTodaysFoodItems();
                        
                        // Clear input fields
                        edtFoodName.setText("");
                        edtCalories.setText("");
                        edtFoodName.requestFocus();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving food item to meals: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Failed to log food to meals: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving food item to food_logs: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Failed to log food: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
        
        // Create a TEST food item directly in the today's node to verify writing works
        java.util.Map<String, Object> testFood = new java.util.HashMap<>();
        testFood.put("name", "Test Food");
        testFood.put("calories", 100);
        testFood.put("time", currentTime);
        
        userRef.child(today).child("test_item").setValue(testFood)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Test food item saved successfully");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving test food item: " + e.getMessage(), e);
            });
    }
} 