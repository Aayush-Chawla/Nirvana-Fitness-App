// FatSecretApi.java
package com.example.nirvana.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FatSecretApi {
    @GET("server.api")
    Call<FoodSearchResponse> searchFoods(
            @Query("method") String method,
            @Query("search_expression") String query,
            @Query("page_number") int pageNumber,
            @Query("max_results") int maxResults,
            @Query("format") String format
    );

    @GET("server.api")
    Call<FoodGetResponse> getFood(
            @Query("method") String method,
            @Query("food_id") String foodId,
            @Query("format") String format
    );
}