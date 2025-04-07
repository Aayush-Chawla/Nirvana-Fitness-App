package com.example.nirvana.api;

import com.example.nirvana.models.BlogPost;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface BlogApiService {
    @GET("posts")
    Call<List<BlogPost>> getPosts(
        @Query("page") int page,
        @Query("per_page") int perPage
    );

    @GET("posts/categories/fitness")
    Call<List<BlogPost>> getFitnessPosts(
        @Query("page") int page,
        @Query("per_page") int perPage
    );

    @GET("posts/categories/nutrition")
    Call<List<BlogPost>> getNutritionPosts(
        @Query("page") int page,
        @Query("per_page") int perPage
    );
} 