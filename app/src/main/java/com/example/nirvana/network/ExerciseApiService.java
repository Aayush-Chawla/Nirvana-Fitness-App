package com.example.nirvana.network;

import com.example.nirvana.data.models.ExerciseResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ExerciseApiService {
    @GET("exercise/?language=2&limit=20") // Fetch 20 exercises in English
    Call<ExerciseResponse> getExercises();
}
