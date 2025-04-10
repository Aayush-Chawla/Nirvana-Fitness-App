package com.example.nirvana_fitness_tracker.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.nirvana_fitness_tracker.models.HealthData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Utility class to handle Firebase synchronization
 */
public class FirebaseSyncUtil {
    private static final String TAG = "FirebaseSyncUtil";
    private static final String HEALTH_DATA_PATH = "health_data";
    private static final String WATCH_DATA_PATH = "watch_data";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private SyncCallback syncCallback;

    public interface SyncCallback {
        void onSyncComplete(boolean success);
    }

    public FirebaseSyncUtil(SyncCallback callback) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.syncCallback = callback;
    }

    /**
     * Synchronize health data with Firebase
     * @param healthData The health data to sync
     */
    public void syncHealthData(HealthData healthData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user logged in, cannot sync data");
            if (syncCallback != null) {
                syncCallback.onSyncComplete(false);
            }
            return;
        }

        // Ensure the user ID is set in the health data
        String userId = currentUser.getUid();
        healthData.setUserId(userId);

        // Create a unique key for this data point
        String key = databaseReference.child(HEALTH_DATA_PATH)
                .child(userId)
                .child(WATCH_DATA_PATH)
                .push().getKey();

        if (key == null) {
            Log.e(TAG, "Couldn't get Firebase key for new data");
            if (syncCallback != null) {
                syncCallback.onSyncComplete(false);
            }
            return;
        }

        // Save to the database
        databaseReference.child(HEALTH_DATA_PATH)
                .child(userId)
                .child(WATCH_DATA_PATH)
                .child(key)
                .setValue(healthData.toMap())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Data synced successfully");
                            if (syncCallback != null) {
                                syncCallback.onSyncComplete(true);
                            }
                        } else {
                            Log.e(TAG, "Data sync failed", task.getException());
                            if (syncCallback != null) {
                                syncCallback.onSyncComplete(false);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Data sync failed", e);
                        if (syncCallback != null) {
                            syncCallback.onSyncComplete(false);
                        }
                    }
                });
    }
} 