package com.example.nirvana.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FoodRecognitionModel {
    private static final String TAG = "FoodRecognitionModel";
    private static final String MODEL_FILE = "food_recognition_model.tflite";
    private static final String LABELS_FILE = "food_labels.txt";
    private static final int IMAGE_SIZE = 224;
    private static final int MAX_RESULTS = 5;
    private static final float SCORE_THRESHOLD = 0.1f;

    private ImageClassifier classifier;
    private ImageProcessor imageProcessor;
    private List<String> labels;
    private final Context context;

    public FoodRecognitionModel(Context context) {
        this.context = context;
        initializeModel();
    }

    private void initializeModel() {
        try {
            // Log the contents of the assets directory to verify model and labels files
            try {
                String[] assets = context.getAssets().list("");
                Log.d(TAG, "Assets directory contents:");
                for (String asset : assets) {
                    Log.d(TAG, "Asset: " + asset);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error listing assets", e);
            }

            BaseOptions baseOptions = BaseOptions.builder().build();
            ImageClassifier.ImageClassifierOptions options = ImageClassifier.ImageClassifierOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMaxResults(MAX_RESULTS)
                    .setScoreThreshold(SCORE_THRESHOLD)
                    .build();
            
            classifier = ImageClassifier.createFromFileAndOptions(context, MODEL_FILE, options);
            Log.d(TAG, "Successfully created ImageClassifier");

            imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(IMAGE_SIZE, IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(0.0f, 1.0f))
                    .build();
            Log.d(TAG, "Successfully created ImageProcessor");

            labels = FileUtil.loadLabels(context, LABELS_FILE);
            Log.d(TAG, "Successfully loaded labels: " + labels.size() + " labels found");
            for (String label : labels) {
                Log.d(TAG, "Available label: " + label);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error initializing food recognition model", e);
            classifier = null;
            labels = new ArrayList<>();
        }
    }

    public List<FoodRecognitionResult> recognizeFood(Bitmap image) {
        if (classifier == null || image == null) {
            Log.e(TAG, "Classifier or image is null");
            return new ArrayList<>();
        }

        try {
            Log.d(TAG, "Starting image recognition process");
            Log.d(TAG, "Input image size: " + image.getWidth() + "x" + image.getHeight());

            if (image.isRecycled()) {
                Log.e(TAG, "Image is recycled");
                return new ArrayList<>();
            }

            // Scale down large images to prevent OOM
            Bitmap processedBitmap;
            if (image.getWidth() > 1024 || image.getHeight() > 1024) {
                float scale = Math.min(1024f / image.getWidth(), 1024f / image.getHeight());
                int newWidth = Math.round(image.getWidth() * scale);
                int newHeight = Math.round(image.getHeight() * scale);
                processedBitmap = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
                Log.d(TAG, "Scaled image to: " + newWidth + "x" + newHeight);
            } else {
                processedBitmap = image.copy(image.getConfig(), true);
            }

            TensorImage tensorImage = TensorImage.fromBitmap(processedBitmap);
            tensorImage = imageProcessor.process(tensorImage);
            Log.d(TAG, "Image preprocessing completed");

            List<Classifications> results = classifier.classify(tensorImage);
            Log.d(TAG, "Classification completed. Results size: " + results.size());
            
            List<FoodRecognitionResult> foodResults = new ArrayList<>();
            
            if (!results.isEmpty()) {
                Classifications classification = results.get(0);
                Log.d(TAG, "Raw classification results:");
                for (org.tensorflow.lite.support.label.Category category : classification.getCategories()) {
                    Log.d(TAG, String.format("Label: %s, Score: %.4f", category.getLabel(), category.getScore()));
                    foodResults.add(new FoodRecognitionResult(category.getLabel(), category.getScore()));
                }
            } else {
                Log.d(TAG, "No classifications found");
            }

            if (processedBitmap != image) {
                processedBitmap.recycle();
            }
            return foodResults;
        } catch (Exception e) {
            Log.e(TAG, "Error recognizing food", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void close() {
        if (classifier != null) {
            try {
                classifier.close();
                Log.d(TAG, "Classifier closed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error closing classifier", e);
            }
        }
    }

    public static class FoodRecognitionResult {
        private final String foodName;
        private final float confidence;

        public FoodRecognitionResult(String foodName, float confidence) {
            this.foodName = foodName;
            this.confidence = confidence;
        }

        public String getFoodName() {
            return foodName;
        }

        public float getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            return String.format("%s (%.1f%%)", foodName, confidence * 100);
        }
    }
} 