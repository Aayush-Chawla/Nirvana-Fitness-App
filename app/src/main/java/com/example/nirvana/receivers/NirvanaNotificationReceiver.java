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
    
    public static final String EXTRA_NOTIFICATION_TITLE = "notification_title";
    public static final String EXTRA_NOTIFICATION_CONTENT = "notification_content";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String EXTRA_USERNAME = "username";
    
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
} 