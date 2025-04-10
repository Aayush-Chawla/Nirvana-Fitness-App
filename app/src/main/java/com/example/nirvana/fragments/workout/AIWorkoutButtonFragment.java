package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nirvana.R;

/**
 * Fragment that displays an AI Workout Plan Generator button
 * This can be reused across multiple screens where the button should appear
 */
public class AIWorkoutButtonFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_ai_workout_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        view.findViewById(R.id.cardAiWorkoutPlan).setOnClickListener(v -> {
            try {
                Navigation.findNavController(view)
                    .navigate(R.id.workoutPlanGeneratorFragment);
            } catch (Exception e) {
                // Fallback if direct navigation fails
                try {
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.workoutPlanGeneratorFragment);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
} 