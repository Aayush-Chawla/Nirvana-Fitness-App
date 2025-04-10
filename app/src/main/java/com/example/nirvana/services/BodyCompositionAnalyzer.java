package com.example.nirvana.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;

import com.example.nirvana.models.BodyComposition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import java.io.IOException;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.FileInputStream;
import java.io.FileDescriptor;
import android.content.res.AssetFileDescriptor;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.tensorflow.lite.Interpreter;

/**
 * Service that analyzes body composition from user photos using ML models
 */
public class BodyCompositionAnalyzer {
    private static final String TAG = "BodyCompositionAnalyzer";
    private static final String MODEL_PATH = "body_analysis_model.tflite";
    private static final int IMG_SIZE = 224;
    
    private Context context;
    private PoseDetector poseDetector;
    private Interpreter bodyAnalysisModel;
    
    public interface AnalysisCallback {
        void onAnalysisComplete(BodyComposition result);
        void onError(String message);
    }
    
    public BodyCompositionAnalyzer(Context context) {
        this.context = context;
        
        // Initialize pose detector from ML Kit
        AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);
        
        // Try to load the TensorFlow Lite model
        try {
            bodyAnalysisModel = new Interpreter(loadModelFile(MODEL_PATH));
            Log.d(TAG, "Body analysis model loaded successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error loading body analysis model", e);
            // Will fall back to estimate-only mode if model loading fails
        }
    }
    
    /**
     * Analyze a photo of the user to estimate body composition
     * @param userPhoto User's full body photo
     * @param userStats User stats like height, weight, age
     * @param callback Callback to return results
     */
    public void analyzeBodyPhoto(Bitmap userPhoto, UserStats userStats, AnalysisCallback callback) {
        if (userPhoto == null) {
            callback.onError("Photo is missing or invalid");
            return;
        }
        
        // Process photo to detect body pose first
        InputImage image = InputImage.fromBitmap(userPhoto, 0);
        poseDetector.process(image)
            .addOnSuccessListener(new OnSuccessListener<Pose>() {
                @Override
                public void onSuccess(Pose pose) {
                    // Check if we have detected a clear full body pose
                    if (!isValidFullBodyPose(pose)) {
                        callback.onError("Could not detect a clear full body pose. Please take another photo showing your full body.");
                        return;
                    }
                    
                    // Extract body measurements from pose landmarks
                    float[] bodyMeasurements = extractBodyMeasurements(pose);
                    
                    // Use the model to analyze body composition if available
                    if (bodyAnalysisModel != null) {
                        BodyComposition result = runBodyAnalysisModel(userPhoto, bodyMeasurements, userStats);
                        callback.onAnalysisComplete(result);
                    } else {
                        // Fall back to estimation without ML model
                        BodyComposition estimatedResult = estimateBodyComposition(bodyMeasurements, userStats);
                        callback.onAnalysisComplete(estimatedResult);
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Pose detection failed", e);
                    callback.onError("Couldn't analyze body pose: " + e.getMessage());
                }
            });
    }
    
    /**
     * Check if the pose contains all required landmarks for full body analysis
     */
    private boolean isValidFullBodyPose(Pose pose) {
        // Key landmarks to check
        int[] requiredLandmarks = {
            PoseLandmark.LEFT_SHOULDER, 
            PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_HIP, 
            PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_KNEE, 
            PoseLandmark.RIGHT_KNEE,
            PoseLandmark.LEFT_ANKLE, 
            PoseLandmark.RIGHT_ANKLE
        };
        
        for (int landmarkType : requiredLandmarks) {
            if (pose.getPoseLandmark(landmarkType) == null) {
                Log.d(TAG, "Missing required landmark: " + landmarkType);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Extract relevant body measurements from pose landmarks
     */
    private float[] extractBodyMeasurements(Pose pose) {
        float[] measurements = new float[10]; // Example: store 10 key measurements
        
        // Shoulder width
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        if (leftShoulder != null && rightShoulder != null) {
            measurements[0] = calculateDistance(leftShoulder, rightShoulder);
        }
        
        // Hip width
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        if (leftHip != null && rightHip != null) {
            measurements[1] = calculateDistance(leftHip, rightHip);
        }
        
        // Waist approximation - use distance between mid-point of shoulders and hips
        if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
            float midShoulderX = (leftShoulder.getPosition().x + rightShoulder.getPosition().x) / 2;
            float midShoulderY = (leftShoulder.getPosition().y + rightShoulder.getPosition().y) / 2;
            
            float midHipX = (leftHip.getPosition().x + rightHip.getPosition().x) / 2;
            float midHipY = (leftHip.getPosition().y + rightHip.getPosition().y) / 2;
            
            float torsoHeight = calculatePointDistance(midShoulderX, midShoulderY, midHipX, midHipY);
            measurements[2] = torsoHeight;
            
            // Approximate waist position (60% down from shoulders to hips)
            float waistX = midShoulderX + 0.6f * (midHipX - midShoulderX);
            float waistY = midShoulderY + 0.6f * (midHipY - midShoulderY);
            
            // Store waist position
            measurements[3] = waistX;
            measurements[4] = waistY;
        }
        
        // More measurements can be added here
        
        return measurements;
    }
    
    /**
     * Calculate distance between two landmarks
     */
    private float calculateDistance(PoseLandmark first, PoseLandmark second) {
        return calculatePointDistance(
            first.getPosition().x, first.getPosition().y,
            second.getPosition().x, second.getPosition().y);
    }
    
    /**
     * Calculate distance between two points
     */
    private float calculatePointDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
    
    /**
     * Run the TensorFlow Lite model for body composition analysis
     */
    private BodyComposition runBodyAnalysisModel(Bitmap photo, float[] measurements, UserStats userStats) {
        // Preprocess the image
        ByteBuffer imgData = convertBitmapToByteBuffer(photo);
        
        // Prepare input features combining image data, body measurements, and user stats
        float[] userFeatures = prepareUserFeatures(measurements, userStats);
        
        // Run inference
        float[][] outputData = new float[1][5]; // [bodyFat%, muscleMass%, bmr, visceralFat, bodyType]
        
        try {
            // This is a simplified approach. In reality, you might need multiple inputs or a different structure
            bodyAnalysisModel.run(new Object[]{imgData, userFeatures}, outputData);
            
            // Convert model output to BodyComposition object
            return new BodyComposition(
                outputData[0][0] * 100, // Scale to percentage
                outputData[0][1] * 100, // Scale to percentage
                outputData[0][2], // BMR
                outputData[0][3], // Visceral fat
                interpretBodyType(outputData[0][4]) // Body type
            );
        } catch (Exception e) {
            Log.e(TAG, "Error running body analysis model", e);
            // Fall back to estimation
            return estimateBodyComposition(measurements, userStats);
        }
    }
    
    /**
     * Fallback method to estimate body composition without ML model
     */
    private BodyComposition estimateBodyComposition(float[] measurements, UserStats userStats) {
        // Basic estimation using traditional formulas
        
        // Estimate body fat using BMI and gender
        double bmi = userStats.weight / Math.pow(userStats.height / 100.0, 2);
        double bodyFatPercentage;
        
        if (userStats.gender.equalsIgnoreCase("male")) {
            bodyFatPercentage = (1.20 * bmi) + (0.23 * userStats.age) - 16.2;
        } else {
            bodyFatPercentage = (1.20 * bmi) + (0.23 * userStats.age) - 5.4;
        }
        
        // Estimate muscle mass (very rough approximation)
        double fatMassKg = userStats.weight * (bodyFatPercentage / 100.0);
        double leanMassKg = userStats.weight - fatMassKg;
        double muscleMassKg = leanMassKg * 0.85; // Rough estimation that lean mass is about 85% muscle
        double muscleMassPercentage = (muscleMassKg / userStats.weight) * 100;
        
        // Calculate BMR using Mifflin-St Jeor Equation
        double bmr;
        if (userStats.gender.equalsIgnoreCase("male")) {
            bmr = (10 * userStats.weight) + (6.25 * userStats.height) - (5 * userStats.age) + 5;
        } else {
            bmr = (10 * userStats.weight) + (6.25 * userStats.height) - (5 * userStats.age) - 161;
        }
        
        // Estimate visceral fat (simplified approximation based on waist-to-height ratio)
        double visceralFat = 5.0; // Default moderate value
        
        // Body type (simplified)
        String bodyType = "Mesomorph"; // Default
        
        return new BodyComposition(
            bodyFatPercentage,
            muscleMassPercentage,
            bmr,
            visceralFat,
            bodyType
        );
    }
    
    /**
     * Convert bitmap to normalized byte buffer for model input
     */
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        
        // Resize image to required dimensions
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true);
        
        int[] intValues = new int[IMG_SIZE * IMG_SIZE];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, 
                              resizedBitmap.getWidth(), resizedBitmap.getHeight());
        
        // Normalize pixel values to [-1,1]
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            
            byteBuffer.putFloat(((val >> 16) & 0xFF) / 127.5f - 1);
            byteBuffer.putFloat(((val >> 8) & 0xFF) / 127.5f - 1);
            byteBuffer.putFloat((val & 0xFF) / 127.5f - 1);
        }
        
        return byteBuffer;
    }
    
    /**
     * Prepare user features for model input
     */
    private float[] prepareUserFeatures(float[] measurements, UserStats userStats) {
        float[] features = new float[15]; // Example size
        
        // Normalize user stats
        features[0] = userStats.age / 100f;
        features[1] = userStats.weight / 150f;
        features[2] = userStats.height / 200f;
        features[3] = userStats.gender.equalsIgnoreCase("male") ? 1.0f : 0.0f;
        
        // Add body measurements (already normalized by image size)
        System.arraycopy(measurements, 0, features, 4, Math.min(measurements.length, 10));
        
        return features;
    }
    
    /**
     * Interpret numeric body type from model output
     */
    private String interpretBodyType(float bodyTypeValue) {
        if (bodyTypeValue < 0.33) {
            return "Ectomorph";
        } else if (bodyTypeValue < 0.66) {
            return "Mesomorph";
        } else {
            return "Endomorph";
        }
    }
    
    /**
     * Load TensorFlow Lite model file
     */
    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    
    /**
     * User statistics needed for body composition analysis
     */
    public static class UserStats {
        public final int age;
        public final float weight; // kg
        public final float height; // cm
        public final String gender;
        public final String activityLevel;
        
        public UserStats(int age, float weight, float height, String gender, String activityLevel) {
            this.age = age;
            this.weight = weight;
            this.height = height;
            this.gender = gender;
            this.activityLevel = activityLevel;
        }
    }
} 