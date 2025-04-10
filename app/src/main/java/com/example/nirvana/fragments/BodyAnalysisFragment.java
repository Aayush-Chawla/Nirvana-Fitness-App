package com.example.nirvana.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.nirvana.R;
import com.example.nirvana.models.BodyComposition;
import com.example.nirvana.services.BodyCompositionAnalyzer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Fragment for body composition analysis from user photos
 */
public class BodyAnalysisFragment extends Fragment {
    private static final String TAG = "BodyAnalysisFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    
    private ImageView ivBodyPhoto;
    private Button btnTakePhoto;
    private Button btnPickPhoto;
    private Button btnAnalyze;
    private ProgressBar progressBar;
    private CardView resultsCard;
    private TextView tvResults;
    
    private Bitmap userPhoto;
    private BodyCompositionAnalyzer analyzer;
    private DatabaseReference userRef;
    
    private int userAge = 30; // Default
    private float userWeight = 70; // Default in kg
    private float userHeight = 170; // Default in cm
    private String userGender = "Male"; // Default
    private String activityLevel = "Moderate"; // Default
    
    // UI Elements
    private RadioGroup radioGender;
    private RadioButton radioMale, radioFemale;
    private TextInputEditText editAge, editHeight, editWeight, editWaist;
    private Button btnCalculate;
    private CardView cardResults;
    private TextView txtBmiValue, txtBmiCategory, txtBodyFatValue, txtBodyFatCategory;
    private TextView txtBmrValue, txtRecommendations;

    // Constants for BMI categories
    private static final double UNDERWEIGHT_THRESHOLD = 18.5;
    private static final double NORMAL_THRESHOLD = 24.9;
    private static final double OVERWEIGHT_THRESHOLD = 29.9;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyzer = new BodyCompositionAnalyzer(requireContext());
        
        // Setup Firebase reference for user profile data
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("profile");
            loadUserProfile();
        } catch (Exception e) {
            Log.w(TAG, "Error setting up Firebase: " + e.getMessage());
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_body_analysis, container, false);
        
        // Initialize views
        ivBodyPhoto = view.findViewById(R.id.ivBodyPhoto);
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
        btnPickPhoto = view.findViewById(R.id.btnPickPhoto);
        btnAnalyze = view.findViewById(R.id.btnAnalyze);
        progressBar = view.findViewById(R.id.progressBar);
        resultsCard = view.findViewById(R.id.resultsCard);
        tvResults = view.findViewById(R.id.tvResults);
        
        // Initial UI state
        btnAnalyze.setEnabled(false);
        resultsCard.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        
        // Set up button listeners
        btnTakePhoto.setOnClickListener(v -> takePhoto());
        btnPickPhoto.setOnClickListener(v -> pickPhoto());
        btnAnalyze.setOnClickListener(v -> analyzePhoto());
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // The new layout doesn't have these views, so we'll remove this initialization
        // initializeViews(view);
        
        // Set up listeners for the new layout
        // btnCalculate.setOnClickListener(v -> calculateBodyComposition());
    }
    
    private void initializeViews(View view) {
        // This method is no longer needed for the new layout
        // We're using the photo-based analysis instead of manual measurements
    }
    
    private void loadUserProfile() {
        if (userRef == null) return;
        
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Load user stats for more accurate analysis
                    if (snapshot.child("age").exists()) {
                        userAge = snapshot.child("age").getValue(Integer.class);
                    }
                    
                    if (snapshot.child("weight").exists()) {
                        userWeight = snapshot.child("weight").getValue(Float.class);
                    }
                    
                    if (snapshot.child("height").exists()) {
                        userHeight = snapshot.child("height").getValue(Float.class);
                    }
                    
                    if (snapshot.child("gender").exists()) {
                        userGender = snapshot.child("gender").getValue(String.class);
                    }
                    
                    if (snapshot.child("activityLevel").exists()) {
                        activityLevel = snapshot.child("activityLevel").getValue(String.class);
                    }
                    
                    Log.d(TAG, "Loaded user profile: age=" + userAge + ", weight=" + userWeight + 
                          ", height=" + userHeight + ", gender=" + userGender);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading user profile", error.toException());
            }
        });
    }
    
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(requireContext(), "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Handle camera photo
                Bundle extras = data.getExtras();
                if (extras != null) {
                    userPhoto = (Bitmap) extras.get("data");
                    ivBodyPhoto.setImageBitmap(userPhoto);
                    btnAnalyze.setEnabled(true);
                }
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // Handle picked photo
                Uri selectedImage = data.getData();
                try {
                    userPhoto = MediaStore.Images.Media.getBitmap(
                            requireActivity().getContentResolver(), selectedImage);
                    ivBodyPhoto.setImageBitmap(userPhoto);
                    btnAnalyze.setEnabled(true);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading image", e);
                    Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void analyzePhoto() {
        if (userPhoto == null) {
            Toast.makeText(requireContext(), "Please take or select a photo first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnAnalyze.setEnabled(false);
        resultsCard.setVisibility(View.GONE);
        
        // Create user stats object
        BodyCompositionAnalyzer.UserStats userStats = 
                new BodyCompositionAnalyzer.UserStats(userAge, userWeight, userHeight, userGender, activityLevel);
        
        // Run analysis
        analyzer.analyzeBodyPhoto(userPhoto, userStats, new BodyCompositionAnalyzer.AnalysisCallback() {
            @Override
            public void onAnalysisComplete(BodyComposition result) {
                requireActivity().runOnUiThread(() -> {
                    // Hide progress
                    progressBar.setVisibility(View.GONE);
                    btnAnalyze.setEnabled(true);
                    
                    // Show results
                    tvResults.setText(result.getSummary());
                    resultsCard.setVisibility(View.VISIBLE);
                    
                    // Save results to Firebase if available
                    saveResultsToFirebase(result);
                });
            }
            
            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() -> {
                    // Hide progress
                    progressBar.setVisibility(View.GONE);
                    btnAnalyze.setEnabled(true);
                    
                    // Show error
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void saveResultsToFirebase(BodyComposition result) {
        if (userRef == null) return;
        
        DatabaseReference bodyCompRef = userRef.getParent().child("body_composition");
        
        // Create a map of the results
        java.util.Map<String, Object> resultMap = new java.util.HashMap<>();
        resultMap.put("bodyFatPercentage", result.getBodyFatPercentage());
        resultMap.put("muscleMassPercentage", result.getMuscleMassPercentage());
        resultMap.put("bmr", result.getBmr());
        resultMap.put("visceralFat", result.getVisceralFat());
        resultMap.put("bodyType", result.getBodyType());
        resultMap.put("timestamp", java.util.Calendar.getInstance().getTimeInMillis());
        
        // Save to Firebase
        bodyCompRef.setValue(resultMap)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Body composition saved to Firebase"))
            .addOnFailureListener(e -> Log.e(TAG, "Error saving body composition", e));
    }

    private void calculateBodyComposition() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get values from inputs
        boolean isMale = radioMale.isChecked();
        int age = Integer.parseInt(editAge.getText().toString());
        double height = Double.parseDouble(editHeight.getText().toString());
        double weight = Double.parseDouble(editWeight.getText().toString());
        
        // Optional waist circumference
        double waist = 0;
        if (!TextUtils.isEmpty(editWaist.getText())) {
            waist = Double.parseDouble(editWaist.getText().toString());
        }

        // Calculate BMI
        double bmi = calculateBMI(weight, height);
        String bmiCategory = getBMICategory(bmi);
        
        // Calculate body fat percentage
        double bodyFat = calculateBodyFat(bmi, age, isMale);
        String bodyFatCategory = getBodyFatCategory(bodyFat, isMale);
        
        // Calculate BMR
        double bmr = calculateBMR(weight, height, age, isMale);
        
        // Generate recommendations
        String recommendations = generateRecommendations(bmi, bodyFat, isMale);

        // Display results
        DecimalFormat df = new DecimalFormat("#.#");
        txtBmiValue.setText(df.format(bmi));
        txtBmiCategory.setText("Category: " + bmiCategory);
        txtBodyFatValue.setText(df.format(bodyFat) + "%");
        txtBodyFatCategory.setText("Category: " + bodyFatCategory);
        txtBmrValue.setText(df.format(bmr) + " calories/day");
        txtRecommendations.setText(recommendations);
        
        // Show results card
        cardResults.setVisibility(View.VISIBLE);
    }
    
    private boolean validateInputs() {
        boolean valid = true;
        
        if (TextUtils.isEmpty(editAge.getText())) {
            editAge.setError("Age is required");
            valid = false;
        } else if (Integer.parseInt(editAge.getText().toString()) < 18 || Integer.parseInt(editAge.getText().toString()) > 100) {
            editAge.setError("Age must be between 18 and 100");
            valid = false;
        }
        
        if (TextUtils.isEmpty(editHeight.getText())) {
            editHeight.setError("Height is required");
            valid = false;
        } else if (Double.parseDouble(editHeight.getText().toString()) < 100 || Double.parseDouble(editHeight.getText().toString()) > 250) {
            editHeight.setError("Height must be in cm (between 100-250)");
            valid = false;
        }
        
        if (TextUtils.isEmpty(editWeight.getText())) {
            editWeight.setError("Weight is required");
            valid = false;
        } else if (Double.parseDouble(editWeight.getText().toString()) < 30 || Double.parseDouble(editWeight.getText().toString()) > 300) {
            editWeight.setError("Weight must be in kg (between 30-300)");
            valid = false;
        }
        
        if (!TextUtils.isEmpty(editWaist.getText())) {
            double waist = Double.parseDouble(editWaist.getText().toString());
            if (waist < 50 || waist > 200) {
                editWaist.setError("Waist circumference must be in cm (between 50-200)");
                valid = false;
            }
        }
        
        return valid;
    }
    
    // Calculate BMI (kg/m²)
    private double calculateBMI(double weight, double height) {
        // Convert height from cm to m
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }
    
    // Get BMI category
    private String getBMICategory(double bmi) {
        if (bmi < UNDERWEIGHT_THRESHOLD) {
            return "Underweight";
        } else if (bmi < NORMAL_THRESHOLD) {
            return "Normal";
        } else if (bmi < OVERWEIGHT_THRESHOLD) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
    
    // Calculate body fat percentage using BMI, age and gender
    private double calculateBodyFat(double bmi, int age, boolean isMale) {
        // Based on the Deurenberg equation
        if (isMale) {
            return (1.20 * bmi) + (0.23 * age) - 16.2;
        } else {
            return (1.20 * bmi) + (0.23 * age) - 5.4;
        }
    }
    
    // Get body fat category
    private String getBodyFatCategory(double bodyFat, boolean isMale) {
        if (isMale) {
            if (bodyFat < 8) return "Very Low";
            if (bodyFat < 15) return "Athletic";
            if (bodyFat < 20) return "Fitness";
            if (bodyFat < 25) return "Average";
            return "High";
        } else {
            if (bodyFat < 15) return "Very Low";
            if (bodyFat < 22) return "Athletic";
            if (bodyFat < 27) return "Fitness";
            if (bodyFat < 32) return "Average";
            return "High";
        }
    }
    
    // Calculate Basal Metabolic Rate using the Mifflin-St Jeor Equation
    private double calculateBMR(double weight, double height, int age, boolean isMale) {
        if (isMale) {
            return (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            return (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }
    }
    
    // Generate personalized recommendations
    private String generateRecommendations(double bmi, double bodyFat, boolean isMale) {
        StringBuilder recommendations = new StringBuilder();
        
        // BMI-based recommendations
        if (bmi < UNDERWEIGHT_THRESHOLD) {
            recommendations.append("• Focus on gradually increasing caloric intake with nutrient-dense foods.\n");
            recommendations.append("• Consider strength training to build muscle mass.\n");
        } else if (bmi < NORMAL_THRESHOLD) {
            recommendations.append("• Maintain your current healthy weight with balanced nutrition.\n");
            recommendations.append("• Aim for at least 150 minutes of moderate exercise weekly.\n");
        } else if (bmi < OVERWEIGHT_THRESHOLD) {
            recommendations.append("• Focus on portion control and increasing physical activity.\n");
            recommendations.append("• Aim for 250-300 minutes of exercise per week.\n");
        } else {
            recommendations.append("• Consider consulting with a healthcare professional about weight management.\n");
            recommendations.append("• Focus on creating a sustainable caloric deficit through diet and exercise.\n");
        }

        // Body fat specific recommendations
        if (isMale) {
            if (bodyFat > 25) {
                recommendations.append("• Prioritize fat loss through combined cardio and resistance training.\n");
            } else if (bodyFat < 8) {
                recommendations.append("• Current body fat is very low - ensure adequate nutrition for health.\n");
            }
        } else {
            if (bodyFat > 32) {
                recommendations.append("• Prioritize fat loss through combined cardio and resistance training.\n");
            } else if (bodyFat < 15) {
                recommendations.append("• Current body fat is very low - ensure adequate nutrition for health.\n");
            }
        }
        
        // General recommendation
        recommendations.append("• Stay hydrated and aim for 7-9 hours of quality sleep daily for optimal results.");
        
        return recommendations.toString();
    }
} 