package com.example.nirvana;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {
    private static final String TAG = "ProfileSetupActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etName, etAge, etWeight, etHeight;
    private Spinner spinnerFitnessGoals, spinnerActivityLevel;
    private Button btnMale, btnFemale, btnSaveProfile;
    private String selectedGender = ""; // Holds selected gender

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup); // Updated XML file

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind views
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        spinnerFitnessGoals = findViewById(R.id.spinnerFitnessGoal);
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel);
        btnMale = findViewById(R.id.btnMale);
        btnFemale = findViewById(R.id.btnFemale);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Gender Selection Logic
        btnMale.setOnClickListener(v -> selectGender("Male"));
        btnFemale.setOnClickListener(v -> selectGender("Female"));

        // Set click listener for save profile button
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    // Method to handle gender selection
    private void selectGender(String gender) {
        selectedGender = gender;

        // Highlight selected button
        if (gender.equals("Male")) {
            btnMale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primaryColor));
            btnMale.setTextColor(Color.WHITE);

            btnFemale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light));
            btnFemale.setTextColor(Color.BLACK);
        } else {
            btnFemale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primaryColor));
            btnFemale.setTextColor(Color.WHITE);

            btnMale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light));
            btnMale.setTextColor(Color.BLACK);
        }
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        String fitnessGoal = spinnerFitnessGoals.getSelectedItem().toString();
        String activityLevel = spinnerActivityLevel.getSelectedItem().toString();

        if (name.isEmpty() || age.isEmpty() || weight.isEmpty() || height.isEmpty() || selectedGender.isEmpty()) {
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
        profile.put("gender", selectedGender);
        profile.put("fitnessGoal", fitnessGoal);
        profile.put("activityLevel", activityLevel);

        // Save profile to Firestore
        db.collection("users").document(userId)
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile saved successfully");
                    Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();

                    // Navigate to MainActivity after saving profile
                    Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving profile: " + e.getMessage());
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
