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

public class GymWorkoutFragment extends Fragment {

    private CardView cardChest, cardBack, cardShoulders, cardArms, cardLegs, cardCore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gym_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupClickListeners();
    }

    private void initializeViews(View view) {
        cardChest = view.findViewById(R.id.cardChest);
        cardBack = view.findViewById(R.id.cardBack);
        cardShoulders = view.findViewById(R.id.cardShoulders);
        cardArms = view.findViewById(R.id.cardArms);
        cardLegs = view.findViewById(R.id.cardLegs);
        cardCore = view.findViewById(R.id.cardCore);
    }

    private void setupClickListeners() {
        cardChest.setOnClickListener(v -> navigateToWorkout("Chest"));
        cardBack.setOnClickListener(v -> navigateToWorkout("Back"));
        cardShoulders.setOnClickListener(v -> navigateToWorkout("Shoulders"));
        cardArms.setOnClickListener(v -> navigateToWorkout("Arms"));
        cardLegs.setOnClickListener(v -> navigateToWorkout("Legs"));
        cardCore.setOnClickListener(v -> navigateToWorkout("Core"));
    }

    private void navigateToWorkout(String category) {
        Bundle args = new Bundle();
        args.putString("category", category);
        Navigation.findNavController(requireView()).navigate(R.id.action_gymWorkoutFragment_to_workoutDetailFragment, args);
    }
}
