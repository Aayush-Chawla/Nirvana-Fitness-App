package com.example.nirvana.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.nirvana.R;
import com.example.nirvana.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore .FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupFragment extends Fragment {

    private EditText etName, etAge, etWeight, etHeight;
    private RadioGroup rgGender;
    private Button btnSaveContinue;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileSetupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_setup, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind Views
        etName = view.findViewById(R.id.etName);
        etAge = view.findViewById(R.id.etAge);
        etWeight = view.findViewById(R.id.etWeight);
        etHeight = view.findViewById(R.id.etHeight);
        rgGender = view.findViewById(R.id.rgGender);
        btnSaveContinue = view.findViewById(R.id.btnSaveProfile);

        // Set button click listener
        btnSaveContinue.setOnClickListener(v -> saveProfileDetails());

        return view;
    }

    private void saveProfileDetails() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String gender = getSelectedGender();

        // Validate input fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(weightStr) || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(gender)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate numeric values
        int age, weight, height;
        try {
            age = Integer.parseInt(ageStr);
            weight = Integer.parseInt(weightStr);
            height = Integer.parseInt(heightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numeric values for age, weight, and height", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure valid age, weight, and height
        if (age < 10 || age > 100) {
            Toast.makeText(getContext(), "Enter a valid age (10-100)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (weight < 20 || weight > 300) {
            Toast.makeText(getContext(), "Enter a valid weight (20-300 kg)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (height < 50 || height > 250) {
            Toast.makeText(getContext(), "Enter a valid height (50-250 cm)", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user data map
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("age", age);
        userProfile.put("weight", weight);
        userProfile.put("height", height);
        userProfile.put("gender", gender);
        userProfile.put("email", user.getEmail());

        // Save to Firestore
        db.collection("Users").document(user.getUid())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String getSelectedGender() {
        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == R.id.rbMale) {
            return "Male";
        } else if (selectedId == R.id.rbFemale) {
            return "Female";
        } else if (selectedId == R.id.rbOther) {
            return "Other";
        } else {
            return "";
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
