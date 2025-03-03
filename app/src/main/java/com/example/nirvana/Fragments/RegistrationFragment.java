package com.example.nirvana.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.nirvana.R;
import com.example.nirvana.databinding.FragmentRegistrationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RegistrationFragment extends BottomSheetDialogFragment {

    private static final String TAG = "RegistrationFragment";
    private FirebaseAuth mAuth;
    private FragmentRegistrationBinding binding;
//    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
//        navController = Navigation.findNavController(view);

        // Button to close or dismiss registration (go to login)
        binding.btnLogin.setOnClickListener(v -> {
            dismiss(); // Close the Bottom Sheet
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.auth_container, new LoginFragment()) // Show LoginFragment again
                    .commit();
        }); // Dismiss the Bottom Sheet

        // Register user
        binding.btnRegister.setOnClickListener(v -> registerUser());
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
                            Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                            dismiss(); // Close Bottom Sheet after successful registration
                            // You can handle navigation to another fragment here if needed
                            // For example, navigating to a profile setup fragment:
                            // navController.navigate(R.id.action_registrationFragment_to_profileSetupFragment);
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.auth_container, new ProfileSetupFragment())
                                    .addToBackStack(null) // Allow back navigation
                                    .commit();
                        }
                    } else {
                        Log.e(TAG, "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(requireContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
