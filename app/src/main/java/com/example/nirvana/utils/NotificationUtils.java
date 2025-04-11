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
} 