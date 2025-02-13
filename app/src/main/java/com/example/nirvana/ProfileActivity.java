package com.example.nirvana;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etName, etAge, etWeight, etHeight, etFitnessGoals;
    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind views
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etFitnessGoals = findViewById(R.id.etFitnessGoals);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Set click listener for save profile button
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        String fitnessGoals = etFitnessGoals.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty() || weight.isEmpty() || height.isEmpty() || fitnessGoals.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user ID
        String userId = mAuth.getCurrentUser().getUid();

        // Create a profile map
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("age", age);
        profile.put("weight", weight);
        profile.put("height", height);
        profile.put("fitnessGoals", fitnessGoals);

        // Save profile to Firestore
        db.collection("users").document(userId)
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile saved successfully");
                    Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving profile: " + e.getMessage());
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}