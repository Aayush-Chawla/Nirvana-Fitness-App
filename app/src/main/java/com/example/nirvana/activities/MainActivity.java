package com.example.nirvana.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.nirvana.fragments.home.BottomMenuFragment;
import com.example.nirvana.R;
import com.example.nirvana.receivers.NirvanaNotificationReceiver;
import com.example.nirvana.utils.NotificationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "NirvanaApp";
    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 100;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return; // Exit onCreate to prevent further execution
        } else {
            Log.d(TAG, "User logged in: " + currentUser.getEmail());
            
            // Check and request notification permission for Android 13+ 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, 
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                            PERMISSION_REQUEST_NOTIFICATIONS);
                } else {
                    showWelcomeNotification(currentUser);
                }
            } else {
                showWelcomeNotification(currentUser);
            }
        }

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Ensure Home is selected by default
        bottomNavigationView.post(() -> bottomNavigationView.setSelectedItemId(R.id.homeFragment));

        // Profile Button
        ImageButton imgProfile = findViewById(R.id.imgProfile);
        imgProfile.setOnClickListener(v -> {
            BottomMenuFragment bottomSheetDialog = new BottomMenuFragment();
            bottomSheetDialog.show(getSupportFragmentManager(), "Test"); // Fixed method call
        });


        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String timeGreeting;
        if (hour >= 5 && hour < 12) {
            timeGreeting = "Morning";
        } else if (hour >= 12 && hour < 17) {
            timeGreeting = "Afternoon";
//        } else if (hour >= 17 && hour < 21) {
//            timeGreeting = "Evening";
        } else {
            timeGreeting = "Evening";
        }

        TextView greeting = findViewById(R.id.text_greeting);
        String name = currentUser.getEmail().split("@")[0];
        greeting.setText("Good "+ timeGreeting+ ", "  + name);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, show welcome notification
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    showWelcomeNotification(currentUser);
                }
            } else {
                // Permission denied
                Log.d(TAG, "Notification permission denied");
            }
        }
    }
    
    private void showWelcomeNotification(FirebaseUser user) {
        String username = user.getEmail().split("@")[0];
        
        // Method 1: Using the BroadcastReceiver
        Intent intent = new Intent(this, NirvanaNotificationReceiver.class);
        intent.setAction(NirvanaNotificationReceiver.ACTION_WELCOME_NOTIFICATION);
        intent.putExtra(NirvanaNotificationReceiver.EXTRA_USERNAME, username);
        sendBroadcast(intent);
        
        // Method 2: Direct notification (alternative)
        // NotificationUtils.showWelcomeNotification(this, username);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}
