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
import android.widget.TextView;
import android.widget.Toast;

public class CustomWorkoutFragment extends Fragment implements ExerciseAdapter.OnExerciseClickListener {
    private static final String TAG = "CustomWorkoutFragment";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ExerciseAdapter adapter;
    private String selectedDay = "Monday";
    private View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_custom_workout, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            setupRecyclerView(view);
            setupWeekdayTabs(view);
            setupFabButton(view);
            setupFirebase();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            Toast.makeText(requireContext(), "Error setting up custom workout", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView(View view) {
        try {
            RecyclerView recyclerView = view.findViewById(R.id.exerciseList);
            if (recyclerView == null) {
                Log.e(TAG, "RecyclerView not found in layout");
                return;
            }
            
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new ExerciseAdapter(this);
            recyclerView.setAdapter(adapter);
            
            // Initialize with empty list
            adapter.setExercises(new ArrayList<>());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupWeekdayTabs(View view) {
        try {
            TabLayout tabLayout = view.findViewById(R.id.weekdayTabs);
            if (tabLayout == null) {
                Log.e(TAG, "TabLayout not found in layout");
                return;
            }
            
            String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            
            for (String day : weekdays) {
                tabLayout.addTab(tabLayout.newTab().setText(day));
            }

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab != null && tab.getText() != null) {
                        selectedDay = tab.getText().toString();
                        loadWorkoutForDay(selectedDay);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up weekday tabs", e);
        }
    }

    private void setupFabButton(View view) {
        try {
            View fabButton = view.findViewById(R.id.fabAddExercise);
            if (fabButton != null) {
                fabButton.setOnClickListener(v -> {
                    showExerciseSelectionDialog();
                });
            } else {
                Log.e(TAG, "FAB button not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up FAB button", e);
        }
    }

    private void showExerciseSelectionDialog() {
        try {
            ExerciseSelectionDialog dialog = ExerciseSelectionDialog.newInstance();
            dialog.setOnExerciseSelectedListener(exercise -> {
                // Handle the selected exercise
                addExerciseToWorkout(exercise);
            });
            dialog.show(getChildFragmentManager(), "exercise_selection");
        } catch (Exception e) {
            Log.e(TAG, "Error showing exercise selection dialog", e);
            Toast.makeText(requireContext(), "Error showing exercise selection", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addExerciseToWorkout(Exercise exercise) {
        if (exercise == null) {
            Log.e(TAG, "Cannot add null exercise");
            return;
        }
        
        if (auth == null || auth.getCurrentUser() == null) {
            Log.w(TAG, "No user logged in");
            Toast.makeText(requireContext(), "Please log in to add exercises", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String userId = auth.getCurrentUser().getUid();
            Log.d(TAG, "Adding exercise to day: " + selectedDay + " for user: " + userId);
            
            db.collection("users")
                .document(userId)
                .collection("workouts")
                .document(selectedDay.toLowerCase())
                .collection("exercises")
                .add(exercise)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Exercise added with ID: " + documentReference.getId());
                    loadWorkoutForDay(selectedDay);
                    Toast.makeText(requireContext(), "Exercise added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding exercise", e);
                    Toast.makeText(requireContext(), "Failed to add exercise", Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Exception adding exercise", e);
            Toast.makeText(requireContext(), "Error adding exercise", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFirebase() {
        try {
            loadWorkoutForDay(selectedDay);
        } catch (Exception e) {
            Log.e(TAG, "Error in setupFirebase", e);
        }
    }

    private void loadWorkoutForDay(String day) {
        if (day == null || day.isEmpty()) {
            Log.e(TAG, "Invalid day parameter");
            return;
        }
        
        if (auth == null || auth.getCurrentUser() == null) {
            Log.w(TAG, "No user logged in");
            adapter.setExercises(new ArrayList<>());
            showEmptyState(day);
            return;
        }
        
        try {
            String userId = auth.getCurrentUser().getUid();
            Log.d(TAG, "Loading exercises for day: " + day + " and user: " + userId);
            
            // Update the TextView to show the selected day
            if (rootView != null) {
                TextView txtSelectedDay = rootView.findViewById(R.id.txtSelectedDay);
                if (txtSelectedDay != null) {
                    txtSelectedDay.setText("Selected Day: " + day);
                }
            }
            
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
                    Log.d(TAG, "Loaded " + exercises.size() + " exercises");
                    
                    // Show empty state message if no exercises
                    if (exercises.isEmpty()) {
                        showEmptyState(day);
                    } else {
                        hideEmptyState();
                    }
                    
                    adapter.setExercises(exercises);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading exercises", e);
                    showEmptyState(day);
                    adapter.setExercises(new ArrayList<>());
                    Toast.makeText(requireContext(), "Error loading exercises", Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Exception loading exercises", e);
            showEmptyState(day);
            adapter.setExercises(new ArrayList<>());
            Toast.makeText(requireContext(), "Error loading exercises", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showEmptyState(String day) {
        if (rootView == null) return;
        
        try {
            TextView emptyStateText = rootView.findViewById(R.id.emptyStateText);
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.VISIBLE);
                emptyStateText.setText("No exercises added for " + day + ".\nTap the + button to add exercises.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing empty state", e);
        }
    }
    
    private void hideEmptyState() {
        if (rootView == null) return;
        
        try {
            TextView emptyStateText = rootView.findViewById(R.id.emptyStateText);
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding empty state", e);
        }
    }

    @Override
    public void onExerciseClick(Exercise exercise) {
        if (exercise == null) {
            Log.e(TAG, "Cannot navigate to null exercise");
            return;
        }
        
        try {
            Bundle args = new Bundle();
            args.putSerializable("exercise", exercise);
            Navigation.findNavController(requireView())
                .navigate(R.id.action_customWorkoutFragment_to_exerciseDetailFragment, args);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to exercise detail", e);
            Toast.makeText(requireContext(), "Error viewing exercise details", Toast.LENGTH_SHORT).show();
        }
    }
}
