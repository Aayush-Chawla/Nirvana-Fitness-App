package com.example.nirvana.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class GeminiService {
    private static final String TAG = "GeminiService";
    // Updated with user-provided API key
    private static final String API_KEY = "AIzaSyCZObahoZ3AvLK3NCtFHtciVthzq-iToPA"; 
    
    // Model name verified to work with v0.4.0
    private static final String MODEL_NAME = "gemini-pro";
    
    private GenerativeModel model;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;
    private boolean isInitialized = false;
    
    public interface GeminiResponseCallback {
        void onResponse(String response);
        void onError(String errorMessage);
    }
    
    public GeminiService(Context context) {
        this.context = context;
        Log.d(TAG, "Initializing GeminiService with API key: " + API_KEY.substring(0, 8) + "...");
        
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No network connection available");
            Toast.makeText(context, "No internet connection available for AI features", Toast.LENGTH_SHORT).show();
        }
        
        try {
            // Create a minimal configuration to avoid compatibility issues
            GenerationConfig config = new GenerationConfig.Builder()
                    .build();
            
            model = new GenerativeModel(MODEL_NAME, API_KEY, config);
            isInitialized = true;
            Log.d(TAG, "GeminiService initialized successfully with model: " + MODEL_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize GeminiService", e);
            Toast.makeText(context, "AI features may not be available: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    public void generateContent(String prompt, GeminiResponseCallback callback) {
        if (!isInitialized) {
            callback.onError("GeminiService not properly initialized");
            return;
        }
        
        if (!isNetworkAvailable()) {
            Log.e(TAG, "Cannot generate content: No network connection");
            callback.onError("No internet connection available. Please check your connection and try again.");
            return;
        }
        
        if (prompt == null || prompt.trim().isEmpty()) {
            Log.e(TAG, "Cannot generate content: Empty prompt");
            callback.onError("Please provide a question or prompt");
            return;
        }
        
        // Ensure the prompt isn't too long
        if (prompt.length() > 4000) {
            Log.w(TAG, "Prompt too long (" + prompt.length() + " chars), truncating to 4000 chars");
            prompt = prompt.substring(0, 4000);
        }
        
        final String finalPrompt = prompt;
        Log.d(TAG, "generateContent called with prompt length: " + finalPrompt.length());
        Log.d(TAG, "Using model: " + MODEL_NAME);
        
        executor.execute(() -> {
            try {
                Log.d(TAG, "Executing API call on background thread");
                model.generateContent(finalPrompt, new Continuation<GenerateContentResponse>() {
                    @Override
                    public CoroutineContext getContext() {
                        Log.d(TAG, "getContext called for Continuation");
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(Object result) {
                        Log.d(TAG, "resumeWith called with result type: " + (result != null ? result.getClass().getName() : "null"));
                        try {
                            // Handle kotlin.Result without using reflection on specific methods
                            if (result != null) {
                                String className = result.getClass().getName();
                                
                                // Check if it's a failure by class name rather than calling methods
                                if (className.contains("kotlin.Result$Failure")) {
                                    // It's a failure, try to extract the exception directly through reflection
                                    Log.d(TAG, "Result is a kotlin.Result.Failure");
                                    try {
                                        Throwable exception = extractExceptionFromFailure(result);
                                        if (exception != null) {
                                            Log.e(TAG, "API call failed with exception", exception);
                                            callback.onError("Gemini API error: " + exception.getMessage());
                                        } else {
                                            Log.e(TAG, "API call failed but couldn't extract exception details");
                                            callback.onError("Gemini API call failed");
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error extracting exception from Result.Failure", e);
                                        callback.onError("Error processing API response: " + e.getMessage());
                                    }
                                    return;
                                } 
                                else if (className.contains("kotlin.Result$Success")) {
                                    // It's a success, try to extract value directly through reflection
                                    Log.d(TAG, "Result is a kotlin.Result.Success");
                                    try {
                                        Object value = extractValueFromSuccess(result);
                                        if (value instanceof GenerateContentResponse) {
                                            GenerateContentResponse response = (GenerateContentResponse) value;
                                            handleSuccessResponse(response, callback);
                                            return;
                                        } else {
                                            Log.e(TAG, "Unexpected value type: " + (value != null ? value.getClass().getName() : "null"));
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error extracting value from Result.Success", e);
                                    }
                                }
                                else if (result instanceof GenerateContentResponse) {
                                    // Direct instance of GenerateContentResponse
                                    Log.d(TAG, "Result is directly a GenerateContentResponse");
                                    GenerateContentResponse response = (GenerateContentResponse) result;
                                    handleSuccessResponse(response, callback);
                                    return;
                                }
                                else if (result instanceof Throwable) {
                                    // Direct instance of Throwable
                                    Log.e(TAG, "Result is an exception", (Throwable) result);
                                    callback.onError("Error: " + ((Throwable) result).getMessage());
                                    return;
                                }
                            }
                            
                            // If we reach here, we couldn't handle the result properly
                            Log.e(TAG, "Could not process the result: " + result);
                            callback.onError("Failed to process the AI response. Please try again.");
                        } catch (Throwable e) {
                            Log.e(TAG, "Error processing response", e);
                            callback.onError("Error from AI model: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error generating content", e);
                callback.onError("Error generating content: " + e.getMessage());
            }
        });
    }
    
    private Throwable extractExceptionFromFailure(Object failure) {
        try {
            // Try to access the exception field directly
            Field exceptionField = failure.getClass().getDeclaredField("exception");
            exceptionField.setAccessible(true);
            return (Throwable) exceptionField.get(failure);
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract exception from Result.Failure", e);
            return null;
        }
    }
    
    private Object extractValueFromSuccess(Object success) {
        try {
            // Try to access the value field directly
            Field valueField = success.getClass().getDeclaredField("value");
            valueField.setAccessible(true);
            return valueField.get(success);
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract value from Result.Success", e);
            return null;
        }
    }
    
    private void handleSuccessResponse(GenerateContentResponse response, GeminiResponseCallback callback) {
        if (response != null && response.getText() != null) {
            String generatedText = response.getText();
            Log.d(TAG, "Generated response length: " + generatedText.length());
            if (generatedText.length() > 0) {
                Log.d(TAG, "Generated response preview: " + generatedText.substring(0, Math.min(100, generatedText.length())));
                
                // Format the response for better readability
                String formattedResponse = formatResponse(generatedText);
                Log.d(TAG, "Delivering formatted response to callback");
                callback.onResponse(formattedResponse);
            } else {
                Log.e(TAG, "Generated response was empty");
                callback.onError("The AI model returned an empty response.");
            }
        } else {
            // Fall back to a default response if we couldn't get a valid one
            Log.e(TAG, "Failed to generate response - response or text was null");
            callback.onError("Failed to generate response. The AI model may be temporarily unavailable.");
        }
    }
    
    private String formatResponse(String response) {
        // Simple formatting, remove extra whitespace and trim
        if (response == null) {
            Log.w(TAG, "formatResponse called with null response");
            return "I'm sorry, I couldn't generate a response.";
        }
        
        // Replace multiple newlines with double newline for better readability
        response = response.replaceAll("\\n{3,}", "\n\n");
        
        // Trim whitespace
        return response.trim();
    }
} 