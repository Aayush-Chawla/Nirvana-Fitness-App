package com.example.nirvana.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.nirvana.data.models.WorkoutCategoryResponse;
import com.example.nirvana.data.models.ExerciseResponse;
import com.example.nirvana.utils.JsonUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mock API client that reads data from local JSON files instead of making network requests
 */
public class LocalMockApiClient {
    private static final String TAG = "LocalMockApiClient";
    private static Context appContext;
    private static Gson gson = new Gson();
    private static WorkoutApiService mockApiService;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
        mockApiService = new MockWorkoutApiService();
    }

    public static WorkoutApiService getWorkoutApiService() {
        if (mockApiService == null) {
            Log.e(TAG, "LocalMockApiClient not initialized. Call init() first.");
            mockApiService = new MockWorkoutApiService();
        }
        return mockApiService;
    }

    /**
     * Mock implementation of WorkoutApiService that loads data from JSON files
     */
    private static class MockWorkoutApiService implements WorkoutApiService {
        
        @Override
        public Call<WorkoutCategoryResponse> getGymWorkoutCategories() {
            return new MockCall<>(new Callback<WorkoutCategoryResponse>() {
                @Override
                public void onResponse(Call<WorkoutCategoryResponse> call, Response<WorkoutCategoryResponse> response) {
                    Log.d(TAG, "Mock API: getGymWorkoutCategories called");
                }

                @Override
                public void onFailure(Call<WorkoutCategoryResponse> call, Throwable t) {
                    Log.e(TAG, "Mock API failure", t);
                }
            }, loadGymWorkoutCategories());
        }

        @Override
        public Call<WorkoutCategoryResponse> getHomeWorkoutCategories() {
            return new MockCall<>(new Callback<WorkoutCategoryResponse>() {
                @Override
                public void onResponse(Call<WorkoutCategoryResponse> call, Response<WorkoutCategoryResponse> response) {
                    Log.d(TAG, "Mock API: getHomeWorkoutCategories called");
                }

                @Override
                public void onFailure(Call<WorkoutCategoryResponse> call, Throwable t) {
                    Log.e(TAG, "Mock API failure", t);
                }
            }, loadHomeWorkoutCategories());
        }

        @Override
        public Call<WorkoutCategoryResponse> getWorkoutsByCategory(String category, String experience) {
            return new MockCall<>(new Callback<WorkoutCategoryResponse>() {
                @Override
                public void onResponse(Call<WorkoutCategoryResponse> call, Response<WorkoutCategoryResponse> response) {
                    Log.d(TAG, "Mock API: getWorkoutsByCategory called with category=" + category + ", experience=" + experience);
                }

                @Override
                public void onFailure(Call<WorkoutCategoryResponse> call, Throwable t) {
                    Log.e(TAG, "Mock API failure", t);
                }
            }, loadWorkoutsByCategory(category, experience));
        }
        
        @Override
        public Call<ExerciseResponse> getExercisesByMuscleGroup(String muscleGroup) {
            return new MockCall<>(new Callback<ExerciseResponse>() {
                @Override
                public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                    Log.d(TAG, "Mock API: getExercisesByMuscleGroup called with muscleGroup=" + muscleGroup);
                }

                @Override
                public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                    Log.e(TAG, "Mock API failure", t);
                }
            }, loadExercisesByMuscleGroup(muscleGroup));
        }

        private WorkoutCategoryResponse loadGymWorkoutCategories() {
            try {
                String json = JsonUtils.loadJSONFromAsset(appContext, "gym_workout_categories.json");
                return gson.fromJson(json, WorkoutCategoryResponse.class);
            } catch (Exception e) {
                Log.e(TAG, "Error loading gym workout categories", e);
                return null;
            }
        }

        private WorkoutCategoryResponse loadHomeWorkoutCategories() {
            try {
                String json = JsonUtils.loadJSONFromAsset(appContext, "home_workout_categories.json");
                return gson.fromJson(json, WorkoutCategoryResponse.class);
            } catch (Exception e) {
                Log.e(TAG, "Error loading home workout categories", e);
                return null;
            }
        }

        private WorkoutCategoryResponse loadWorkoutsByCategory(String category, String experience) {
            // In a real implementation, you'd have category-specific JSON files
            // For now, we'll return the same data as home workouts
            try {
                String json = JsonUtils.loadJSONFromAsset(appContext, "home_workout_categories.json");
                return gson.fromJson(json, WorkoutCategoryResponse.class);
            } catch (Exception e) {
                Log.e(TAG, "Error loading workouts by category", e);
                return null;
            }
        }
        
        private ExerciseResponse loadExercisesByMuscleGroup(String muscleGroup) {
            try {
                // Convert muscleGroup to lowercase and remove spaces for file naming
                String fileName = "exercises_" + muscleGroup.toLowerCase().replace(' ', '_') + ".json";
                Log.d(TAG, "Loading exercises from file: " + fileName);
                String json = JsonUtils.loadJSONFromAsset(appContext, fileName);
                
                if (json == null) {
                    Log.w(TAG, "JSON file not found: " + fileName + ". Falling back to full_body exercises.");
                    json = JsonUtils.loadJSONFromAsset(appContext, "exercises_full_body.json");
                }
                
                return gson.fromJson(json, ExerciseResponse.class);
            } catch (Exception e) {
                Log.e(TAG, "Error loading exercises for muscle group: " + muscleGroup, e);
                return null;
            }
        }
    }

    /**
     * Mock implementation of Call<T> that returns predefined responses
     * @param <T> The response type
     */
    private static class MockCall<T> implements Call<T> {
        private final Callback<T> callback;
        private final T response;
        private boolean canceled = false;
        private final Timeout timeout;

        public MockCall(Callback<T> callback, T response) {
            this.callback = callback;
            this.response = response;
            this.timeout = Timeout.NONE;
        }

        @Override
        public Response<T> execute() {
            return Response.success(response);
        }

        @Override
        public void enqueue(Callback<T> callback) {
            if (canceled) {
                callback.onFailure(this, new IllegalStateException("Call is canceled"));
                return;
            }
            
            if (response == null) {
                callback.onFailure(this, new IllegalStateException("Response is null"));
                return;
            }

            // Simulate network delay (optional)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!canceled) {
                    callback.onResponse(this, Response.success(response));
                }
            }, 300);
        }

        @Override
        public boolean isExecuted() {
            return false;
        }

        @Override
        public void cancel() {
            canceled = true;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public Call<T> clone() {
            return new MockCall<>(callback, response);
        }

        @Override
        public okhttp3.Request request() {
            return new okhttp3.Request.Builder().url("https://mock.api/").build();
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }
    }
} 