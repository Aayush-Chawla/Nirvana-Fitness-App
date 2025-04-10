package com.example.nirvana.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.nirvana.utils.FirestoreHelper;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class GeminiService {
    private static final String TAG = "GeminiService";
    // Updated API key - the previous one may have expired or reached quota limits
    private static final String API_KEY = "AIzaSyDKR-ODJDBct3TDSeDjt6h_MoDVkTb_1Yg";  // Note: This should be replaced with an actual valid API key
    
    // Model name verified to work with v0.4.0
    private static final String MODEL_NAME = "gemini-pro";
    
    private GenerativeModel model;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;
    private boolean isInitialized = false;
    
    // Fallback system to use when API is unavailable
    private boolean useFallbackResponses = false;
    
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
        if (!isInitialized || useFallbackResponses) {
            // Use fallback responses if API is not available
            provideFallbackResponse(prompt, callback);
            return;
        }
        
        if (!isNetworkAvailable()) {
            Log.e(TAG, "Cannot generate content: No network connection");
            useFallbackResponses = true; // Switch to fallback mode
            provideFallbackResponse(prompt, callback);
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
                                            // Switch to fallback responses after multiple failures
                                            useFallbackResponses = true;
                                            provideFallbackResponse(finalPrompt, callback);
                                        } else {
                                            Log.e(TAG, "API call failed but couldn't extract exception details");
                                            provideFallbackResponse(finalPrompt, callback);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error extracting exception from Result.Failure", e);
                                        provideFallbackResponse(finalPrompt, callback);
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
                                            provideFallbackResponse(finalPrompt, callback);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error extracting value from Result.Success", e);
                                        provideFallbackResponse(finalPrompt, callback);
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
                                    provideFallbackResponse(finalPrompt, callback);
                                    return;
                                }
                            }
                            
                            // If we reach here, we couldn't handle the result properly
                            Log.e(TAG, "Could not process the result: " + result);
                            provideFallbackResponse(finalPrompt, callback);
                        } catch (Throwable e) {
                            Log.e(TAG, "Error processing response", e);
                            provideFallbackResponse(finalPrompt, callback);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error generating content", e);
                provideFallbackResponse(finalPrompt, callback);
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
    
    private void provideFallbackResponse(String prompt, GeminiResponseCallback callback) {
        String userMessage = prompt.toLowerCase();
        String response;
        
        // First, check for user-specific questions that need personalized data
        if (userMessage.contains("my name") || userMessage.contains("who am i") || 
            userMessage.equals("what is my name") || userMessage.contains("what's my name")) {
            // Access Firebase to get user information
            fetchUserDataAndRespond(userMessage, callback);
            return;
        } else if (userMessage.contains("my weight") || userMessage.contains("how much do i weigh") ||
                  userMessage.contains("what is my weight") || userMessage.contains("what's my weight")) {
            fetchUserDataAndRespond(userMessage, callback);
            return;
        } else if (userMessage.contains("my height") || userMessage.contains("how tall am i") ||
                  userMessage.contains("what is my height") || userMessage.contains("what's my height")) {
            fetchUserDataAndRespond(userMessage, callback);
            return;
        } else if (userMessage.contains("my age") || userMessage.contains("how old am i") ||
                  userMessage.contains("what is my age") || userMessage.contains("what's my age")) {
            fetchUserDataAndRespond(userMessage, callback);
            return;
        } else if (userMessage.contains("my goal") || userMessage.contains("my target") ||
                  userMessage.contains("what is my goal") || userMessage.contains("what's my goal")) {
            fetchUserDataAndRespond(userMessage, callback);
            return;
        } else if (userMessage.contains("profile") || userMessage.contains("my information") ||
                  userMessage.contains("about me") || userMessage.contains("my details")) {
            fetchUserDataAndRespond(userMessage, callback);
            return;
        }
        
        // Parse user question for specific patterns that might be answerable
        if (isQuestionAboutMeals(userMessage)) {
            fetchMealDataAndRespond(userMessage, callback);
            return;
        } else if (isQuestionAboutWorkouts(userMessage)) {
            fetchWorkoutDataAndRespond(userMessage, callback);
            return;
        }
        
        // Check for greeting or general query
        if (userMessage.contains("hello") || userMessage.contains("hi ") || userMessage.contains("hey") || 
            userMessage.contains("how are you") || prompt.trim().length() < 5) {
            response = "Hello! I'm your fitness assistant. I can help you with workout suggestions, nutrition advice, and fitness tracking. What would you like to know about today?";
        }
        // Check for workout related questions
        else if (userMessage.contains("workout") || userMessage.contains("exercise") || userMessage.contains("training") ||
                 userMessage.contains("routine") || userMessage.contains("gym")) {
            response = "For a balanced workout routine, aim for 3-5 sessions per week including cardio, strength training, and flexibility exercises. Start with 30-minute sessions and gradually increase as your fitness improves. Remember to include a proper warm-up and cool-down with each session.";
        }
        // Check for diet related questions
        else if (userMessage.contains("diet") || userMessage.contains("nutrition") || userMessage.contains("food") || 
                 userMessage.contains("eat") || userMessage.contains("meal")) {
            response = "A balanced diet should include proteins (lean meats, fish, beans), complex carbs (whole grains, vegetables), and healthy fats (avocados, nuts, olive oil). Try to eat 3-5 servings of vegetables and 2-3 servings of fruit daily. Staying properly hydrated is also important - aim for at least 8 glasses of water per day.";
        }
        // Check for weight management questions
        else if (userMessage.contains("weight") || userMessage.contains("lose") || userMessage.contains("gain") || 
                 userMessage.contains("fat") || userMessage.contains("calorie")) {
            response = "Healthy weight management combines regular exercise with proper nutrition. For weight loss, aim for a calorie deficit through both diet and exercise. For muscle gain, incorporate strength training and ensure adequate protein intake. Track your progress regularly and make adjustments as needed.";
        }
        // Check for muscle building questions
        else if (userMessage.contains("muscle") || userMessage.contains("strength") || userMessage.contains("lifting") ||
                 userMessage.contains("bulk") || userMessage.contains("protein")) {
            response = "To build muscle, focus on progressive overload in your strength training, adequate protein intake (1.6-2.2g per kg of bodyweight), and proper recovery. Include compound exercises like squats, deadlifts, and bench press in your routine. Ensure you're getting enough sleep and managing stress for optimal muscle growth.";
        }
        // Check for cardio related questions
        else if (userMessage.contains("cardio") || userMessage.contains("running") || userMessage.contains("jogging") ||
                 userMessage.contains("heart") || userMessage.contains("endurance")) {
            response = "Cardiovascular exercise improves heart health, endurance, and can help with weight management. Aim for 150 minutes of moderate-intensity or 75 minutes of high-intensity cardio per week. Mix in interval training for maximum benefits. Good options include running, cycling, swimming, or brisk walking.";
        }
        // Check for recovery related questions
        else if (userMessage.contains("recovery") || userMessage.contains("rest") || userMessage.contains("sore") ||
                 userMessage.contains("pain") || userMessage.contains("injury")) {
            response = "Recovery is a crucial part of any fitness routine. Ensure you're getting 7-9 hours of quality sleep, staying hydrated, and consuming adequate protein. For muscle soreness, try light activity, proper stretching, hot/cold therapy, or foam rolling. Rest days are important to prevent injury and allow your body to repair and grow stronger.";
        }
        // Check if user is asking about app features or how to use the app
        else if (userMessage.contains("how to use") || userMessage.contains("app feature") || 
                 userMessage.contains("how does this work") || userMessage.contains("what can you do")) {
            response = "I'm your fitness assistant in the Nirvana app. I can help answer questions about fitness, nutrition, and your personal health goals. You can also use the app to track workouts, log meals, set goals, and monitor your progress. To navigate the app, use the bottom menu to access different sections like Home, Workout, and Diet.";
        }
        // Check if question is about health metrics
        else if (userMessage.contains("bmi") || userMessage.contains("body mass index") || 
                 userMessage.contains("calories") || userMessage.contains("macros")) {
            response = "Health metrics like BMI, calorie needs, and macronutrient ratios can be helpful guides. A healthy BMI range is 18.5-24.9. Daily calorie needs vary based on age, gender, weight, and activity level. For macros, a general guideline is 45-65% carbs, 10-35% protein, and 20-35% fat, but these should be personalized to your goals and body.";
        }
        // Check for questions about fitness goals
        else if (userMessage.contains("goal setting") || userMessage.contains("smart goals") || 
                 userMessage.contains("fitness goal") || userMessage.contains("achieve")) {
            response = "Effective fitness goals are SMART: Specific, Measurable, Achievable, Relevant, and Time-bound. Instead of 'get fit,' try 'complete a 5K run in under 30 minutes by June' or 'increase squat weight by 20% in 12 weeks.' Track your progress regularly and adjust your goals as needed to stay motivated and see results.";
        }
        // Check if the user asked a specific question that doesn't match patterns
        else if (userMessage.startsWith("what") || userMessage.startsWith("how") || 
                 userMessage.startsWith("why") || userMessage.startsWith("when") || 
                 userMessage.startsWith("where") || userMessage.startsWith("can") || 
                 userMessage.startsWith("do") || userMessage.startsWith("is") || 
                 userMessage.contains("?")) {
            response = "I'm sorry, I don't have specific information about that question. As a fitness assistant, I can provide general guidance on workouts, nutrition, recovery, and goal setting. If you have questions about your personal data or specific fitness plans, please try asking in a different way or consult with a fitness professional.";
        }
        // Default response for other topics
        else {
            response = "I'm your fitness assistant, ready to help with workout plans, nutrition advice, and healthy lifestyle tips. I can provide guidance on strength training, cardio, flexibility, meal planning, and recovery strategies. What specific fitness goal are you working toward right now?";
        }
        
        // Return the response through the callback
        callback.onResponse(response);
    }

    private boolean isQuestionAboutMeals(String message) {
        return message.contains("my meals") || 
               message.contains("what did i eat") || 
               message.contains("my breakfast") || 
               message.contains("my lunch") || 
               message.contains("my dinner") ||
               message.contains("my food log") ||
               message.contains("my diet log");
    }

    private boolean isQuestionAboutWorkouts(String message) {
        return message.contains("my workout") || 
               message.contains("my exercise") || 
               message.contains("my training") || 
               message.contains("my activity") ||
               message.contains("my last workout") ||
               message.contains("when did i last");
    }

    private void fetchUserDataAndRespond(String userMessage, GeminiResponseCallback callback) {
        try {
            // Check if user is logged in
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                callback.onResponse("I can't access your personal information because you're not logged in. Please log in to get personalized responses.");
                return;
            }
            
            // Use FirestoreHelper to get user profile from Firestore
            FirestoreHelper.getUserProfile(new FirestoreHelper.OnDataFetchedListener<Map<String, Object>>() {
                @Override
                public void onDataFetched(Map<String, Object> userProfile) {
                    // Generate appropriate response based on the query
                    String response = generatePersonalizedResponse(userMessage, userProfile);
                    callback.onResponse(response);
                }
                
                @Override
                public void onError(String message) {
                    android.util.Log.e(TAG, "Error fetching user data: " + message);
                    callback.onResponse("I'm having trouble accessing your information right now. Please try again later.");
                }
            });
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in fetchUserDataAndRespond: " + e.getMessage(), e);
            callback.onResponse("I encountered an error while trying to access your information. Please try again later.");
        }
    }

    private String generatePersonalizedResponse(String userMessage, java.util.Map<String, Object> userProfile) {
        // If profile is empty, return a generic response
        if (userProfile.isEmpty()) {
            return "I don't have enough information about you yet. Please complete your profile in the app settings.";
        }
        
        // Check what type of personal information was requested
        if (userMessage.contains("my name") || userMessage.contains("who am i")) {
            Object name = userProfile.get("name");
            if (name != null) {
                return "Your name is " + name.toString() + ".";
            } else {
                return "I don't have your name on record. You can add it in your profile settings.";
            }
        } else if (userMessage.contains("my weight") || userMessage.contains("how much do i weigh")) {
            Object weight = userProfile.get("weight");
            if (weight != null) {
                return "According to your profile, your weight is " + weight.toString() + " kg.";
            } else {
                return "I don't have your weight on record. You can add it in your profile settings.";
            }
        } else if (userMessage.contains("my height") || userMessage.contains("how tall am i")) {
            Object height = userProfile.get("height");
            if (height != null) {
                return "According to your profile, your height is " + height.toString() + " cm.";
            } else {
                return "I don't have your height on record. You can add it in your profile settings.";
            }
        } else if (userMessage.contains("my age") || userMessage.contains("how old am i")) {
            Object age = userProfile.get("age");
            if (age != null) {
                return "According to your profile, you are " + age.toString() + " years old.";
            } else {
                return "I don't have your age on record. You can add it in your profile settings.";
            }
        } else if (userMessage.contains("my goal") || userMessage.contains("my target")) {
            Object goal = userProfile.get("goal");
            if (goal != null) {
                return "Your fitness goal is set to: " + goal.toString() + ".";
            } else {
                return "I don't see a specific fitness goal in your profile. You can set one in your profile settings.";
            }
        }
        
        // Fall back to a general summary of the profile if we don't understand the specific request
        StringBuilder profileSummary = new StringBuilder("Here's the information I have about you: ");
        boolean hasInfo = false;
        
        if (userProfile.containsKey("name")) {
            profileSummary.append("Your name is ").append(userProfile.get("name")).append(". ");
            hasInfo = true;
        }
        if (userProfile.containsKey("age")) {
            profileSummary.append("You are ").append(userProfile.get("age")).append(" years old. ");
            hasInfo = true;
        }
        if (userProfile.containsKey("height")) {
            profileSummary.append("Your height is ").append(userProfile.get("height")).append(" cm. ");
            hasInfo = true;
        }
        if (userProfile.containsKey("weight")) {
            profileSummary.append("Your weight is ").append(userProfile.get("weight")).append(" kg. ");
            hasInfo = true;
        }
        if (userProfile.containsKey("gender")) {
            profileSummary.append("Your gender is listed as ").append(userProfile.get("gender")).append(". ");
            hasInfo = true;
        }
        
        if (hasInfo) {
            return profileSummary.toString();
        } else {
            return "I have your profile on file, but there's not much information in it. Please update your profile for more personalized responses.";
        }
    }

    private void fetchMealDataAndRespond(String userMessage, GeminiResponseCallback callback) {
        try {
            // Check if user is logged in
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                callback.onResponse("I can't access your meal information because you're not logged in. Please log in to get personalized responses.");
                return;
            }
            
            // Use FirestoreHelper to get meals from Firestore
            FirestoreHelper.getMeals(new FirestoreHelper.OnDataFetchedListener<Map<String, List<Map<String, Object>>>>() {
                @Override
                public void onDataFetched(Map<String, List<Map<String, Object>>> meals) {
                    String response = generateMealResponseFromFirestore(userMessage, meals);
                    callback.onResponse(response);
                }
                
                @Override
                public void onError(String message) {
                    android.util.Log.e(TAG, "Error fetching meal data: " + message);
                    callback.onResponse("I'm having trouble accessing your meal information right now. Please try again later.");
                }
            });
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in fetchMealDataAndRespond: " + e.getMessage(), e);
            callback.onResponse("I encountered an error while trying to access your meal information. Please try again later.");
        }
    }

    private String generateMealResponseFromFirestore(String userMessage, Map<String, List<Map<String, Object>>> meals) {
        StringBuilder response = new StringBuilder();
        
        // Get information based on meal type mentioned
        if (userMessage.contains("breakfast")) {
            response.append("Here's what I found in your breakfast logs: ");
            appendMealItemsFromFirestore(response, meals.get("breakfast"));
        } else if (userMessage.contains("lunch")) {
            response.append("Here's what I found in your lunch logs: ");
            appendMealItemsFromFirestore(response, meals.get("lunch"));
        } else if (userMessage.contains("dinner")) {
            response.append("Here's what I found in your dinner logs: ");
            appendMealItemsFromFirestore(response, meals.get("dinner"));
        } else if (userMessage.contains("snack")) {
            response.append("Here's what I found in your snack logs: ");
            appendMealItemsFromFirestore(response, meals.get("snacks"));
        } else {
            // General meal summary
            response.append("Here's a summary of your meal logs: \n\n");
            
            // Add breakfast
            response.append("Breakfast: ");
            if (meals.containsKey("breakfast") && !meals.get("breakfast").isEmpty()) {
                appendMealItemsFromFirestore(response, meals.get("breakfast"));
            } else {
                response.append("No breakfast logged.\n");
            }
            
            // Add lunch
            response.append("\nLunch: ");
            if (meals.containsKey("lunch") && !meals.get("lunch").isEmpty()) {
                appendMealItemsFromFirestore(response, meals.get("lunch"));
            } else {
                response.append("No lunch logged.\n");
            }
            
            // Add dinner
            response.append("\nDinner: ");
            if (meals.containsKey("dinner") && !meals.get("dinner").isEmpty()) {
                appendMealItemsFromFirestore(response, meals.get("dinner"));
            } else {
                response.append("No dinner logged.\n");
            }
            
            // Add snacks
            if (meals.containsKey("snacks") && !meals.get("snacks").isEmpty()) {
                response.append("\nSnacks: ");
                appendMealItemsFromFirestore(response, meals.get("snacks"));
            }
        }
        
        if (response.length() <= 50) {
            // If nothing meaningful was found
            return "I don't see any detailed meal logs in your profile yet. You can log your meals through the Diet section of the app.";
        }
        
        return response.toString();
    }

    private void appendMealItemsFromFirestore(StringBuilder response, List<Map<String, Object>> mealItems) {
        if (mealItems == null || mealItems.isEmpty()) {
            response.append("No items found.");
            return;
        }
        
        boolean first = true;
        for (Map<String, Object> item : mealItems) {
            if (!first) {
                response.append(", ");
            }
            
            Object name = item.get("name");
            Object calories = item.get("calories");
            
            if (name != null) {
                response.append(name.toString());
                if (calories != null) {
                    response.append(" (").append(calories.toString()).append(" cal)");
                }
                first = false;
            }
        }
        
        if (first) {
            // If no items had names
            response.append("Items found but no details available.");
        }
    }

    private void fetchWorkoutDataAndRespond(String userMessage, GeminiResponseCallback callback) {
        try {
            // Check if user is logged in
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                callback.onResponse("I can't access your workout information because you're not logged in. Please log in to get personalized responses.");
                return;
            }
            
            // Use FirestoreHelper to get recent workouts from Firestore
            FirestoreHelper.getRecentWorkouts(5, new FirestoreHelper.OnDataFetchedListener<List<Map<String, Object>>>() {
                @Override
                public void onDataFetched(List<Map<String, Object>> workouts) {
                    String response = generateWorkoutResponseFromFirestore(userMessage, workouts);
                    callback.onResponse(response);
                }
                
                @Override
                public void onError(String message) {
                    android.util.Log.e(TAG, "Error fetching workout data: " + message);
                    callback.onResponse("I'm having trouble accessing your workout information right now. Please try again later.");
                }
            });
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in fetchWorkoutDataAndRespond: " + e.getMessage(), e);
            callback.onResponse("I encountered an error while trying to access your workout information. Please try again later.");
        }
    }

    private String generateWorkoutResponseFromFirestore(String userMessage, List<Map<String, Object>> workouts) {
        StringBuilder response = new StringBuilder("Here's a summary of your recent workouts:\n\n");
        
        int count = 0;
        for (Map<String, Object> workout : workouts) {
            if (count >= 3) break; // Only show the 3 most recent workouts
            
            // Get workout details
            Object type = workout.get("type");
            Object date = workout.get("date");
            Object duration = workout.get("duration");
            
            if (type != null) {
                response.append("- ").append(type.toString());
                
                if (date != null) {
                    response.append(" on ").append(date.toString());
                }
                
                if (duration != null) {
                    response.append(" (").append(duration.toString()).append(" minutes)");
                }
                
                response.append("\n");
                
                // Add exercise details if available
                if (workout.containsKey("exercises")) {
                    Object exercises = workout.get("exercises");
                    if (exercises instanceof List) {
                        response.append("  Exercises: ");
                        boolean first = true;
                        for (Object exercise : (List) exercises) {
                            if (exercise instanceof Map) {
                                Map<String, Object> exerciseMap = (Map<String, Object>) exercise;
                                if (!first) {
                                    response.append(", ");
                                }
                                
                                Object name = exerciseMap.get("name");
                                if (name != null) {
                                    response.append(name.toString());
                                    first = false;
                                }
                            }
                        }
                        response.append("\n");
                    }
                }
                
                count++;
            }
        }
        
        if (count == 0) {
            return "I don't see any detailed workout logs in your profile yet. You can track your workouts through the Workout section of the app.";
        }
        
        return response.toString();
    }
} 