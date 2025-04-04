package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nirvana.R;

public class HomeWorkoutFragment extends Fragment {

    private CardView cardUpperBody, cardLowerBody, cardAbs, cardLegs, cardCalisthenics;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupClickListeners();
    }

    private void initializeViews(View view) {
        cardUpperBody = view.findViewById(R.id.cardUpperBody);
        cardLowerBody = view.findViewById(R.id.cardLowerBody);
        cardAbs = view.findViewById(R.id.cardAbs);
        cardLegs = view.findViewById(R.id.cardLegs);
        cardCalisthenics = view.findViewById(R.id.cardCalisthenics);
    } 

    private void setupClickListeners() {
        cardUpperBody.setOnClickListener(v -> navigateToWorkout("Upper Body"));
        cardLowerBody.setOnClickListener(v -> navigateToWorkout("Lower Body"));
        cardAbs.setOnClickListener(v -> navigateToWorkout("Abs"));
        cardLegs.setOnClickListener(v -> navigateToWorkout("Legs"));
        cardCalisthenics.setOnClickListener(v -> navigateToWorkout("Calisthenics"));
    }

    private void navigateToWorkout(String category) {
        Bundle args = new Bundle();
        args.putString("category", category);
        Navigation.findNavController(requireView()).navigate(R.id.action_homeWorkoutFragment_to_workoutDetailFragment, args);
    }
}
