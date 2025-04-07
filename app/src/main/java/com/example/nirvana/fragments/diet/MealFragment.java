package com.example.nirvana.fragments.diet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.FoodItem;
import com.example.nirvana.ui.adapters.FoodItemAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MealFragment extends Fragment {

    private static final String ARG_MEAL_TYPE = "mealType";
    private String mealType;

    // Views
    private TextView tvMealHeader;
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
    private DatabaseReference mDatabase;
    private String userId;

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
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal, container, false);

        // Initialize views
        tvMealHeader = view.findViewById(R.id.tvMealHeader);
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvTotalProtein = view.findViewById(R.id.tvTotalProtein);
        tvTotalCarbs = view.findViewById(R.id.tvTotalCarbs);
        tvTotalFat = view.findViewById(R.id.tvTotalFat);
        rvFoodItems = view.findViewById(R.id.rvFoodItems);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        // Set meal header
        tvMealHeader.setText(mealType);

        // Setup RecyclerView
        rvFoodItems.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FoodItemAdapter(foodItems, foodItem -> {
            // Handle item click (e.g., edit or delete)
            showFoodItemOptions(foodItem);
        });
        rvFoodItems.setAdapter(adapter);

        // Load meal items
        loadMealItems();

        return view;
    }

    private void loadMealItems() {
        mDatabase.child("users").child(userId).child("meals").child(mealType)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        foodItems.clear();
                        double totalCalories = 0;
                        double totalProtein = 0;
                        double totalCarbs = 0;
                        double totalFat = 0;

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FoodItem foodItem = dataSnapshot.getValue(FoodItem.class);
                            if (foodItem != null) {
                                foodItems.add(foodItem);
                                totalCalories += foodItem.getCalories();
                                totalProtein += foodItem.getProtein();
                                totalCarbs += foodItem.getCarbs();
                                totalFat += foodItem.getFat();
                            }
                        }

                        // Update totals
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void showFoodItemOptions(FoodItem foodItem) {
        // Implement dialog for edit/delete options
        // We'll add this in the next step
    }

    public void addFoodItem(FoodItem foodItem) {
        String key = mDatabase.child("users").child(userId).child("meals").child(mealType).push().getKey();
        if (key != null) {
            mDatabase.child("users").child(userId).child("meals").child(mealType).child(key).setValue(foodItem);
        }
    }

    public String getMealType() {
        return mealType;
    }

    public void refreshFoodList() {
        loadMealItems();
    }

    private void deleteFoodItem(FoodItem foodItem, String key) {
        mDatabase.child("users").child(userId).child("meals").child(mealType).child(key).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Item deleted successfully
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
}