package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nirvana.R;
import com.example.nirvana.models.Exercise;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;
import android.os.Handler;
import android.widget.Toast;
import java.util.Locale;

public class ActiveWorkoutFragment extends Fragment {
    private String category;
    private String experienceLevel;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    // Exercise and timer related variables
    private List<Exercise> exercises;
    private int currentExerciseIndex = 0;
    private CountDownTimer exerciseTimer;
    private boolean timerRunning = false;
    private long timeLeftInMillis = 0;
    private static final long REST_DURATION = 30000; // 30 seconds rest between exercises
    private boolean isRestPeriod = false;
    
    // UI elements
    private TextView txtExerciseName;
    private TextView txtExerciseDescription;
    private TextView txtTimer;
    private ImageView imgExercise;
    private MaterialButton btnNext;
    private MaterialButton btnPrevious;
    private MaterialButton btnPauseResume;
    private MaterialButton btnFinishWorkout;
    private LinearProgressIndicator progressTimer;
    private MaterialCardView cardExerciseInfo;
    private RecyclerView recyclerExercises;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category", "");
            experienceLevel = getArguments().getString("experience", "Beginner");
        }
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        exercises = new ArrayList<>();
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
        initViews(view);
        loadExercises();
        setupButtons();
    }
    
    private void initViews(View view) {
        txtExerciseName = view.findViewById(R.id.txtExerciseName);
        txtExerciseDescription = view.findViewById(R.id.txtExerciseDescription);
        txtTimer = view.findViewById(R.id.txtTimer);
        imgExercise = view.findViewById(R.id.imgExercise);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnPauseResume = view.findViewById(R.id.btnPauseResume);
        btnFinishWorkout = view.findViewById(R.id.btnFinishWorkout);
        progressTimer = view.findViewById(R.id.progressTimer);
        cardExerciseInfo = view.findViewById(R.id.cardExerciseInfo);
        recyclerExercises = view.findViewById(R.id.recyclerExercises);
        
        // Hide the RecyclerView as we're showing exercises one by one
        if (recyclerExercises != null) {
            recyclerExercises.setVisibility(View.GONE);
        }
        
        // Show the exercise card
        if (cardExerciseInfo != null) {
            cardExerciseInfo.setVisibility(View.VISIBLE);
        }
    }

    private void setupTopBar(View view) {
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton imgProfile = view.findViewById(R.id.imgProfile);
        TextView txtUsername = view.findViewById(R.id.txtUsername);

        // Set up navigation
        btnBack.setOnClickListener(v -> 
            showExitConfirmationDialog()
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
    
    private void loadExercises() {
        // This would typically involve loading exercises from a database
        // For now, we'll use the getExercisesForCategory method from WorkoutListFragment
        exercises = getExercisesForCategory();
        
        if (exercises.isEmpty()) {
            Toast.makeText(requireContext(), "No exercises found for this workout", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
            return;
        }
        
        // Start with the first exercise
        displayCurrentExercise();
    }
    
    private void setupButtons() {
        btnNext.setOnClickListener(v -> goToNextExercise());
        btnPrevious.setOnClickListener(v -> goToPreviousExercise());
        btnPauseResume.setOnClickListener(v -> toggleTimer());
        btnFinishWorkout.setOnClickListener(v -> {
            stopTimer();
            showWorkoutCompletionDialog();
        });
    }
    
    private void displayCurrentExercise() {
        if (currentExerciseIndex < 0 || currentExerciseIndex >= exercises.size()) {
            return;
        }
        
        Exercise exercise = exercises.get(currentExerciseIndex);
        
        // Update UI with exercise details
        txtExerciseName.setText(isRestPeriod ? "Rest Period" : exercise.getName());
        txtExerciseDescription.setText(isRestPeriod ? 
            "Take a break and prepare for the next exercise" : 
            exercise.getDescription());
        
        // Load exercise image
        if (isRestPeriod) {
            imgExercise.setImageResource(R.drawable.ic_rest_period);
        } else if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(exercise.getImageUrl())
                .placeholder(R.drawable.placeholder_exercise)
                .error(R.drawable.ic_exercise_default)
                .into(imgExercise);
        } else {
            imgExercise.setImageResource(R.drawable.ic_exercise_default);
        }
        
        // Update button states
        btnPrevious.setEnabled(currentExerciseIndex > 0);
        btnNext.setEnabled(currentExerciseIndex < exercises.size() - 1 || isRestPeriod);
        
        // Start the timer for this exercise or rest period
        startTimer(isRestPeriod ? REST_DURATION : exercise.getDurationSeconds() * 1000L);
        
        // Update the UI to show current exercise number out of total
        TextView txtProgress = requireView().findViewById(R.id.txtProgress);
        if (txtProgress != null) {
            txtProgress.setText(String.format("Exercise %d/%d", 
                isRestPeriod ? currentExerciseIndex : currentExerciseIndex + 1, 
                exercises.size()));
        }
    }
    
    private void startTimer(long duration) {
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        
        timeLeftInMillis = duration;
        
        // Update progress max value
        if (progressTimer != null) {
            progressTimer.setMax((int) (duration / 1000));
        }
        
        exerciseTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerUI();
            }
            
            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateTimerUI();
                
                // Handle timer finish based on current state
                if (isRestPeriod) {
                    // If rest period is over, move to the next exercise
                    isRestPeriod = false;
                    displayCurrentExercise();
                } else {
                    // If exercise is over, check if we should show rest period
                    if (currentExerciseIndex < exercises.size() - 1) {
                        // Start rest period before next exercise
                        isRestPeriod = true;
                        displayCurrentExercise();
                    } else {
                        // This was the last exercise
                        showWorkoutCompletionDialog();
                    }
                }
            }
        }.start();
        
        timerRunning = true;
        updateTimerButtonUI();
    }
    
    private void stopTimer() {
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            timerRunning = false;
            updateTimerButtonUI();
        }
    }
    
    private void toggleTimer() {
        if (timerRunning) {
            stopTimer();
        } else {
            startTimer(timeLeftInMillis);
        }
    }
    
    private void updateTimerUI() {
        if (txtTimer != null) {
            int seconds = (int) (timeLeftInMillis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            
            txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        }
        
        if (progressTimer != null) {
            progressTimer.setProgress((int) (timeLeftInMillis / 1000));
        }
    }
    
    private void updateTimerButtonUI() {
        if (btnPauseResume != null) {
            btnPauseResume.setText(timerRunning ? "Pause" : "Resume");
            btnPauseResume.setIcon(getResources().getDrawable(
                timerRunning ? R.drawable.ic_pause : R.drawable.ic_play, null));
        }
    }
    
    private void goToNextExercise() {
        stopTimer();
        
        if (isRestPeriod) {
            // If in rest period, go to the next exercise
            isRestPeriod = false;
            displayCurrentExercise();
        } else if (currentExerciseIndex < exercises.size() - 1) {
            // Move to next exercise with rest period in between
            currentExerciseIndex++;
            isRestPeriod = false;
            displayCurrentExercise();
        }
    }
    
    private void goToPreviousExercise() {
        stopTimer();
        
        if (isRestPeriod) {
            // If in rest period, go back to the previous exercise
            isRestPeriod = false;
            displayCurrentExercise();
        } else if (currentExerciseIndex > 0) {
            // Move to previous exercise
            currentExerciseIndex--;
            isRestPeriod = false;
            displayCurrentExercise();
        }
    }
    
    private void showExitConfirmationDialog() {
        stopTimer();
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exit Workout")
            .setMessage("Are you sure you want to exit? Your progress will not be saved.")
            .setPositiveButton("Exit", (dialog, which) -> {
                Navigation.findNavController(requireView()).navigateUp();
            })
            .setNegativeButton("Continue Workout", (dialog, which) -> {
                // Resume timer
                if (timeLeftInMillis > 0) {
                    startTimer(timeLeftInMillis);
                }
            })
            .show();
    }
    
    private void showWorkoutCompletionDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Workout Complete!")
            .setMessage("Congratulations! You've completed your workout.")
            .setPositiveButton("Finish", (dialog, which) -> {
                // TODO: Save workout data to user history
                Navigation.findNavController(requireView()).navigateUp();
            })
            .setCancelable(false)
            .show();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Pause timer when the fragment is paused
        if (timerRunning) {
            stopTimer();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up timer resources
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
    }
    
    // Helper method to get exercises for the current category
    // In a real app, this would likely be fetched from a database
    private List<Exercise> getExercisesForCategory() {
        List<Exercise> exerciseList = new ArrayList<>();
        String baseImageUrl = "https://firebasestorage.googleapis.com/v0/b/nirvana-fitness-app.appspot.com/o/exercises%2F";
        
        if (category.equalsIgnoreCase("Lower Body") || category.equalsIgnoreCase("Legs")) {
            exerciseList.add(new Exercise("1", "Squats", 
                "Stand with feet shoulder-width apart, lower your body by bending your knees, then return to starting position.",
                "Legs", experienceLevel, 45, 
                baseImageUrl + "squats.jpg?alt=media",
                "https://www.youtube.com/watch?v=rMvwVtlqjTE"));
                
            exerciseList.add(new Exercise("2", "Lunges", 
                "Step forward with one foot, lower your body until both knees are bent at 90-degree angles, then return to starting position.",
                "Legs", experienceLevel, 30,
                baseImageUrl + "lunges.jpg?alt=media",
                "https://www.youtube.com/watch?v=QE_hU8XX48I"));
                
            exerciseList.add(new Exercise("3", "Glute Bridges", 
                "Lie on your back with knees bent, feet flat on the floor. Raise your hips, creating a straight line from shoulders to knees.",
                "Legs", experienceLevel, 30,
                baseImageUrl + "glute_bridges.jpg?alt=media",
                "https://www.youtube.com/watch?v=wPM8icPu6H8"));
                
            exerciseList.add(new Exercise("4", "Calf Raises", 
                "Stand with feet shoulder-width apart, raise your heels until you're standing on your toes, then lower back down.",
                "Legs", experienceLevel, 25,
                baseImageUrl + "calf_raises.jpg?alt=media",
                "https://www.youtube.com/watch?v=gwLzBJYoWlI"));
        } else if (category.equalsIgnoreCase("Upper Body") || category.equalsIgnoreCase("Chest") || category.equalsIgnoreCase("Arms")) {
            exerciseList.add(new Exercise("5", "Push-Ups", 
                "Start in a plank position, lower your body until your chest nearly touches the floor, then push back up.",
                "Chest", experienceLevel, 30,
                baseImageUrl + "pushups.jpg?alt=media",
                "https://www.youtube.com/watch?v=IODxDxX7oi4"));
                
            exerciseList.add(new Exercise("6", "Tricep Dips", 
                "Sit on the edge of a chair, place hands next to hips, slide off the chair, bend elbows to lower body, then push back up.",
                "Arms", experienceLevel, 25,
                baseImageUrl + "tricep_dips.jpg?alt=media",
                "https://www.youtube.com/watch?v=6kALZikXxLc"));
                
            exerciseList.add(new Exercise("7", "Shoulder Press", 
                "Hold weights at shoulder height, palms forward. Press weights upward until arms are extended, then lower back down.",
                "Shoulders", experienceLevel, 30,
                baseImageUrl + "shoulder_press.jpg?alt=media",
                "https://www.youtube.com/watch?v=qEwKCR5JCog"));
                
            exerciseList.add(new Exercise("8", "Bicep Curls", 
                "Hold weights with arms extended, palms forward. Bend elbows to bring weights toward shoulders, then lower back down.",
                "Arms", experienceLevel, 25,
                baseImageUrl + "bicep_curls.jpg?alt=media",
                "https://www.youtube.com/watch?v=ykJmrZ5v0Oo"));
        } else if (category.equalsIgnoreCase("Core") || category.equalsIgnoreCase("Abs")) {
            exerciseList.add(new Exercise("9", "Crunches", 
                "Lie on your back with knees bent, feet flat on the floor. Lift your shoulders off the floor, then lower back down.",
                "Core", experienceLevel, 30,
                baseImageUrl + "crunches.jpg?alt=media",
                "https://www.youtube.com/watch?v=Xyd_fa5zoEU"));
                
            exerciseList.add(new Exercise("10", "Planks", 
                "Start in a push-up position, but with your weight on your forearms. Hold the position, keeping your body in a straight line.",
                "Core", experienceLevel, 30,
                baseImageUrl + "planks.jpg?alt=media",
                "https://www.youtube.com/watch?v=pSHjTRCQxIw"));
                
            exerciseList.add(new Exercise("11", "Russian Twists", 
                "Sit on the floor with knees bent, feet lifted. Twist your torso from side to side, moving your hands across your body.",
                "Core", experienceLevel, 25,
                baseImageUrl + "russian_twists.jpg?alt=media",
                "https://www.youtube.com/watch?v=wkD8rjkodUI"));
                
            exerciseList.add(new Exercise("12", "Leg Raises", 
                "Lie on your back with hands under your lower back for support. Raise your legs until perpendicular to the floor, then lower back down.",
                "Core", experienceLevel, 25,
                baseImageUrl + "leg_raises.jpg?alt=media",
                "https://www.youtube.com/watch?v=JB2oyawG9KI"));
        } else {
            // Default exercises for any other category
            exerciseList.add(new Exercise("13", "Jumping Jacks", 
                "Stand with feet together, arms at sides. Jump while spreading legs and raising arms overhead, then return to starting position.",
                "Full Body", experienceLevel, 30,
                baseImageUrl + "jumping_jacks.jpg?alt=media",
                "https://www.youtube.com/watch?v=c4DAnQ6DtF8"));
                
            exerciseList.add(new Exercise("14", "Mountain Climbers", 
                "Start in a plank position. Alternate bringing each knee toward your chest in a running motion.",
                "Full Body", experienceLevel, 30,
                baseImageUrl + "mountain_climbers.jpg?alt=media",
                "https://www.youtube.com/watch?v=nmwgirgXLYM"));
                
            exerciseList.add(new Exercise("15", "Burpees", 
                "Stand, then move into a squat position, kick feet back to a plank, do a push-up, return to squat, and jump up.",
                "Full Body", experienceLevel, 30,
                baseImageUrl + "burpees.jpg?alt=media",
                "https://www.youtube.com/watch?v=dZgVxmf6jkA"));
                
            exerciseList.add(new Exercise("16", "High Knees", 
                "Run in place, lifting knees toward chest. Keep a quick pace, pumping arms as you run.",
                "Full Body", experienceLevel, 25,
                baseImageUrl + "high_knees.jpg?alt=media",
                "https://www.youtube.com/watch?v=tx5rgpDAJRI"));
        }
        
        // Adjust duration based on experience level
        for (Exercise exercise : exerciseList) {
            if (experienceLevel.equalsIgnoreCase("Beginner")) {
                exercise.setDurationSeconds(30); // 30 seconds for beginners
            } else if (experienceLevel.equalsIgnoreCase("Intermediate")) {
                exercise.setDurationSeconds(45); // 45 seconds for intermediate
            } else {
                exercise.setDurationSeconds(60); // 60 seconds for advanced
            }
        }
        
        return exerciseList;
    }
} 