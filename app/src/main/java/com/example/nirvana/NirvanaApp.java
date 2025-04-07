package com.example.nirvana;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class NirvanaApp extends Application {
    private static final String TAG = "NirvanaApp";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "Firebase initialized successfully");
    }
}