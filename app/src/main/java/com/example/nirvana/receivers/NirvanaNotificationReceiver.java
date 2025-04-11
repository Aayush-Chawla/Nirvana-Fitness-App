package com.example.nirvana.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.nirvana.utils.NotificationUtils;

public class NirvanaNotificationReceiver extends BroadcastReceiver {
    
    private static final String TAG = "NirvanaNotificationReceiver";
    
    public static final String ACTION_SHOW_NOTIFICATION = "com.example.nirvana.ACTION_SHOW_NOTIFICATION";
    public static final String ACTION_WELCOME_NOTIFICATION = "com.example.nirvana.ACTION_WELCOME_NOTIFICATION";
    public static final String ACTION_CALORIE_GOAL_50_PERCENT = "com.example.nirvana.ACTION_CALORIE_GOAL_50_PERCENT";
    public static final String ACTION_CALORIE_GOAL_COMPLETE = "com.example.nirvana.ACTION_CALORIE_GOAL_COMPLETE";
    
    public static final String EXTRA_NOTIFICATION_TITLE = "notification_title";
    public static final String EXTRA_NOTIFICATION_CONTENT = "notification_content";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_CURRENT_CALORIES = "current_calories";
    public static final String EXTRA_CALORIE_GOAL = "calorie_goal";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received action: " + intent.getAction());
        
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        
        switch (action) {
            case ACTION_WELCOME_NOTIFICATION:
                String username = intent.getStringExtra(EXTRA_USERNAME);
                if (username != null) {
                    NotificationUtils.showWelcomeNotification(context, username);
                    Log.d(TAG, "Showing welcome notification for user: " + username);
                }
                break;
                
            case ACTION_CALORIE_GOAL_50_PERCENT:
                long currentCal50 = intent.getLongExtra(EXTRA_CURRENT_CALORIES, 0);
                long goal50 = intent.getLongExtra(EXTRA_CALORIE_GOAL, 0);
                if (goal50 > 0) {
                    showCalorieHalfwayNotification(context, currentCal50, goal50);
                    Log.d(TAG, "Showing 50% calorie goal notification: " + currentCal50 + "/" + goal50);
                }
                break;
                
            case ACTION_CALORIE_GOAL_COMPLETE:
                long currentCal100 = intent.getLongExtra(EXTRA_CURRENT_CALORIES, 0);
                long goal100 = intent.getLongExtra(EXTRA_CALORIE_GOAL, 0);
                if (goal100 > 0) {
                    showCalorieGoalCompleteNotification(context, currentCal100, goal100);
                    Log.d(TAG, "Showing 100% calorie goal notification: " + currentCal100 + "/" + goal100);
                }
                break;
                
            case ACTION_SHOW_NOTIFICATION:
                String title = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE);
                String content = intent.getStringExtra(EXTRA_NOTIFICATION_CONTENT);
                int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1000);
                
                if (title != null && content != null) {
                    NotificationUtils.showNotification(context, title, content, notificationId);
                    Log.d(TAG, "Showing notification: " + title);
                }
                break;
                
            default:
                Log.d(TAG, "Unknown action: " + action);
                break;
        }
    }
    
    /**
     * Show notification when user reaches 50% of their calorie goal
     */
    private void showCalorieHalfwayNotification(Context context, long currentCalories, long calorieGoal) {
        Intent intent = new Intent(context, com.example.nirvana.activities.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("fragment", "diet"); // Optional: to navigate to diet fragment
        
        NotificationUtils.showNotification(
                context, 
                "Halfway to Your Calorie Goal!", 
                "You've consumed " + currentCalories + " calories, halfway to your daily goal of " + calorieGoal + " calories.",
                NotificationUtils.CALORIE_GOAL_50_PERCENT_ID);
    }
    
    /**
     * Show notification when user reaches 100% of their calorie goal
     */
    private void showCalorieGoalCompleteNotification(Context context, long currentCalories, long calorieGoal) {
        Intent intent = new Intent(context, com.example.nirvana.activities.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("fragment", "diet"); // Optional: to navigate to diet fragment
        
        NotificationUtils.showNotification(
                context, 
                "Daily Calorie Goal Reached!", 
                "Congratulations! You've reached your daily goal of " + calorieGoal + " calories.",
                NotificationUtils.CALORIE_GOAL_COMPLETE_ID);
    }
} 