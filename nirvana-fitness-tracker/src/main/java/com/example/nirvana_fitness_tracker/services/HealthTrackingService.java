package com.example.nirvana_fitness_tracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.nirvana_fitness_tracker.MainActivity;
import com.example.nirvana_fitness_tracker.R;
import com.example.nirvana_fitness_tracker.models.HealthData;

/**
 * Service to track health metrics using device sensors
 */
public class HealthTrackingService extends Service implements SensorEventListener {
    private static final String TAG = "HealthTrackingService";
    private static final String NOTIFICATION_CHANNEL_ID = "health_tracking_channel";
    private static final int NOTIFICATION_ID = 1;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private Sensor heartRateSensor;
    
    // LiveData to communicate with the UI
    public static final MutableLiveData<HealthData> healthData = new MutableLiveData<>();
    
    // Current health metrics
    private long steps = 0;
    private double heartRate = 0;
    private double calories = 0;
    private double distance = 0;
    
    // Handler for periodic updates
    private Handler updateHandler;
    private static final long UPDATE_INTERVAL_MS = 30 * 1000; // 30 seconds

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Health Tracking Service created");
        
        // Initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        // Initialize sensors
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        
        // Initialize health data
        healthData.postValue(new HealthData(0, 0, 0, 0, ""));
        
        // Initialize update handler
        updateHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Health Tracking Service started");
        
        // Create notification channel for foreground service
        createNotificationChannel();
        
        // Start as foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Register sensor listeners
        registerSensors();
        
        // Start periodic updates
        startPeriodicUpdates();
        
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Health Tracking Service destroyed");
        
        // Unregister sensor listeners
        unregisterSensors();
        
        // Stop periodic updates
        stopPeriodicUpdates();
    }

    /**
     * Create notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.health_tracking_notification_title),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Create notification for foreground service
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.health_tracking_notification_title))
                .setContentText(getString(R.string.health_tracking_notification_text))
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .build();
    }

    /**
     * Register sensor listeners
     */
    private void registerSensors() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Step sensor registered");
        } else {
            Log.w(TAG, "Step sensor not available");
        }
        
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Heart rate sensor registered");
        } else {
            Log.w(TAG, "Heart rate sensor not available");
        }
    }

    /**
     * Unregister sensor listeners
     */
    private void unregisterSensors() {
        sensorManager.unregisterListener(this);
        Log.d(TAG, "Sensors unregistered");
    }

    /**
     * Start periodic updates for calculated metrics (calories, distance)
     */
    private void startPeriodicUpdates() {
        updateHandler.postDelayed(updateRunnable, UPDATE_INTERVAL_MS);
        Log.d(TAG, "Periodic updates started");
    }

    /**
     * Stop periodic updates
     */
    private void stopPeriodicUpdates() {
        updateHandler.removeCallbacks(updateRunnable);
        Log.d(TAG, "Periodic updates stopped");
    }

    /**
     * Runnable for periodic updates
     */
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            // Update calculated metrics
            updateCalculatedMetrics();
            
            // Schedule next update
            updateHandler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    /**
     * Update calculated metrics (calories, distance)
     */
    private void updateCalculatedMetrics() {
        // Simple calculation for calories based on steps (very approximate)
        // Average person burns about 0.04 calories per step
        calories = steps * 0.04;
        
        // Simple calculation for distance based on steps (very approximate)
        // Average step length is about 0.762 meters
        distance = steps * 0.762 / 1000.0; // Convert to kilometers
        
        // Update health data
        updateHealthData();
        
        Log.d(TAG, "Calculated metrics updated: calories=" + calories + ", distance=" + distance);
    }

    /**
     * Handle sensor events
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // For step counter, the value is cumulative since device reboot
            // In a real app, you'd need to track the initial value and calculate the difference
            steps = (long) event.values[0];
            Log.d(TAG, "Steps updated: " + steps);
            
            // Update calculated metrics when steps change
            updateCalculatedMetrics();
        } else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heartRate = event.values[0];
            Log.d(TAG, "Heart rate updated: " + heartRate);
            
            // Update health data when heart rate changes
            updateHealthData();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    /**
     * Update the health data and notify observers
     */
    private void updateHealthData() {
        HealthData data = new HealthData(steps, heartRate, calories, distance, "");
        healthData.postValue(data);
    }
} 