package com.example.nirvana_fitness_tracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.Observer;

import com.example.nirvana_fitness_tracker.models.HealthData;
import com.example.nirvana_fitness_tracker.utils.FirebaseSyncUtil;

/**
 * Service to periodically sync health data with Firebase
 */
public class DataSyncService extends Service implements FirebaseSyncUtil.SyncCallback {
    private static final String TAG = "DataSyncService";
    private static final long SYNC_INTERVAL_MS = 15 * 60 * 1000; // 15 minutes
    private static final String ACTION_SYNC_NOW = "SYNC_NOW";

    private FirebaseSyncUtil firebaseSyncUtil;
    private Handler syncHandler;
    private Runnable syncRunnable;
    private HealthData lastSyncedData;
    private Observer<HealthData> healthDataObserver;
    private boolean isSyncing = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Data Sync Service created");
        
        // Initialize Firebase sync utility
        firebaseSyncUtil = new FirebaseSyncUtil(this);
        
        // Initialize handler for periodic sync
        syncHandler = new Handler();
        
        // Create sync runnable
        syncRunnable = () -> {
            if (lastSyncedData != null && !isSyncing) {
                Log.d(TAG, "Performing scheduled sync of health data");
                syncHealthData(lastSyncedData);
            }
            
            // Schedule next sync
            scheduleNextSync();
        };
        
        // Create observer for health data updates
        healthDataObserver = healthData -> {
            if (healthData != null) {
                Log.d(TAG, "Health data updated, saving for next sync");
                lastSyncedData = healthData;
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Data Sync Service started");
        
        // Handle sync now action
        if (intent != null && ACTION_SYNC_NOW.equals(intent.getAction())) {
            syncNow();
        }
        
        // Observe health data updates from the tracking service
        HealthTrackingService.healthData.observeForever(healthDataObserver);
        
        // Schedule first sync
        scheduleNextSync();
        
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Data Sync Service destroyed");
        
        // Stop observing health data
        HealthTrackingService.healthData.removeObserver(healthDataObserver);
        
        // Remove scheduled sync
        syncHandler.removeCallbacks(syncRunnable);
    }

    /**
     * Schedule the next sync after the defined interval
     */
    private void scheduleNextSync() {
        syncHandler.removeCallbacks(syncRunnable);
        syncHandler.postDelayed(syncRunnable, SYNC_INTERVAL_MS);
        Log.d(TAG, "Next sync scheduled in " + (SYNC_INTERVAL_MS / 60000) + " minutes");
    }

    /**
     * Sync health data immediately (called from MainActivity)
     */
    public void syncNow() {
        if (lastSyncedData != null && !isSyncing) {
            Log.d(TAG, "Performing immediate sync of health data");
            syncHealthData(lastSyncedData);
        } else {
            Log.w(TAG, "No health data available to sync or sync already in progress");
        }
    }

    /**
     * Sync health data with Firebase
     */
    private void syncHealthData(HealthData healthData) {
        isSyncing = true;
        firebaseSyncUtil.syncHealthData(healthData);
    }

    /**
     * Callback when sync is complete
     */
    @Override
    public void onSyncComplete(boolean success) {
        String status = success ? "successfully" : "with errors";
        Log.d(TAG, "Health data synced " + status);
        
        // Reset syncing flag
        isSyncing = false;
        
        // Notify MainActivity of sync status (implement broadcast or other mechanism if needed)
        Intent syncStatusIntent = new Intent("com.example.nirvana_fitness_tracker.SYNC_STATUS");
        syncStatusIntent.putExtra("success", success);
        sendBroadcast(syncStatusIntent);
    }
} 