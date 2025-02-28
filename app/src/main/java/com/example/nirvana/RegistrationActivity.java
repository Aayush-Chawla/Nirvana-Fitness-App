package com.example.nirvana;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLogin; // Added TextView for "Already have an account? Login"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Set click listener for register button
        btnRegister.setOnClickListener(v -> registerUser());

        // Set click listener for login navigation
        tvLogin.setOnClickListener(this::navigateToLogin);
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User registered successfully");

                        // Get the newly created user
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            navigateToProfileSetup();
                        }
                    } else {
                        Log.e(TAG, "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToProfileSetup() {
        Intent intent = new Intent(this, ProfileSetupActivity.class);
        startActivity(intent);
        finish(); // Close RegistrationActivity to prevent going back
    }

    public void navigateToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close RegistrationActivity so user can't go back to it
    }
}
