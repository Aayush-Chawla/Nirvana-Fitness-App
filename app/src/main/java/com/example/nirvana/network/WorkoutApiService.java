package com.example.nirvana.network;

import com.example.nirvana.data.models.WorkoutCategoryResponse;
import com.example.nirvana.data.models.ExerciseResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WorkoutApiService {
    @GET("workouts/categories")
    Call<WorkoutCategoryResponse> getGymWorkoutCategories();
    
    @GET("workouts/categories")
    Call<WorkoutCategoryResponse> getHomeWorkoutCategories();
    
    @GET("workouts")
    Call<WorkoutCategoryResponse> getWorkoutsByCategory(
        @Query("category") String category,
        @Query("experience") String experience
    );
    
    @GET("exercises")
    Call<ExerciseResponse> getExercisesByCategory(
        @Query("category") String category
    );
} 