package com.example.nirvana.fragments.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nirvana.R;
import com.example.nirvana.databinding.FragmentRegistrationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationFragment extends Fragment {

    private static final String TAG = "RegistrationFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FragmentRegistrationBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Button to go to the login fragment
        binding.btnLogin.setOnClickListener(v -> navigateToLogin());

        // Register user
        binding.btnRegister.setOnClickListener(v -> registerUser());
    }

    // Add this method to handle the onClick event
    private void navigateToLogin() {
        Navigation.findNavController(requireView()).navigate(R.id.action_registrationFragment_to_loginFragment);
    }

    private void registerUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User registered successfully");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to Firestore
                            saveUserToFirestore(user);
                            
                            Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                            // Navigate to ProfileSetupFragment after successful registration
                            Navigation.findNavController(requireView()).navigate(R.id.action_registrationFragment_to_profileSetupFragment);
                        }
                    } else {
                        Log.e(TAG, "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(requireContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void saveUserToFirestore(FirebaseUser user) {
        String userId = user.getUid();
        DocumentReference userDocRef = mFirestore.collection("users").document(userId);
        
        // Check if user document already exists
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    // Create new user document
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", userId);
                    userData.put("email", user.getEmail());
                    userData.put("displayName", user.getDisplayName());
                    userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
                    userData.put("createdAt", System.currentTimeMillis());
                    
                    // Set user data
                    userDocRef.set(userData)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User document created successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error creating user document", e));
                } else {
                    // Update last login time
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lastLogin", System.currentTimeMillis());
                    
                    // Update user data
                    userDocRef.update(updates)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User document updated successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating user document", e));
                }
            } else {
                Log.e(TAG, "Error checking if user exists", task.getException());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // Avoid memory leaks
    }
}
