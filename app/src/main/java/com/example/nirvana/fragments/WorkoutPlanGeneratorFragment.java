package com.example.nirvana.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirvana.R;
import com.example.nirvana.adapters.WorkoutAdapter;
import com.example.nirvana.models.UserProfile;
import com.example.nirvana.models.Workout;
import com.example.nirvana.models.WorkoutPlan;
import com.example.nirvana.services.WorkoutPlanGenerator;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment for generating personalized workout plans
 */
public class WorkoutPlanGeneratorFragment extends Fragment {
    private static final String TAG = "WorkoutPlanGenerator";
    
    // UI Elements
    private Spinner spinnerFitnessLevel;
    private Spinner spinnerGoal;
    private Slider sliderDaysPerWeek;
    private Slider sliderTimePerWorkout;
    private ChipGroup chipGroupEquipment;
    private Chip chipHasEquipment;
    private Chip chipNoEquipment;
    private Button btnGeneratePlan;
    private ProgressBar progressBar;
    private CardView cardPlanSummary;
    private TextView tvPlanSummary;
    private RecyclerView rvWorkouts;
    private ScrollView scrollView;
    
    // Data
    private WorkoutPlanGenerator planGenerator;
    private UserProfile userProfile;
    private WorkoutPlan generatedPlan;
    private WorkoutAdapter workoutAdapter;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        planGenerator = new WorkoutPlanGenerator(requireContext());
        
        // Create default user profile
        userProfile = new UserProfile();
        userProfile.setName("User");
        userProfile.setAge(30);
        userProfile.setGender("Male");
        userProfile.setWeight(70);
        userProfile.setHeight(170);
        userProfile.setFitnessLevel("Beginner");
        userProfile.setFitnessGoal("General Fitness");
        
        // Load user profile from Firebase if available
        loadUserProfile();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_plan_generator, container, false);
        
        // Initialize views
        spinnerFitnessLevel = view.findViewById(R.id.spinnerFitnessLevel);
        spinnerGoal = view.findViewById(R.id.spinnerGoal);
        sliderDaysPerWeek = view.findViewById(R.id.sliderDaysPerWeek);
        sliderTimePerWorkout = view.findViewById(R.id.sliderTimePerWorkout);
        chipGroupEquipment = view.findViewById(R.id.chipGroupEquipment);
        chipHasEquipment = view.findViewById(R.id.chipHasEquipment);
        chipNoEquipment = view.findViewById(R.id.chipNoEquipment);
        btnGeneratePlan = view.findViewById(R.id.btnGeneratePlan);
        progressBar = view.findViewById(R.id.progressBar);
        cardPlanSummary = view.findViewById(R.id.cardPlanSummary);
        tvPlanSummary = view.findViewById(R.id.tvPlanSummary);
        rvWorkouts = view.findViewById(R.id.rvWorkouts);
        scrollView = view.findViewById(R.id.scrollView);
        
        // Set up fitness level spinner
        ArrayAdapter<CharSequence> fitnessLevelAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.fitness_levels, android.R.layout.simple_spinner_item);
        fitnessLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFitnessLevel.setAdapter(fitnessLevelAdapter);
        
        // Set up goal spinner
        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.fitness_goals, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);
        
        // Set up recycler view
        workoutAdapter = new WorkoutAdapter();
        rvWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWorkouts.setAdapter(workoutAdapter);
        
        // Set initial UI state
        progressBar.setVisibility(View.GONE);
        cardPlanSummary.setVisibility(View.GONE);
        
        // Set up listeners
        setupListeners();
        
        return view;
    }
    
    /**
     * Set up UI event listeners
     */
    private void setupListeners() {
        // Fitness level spinner
        spinnerFitnessLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fitnessLevel = parent.getItemAtPosition(position).toString();
                userProfile.setFitnessLevel(fitnessLevel);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // Goal spinner
        spinnerGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String goal = parent.getItemAtPosition(position).toString();
                userProfile.setFitnessGoal(goal);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // Equipment chip group
        chipGroupEquipment.setOnCheckedChangeListener((group, checkedId) -> {
            // Handled in generate plan method
        });
        
        // Generate plan button
        btnGeneratePlan.setOnClickListener(v -> generateWorkoutPlan());
    }
    
    /**
     * Load user profile from Firebase
     */
    private void loadUserProfile() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("profile");
            
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Update user profile with data from Firebase
                        userProfile.setUserId(userId);
                        
                        if (snapshot.child("name").exists()) {
                            userProfile.setName(snapshot.child("name").getValue(String.class));
                        }
                        
                        if (snapshot.child("age").exists()) {
                            userProfile.setAge(snapshot.child("age").getValue(Integer.class));
                        }
                        
                        if (snapshot.child("gender").exists()) {
                            userProfile.setGender(snapshot.child("gender").getValue(String.class));
                        }
                        
                        if (snapshot.child("weight").exists()) {
                            userProfile.setWeight(snapshot.child("weight").getValue(Float.class));
                        }
                        
                        if (snapshot.child("height").exists()) {
                            userProfile.setHeight(snapshot.child("height").getValue(Float.class));
                        }
                        
                        if (snapshot.child("fitnessLevel").exists()) {
                            String fitnessLevel = snapshot.child("fitnessLevel").getValue(String.class);
                            userProfile.setFitnessLevel(fitnessLevel);
                            setSpinnerSelection(spinnerFitnessLevel, fitnessLevel);
                        }
                        
                        if (snapshot.child("fitnessGoal").exists()) {
                            String fitnessGoal = snapshot.child("fitnessGoal").getValue(String.class);
                            userProfile.setFitnessGoal(fitnessGoal);
                            setSpinnerSelection(spinnerGoal, fitnessGoal);
                        }
                        
                        Log.d(TAG, "Loaded user profile: " + userProfile.getName());
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error loading user profile", error.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Firebase", e);
        }
    }
    
    /**
     * Set spinner selection based on value
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner != null && spinner.getAdapter() != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }
    
    /**
     * Generate a personalized workout plan
     */
    private void generateWorkoutPlan() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        cardPlanSummary.setVisibility(View.GONE);
        btnGeneratePlan.setEnabled(false);
        
        // Get user preferences
        Map<String, Object> preferences = new HashMap<>();
        
        // Days per week (1-7)
        int daysPerWeek = (int) sliderDaysPerWeek.getValue();
        preferences.put("daysPerWeek", daysPerWeek);
        
        // Time per workout (min)
        int timePerWorkout = (int) sliderTimePerWorkout.getValue();
        preferences.put("timePerWorkout", timePerWorkout);
        
        // Equipment availability
        boolean hasEquipment = chipHasEquipment.isChecked();
        preferences.put("hasEquipment", hasEquipment);
        
        // Primary goal
        String primaryGoal = spinnerGoal.getSelectedItem().toString();
        preferences.put("primaryGoal", primaryGoal);
        
        // Generate plan
        planGenerator.generatePersonalizedPlan(userProfile, preferences, 
                new WorkoutPlanGenerator.PlanGenerationCallback() {
                    @Override
                    public void onPlanGenerated(WorkoutPlan plan) {
                        generatedPlan = plan;
                        requireActivity().runOnUiThread(() -> {
                            // Hide progress
                            progressBar.setVisibility(View.GONE);
                            btnGeneratePlan.setEnabled(true);
                            
                            // Show plan
                            tvPlanSummary.setText(plan.getSummary());
                            cardPlanSummary.setVisibility(View.VISIBLE);
                            
                            // Update workouts in adapter
                            workoutAdapter.setWorkouts(plan.getWorkouts().values());
                            
                            // Scroll to see results
                            scrollView.post(() -> scrollView.smoothScrollTo(0, cardPlanSummary.getTop()));
                        });
                    }
                    
                    @Override
                    public void onError(String message) {
                        requireActivity().runOnUiThread(() -> {
                            // Hide progress
                            progressBar.setVisibility(View.GONE);
                            btnGeneratePlan.setEnabled(true);
                            
                            // Show error
                            Toast.makeText(requireContext(), 
                                    "Error generating workout plan: " + message, 
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }
} 