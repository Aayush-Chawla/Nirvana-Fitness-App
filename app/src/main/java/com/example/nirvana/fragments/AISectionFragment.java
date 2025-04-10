package com.example.nirvana.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nirvana.R;
import com.example.nirvana.activities.ChatbotActivity;

public class AISectionFragment extends Fragment {

    private CardView cardFitnessAssistant, cardWorkoutPlanGenerator, cardBodyAnalysis, cardMealRecognition;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_section, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize cards
        cardFitnessAssistant = view.findViewById(R.id.cardFitnessAssistant);
        cardWorkoutPlanGenerator = view.findViewById(R.id.cardWorkoutPlanGenerator);
        cardBodyAnalysis = view.findViewById(R.id.cardBodyAnalysis);
        cardMealRecognition = view.findViewById(R.id.cardMealRecognition);
        
        // Set click listeners
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        // Fitness Assistant (ChatbotActivity)
        cardFitnessAssistant.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ChatbotActivity.class);
            startActivity(intent);
        });
        
        // Workout Plan Generator
        cardWorkoutPlanGenerator.setOnClickListener(v -> {
            try {
                Navigation.findNavController(requireView())
                        .navigate(R.id.workoutPlanGeneratorFragment);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Could not navigate to Workout Plan Generator", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Body Composition Analysis
        cardBodyAnalysis.setOnClickListener(v -> {
            try {
                Navigation.findNavController(requireView())
                        .navigate(R.id.bodyAnalysisFragment);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Body Composition Analysis coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Meal Recognition
        cardMealRecognition.setOnClickListener(v -> {
            try {
                Navigation.findNavController(requireView())
                        .navigate(R.id.mealRecognitionFragment);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Meal Recognition coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 