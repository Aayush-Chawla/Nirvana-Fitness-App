package com.example.nirvana.fragments.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nirvana.R;
import com.example.nirvana.activities.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageButton btnGoogleSignIn;
    private Button tvRegister;

    // ActivityResultLauncher for Google Sign-In
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        
        // Set up the ActivityResultLauncher
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    handleGoogleSignInResult(data);
                } else {
                    Log.w(TAG, "Google sign in failed: result code " + result.getResultCode());
                    Toast.makeText(getContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
        tvRegister = view.findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        btnGoogleSignIn.setOnClickListener(v -> handleGoogleSignIn());
        tvRegister.setOnClickListener(v -> navigateToRegister());

        return view;
    }

    private void handleGoogleSignIn() {
        // Sign out first to force the account chooser to appear
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            // After sign out, show the account chooser
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Intent data) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            
            // Google Sign In was successful, authenticate with Firebase
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            // Google Sign In failed
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(getContext(), "Google sign in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful()) {
                    // Sign in success
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    
                    if (user != null) {
                        // Check if the user needs to complete profile setup
                        checkUserProfileStatus(user);
                    }
                } else {
                    // Sign in failed
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(getContext(), "Authentication failed: " + task.getException().getMessage(), 
                            Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void checkUserProfileStatus(FirebaseUser user) {
        String userId = user.getUid();
        DocumentReference userDocRef = mFirestore.collection("users").document(userId);
        DocumentReference profileDocRef = mFirestore.collection("users").document(userId).collection("profile").document("details");
        
        // First save basic user info regardless
        saveUserToFirestore(user);
        
        // Check if profile document exists and is complete
        profileDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists() && task.getResult().contains("profileCompleted") 
                        && Boolean.TRUE.equals(task.getResult().getBoolean("profileCompleted"))) {
                    // Profile exists and is complete, go to main activity
                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                } else {
                    // Profile doesn't exist or is incomplete, go to profile setup
                    Toast.makeText(getContext(), "Please complete your profile setup", Toast.LENGTH_SHORT).show();
                    navigateToProfileSetup();
                }
            } else {
                // Error checking profile, default to profile setup to be safe
                Log.e(TAG, "Error checking profile status", task.getException());
                navigateToProfileSetup();
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

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User logged in successfully");
                        
                        // Check if the user needs to complete profile setup
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserProfileStatus(user);
                        }
                    } else {
                        Log.e(TAG, "Login failed: " + task.getException().getMessage());
                        Toast.makeText(getContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
    
    private void navigateToProfileSetup() {
        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_profileSetupFragment);
    }

    private void navigateToRegister() {
        Navigation.findNavController(tvRegister).navigate(R.id.action_loginFragment_to_registrationFragment);
    }
}
