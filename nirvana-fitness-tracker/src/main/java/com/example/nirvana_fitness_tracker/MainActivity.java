package com.example.nirvana_fitness_tracker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.wear.ambient.AmbientModeSupport;

import com.example.nirvana_fitness_tracker.databinding.ActivityMainBinding;
import com.example.nirvana_fitness_tracker.models.HealthData;
import com.example.nirvana_fitness_tracker.services.DataSyncService;
import com.example.nirvana_fitness_tracker.services.HealthTrackingService;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.DataClient;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements AmbientModeSupport.AmbientCallbackProvider {
    private static final String TAG = "MainActivity";
    private static final String SYNC_STATUS_ACTION = "com.example.nirvana_fitness_tracker.SYNC_STATUS";
    
    // UI elements
    private ActivityMainBinding binding;
    private TextView stepsTextView;
    private TextView heartRateTextView;
    private TextView caloriesTextView;
    private TextView distanceTextView;
    private TextView statusTextView;
    private Button syncButton;
    
    // Data client for communication with phone
    private DataClient dataClient;
    
    // Permission request launcher
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    
    // Health data observer
    private Observer<HealthData> healthDataObserver;
    
    // Sync status receiver
    private BroadcastReceiver syncStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize UI elements
        stepsTextView = binding.stepsTextView;
        heartRateTextView = binding.heartRateTextView;
        caloriesTextView = binding.caloriesTextView;
        distanceTextView = binding.distanceTextView;
        statusTextView = binding.statusTextView;
        syncButton = binding.syncButton;
        
        // Set up ambient mode
        AmbientModeSupport.attach(this);
        
        // Initialize Wearable API client
        dataClient = Wearable.getDataClient(this);
        
        // Set up permission request launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    
                    if (allGranted) {
                        // All permissions granted, start services
                        startServices();
                    } else {
                        // Some permissions denied
                        statusTextView.setText(R.string.permission_required);
                        Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
                    }
                }
        );
        
        // Set up health data observer
        healthDataObserver = healthData -> {
            if (healthData != null) {
                updateUI(healthData);
            }
        };
        
        // Set up sync button
        syncButton.setOnClickListener(v -> syncData());
        
        // Set up sync status receiver
        syncStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra("success", false);
                String message = success ? getString(R.string.sync_success) : getString(R.string.sync_failed);
                statusTextView.setText(message);
                
                // Reset after 3 seconds
                binding.getRoot().postDelayed(() -> {
                    statusTextView.setText("Tracking active");
                }, 3000);
            }
        };
        
        // Check and request permissions
        checkAndRequestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Observe health data updates
        HealthTrackingService.healthData.observe(this, healthDataObserver);
        
        // Register sync status receiver
        registerReceiver(syncStatusReceiver, new IntentFilter(SYNC_STATUS_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Stop observing health data updates
        HealthTrackingService.healthData.removeObserver(healthDataObserver);
        
        // Unregister sync status receiver
        try {
            unregisterReceiver(syncStatusReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
    }

    /**
     * Check and request necessary permissions for health tracking
     */
    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Basic permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BODY_SENSORS);
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }
        
        if (!permissionsToRequest.isEmpty()) {
            // Request permissions
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            // All permissions already granted
            startServices();
        }
    }

    /**
     * Start the health tracking and data sync services
     */
    private void startServices() {
        // Start health tracking service
        Intent healthTrackingIntent = new Intent(this, HealthTrackingService.class);
        startService(healthTrackingIntent);
        
        // Start data sync service
        Intent dataSyncIntent = new Intent(this, DataSyncService.class);
        startService(dataSyncIntent);
        
        statusTextView.setText("Tracking active");
        Log.d(TAG, "Health tracking and data sync services started");
    }

    /**
     * Manually trigger data sync
     */
    private void syncData() {
        statusTextView.setText("Syncing...");
        
        // Create explicit intent for DataSyncService with sync action
        Intent syncIntent = new Intent(this, DataSyncService.class);
        syncIntent.setAction("SYNC_NOW");
        startService(syncIntent);
    }

    /**
     * Update the UI with health data
     */
    private void updateUI(HealthData healthData) {
        if (healthData == null) return;
        
        DecimalFormat df = new DecimalFormat("#.##");
        
        stepsTextView.setText(String.valueOf(healthData.getSteps()));
        
        if (healthData.getHeartRate() > 0) {
            heartRateTextView.setText(df.format(healthData.getHeartRate()) + " bpm");
        } else {
            heartRateTextView.setText("-- bpm");
        }
        
        caloriesTextView.setText(df.format(healthData.getCalories()) + " kcal");
        distanceTextView.setText(df.format(healthData.getDistance()) + " km");
    }

    /**
     * Implement ambient mode callback
     */
    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    /**
     * Ambient mode callback class
     */
    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            // Handle entering ambient mode (simplify UI, reduce updates, etc.)
            super.onEnterAmbient(ambientDetails);
        }

        @Override
        public void onExitAmbient() {
            // Handle exiting ambient mode
            super.onExitAmbient();
        }

        @Override
        public void onUpdateAmbient() {
            // Update the ambient UI
            super.onUpdateAmbient();
        }
    }
} 