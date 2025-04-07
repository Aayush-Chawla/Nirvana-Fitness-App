package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nirvana.R;
import com.example.nirvana.data.models.FoodItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogFoodFragment extends Fragment {

    private TextInputEditText edtFoodName;
    private TextInputEditText edtCalories;
    private MaterialButton btnSave;
    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(userId);

        // Initialize views
        edtFoodName = view.findViewById(R.id.edtFoodName);
        edtCalories = view.findViewById(R.id.edtCalories);
        btnSave = view.findViewById(R.id.btnSave);

        // Setup click listener
        btnSave.setOnClickListener(v -> saveFood());
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
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FoodItem foodItem = new FoodItem();
        foodItem.setName(foodName);
        foodItem.setCalories(calories);
        foodItem.setTime(currentTime);

        userRef.child("foodLog")
            .child(today)
            .push()
            .setValue(foodItem)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "Food logged successfully", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(requireContext(), "Failed to log food: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }
} 