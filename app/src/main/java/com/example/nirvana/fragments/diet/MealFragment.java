package com.example.nirvana.fragments.diet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.models.FoodItem;
import com.example.nirvana.ui.adapters.FoodItemAdapter;
import com.example.nirvana.utils.FirestoreHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class MealFragment extends Fragment {

    private static final String ARG_MEAL_TYPE = "mealType";
    private String mealType;

    // Views
    private TextView tvTotalCalories;
    private TextView tvTotalProtein;
    private TextView tvTotalCarbs;
    private TextView tvTotalFat;
    private RecyclerView rvFoodItems;
    private TextView tvEmptyState;

    // Adapter
    private FoodItemAdapter adapter;
    private List<FoodItem> foodItems = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private String userId;
    private ListenerRegistration mealsListener;

    public MealFragment() {
        // Required empty public constructor
    }

    public static MealFragment newInstance(String mealType) {
        MealFragment fragment = new MealFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEAL_TYPE, mealType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mealType = getArguments().getString(ARG_MEAL_TYPE);
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal, container, false);

        // Initialize views
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvTotalProtein = view.findViewById(R.id.tvTotalProtein);
        tvTotalCarbs = view.findViewById(R.id.tvTotalCarbs);
        tvTotalFat = view.findViewById(R.id.tvTotalFat);
        rvFoodItems = view.findViewById(R.id.rvFoodItems);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        // Setup RecyclerView
        rvFoodItems.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FoodItemAdapter(foodItems, new FoodItemAdapter.OnFoodItemClickListener() {
            @Override
            public void onFoodItemClick(FoodItem foodItem) {
                // Handle item click (e.g., edit or delete)
                showFoodItemOptions(foodItem);
            }
            
            @Override
            public void onDeleteClick(FoodItem foodItem, int position) {
                deleteFoodItemByPosition(foodItem, position);
            }
        });
        rvFoodItems.setAdapter(adapter);

        // Load meal items
        loadMealItems();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listeners to prevent memory leaks
        if (mealsListener != null) {
            mealsListener.remove();
        }
    }

    private void loadMealItems() {
        // Clear any existing listener to prevent memory leaks
        if (mealsListener != null) {
            mealsListener.remove();
        }

        // Get the user document reference
        DocumentReference userRef = FirestoreHelper.getUserDocRef();
        if (userRef == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get today's date in the format used by the app
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        
        // Log what we're listening to
        Log.d("MealFragment", "Setting up real-time listener for: " + FirestoreHelper.MEALS_COLLECTION + "/" + today + "/" + mealType.toLowerCase());

        // Set up real-time listener for the meal type
        mealsListener = userRef.collection(FirestoreHelper.MEALS_COLLECTION)
            .document(today)
            .collection(mealType.toLowerCase())
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e("MealFragment", "Listen failed: " + error);
                    Toast.makeText(getContext(), "Error loading meals: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshots == null) {
                    Log.d("MealFragment", "No snapshots received");
                    return;
                }

                Log.d("MealFragment", "Received " + snapshots.getDocuments().size() + " items for " + mealType);

                // Process meal data
                foodItems.clear();
                double totalCalories = 0;
                double totalProtein = 0;
                double totalCarbs = 0;
                double totalFat = 0;

                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Map<String, Object> item = document.getData();
                    if (item != null) {
                        item.put("docId", document.getId());
                        FoodItem foodItem = convertMapToFoodItem(item);
                        if (foodItem != null) {
                            foodItems.add(foodItem);
                            totalCalories += foodItem.getCalories();
                            totalProtein += foodItem.getProtein();
                            totalCarbs += foodItem.getCarbs();
                            totalFat += foodItem.getFat();
                        }
                    }
                }

                // Update UI with the data
                updateMealDisplay(totalCalories, totalProtein, totalCarbs, totalFat);
            });
    }

    private void updateMealDisplay(double totalCalories, double totalProtein, double totalCarbs, double totalFat) {
        // Update nutritional totals
        tvTotalCalories.setText(String.format("%.0f", totalCalories));
        tvTotalProtein.setText(String.format("%.0fg", totalProtein));
        tvTotalCarbs.setText(String.format("%.0fg", totalCarbs));
        tvTotalFat.setText(String.format("%.0fg", totalFat));

        // Show empty state if no items
        if (foodItems.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvFoodItems.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvFoodItems.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private FoodItem convertMapToFoodItem(Map<String, Object> map) {
        try {
            String id = map.containsKey("id") ? (String) map.get("id") : "";
            String name = map.containsKey("name") ? (String) map.get("name") : "";
            
            // Handle various numeric types
            int calories = 0;
            if (map.containsKey("calories")) {
                Object cal = map.get("calories");
                if (cal instanceof Long) {
                    calories = ((Long) cal).intValue();
                } else if (cal instanceof Integer) {
                    calories = (Integer) cal;
                } else if (cal instanceof Double) {
                    calories = ((Double) cal).intValue();
                }
            }
            
            // Get serving size as string from the map, default to "100g"
            String servingSize = "100g";
            if (map.containsKey("servingSize") && map.get("servingSize") != null) {
                servingSize = (String) map.get("servingSize");
            }

            // Handle various numeric types for macros
            double protein = 0;
            if (map.containsKey("protein")) {
                Object prot = map.get("protein");
                if (prot instanceof Long) {
                    protein = ((Long) prot).doubleValue();
                } else if (prot instanceof Integer) {
                    protein = ((Integer) prot).doubleValue();
                } else if (prot instanceof Double) {
                    protein = (Double) prot;
                }
            }

            double carbs = 0;
            if (map.containsKey("carbs")) {
                Object carb = map.get("carbs");
                if (carb instanceof Long) {
                    carbs = ((Long) carb).doubleValue();
                } else if (carb instanceof Integer) {
                    carbs = ((Integer) carb).doubleValue();
                } else if (carb instanceof Double) {
                    carbs = (Double) carb;
                }
            }

            double fat = 0;
            if (map.containsKey("fat")) {
                Object f = map.get("fat");
                if (f instanceof Long) {
                    fat = ((Long) f).doubleValue();
                } else if (f instanceof Integer) {
                    fat = ((Integer) f).doubleValue();
                } else if (f instanceof Double) {
                    fat = (Double) f;
                }
            }
            
            // Create food item with the extracted values
            FoodItem foodItem = new FoodItem(id, name, calories, servingSize, protein, carbs, fat);
            
            // Also save the document ID for later use
            if (map.containsKey("docId")) {
                foodItem.setDocId((String) map.get("docId"));
            }
            
            return foodItem;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showFoodItemOptions(FoodItem foodItem) {
        // Implement dialog for edit/delete options
        // We'll add this in the next step
    }

    public void addFoodItem(FoodItem foodItem) {
        // Use FirestoreHelper to log food
        FirestoreHelper.logFood(mealType, foodItem, foodItem.getServingSize(), new FirestoreHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                refreshFoodList();
                Toast.makeText(getContext(), "Food item added to " + mealType, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error adding food item: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getMealType() {
        return mealType;
    }

    public void refreshFoodList() {
        loadMealItems();
    }

    private void deleteFoodItemByPosition(FoodItem foodItem, int position) {
        if (foodItem.getDocId() == null || foodItem.getDocId().isEmpty()) {
            Toast.makeText(getContext(), "Cannot delete item: missing document ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use FirestoreHelper to delete food item
        FirestoreHelper.deleteFoodItem(mealType, foodItem.getDocId(), foodItem.getCalories(), new FirestoreHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                // Remove from local list
                if (position >= 0 && position < foodItems.size()) {
                    foodItems.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateNutrientTotals();
                }
                Toast.makeText(getContext(), "Food item deleted", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error deleting food item: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNutrientTotals() {
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;

        for (FoodItem foodItem : foodItems) {
            totalCalories += foodItem.getCalories();
            totalProtein += foodItem.getProtein();
            totalCarbs += foodItem.getCarbs();
            totalFat += foodItem.getFat();
        }

        // Update UI
        tvTotalCalories.setText(String.format("%.0f", totalCalories));
        tvTotalProtein.setText(String.format("%.0fg", totalProtein));
        tvTotalCarbs.setText(String.format("%.0fg", totalCarbs));
        tvTotalFat.setText(String.format("%.0fg", totalFat));
    }
}