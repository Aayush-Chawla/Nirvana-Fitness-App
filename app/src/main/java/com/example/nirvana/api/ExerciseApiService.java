package com.example.nirvana.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Headers;

public interface ExerciseApiService {
    @Headers({
        "Accept: application/json",
        "Authorization: Token YOUR_WGER_API_TOKEN" // Replace with your Wger API token
    })
    @GET("exercise/")
    Call<ExerciseResponse> getExercises(
        @Query("language") int language,
        @Query("limit") int limit,
        @Query("offset") int offset
    );

    @GET("exerciseinfo/")
    Call<ExerciseInfoResponse> getExerciseInfo(
        @Query("language") int language,
        @Query("limit") int limit,
        @Query("offset") int offset
    );

    @GET("exercisecategory/")
    Call<ExerciseCategoryResponse> getExerciseCategories();
} 