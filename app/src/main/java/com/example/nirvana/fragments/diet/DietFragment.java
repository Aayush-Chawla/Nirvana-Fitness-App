package com.example.nirvana.fragments.diet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nirvana.R;
import com.example.nirvana.data.models.FoodItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DietFragment extends Fragment {

    private PieChart macrosChart;
    private MaterialButton btnLogDiet;
    private TextView tvCalories, tvRemaining, tvGoal;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        initializeViews(view);
        initializeFirebase();
        setupButtonListeners();
        loadUserDietData();

        return view;
    }

    private void initializeViews(View view) {
        macrosChart = view.findViewById(R.id.macrosChart);
        btnLogDiet = view.findViewById(R.id.btnLogDiet);
        tvCalories = view.findViewById(R.id.tvCalories);
        tvRemaining = view.findViewById(R.id.tvRemaining);
        tvGoal = view.findViewById(R.id.tvGoal);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();
    }

    private void setupButtonListeners() {
        btnLogDiet.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dietFragment_to_logDietFragment));
    }

    private void loadUserDietData() {
        mDatabase.child("users").child(userId).child("meals").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                calculateNutrientTotals(snapshot);
                fetchCalorieGoal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void calculateNutrientTotals(DataSnapshot snapshot) {
        long totalCalories = 0;
        long totalProtein = 0;
        long totalCarbs = 0;
        long totalFat = 0;

        for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
            for (DataSnapshot foodSnapshot : mealSnapshot.getChildren()) {
                FoodItem foodItem = foodSnapshot.getValue(FoodItem.class);
                if (foodItem != null) {
                    totalCalories += foodItem.getCalories();
                    totalProtein += foodItem.getProtein();
                    totalCarbs += foodItem.getCarbs();
                    totalFat += foodItem.getFat();
                }
            }
        }

        updateUI(totalCalories, totalProtein, totalCarbs, totalFat);
    }

    private void fetchCalorieGoal() {
        mDatabase.child("users").child(userId).child("diet").child("goal").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Long goal = task.getResult().getValue(Long.class);
                        if (goal != null) {
                            updateCalorieGoal(goal);
                        }
                    }
                });
    }

    private void updateUI(long calories, long protein, long carbs, long fat) {
        tvCalories.setText(String.valueOf(calories));
        setupMacrosChart(protein, carbs, fat);
    }

    private void updateCalorieGoal(long goal) {
        tvGoal.setText(String.valueOf(goal));
        long remaining = goal - Long.parseLong(tvCalories.getText().toString());
        tvRemaining.setText(String.valueOf(remaining));
    }

    private void setupMacrosChart(long protein, long carbs, long fat) {
        List<PieEntry> entries = new ArrayList<>();
        if (protein > 0) entries.add(new PieEntry(protein, "Protein"));
        if (carbs > 0) entries.add(new PieEntry(carbs, "Carbs"));
        if (fat > 0) entries.add(new PieEntry(fat, "Fat"));

        PieDataSet dataSet = new PieDataSet(entries, "Macros Breakdown");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        macrosChart.setData(new PieData(dataSet));
        macrosChart.getDescription().setEnabled(false);
        macrosChart.setCenterText("Macros");
        macrosChart.setEntryLabelColor(getResources().getColor(android.R.color.black));
        macrosChart.animateY(1000);
        macrosChart.invalidate();
    }
}