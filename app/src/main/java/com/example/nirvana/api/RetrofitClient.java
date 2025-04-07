package com.example.nirvana.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String WGER_BASE_URL = "https://wger.de/api/v2/";
    private static final String BLOG_BASE_URL = "https://api.wordpress.org/wp/v2/";  // Using WordPress API as a temporary solution

    private static Retrofit wgerRetrofit = null;
    private static Retrofit blogRetrofit = null;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public static Retrofit getWgerClient() {
        if (wgerRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            wgerRetrofit = new Retrofit.Builder()
                .baseUrl(WGER_BASE_URL)
                .client(httpClient.addInterceptor(logging).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return wgerRetrofit;
    }

    public static Retrofit getBlogClient() {
        if (blogRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            blogRetrofit = new Retrofit.Builder()
                .baseUrl(BLOG_BASE_URL)
                .client(httpClient.addInterceptor(logging).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return blogRetrofit;
    }

    public static ExerciseApiService getExerciseService() {
        return getWgerClient().create(ExerciseApiService.class);
    }

    public static BlogApiService getBlogService() {
        return getBlogClient().create(BlogApiService.class);
    }
} 