package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nirvana.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActiveWorkoutFragment extends Fragment {
    private String category;
    private String experienceLevel;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category", "");
            experienceLevel = getArguments().getString("experience", "Beginner");
        }
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_active_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopBar(view);
        setupWorkoutInfo(view);
    }

    private void setupTopBar(View view) {
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton imgProfile = view.findViewById(R.id.imgProfile);
        TextView txtUsername = view.findViewById(R.id.txtUsername);

        // Set up navigation
        btnBack.setOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigateUp()
        );

        // Set up profile button
        imgProfile.setOnClickListener(v -> {
            // TODO: Navigate to profile screen
            // Navigation.findNavController(requireView()).navigate(R.id.action_activeWorkoutFragment_to_profileFragment);
        });

        // Set username
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null && !username.isEmpty()) {
                            txtUsername.setText(username);
                        } else {
                            txtUsername.setText(currentUser.getEmail());
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    txtUsername.setText(currentUser.getEmail())
                );
        }
    }

    private void setupWorkoutInfo(View view) {
        TextView txtWorkoutTitle = view.findViewById(R.id.txtWorkoutTitle);
        txtWorkoutTitle.setText(String.format("%s Workout - %s", category, experienceLevel));
    }
} 