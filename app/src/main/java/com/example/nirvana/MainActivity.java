package com.example.nirvana;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        // Logout button setup
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out from Firebase
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity so the user can't go back
    }


    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity to prevent going back
    }


}