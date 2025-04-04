package com.example.nirvana.api;

import android.content.Context;
import android.util.Log;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class ApiClient {
    // Remove trailing slash from base URL
    private static final String BASE_URL = "https://platform.fatsecret.com/rest/server.api";
    private static final String CONSUMER_KEY = "e02d0ba3dbad4a788effda4e4a65d794";
    private static final String CONSUMER_SECRET = "aa6318afc5cd400394180e706bc2ffd4";

    private static Retrofit retrofit = null;
    private static OAuth10aService service = new ServiceBuilder(CONSUMER_KEY)
            .apiSecret(CONSUMER_SECRET)
            .build(FatSecretOAuthApi.instance());

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        HttpUrl originalUrl = original.url();

                        // Create and sign OAuth request
                        OAuthRequest oAuthRequest = new OAuthRequest(
                                com.github.scribejava.core.model.Verb.GET,
                                originalUrl.toString()
                        );

                        service.signRequest(new OAuth1AccessToken("", ""), oAuthRequest);
                        Log.d("OAUTH_SIGNATURE", "Signed URL: " + oAuthRequest.getUrl());

                        // Build final signed request
                        Request signedRequest = original.newBuilder()
                                .url(oAuthRequest.getUrl())
                                .build();

                        return chain.proceed(signedRequest);
                    })
                    .addInterceptor(logging);

            // Create a lenient Gson instance
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL + "/")
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static FatSecretApi getFatSecretApi(Context context) {
        return getClient(context).create(FatSecretApi.class);
    }
}