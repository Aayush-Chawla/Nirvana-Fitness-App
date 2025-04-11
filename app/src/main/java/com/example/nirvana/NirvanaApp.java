package com.example.nirvana;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.example.nirvana.api.LocalMockApiClient;
import com.example.nirvana.utils.FirestoreHelper;
import com.example.nirvana.utils.NotificationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NirvanaApp extends Application {
    private static final String TAG = "NirvanaApp";
    private static NirvanaApp instance;
    private LocalMockApiClient mockApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "Firebase initialized successfully");
        
        // Initialize Local Mock API Client
        mockApiClient = new LocalMockApiClient(this);
        Log.d(TAG, "LocalMockApiClient initialized successfully");
        
        // Create notification channels
        NotificationUtils.createNotificationChannels(this);
        Log.d(TAG, "Notification channels created successfully");
        
        // Trigger data migration if user is logged in
        triggerDataMigration();
    }

    public static NirvanaApp getInstance() {
        return instance;
    }

    public LocalMockApiClient getMockApiClient() {
        return mockApiClient;
    }
    
    /**
     * Triggers migration of user data from Firebase Realtime Database to Firestore
     * if a user is currently logged in
     */
    private void triggerDataMigration() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Attempting to migrate data for user: " + userId);
            
            FirestoreHelper.migrateUserData(
                userId,
                unused -> Log.d(TAG, "Migration completed successfully for user: " + userId),
                error -> Log.e(TAG, "Migration failed: " + error)
            );
        } else {
            Log.d(TAG, "No user logged in, skipping data migration");
        }
    }
}