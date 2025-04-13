package com.example.nirvana.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.ml.FoodRecognitionModel;
import com.example.nirvana.ml.FoodNutritionData;
import com.example.nirvana.ui.adapters.IdentifiedFoodAdapter;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for meal recognition and nutritional analysis
 */
public class MealRecognitionFragment extends Fragment {
    private static final String TAG = "MealRecognitionFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;
    
    private ImageView ivMealPreview;
    private Button btnTakePhoto, btnGallery, btnAnalyzeMeal;
    private ProgressBar progressMealAnalysis;
    private MaterialCardView cardAnalysisResults;
    private TextView tvNutritionSummary, tvRecommendations;
    private RecyclerView rvIdentifiedFoods;
    
    private Bitmap mealPhoto;
    private FoodRecognitionModel foodRecognitionModel;
    private List<FoodRecognitionModel.FoodRecognitionResult> recognizedFoods;
    private IdentifiedFoodAdapter foodAdapter;
    
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
        
        // Initialize food recognition model
        foodRecognitionModel = new FoodRecognitionModel(requireContext());
        recognizedFoods = new ArrayList<>();
        
        // Initialize RecyclerView and adapter
        rvIdentifiedFoods.setLayoutManager(new LinearLayoutManager(requireContext()));
        foodAdapter = new IdentifiedFoodAdapter(recognizedFoods);
        rvIdentifiedFoods.setAdapter(foodAdapter);
        
        // Initial UI state
        btnAnalyzeMeal.setEnabled(false);
        cardAnalysisResults.setVisibility(View.GONE);
        progressMealAnalysis.setVisibility(View.GONE);
        
        // Set up button listeners
        btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndOpen());
        btnGallery.setOnClickListener(v -> pickPhoto());
        btnAnalyzeMeal.setOnClickListener(v -> analyzeMeal());
    }
    
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }
    
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(requireContext(), "No camera app found on your device", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening camera", e);
            Toast.makeText(requireContext(), "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take photos",
                        Toast.LENGTH_SHORT).show();
            }
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
                Bundle extras = data.getExtras();
                if (extras != null) {
                    mealPhoto = (Bitmap) extras.get("data");
                    if (mealPhoto != null) {
                        // Scale the bitmap to fit the ImageView while maintaining aspect ratio
                        int targetWidth = ivMealPreview.getWidth();
                        int targetHeight = ivMealPreview.getHeight();
                        if (targetWidth > 0 && targetHeight > 0) {
                            float scale = Math.min((float) targetWidth / mealPhoto.getWidth(),
                                                 (float) targetHeight / mealPhoto.getHeight());
                            int newWidth = Math.round(mealPhoto.getWidth() * scale);
                            int newHeight = Math.round(mealPhoto.getHeight() * scale);
                            mealPhoto = Bitmap.createScaledBitmap(mealPhoto, newWidth, newHeight, true);
                        }
                        ivMealPreview.setImageBitmap(mealPhoto);
                        ivMealPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        btnAnalyzeMeal.setEnabled(true);
                    }
                }
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                Uri selectedImage = data.getData();
                try {
                    // Get the dimensions of the ImageView
                    int targetWidth = ivMealPreview.getWidth();
                    int targetHeight = ivMealPreview.getHeight();
                    
                    // Load the image with proper scaling
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(
                            requireActivity().getContentResolver().openInputStream(selectedImage),
                            null, options);
                    
                    // Calculate inSampleSize
                    options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
                    options.inJustDecodeBounds = false;
                    
                    // Load the scaled bitmap
                    mealPhoto = BitmapFactory.decodeStream(
                            requireActivity().getContentResolver().openInputStream(selectedImage),
                            null, options);
                    
                    if (mealPhoto != null) {
                        ivMealPreview.setImageBitmap(mealPhoto);
                        ivMealPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        btnAnalyzeMeal.setEnabled(true);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error loading image", e);
                    Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    
    private void analyzeMeal() {
        if (mealPhoto == null) {
            Toast.makeText(requireContext(), "Please take or select a photo first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        progressMealAnalysis.setVisibility(View.VISIBLE);
        btnAnalyzeMeal.setEnabled(false);
        
        // Run food recognition in background
        new Thread(() -> {
            // Perform food recognition
            recognizedFoods = foodRecognitionModel.recognizeFood(mealPhoto);
            
            // Calculate total nutrition
            final double[] totals = new double[4]; // calories, protein, carbs, fat
            StringBuilder recommendations = new StringBuilder();
            
            for (FoodRecognitionModel.FoodRecognitionResult result : recognizedFoods) {
                FoodNutritionData.NutritionInfo nutritionInfo = FoodNutritionData.getNutritionInfo(result.getFoodName());
                if (nutritionInfo != null) {
                    totals[0] += nutritionInfo.getCalories();
                    totals[1] += nutritionInfo.getProtein();
                    totals[2] += nutritionInfo.getCarbs();
                    totals[3] += nutritionInfo.getFat();
                }
            }
            
            // Generate recommendations based on nutritional content
            if (totals[0] > 800) {
                recommendations.append("This meal is quite high in calories. Consider reducing portion sizes.\n");
            }
            if (totals[1] < 20) {
                recommendations.append("Consider adding more protein-rich foods.\n");
            }
            if (totals[2] > 100) {
                recommendations.append("The meal is high in carbohydrates. Consider balancing with more vegetables.\n");
            }
            if (totals[3] > 30) {
                recommendations.append("This meal is high in fat. Consider choosing leaner options next time.\n");
            }
            
            final String finalRecommendations = recommendations.toString();
            
            // Update UI on main thread
            requireActivity().runOnUiThread(() -> {
                progressMealAnalysis.setVisibility(View.GONE);
                cardAnalysisResults.setVisibility(View.VISIBLE);
                btnAnalyzeMeal.setEnabled(true);
                
                // Update nutrition summary
                String nutritionText = String.format("Calories: %.0f kcal\nProtein: %.1fg\nCarbs: %.1fg\nFat: %.1fg",
                        totals[0], totals[1], totals[2], totals[3]);
                tvNutritionSummary.setText(nutritionText);
                
                // Update recommendations
                if (finalRecommendations.length() > 0) {
                    tvRecommendations.setText(finalRecommendations);
                } else {
                    tvRecommendations.setText("This meal appears to be well-balanced. Keep up the good work!");
                }
                
                // Update RecyclerView
                foodAdapter.updateFoods(recognizedFoods);
            });
        }).start();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (foodRecognitionModel != null) {
            foodRecognitionModel.close();
        }
    }
} 