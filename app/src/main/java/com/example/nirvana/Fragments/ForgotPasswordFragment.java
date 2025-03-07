package com.example.nirvana.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.nirvana.databinding.FragmentForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    private static final String TAG = "ForgotPasswordFragment";
    private FragmentForgotPasswordBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();

        binding.btnResetPassword.setOnClickListener(v -> resetPassword());

        return view;
    }

    private void resetPassword() {
        String email = binding.etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required!");
            binding.etEmail.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            binding.etEmail.setError("Enter a valid email!");
            binding.etEmail.requestFocus();
            return;
        }

        // Show progress dialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Sending reset email...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        binding.btnResetPassword.setEnabled(false); // Prevent multiple clicks

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    binding.btnResetPassword.setEnabled(true);
                    Log.d(TAG, "Password reset email sent to: " + email);
                    showResetDialog();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    binding.btnResetPassword.setEnabled(true);
                    Log.e(TAG, "Failed to send reset email: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showResetDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Reset Email Sent")
                .setMessage("We've sent a password reset link to your email. Please check your inbox and follow the instructions.")
                .setPositiveButton("OK", (dialog, which) -> navigateToLogin())
                .setCancelable(false)
                .show();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Link Sent")
                .setMessage("A password reset link has been sent to your email.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();

                    // Navigate back to LoginFragment safely
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).navigate(R.id.action_forgotPasswordFragment_to_loginFragment);
                    }
                })
                .show();
    }


    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
