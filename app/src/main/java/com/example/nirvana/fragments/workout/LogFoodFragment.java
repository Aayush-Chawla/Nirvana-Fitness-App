package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.models.PredefinedFoodItem;
import com.example.nirvana.services.FoodItemService;
import com.example.nirvana.services.RecommendationService;
import com.example.nirvana.ui.adapters.FoodLogAdapter;
import com.example.nirvana.data.models.FoodItem;
import com.example.nirvana.fragments.diet.FoodSelectionDialog;
import com.example.nirvana.ui.adapters.RecommendationsAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LogFoodFragment extends Fragment implements FoodSelectionDialog.OnFoodSelectedListener {
    private static final String TAG = "LogFoodFragment";

    // UI elements
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton btnAddFood;
    private RecyclerView rvFoodLog;
    private TextView txtNoItems;
    private TabLayout tabLayout;
    private MaterialCardView cardTodayLog;
    private MaterialCardView cardCategorizedLog;
    
    // Category food lists and adapters
    private RecyclerView rvBreakfast;
    private RecyclerView rvLunch;
    private RecyclerView rvDinner;
    private RecyclerView rvSnacks;
    private RecyclerView rvOthers;
    private TextView tvNoBreakfast;
    private TextView tvNoLunch;
    private TextView tvNoDinner;
    private TextView tvNoSnacks;
    private TextView tvNoOthers;
    private FoodLogAdapter todayAdapter;
    private FoodLogAdapter breakfastAdapter;
    private FoodLogAdapter lunchAdapter;
    private FoodLogAdapter dinnerAdapter;
    private FoodLogAdapter snacksAdapter;
    private FoodLogAdapter othersAdapter;
    
    // Food item lists
    private List<FoodItem> allFoodItems = new ArrayList<>();
    private List<FoodItem> breakfastItems = new ArrayList<>();
    private List<FoodItem> lunchItems = new ArrayList<>();
    private List<FoodItem> dinnerItems = new ArrayList<>();
    private List<FoodItem> snacksItems = new ArrayList<>();
    private List<FoodItem> othersItems = new ArrayList<>();
    
    // Data
    private DatabaseReference userRef;
    private String today;
    private String selectedMealType = "Breakfast"; // Will be set based on time of day

    private RecyclerView rvRecommendations;
    private TextView tvNoRecommendations;
    private RecommendationsAdapter recommendationsAdapter;
    private static final int TARGET_DAILY_CALORIES = 2000; // This should come from user settings

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
        initializeViews(view);

        // Set default meal type based on time of day
        setDefaultMealTypeByTime();

        // Initialize Firebase
        initializeFirebase();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Setup click listeners
        setupListeners();

        // Set initial toggle selection based on time-based meal type
        setToggleButtonForMealType();

        // Load today's food items
        loadTodaysFoodItems();

        // Generate initial recommendations
        updateRecommendations();

        // Force update UI after a delay to ensure data is loaded
        view.postDelayed(this::refreshUI, 500);
    }
    
    private void refreshUI() {
        Log.d(TAG, "Refreshing UI manually");
        if (todayAdapter != null) {
            todayAdapter.notifyDataSetChanged();
        }
        if (breakfastAdapter != null) {
            breakfastAdapter.notifyDataSetChanged();
        }
        if (lunchAdapter != null) {
            lunchAdapter.notifyDataSetChanged();
        }
        if (dinnerAdapter != null) {
            dinnerAdapter.notifyDataSetChanged();
        }
        if (snacksAdapter != null) {
            snacksAdapter.notifyDataSetChanged();
        }
        if (othersAdapter != null) {
            othersAdapter.notifyDataSetChanged();
        }
    }
    
    private void initializeViews(View view) {
        // Base controls
        toggleGroup = view.findViewById(R.id.toggleGroup);
        btnAddFood = view.findViewById(R.id.btnAddFood);
        rvFoodLog = view.findViewById(R.id.rvFoodLog);
        txtNoItems = view.findViewById(R.id.txtNoItems);
        tabLayout = view.findViewById(R.id.tabLayout);
        cardTodayLog = view.findViewById(R.id.cardTodayLog);
        cardCategorizedLog = view.findViewById(R.id.cardCategorizedLog);
        
        // Category RecyclerViews
        rvBreakfast = view.findViewById(R.id.rvBreakfast);
        rvLunch = view.findViewById(R.id.rvLunch);
        rvDinner = view.findViewById(R.id.rvDinner);
        rvSnacks = view.findViewById(R.id.rvSnacks);
        rvOthers = view.findViewById(R.id.rvOthers);
        
        // No items text views
        tvNoBreakfast = view.findViewById(R.id.tvNoBreakfast);
        tvNoLunch = view.findViewById(R.id.tvNoLunch);
        tvNoDinner = view.findViewById(R.id.tvNoDinner);
        tvNoSnacks = view.findViewById(R.id.tvNoSnacks);
        tvNoOthers = view.findViewById(R.id.tvNoOthers);
        
        // Initialize recommendation views
        rvRecommendations = view.findViewById(R.id.rvRecommendations);
        tvNoRecommendations = view.findViewById(R.id.tvNoRecommendations);
        
        // Set initial states
        cardTodayLog.setVisibility(View.VISIBLE);
        cardCategorizedLog.setVisibility(View.GONE);
        txtNoItems.setText("Initializing...");
        txtNoItems.setVisibility(View.VISIBLE);
        
        // Debug log
        Log.d(TAG, "Views initialized. RecyclerView null? " + (rvFoodLog == null ? "Yes" : "No"));
    }
    
    private void initializeFirebase() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .child("food_logs");
            today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Log.d(TAG, "Firebase path: " + userRef.toString());
            Log.d(TAG, "Today's date: " + today);
            
            // Let's add a test food item directly to verify Firebase works
            addTestFoodItem();
            
            Toast.makeText(requireContext(), "Firebase connected!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error connecting to Firebase", Toast.LENGTH_LONG).show();
            showError("Error connecting to database: " + e.getMessage());
        }
    }
    
    private void addTestFoodItem() {
        // Only create a test item for debugging
        if (BuildConfig.DEBUG) {
            // Create a test food item for verification
            FoodItem testFood = new FoodItem();
            testFood.setName("Test Food Item (Debug)");
            testFood.setCaloriesInt(250);
            testFood.setTime(new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            testFood.setMealType(selectedMealType);
            testFood.setProtein(15.0);
            testFood.setCarbs(30.0);
            testFood.setFat(5.0);
            
            // Add it to the database
            String testKey = "test_food_" + System.currentTimeMillis();
            Log.d(TAG, "Creating test food item: " + testFood.getName());
            
            if (userRef != null) {
                Map<String, Object> testValues = new HashMap<>();
                testValues.put("name", testFood.getName());
                testValues.put("calories", testFood.getCaloriesInt());
                testValues.put("time", testFood.getTime());
                testValues.put("mealType", testFood.getMealType());
                testValues.put("protein", testFood.getProtein());
                testValues.put("carbs", testFood.getCarbs());
                testValues.put("fat", testFood.getFat());
                
                // Save directly to a test node to avoid cluttering the main data
                userRef.child(today).child(testKey).setValue(testValues)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Test food item added successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to add test food item", e);
                    });
            } else {
                Log.e(TAG, "Cannot add test food item: userRef is null");
            }
        }
    }

    private void setupListeners() {
        // Meal type selection
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnBreakfast) {
                    selectedMealType = "Breakfast";
                } else if (checkedId == R.id.btnLunch) {
                    selectedMealType = "Lunch";
                } else if (checkedId == R.id.btnDinner) {
                    selectedMealType = "Dinner";
                } else if (checkedId == R.id.btnSnacks) {
                    selectedMealType = "Snacks";
                }
                Log.d(TAG, "Selected meal type: " + selectedMealType);
                
                // Update recommendations when meal type changes
                updateRecommendations();
            }
        });

        // Add food button
        btnAddFood.setOnClickListener(v -> showFoodSelectionDialog());
        
        // Tab selection to switch between Today's log and Categorized log
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Today's Log
                    cardTodayLog.setVisibility(View.VISIBLE);
                    cardCategorizedLog.setVisibility(View.GONE);
                    Log.d(TAG, "Switched to Today's Log tab");
                } else {
                    // Categorized Log
                    cardTodayLog.setVisibility(View.GONE);
                    cardCategorizedLog.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Switched to Categories tab");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerViews() {
        Log.d(TAG, "Setting up RecyclerViews");
        try {
            // Setup divider decoration
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                    requireContext(), LinearLayoutManager.VERTICAL);
            dividerItemDecoration.setDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.divider));
            
            // Today's log
            todayAdapter = new FoodLogAdapter(new ArrayList<>());
            rvFoodLog.setAdapter(todayAdapter);
            rvFoodLog.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvFoodLog.addItemDecoration(dividerItemDecoration);
            
            // Breakfast
            breakfastAdapter = new FoodLogAdapter(new ArrayList<>());
            rvBreakfast.setAdapter(breakfastAdapter);
            rvBreakfast.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvBreakfast.addItemDecoration(dividerItemDecoration);
            
            // Lunch
            lunchAdapter = new FoodLogAdapter(new ArrayList<>());
            rvLunch.setAdapter(lunchAdapter);
            rvLunch.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvLunch.addItemDecoration(dividerItemDecoration);
            
            // Dinner
            dinnerAdapter = new FoodLogAdapter(new ArrayList<>());
            rvDinner.setAdapter(dinnerAdapter);
            rvDinner.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvDinner.addItemDecoration(dividerItemDecoration);
            
            // Snacks
            snacksAdapter = new FoodLogAdapter(new ArrayList<>());
            rvSnacks.setAdapter(snacksAdapter);
            rvSnacks.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvSnacks.addItemDecoration(dividerItemDecoration);
            
            // Others
            othersAdapter = new FoodLogAdapter(new ArrayList<>());
            rvOthers.setAdapter(othersAdapter);
            rvOthers.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvOthers.addItemDecoration(dividerItemDecoration);
            
            // Setup recommendations RecyclerView
            recommendationsAdapter = new RecommendationsAdapter(food -> {
                // When a recommendation is clicked, create a FoodItem and save it
                FoodItem foodItem = new FoodItem();
                foodItem.setName(food.getName());
                foodItem.setCaloriesInt((int) food.calculateCalories(food.getServingSize()));
                foodItem.setProtein(food.calculateProtein(food.getServingSize()));
                foodItem.setCarbs(food.calculateCarbs(food.getServingSize()));
                foodItem.setFat(food.calculateFat(food.getServingSize()));
                foodItem.setMealType(selectedMealType);
                
                // Save the food item
                saveFood(foodItem);
            });
            
            rvRecommendations.setAdapter(recommendationsAdapter);
            rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
            
            // Add divider
            rvRecommendations.addItemDecoration(dividerItemDecoration);
            
            Log.d(TAG, "RecyclerView setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerViews: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error setting up lists", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTodaysFoodItems() {
        Log.d(TAG, "Loading food items for date: " + today);
        
        if (userRef == null) {
            Log.e(TAG, "userRef is null, cannot load food items");
            showError("Database reference is not initialized");
            return;
        }

        // Debug output of the exact Firebase path being queried
        DatabaseReference todayRef = userRef.child(today);
        Log.d(TAG, "Firebase query path: " + todayRef.toString());

        // First check if data exists for today
        todayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "No data exists for today: " + today);
                    // Clear lists and update UI to show no items
                    allFoodItems.clear();
                    breakfastItems.clear();
                    lunchItems.clear();
                    dinnerItems.clear();
                    snacksItems.clear();
                    othersItems.clear();
                    
                    updateTodayFoodLog(allFoodItems);
                    updateCategoryFoodLog(breakfastItems, lunchItems, dinnerItems, snacksItems, othersItems);
                    return;
                }
                
                Log.d(TAG, "Data found for today with " + dataSnapshot.getChildrenCount() + " items");
                
                // If data exists, proceed with continuous listener
                setupContinuousDataListener(todayRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error checking data existence: " + databaseError.getMessage());
            }
        });
    }
    
    private void setupContinuousDataListener(DatabaseReference todayRef) {
        todayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Data changed in Firebase, updating UI");
                Log.d(TAG, "Number of food items found: " + dataSnapshot.getChildrenCount());
                
                // Clear previous data
                allFoodItems.clear();
                breakfastItems.clear();
                lunchItems.clear();
                dinnerItems.clear();
                snacksItems.clear();
                othersItems.clear();
                
                // Parse raw data for debugging
                try {
                    Log.d(TAG, "Raw data: " + dataSnapshot.getValue().toString());
                } catch (Exception e) {
                    Log.w(TAG, "Could not log raw data: " + e.getMessage());
                }
                
                // Process each food item
                int errorCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        // Log the raw food data
                        Log.d(TAG, "Food data: " + snapshot.getKey() + " = " + snapshot.getValue());
                        
                        // Create a new FoodItem for each data snapshot
                        FoodItem foodItem = new FoodItem();
                        
                        // Essential fields with null checks
                        String name = snapshot.child("name").exists() ? 
                            snapshot.child("name").getValue(String.class) : null;
                        
                        Integer calories = snapshot.child("calories").exists() ? 
                            snapshot.child("calories").getValue(Integer.class) : null;
                            
                        String time = snapshot.child("time").exists() ? 
                            snapshot.child("time").getValue(String.class) : null;
                            
                        String mealType = snapshot.child("mealType").exists() ? 
                            snapshot.child("mealType").getValue(String.class) : null;

                        // Debug each value
                        Log.d(TAG, "Parsed values - name: " + name + 
                            ", calories: " + calories + 
                            ", time: " + time + 
                            ", mealType: " + mealType);
                        
                        // Skip if essential data is missing
                        if (name == null || calories == null) {
                            Log.e(TAG, "Skipping item due to missing essential data");
                            errorCount++;
                            continue;
                        }

                        foodItem.setName(name);
                        foodItem.setCaloriesInt(calories);
                        foodItem.setTime(time != null ? time : "00:00");
                        
                        // Default to "Others" if meal type is missing
                        foodItem.setMealType(mealType != null ? mealType : "Others");
                        
                        // Optional nutrition info
                        if (snapshot.child("protein").exists()) {
                            Double protein = snapshot.child("protein").getValue(Double.class);
                            if (protein != null) foodItem.setProtein(protein);
                        }
                        
                        if (snapshot.child("carbs").exists()) {
                            Double carbs = snapshot.child("carbs").getValue(Double.class);
                            if (carbs != null) foodItem.setCarbs(carbs);
                        }
                        
                        if (snapshot.child("fat").exists()) {
                            Double fat = snapshot.child("fat").getValue(Double.class);
                            if (fat != null) foodItem.setFat(fat);
                        }

                        // Add to appropriate lists
                        allFoodItems.add(foodItem);
                        
                        // Add to category lists
                        switch (foodItem.getMealType()) {
                            case "Breakfast":
                                breakfastItems.add(foodItem);
                                break;
                            case "Lunch":
                                lunchItems.add(foodItem);
                                break;
                            case "Dinner":
                                dinnerItems.add(foodItem);
                                break;
                            case "Snacks":
                                snacksItems.add(foodItem);
                                break;
                            default:
                                othersItems.add(foodItem);
                                break;
                        }
                        
                        Log.d(TAG, "Added food item: " + foodItem.getName() + 
                            " (" + foodItem.getCaloriesInt() + " calories) to " +
                            foodItem.getMealType() + " list");
                    } catch (Exception e) {
                        errorCount++;
                        Log.e(TAG, "Error processing food item: " + e.getMessage(), e);
                    }
                }
                
                // Log collection sizes and any errors
                Log.d(TAG, "Collection sizes - All: " + allFoodItems.size() + 
                    ", Breakfast: " + breakfastItems.size() + 
                    ", Lunch: " + lunchItems.size() + 
                    ", Dinner: " + dinnerItems.size() + 
                    ", Snacks: " + snacksItems.size() + 
                    ", Others: " + othersItems.size());
                
                if (errorCount > 0) {
                    Log.w(TAG, "Encountered " + errorCount + " errors while processing food items");
                }
                
                // Update today's food log
                updateTodayFoodLog(allFoodItems);
                
                // Update category food logs
                updateCategoryFoodLog(breakfastItems, lunchItems, dinnerItems, snacksItems, othersItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                showError("Error loading food items: " + error.getMessage());
            }
        });
    }
    
    private void updateTodayFoodLog(List<FoodItem> foodItems) {
        Log.d(TAG, "Updating today's food log with " + (foodItems != null ? foodItems.size() : "null") + " items");
        
        if (foodItems == null || foodItems.isEmpty()) {
            if (txtNoItems != null) {
                txtNoItems.setText("No food items logged yet");
                txtNoItems.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "txtNoItems is null in updateTodayFoodLog");
            }
            
            if (rvFoodLog != null) {
                rvFoodLog.setVisibility(View.GONE);
            } else {
                Log.e(TAG, "rvFoodLog is null in updateTodayFoodLog");
            }
            
            Log.d(TAG, "Today's food log is empty, showing 'No items' message");
        } else {
            if (txtNoItems != null) {
                txtNoItems.setVisibility(View.GONE);
            }
            
            if (rvFoodLog != null) {
                rvFoodLog.setVisibility(View.VISIBLE);
                if (todayAdapter != null) {
                    todayAdapter.updateFoodItems(foodItems);
                    Log.d(TAG, "Today's food log updated with " + foodItems.size() + " items");
                    
                    // Debug log each item
                    for (FoodItem item : foodItems) {
                        if (item != null) {
                            Log.d(TAG, "Today's log item: " + item.getName() + 
                                ", " + item.getCaloriesInt() + " calories" + 
                                ", meal type: " + (item.getMealType() != null ? item.getMealType() : "null"));
                        } else {
                            Log.e(TAG, "Null food item in today's food log");
                        }
                    }
                } else {
                    Log.e(TAG, "todayAdapter is null in updateTodayFoodLog");
                }
            } else {
                Log.e(TAG, "rvFoodLog is null in updateTodayFoodLog");
            }
        }
    }
    
    private void updateCategoryFoodLog(List<FoodItem> breakfastItems, List<FoodItem> lunchItems, 
                                      List<FoodItem> dinnerItems, List<FoodItem> snacksItems, 
                                      List<FoodItem> othersItems) {
        // Update Breakfast
        updateCategorySection("Breakfast", breakfastItems, tvNoBreakfast, rvBreakfast, breakfastAdapter);
        
        // Update Lunch
        updateCategorySection("Lunch", lunchItems, tvNoLunch, rvLunch, lunchAdapter);
        
        // Update Dinner
        updateCategorySection("Dinner", dinnerItems, tvNoDinner, rvDinner, dinnerAdapter);
        
        // Update Snacks
        updateCategorySection("Snacks", snacksItems, tvNoSnacks, rvSnacks, snacksAdapter);
        
        // Update Others
        updateCategorySection("Others", othersItems, tvNoOthers, rvOthers, othersAdapter);
    }
    
    private void updateCategorySection(String category, List<FoodItem> items, TextView noItemsText, 
                                      RecyclerView recyclerView, FoodLogAdapter adapter) {
        if (category == null) {
            Log.e(TAG, "updateCategorySection called with null category");
            return;
        }
        
        Log.d(TAG, "Updating " + category + " section with " + 
            (items != null ? items.size() : "null") + " items");
        
        if (items == null || items.isEmpty()) {
            if (noItemsText != null) {
                noItemsText.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, category + ": noItemsText is null");
            }
            
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            } else {
                Log.e(TAG, category + ": recyclerView is null");
            }
            
            Log.d(TAG, category + " is empty, showing 'No items' message");
        } else {
            if (noItemsText != null) {
                noItemsText.setVisibility(View.GONE);
            }
            
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
                
                if (adapter != null) {
                    adapter.updateFoodItems(items);
                    Log.d(TAG, category + " updated with " + items.size() + " items");
                    
                    // Debug log each item
                    for (FoodItem item : items) {
                        if (item != null) {
                            Log.d(TAG, category + " item: " + item.getName() + 
                                ", " + item.getCaloriesInt() + " calories");
                        } else {
                            Log.e(TAG, "Null food item in " + category + " category");
                        }
                    }
                } else {
                    Log.e(TAG, category + ": adapter is null");
                }
            } else {
                Log.e(TAG, category + ": recyclerView is null");
            }
        }
    }

    private void showFoodSelectionDialog() {
        FoodSelectionDialog dialog = FoodSelectionDialog.newInstance(selectedMealType);
        dialog.setOnFoodSelectedListener(this);
        dialog.show(getChildFragmentManager(), "food_selection");
        Log.d(TAG, "Showing food selection dialog for meal type: " + selectedMealType);
    }

    @Override
    public void onFoodSelected(FoodItem foodItem) {
        if (foodItem != null) {
            Log.d(TAG, "Food selected: " + foodItem.getName() + ", Meal type: " + foodItem.getMealType());
            saveFood(foodItem);
        } else {
            Log.e(TAG, "onFoodSelected called with null food item");
        }
    }

    private void saveFood(FoodItem foodItem) {
        if (foodItem == null) {
            Log.e(TAG, "Cannot save null food item");
            return;
        }
        
        Log.d(TAG, "Saving food item: " + foodItem.getName() + " with calories: " + foodItem.getCaloriesInt());
        
        // Ensure all required fields are set
        if (foodItem.getName() == null || foodItem.getName().isEmpty()) {
            Log.e(TAG, "Food item name is empty");
            return;
        }
        
        if (foodItem.getTime() == null || foodItem.getTime().isEmpty()) {
            // Set current time if missing
            String currentTime = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            foodItem.setTime(currentTime);
            Log.d(TAG, "Setting default time: " + currentTime);
        }
        
        if (foodItem.getMealType() == null || foodItem.getMealType().isEmpty()) {
            foodItem.setMealType(selectedMealType);
            Log.d(TAG, "Setting default meal type: " + selectedMealType);
        }
        
        String foodId = userRef.child(today).push().getKey();
        if (foodId != null) {
            Map<String, Object> foodValues = new HashMap<>();
            foodValues.put("name", foodItem.getName());
            foodValues.put("calories", foodItem.getCaloriesInt());
            foodValues.put("time", foodItem.getTime());
            foodValues.put("mealType", foodItem.getMealType());
            
            // Add nutrition info if available
            if (foodItem.getProtein() > 0) foodValues.put("protein", foodItem.getProtein());
            if (foodItem.getCarbs() > 0) foodValues.put("carbs", foodItem.getCarbs());
            if (foodItem.getFat() > 0) foodValues.put("fat", foodItem.getFat());
            
            Log.d(TAG, "Saving food item values: " + foodValues.toString());
            Log.d(TAG, "Firebase path: " + userRef.child(today).child(foodId).toString());

            userRef.child(today).child(foodId).setValue(foodValues)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Food item saved successfully: " + foodItem.getName());
                    Toast.makeText(getContext(), "Food logged successfully", Toast.LENGTH_SHORT).show();
                    
                    // Update recommendations after saving
                    updateRecommendations();
                    
                    // Force refresh after a delay to ensure the data is loaded
                    if (getView() != null) {
                        getView().postDelayed(() -> loadTodaysFoodItems(), 500);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving food item", e);
                    Toast.makeText(getContext(), "Error saving food item", Toast.LENGTH_SHORT).show();
                });
        } else {
            Log.e(TAG, "Failed to generate food ID");
        }
    }
    
    private void updateRecommendations() {
        // Calculate remaining calories
        int consumedCalories = calculateConsumedCalories();
        int remainingCalories = TARGET_DAILY_CALORIES - consumedCalories;
        
        // Get available foods from FoodItemService
        List<PredefinedFoodItem> availableFoods = FoodItemService.loadFoodItems(requireContext());
        
        // Convert FoodItem list to String list of food names
        List<String> recentFoodNames = new ArrayList<>();
        for (FoodItem item : allFoodItems) {
            recentFoodNames.add(item.getName());
        }
        
        // Get recommendations
        List<PredefinedFoodItem> recommendations = RecommendationService.getRecommendations(
            recentFoodNames,  // Pass the list of food names instead of FoodItem objects
            selectedMealType,
            remainingCalories,
            availableFoods
        );
        
        // Update UI
        if (recommendations.isEmpty()) {
            tvNoRecommendations.setVisibility(View.VISIBLE);
            rvRecommendations.setVisibility(View.GONE);
        } else {
            tvNoRecommendations.setVisibility(View.GONE);
            rvRecommendations.setVisibility(View.VISIBLE);
            recommendationsAdapter.updateRecommendations(recommendations);
        }
    }
    
    private int calculateConsumedCalories() {
        int total = 0;
        for (FoodItem item : allFoodItems) {
            total += item.getCaloriesInt();
        }
        return total;
    }
    
    private void showError(String message) {
        txtNoItems.setText(message);
        txtNoItems.setVisibility(View.VISIBLE);
        rvFoodLog.setVisibility(View.GONE);
        
        // Also hide category recycler views
        rvBreakfast.setVisibility(View.GONE);
        rvLunch.setVisibility(View.GONE);
        rvDinner.setVisibility(View.GONE);
        rvSnacks.setVisibility(View.GONE);
        rvOthers.setVisibility(View.GONE);
        
        Log.e(TAG, "Error shown to user: " + message);
    }

    /**
     * Sets the default meal type based on the current time of day
     */
    private void setDefaultMealTypeByTime() {
        Calendar now = Calendar.getInstance();
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
        
        // Time-based meal type selection:
        // 5:00 AM - 10:59 AM: Breakfast
        // 11:00 AM - 3:59 PM: Lunch
        // 4:00 PM - 8:59 PM: Dinner
        // 9:00 PM - 4:59 AM: Snacks
        
        if (hourOfDay >= 5 && hourOfDay < 11) {
            selectedMealType = "Breakfast";
        } else if (hourOfDay >= 11 && hourOfDay < 16) {
            selectedMealType = "Lunch";
        } else if (hourOfDay >= 16 && hourOfDay < 21) {
            selectedMealType = "Dinner";
        } else {
            selectedMealType = "Snacks";
        }
        
        Log.d(TAG, "Default meal type set to " + selectedMealType + " based on current time: " + hourOfDay + ":00");
    }
    
    /**
     * Sets the toggle button based on the selected meal type
     */
    private void setToggleButtonForMealType() {
        if (toggleGroup == null) {
            Log.e(TAG, "Toggle group is null");
            return;
        }
        
        int buttonId;
        switch (selectedMealType) {
            case "Breakfast":
                buttonId = R.id.btnBreakfast;
                break;
            case "Lunch":
                buttonId = R.id.btnLunch;
                break;
            case "Dinner":
                buttonId = R.id.btnDinner;
                break;
            case "Snacks":
                buttonId = R.id.btnSnacks;
                break;
            default:
                buttonId = R.id.btnBreakfast;
                break;
        }
        
        toggleGroup.check(buttonId);
        Log.d(TAG, "Toggle button set to: " + selectedMealType);
    }
} 