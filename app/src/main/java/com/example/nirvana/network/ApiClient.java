package com.example.nirvana.network;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://wger.de/api/v2/";
    private static final String WORKOUT_API_URL = "https://api.example.com/v1/"; // Unused, using mock data instead
    
    private static Retrofit retrofit = null;
    private static Retrofit workoutRetrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    public static Retrofit getWorkoutClient() {
        if (workoutRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            workoutRetrofit = new Retrofit.Builder()
                    .baseUrl(WORKOUT_API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return workoutRetrofit;
    }
    
    public static ExerciseApiService getExerciseApiService() {
        return getClient().create(ExerciseApiService.class);
    }
    
    public static WorkoutApiService getWorkoutApiService() {
        Log.d(TAG, "Using LocalMockApiClient for workout data instead of remote API");
        // Return the mock implementation instead of the real one
        return LocalMockApiClient.getWorkoutApiService();
    }
}
