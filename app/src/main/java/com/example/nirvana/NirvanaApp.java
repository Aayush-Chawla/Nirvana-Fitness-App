package com.example.nirvana;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class NirvanaApp extends Application {

    private static final String TAG = "NirvanaApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase programmatically
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                .setDatabaseUrl(BuildConfig.FIREBASE_DATABASE_URL)
                .setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
                .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                .build();

        FirebaseApp.initializeApp(this, options);
        Log.d(TAG, "Firebase initialized successfully");
    }
}