package com.example.nirvana.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;

/**
 * Fragment for meal recognition and nutritional analysis
 */
public class MealRecognitionFragment extends Fragment {
    private static final String TAG = "MealRecognitionFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    
    private ImageView ivMealPreview;
    private Button btnTakePhoto, btnGallery, btnAnalyzeMeal;
    private ProgressBar progressMealAnalysis;
    private MaterialCardView cardAnalysisResults;
    private TextView tvNutritionSummary, tvRecommendations;
    private RecyclerView rvIdentifiedFoods;
    
    private Bitmap mealPhoto;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_recognition, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        ivMealPreview = view.findViewById(R.id.ivMealPreview);
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
        btnGallery = view.findViewById(R.id.btnGallery);
        btnAnalyzeMeal = view.findViewById(R.id.btnAnalyzeMeal);
        progressMealAnalysis = view.findViewById(R.id.progressMealAnalysis);
        cardAnalysisResults = view.findViewById(R.id.cardAnalysisResults);
        tvNutritionSummary = view.findViewById(R.id.tvNutritionSummary);
        tvRecommendations = view.findViewById(R.id.tvRecommendations);
        rvIdentifiedFoods = view.findViewById(R.id.rvIdentifiedFoods);
        
        // Initial UI state
        btnAnalyzeMeal.setEnabled(false);
        cardAnalysisResults.setVisibility(View.GONE);
        progressMealAnalysis.setVisibility(View.GONE);
        
        // Set up RecyclerView
        rvIdentifiedFoods.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Set up button listeners
        btnTakePhoto.setOnClickListener(v -> takePhoto());
        btnGallery.setOnClickListener(v -> pickPhoto());
        btnAnalyzeMeal.setOnClickListener(v -> analyzeMeal());
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
                    mealPhoto = (Bitmap) extras.get("data");
                    ivMealPreview.setImageBitmap(mealPhoto);
                    btnAnalyzeMeal.setEnabled(true);
                }
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // Handle picked photo
                Uri selectedImage = data.getData();
                try {
                    mealPhoto = MediaStore.Images.Media.getBitmap(
                            requireActivity().getContentResolver(), selectedImage);
                    ivMealPreview.setImageBitmap(mealPhoto);
                    btnAnalyzeMeal.setEnabled(true);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading image", e);
                    Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void analyzeMeal() {
        if (mealPhoto == null) {
            Toast.makeText(requireContext(), "Please take or select a photo first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        progressMealAnalysis.setVisibility(View.VISIBLE);
        btnAnalyzeMeal.setEnabled(false);
        
        // TODO: Implement meal recognition using ML model
        
        // For now, just show a placeholder result after a delay
        ivMealPreview.postDelayed(() -> {
            progressMealAnalysis.setVisibility(View.GONE);
            cardAnalysisResults.setVisibility(View.VISIBLE);
            btnAnalyzeMeal.setEnabled(true);
            
            // Set placeholder data
            tvNutritionSummary.setText("Calories: 450 kcal\nProtein: 20g\nCarbs: 45g\nFat: 15g");
            tvRecommendations.setText("This meal appears to be balanced. Consider adding more vegetables for additional fiber and micronutrients.");
            
            // TODO: Populate RecyclerView with identified food items
        }, 2000);
    }
} 