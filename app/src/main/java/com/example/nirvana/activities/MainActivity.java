package com.example.nirvana.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.nirvana.fragments.home.BottomMenuFragment;
import com.example.nirvana.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "NirvanaApp";
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

        // Setup Chatbot FAB
        setupChatbotFab();

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
    
    private void setupChatbotFab() {
        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        if (fabChat != null) {
            // Remove the click listener, we'll handle clicks in the touch listener
            
            // Make the FAB movable with touch events
            fabChat.setOnTouchListener(new View.OnTouchListener() {
                private float dX, dY;
                private boolean moving = false;
                private long startClickTime;
                private static final int MAX_CLICK_DURATION = 200;
                private static final float CLICK_MOVEMENT_THRESHOLD = 10f; // pixels
                private float startX, startY;
                
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dX = view.getX() - event.getRawX();
                            dY = view.getY() - event.getRawY();
                            startClickTime = System.currentTimeMillis();
                            moving = false;
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                            
                        case MotionEvent.ACTION_MOVE:
                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;
                            
                            // Check if we've moved enough to consider it a drag
                            float dx = Math.abs(event.getRawX() - startX);
                            float dy = Math.abs(event.getRawY() - startY);
                            if (dx > CLICK_MOVEMENT_THRESHOLD || dy > CLICK_MOVEMENT_THRESHOLD) {
                                moving = true;
                            }
                            
                            // Ensure the FAB stays within screen bounds
                            if (newX < 0) newX = 0;
                            if (newX > getWindow().getDecorView().getWidth() - view.getWidth()) 
                                newX = getWindow().getDecorView().getWidth() - view.getWidth();
                            if (newY < 0) newY = 0;
                            if (newY > getWindow().getDecorView().getHeight() - view.getHeight())
                                newY = getWindow().getDecorView().getHeight() - view.getHeight();
                            
                            view.setX(newX);
                            view.setY(newY);
                            break;
                            
                        case MotionEvent.ACTION_UP:
                            long clickDuration = System.currentTimeMillis() - startClickTime;
                            if (clickDuration < MAX_CLICK_DURATION && !moving) {
                                // This was a click, not a drag
                                Log.d(TAG, "Chatbot button clicked, launching ChatbotActivity");
                                Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
                                startActivity(intent);
                                return true;
                            }
                            break;
                            
                        default:
                            return false;
                    }
                    return true;
                }
            });
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}
