package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.ExerciseAdapter;
import com.example.nirvana.models.Exercise;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class CustomWorkoutFragment extends Fragment implements ExerciseAdapter.OnExerciseClickListener {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ExerciseAdapter adapter;
    private String selectedDay = "Monday";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView(view);
        setupWeekdayTabs(view);
        setupFabButton(view);
        setupFirebase();
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.exerciseList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ExerciseAdapter(this);
        recyclerView.setAdapter(adapter);
        
        // Initialize with empty list
        adapter.setExercises(new ArrayList<>());
    }

    private void setupWeekdayTabs(View view) {
        TabLayout tabLayout = view.findViewById(R.id.weekdayTabs);
        String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        
        for (String day : weekdays) {
            tabLayout.addTab(tabLayout.newTab().setText(day));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedDay = tab.getText().toString();
                loadWorkoutForDay(selectedDay);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupFabButton(View view) {
        view.findViewById(R.id.fabAddExercise).setOnClickListener(v -> {
            showExerciseSelectionDialog();
        });
    }

    private void showExerciseSelectionDialog() {
        ExerciseSelectionDialog dialog = ExerciseSelectionDialog.newInstance();
        dialog.setOnExerciseSelectedListener(exercise -> {
            // Handle the selected exercise
            addExerciseToWorkout(exercise);
        });
        dialog.show(getChildFragmentManager(), "exercise_selection");
    }
    
    private void addExerciseToWorkout(Exercise exercise) {
        if (auth.getCurrentUser() == null) {
            Log.w("CustomWorkout", "No user logged in");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        Log.d("CustomWorkout", "Adding exercise to day: " + selectedDay + " for user: " + userId);
        
        db.collection("users")
            .document(userId)
            .collection("workouts")
            .document(selectedDay.toLowerCase())
            .collection("exercises")
            .add(exercise)
            .addOnSuccessListener(documentReference -> {
                Log.d("CustomWorkout", "Exercise added with ID: " + documentReference.getId());
                loadWorkoutForDay(selectedDay);
            })
            .addOnFailureListener(e -> {
                Log.e("CustomWorkout", "Error adding exercise", e);
            });
    }

    private void setupFirebase() {
        loadWorkoutForDay(selectedDay);
    }

    private void loadWorkoutForDay(String day) {
        if (auth.getCurrentUser() == null) {
            Log.w("CustomWorkout", "No user logged in");
            adapter.setExercises(new ArrayList<>());
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        Log.d("CustomWorkout", "Loading exercises for day: " + day + " and user: " + userId);
        
        db.collection("users")
            .document(userId)
            .collection("workouts")
            .document(day.toLowerCase())
            .collection("exercises")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Exercise> exercises = new ArrayList<>();
                queryDocumentSnapshots.forEach(document -> {
                    Exercise exercise = document.toObject(Exercise.class);
                    exercise.setId(document.getId());
                    exercises.add(exercise);
                });
                Log.d("CustomWorkout", "Loaded " + exercises.size() + " exercises");
                adapter.setExercises(exercises);
            })
            .addOnFailureListener(e -> {
                Log.e("CustomWorkout", "Error loading exercises", e);
                adapter.setExercises(new ArrayList<>());
            });
    }

    @Override
    public void onExerciseClick(Exercise exercise) {
        Bundle args = new Bundle();
        args.putSerializable("exercise", exercise);
        Navigation.findNavController(requireView())
            .navigate(R.id.action_customWorkoutFragment_to_exerciseDetailFragment, args);
    }
}
