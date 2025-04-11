package com.example.nirvana.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.nirvana.R;
import com.example.nirvana.activities.MainActivity;

public class NotificationUtils {
    
    public static final String CHANNEL_ID = "nirvana_notifications";
    public static final int WELCOME_NOTIFICATION_ID = 1001;
    public static final int CALORIE_GOAL_50_PERCENT_ID = 1002;
    public static final int CALORIE_GOAL_COMPLETE_ID = 1003;
    
    // Track if we've already shown the notifications for today to avoid duplicates
    private static boolean calorie50PercentNotificationShown = false;
    private static boolean calorieCompleteNotificationShown = false;

    /**
     * Create notification channels for the app
     * @param context The application context
     */
    public static void createNotificationChannels(Context context) {
        // Only needed for Android 8.0 (API level 26) and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nirvana Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("General notifications from Nirvana Fitness App");
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Show welcome notification to the user
     * @param context The application context
     * @param username The username to personalize the notification
     */
    public static void showWelcomeNotification(Context context, String username) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Welcome to Nirvana Fitness!")
                .setContentText("Hello " + username + "! We're excited to help you on your fitness journey.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        try {
            notificationManager.notify(WELCOME_NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            // This is thrown if the app doesn't have POST_NOTIFICATIONS permission on Android 13+
            e.printStackTrace();
        }
    }
    
    /**
     * Show a custom notification
     * @param context The application context
     * @param title The notification title
     * @param content The notification content
     * @param notificationId A unique ID for the notification
     */
    public static void showNotification(Context context, String title, String content, int notificationId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // This is thrown if the app doesn't have POST_NOTIFICATIONS permission on Android 13+
            e.printStackTrace();
        }
    }
    
    /**
     * Check calorie progress and show notifications at 50% and 100% of goal
     * @param context The application context
     * @param currentCalories Current calories consumed
     * @param calorieGoal Calorie goal for the day
     */
    public static void checkAndShowCalorieGoalNotifications(Context context, long currentCalories, long calorieGoal) {
        if (calorieGoal <= 0) {
            return; // No goal set or invalid goal
        }
        
        // Calculate percentage of goal reached
        double percentComplete = (double) currentCalories / calorieGoal * 100;
        
        // Check for 50% goal completion
        if (!calorie50PercentNotificationShown && percentComplete >= 50 && percentComplete < 100) {
            showCalorieHalfwayNotification(context, currentCalories, calorieGoal);
            calorie50PercentNotificationShown = true;
        }
        
        // Check for 100% goal completion
        if (!calorieCompleteNotificationShown && percentComplete >= 100) {
            showCalorieGoalCompleteNotification(context, currentCalories, calorieGoal);
            calorieCompleteNotificationShown = true;
        }
    }
    
    /**
     * Reset notification flags (should be called at the start of each day)
     */
    public static void resetCalorieNotificationFlags() {
        calorie50PercentNotificationShown = false;
        calorieCompleteNotificationShown = false;
    }
    
    /**
     * Show notification when user reaches 50% of their calorie goal
     */
    private static void showCalorieHalfwayNotification(Context context, long currentCalories, long calorieGoal) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Halfway to Your Calorie Goal!")
                .setContentText("You've consumed " + currentCalories + " calories, halfway to your daily goal of " + calorieGoal + " calories.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        try {
            notificationManager.notify(CALORIE_GOAL_50_PERCENT_ID, builder.build());
        } catch (SecurityException e) {
            // This is thrown if the app doesn't have POST_NOTIFICATIONS permission on Android 13+
            e.printStackTrace();
        }
    }
    
    /**
     * Show notification when user reaches 100% of their calorie goal
     */
    private static void showCalorieGoalCompleteNotification(Context context, long currentCalories, long calorieGoal) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Daily Calorie Goal Reached!")
                .setContentText("Congratulations! You've reached your daily goal of " + calorieGoal + " calories.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        try {
            notificationManager.notify(CALORIE_GOAL_COMPLETE_ID, builder.build());
        } catch (SecurityException e) {
            // This is thrown if the app doesn't have POST_NOTIFICATIONS permission on Android 13+
            e.printStackTrace();
        }
    }
} 