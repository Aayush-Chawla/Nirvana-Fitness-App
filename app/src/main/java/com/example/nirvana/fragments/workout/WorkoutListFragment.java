package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.ExerciseAdapter;
import com.example.nirvana.models.Exercise;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class WorkoutListFragment extends Fragment {
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_EXPERIENCE = "experience";

    private MaterialToolbar toolbar;
    private TextView txtExperienceLevel;
    private RecyclerView recyclerExercises;
    private MaterialButton btnStartWorkout;
    private ExerciseAdapter exerciseAdapter;
    private FirebaseFirestore db;
    private String category;
    private String experienceLevel;

    public static WorkoutListFragment newInstance(String category, String experience) {
        WorkoutListFragment fragment = new WorkoutListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        args.putString(ARG_EXPERIENCE, experience);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
            experienceLevel = getArguments().getString(ARG_EXPERIENCE);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.util.Log.d("WorkoutListFragment", "onViewCreated started");
        
        setupViews(view);
        setupToolbar();
        setupClickListeners();
        loadExercises();
    }

    private void verifyRecyclerViewSetup() {
        if (recyclerExercises != null && recyclerExercises.getAdapter() != null) {
            android.util.Log.d("WorkoutListFragment", "RecyclerView setup verified - adapter attached");
            int itemCount = recyclerExercises.getAdapter().getItemCount();
            android.util.Log.d("WorkoutListFragment", "Current adapter item count: " + itemCount);
        } else {
            android.util.Log.e("WorkoutListFragment", "RecyclerView setup failed - adapter missing");
        }
    }

    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        txtExperienceLevel = view.findViewById(R.id.txtExperienceLevel);
        recyclerExercises = view.findViewById(R.id.recyclerExercises);
        btnStartWorkout = view.findViewById(R.id.btnStartWorkout);

        // Setup RecyclerView first
        recyclerExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerExercises.setItemAnimator(new DefaultItemAnimator());
        recyclerExercises.setHasFixedSize(true);

        // Initialize adapter
        exerciseAdapter = new ExerciseAdapter(exercise -> {
            ExerciseDetailsDialog dialog = ExerciseDetailsDialog.newInstance(exercise);
            dialog.show(getChildFragmentManager(), "exercise_details");
        });

        // Set adapter to RecyclerView
        recyclerExercises.setAdapter(exerciseAdapter);
        android.util.Log.d("WorkoutListFragment", "Adapter set to RecyclerView");

        // Add divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recyclerExercises.addItemDecoration(dividerItemDecoration);

        // Apply animations after setup
        recyclerExercises.setLayoutAnimation(
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));
        btnStartWorkout.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up));
    }

    private void setupToolbar() {
        String username = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "";
        
        toolbar.setTitle(category + " Workout");
        toolbar.setSubtitle(username);
        toolbar.setNavigationOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigateUp());
        
        // Set toolbar colors
        toolbar.setBackgroundColor(requireContext().getColor(R.color.colorPrimary));
        toolbar.setTitleTextColor(requireContext().getColor(R.color.white));
        toolbar.setSubtitleTextColor(requireContext().getColor(R.color.white));

        if (experienceLevel != null && !experienceLevel.isEmpty()) {
            txtExperienceLevel.setVisibility(View.VISIBLE);
            txtExperienceLevel.setText("Experience Level: " + experienceLevel);
        }
    }

    private void loadExercises() {
        if (!isAdded()) {
            android.util.Log.e("WorkoutListFragment", "Fragment not attached to activity");
            return;
        }

        android.util.Log.d("WorkoutListFragment", "Loading exercises for category: " + category);
        List<Exercise> exercises = new ArrayList<>();
        String baseImageUrl = "https://firebasestorage.googleapis.com/v0/b/nirvana-fitness-app.appspot.com/o/exercises%2F";
        
        // Category null check - default to Full Body if category is null
        if (category == null) {
            category = "Full Body";
            android.util.Log.d("WorkoutListFragment", "Category was null, defaulting to: " + category);
        }
        
        // Chest exercises
        if (category.equalsIgnoreCase("Chest")) {
            exercises.add(new Exercise("1", "Bench Press", 
                "The bench press is a compound exercise that primarily targets the chest muscles (pectoralis major and minor). " +
                "It also engages the shoulders (anterior deltoids) and triceps. Proper form is crucial for effectiveness and safety.",
                "Chest", experienceLevel, 15, 
                baseImageUrl + "bench_press.jpg?alt=media",
                "https://www.youtube.com/watch?v=rT7DgCr-3pg"));
                
            exercises.add(new Exercise("2", "Incline Dumbbell Press", 
                "The incline dumbbell press targets the upper portion of the chest muscles. " +
                "Using dumbbells allows for a greater range of motion and helps correct muscle imbalances.",
                "Chest", experienceLevel, 12,
                baseImageUrl + "incline_press.jpg?alt=media",
                "https://www.youtube.com/watch?v=8iPEnn-ltC8"));
                
            exercises.add(new Exercise("3", "Push-Ups", 
                "Push-ups are a fundamental bodyweight exercise that works the chest, shoulders, and triceps. " +
                "They also engage the core muscles for stability. Multiple variations exist for different difficulty levels.",
                "Chest", experienceLevel, 10,
                baseImageUrl + "pushups.jpg?alt=media",
                "https://www.youtube.com/watch?v=IODxDxX7oi4"));
                
            exercises.add(new Exercise("4", "Dumbbell Flyes", 
                "Dumbbell flyes isolate the chest muscles through a wide arc movement. " +
                "This exercise helps develop chest width and improves muscle definition.",
                "Chest", experienceLevel, 10,
                baseImageUrl + "flyes.jpg?alt=media",
                "https://www.youtube.com/watch?v=eozdVDA78K0"));
        }
        
        // Back exercises
        else if (category.equalsIgnoreCase("Back")) {
            exercises.add(new Exercise("5", "Pull-Ups", 
                "Pull-ups are a challenging bodyweight exercise that targets the entire back, particularly the latissimus dorsi. " +
                "They also work the biceps and improve overall upper body strength.",
                "Back", experienceLevel, 12,
                baseImageUrl + "pullups.jpg?alt=media",
                "https://www.youtube.com/watch?v=eGo4IYlbE5g"));
                
            exercises.add(new Exercise("6", "Barbell Rows", 
                "Barbell rows are a compound movement that builds back thickness and strength. " +
                "They target multiple back muscles while also engaging the core and biceps.",
                "Back", experienceLevel, 15,
                baseImageUrl + "rows.jpg?alt=media",
                "https://www.youtube.com/watch?v=9efgcAjQe7E"));
                
            exercises.add(new Exercise("7", "Lat Pulldowns", 
                "Lat pulldowns are a machine exercise that targets the latissimus dorsi muscles. " +
                "They're excellent for developing back width and are easier to learn than pull-ups.",
                "Back", experienceLevel, 12,
                baseImageUrl + "pulldowns.jpg?alt=media",
                "https://www.youtube.com/watch?v=CAwf7n6Luuc"));
                
            exercises.add(new Exercise("8", "Deadlifts", 
                "Deadlifts are a fundamental compound exercise that works the entire posterior chain. " +
                "They target the back, glutes, hamstrings, and core while building overall strength.",
                "Back", experienceLevel, 20,
                baseImageUrl + "deadlifts.jpg?alt=media",
                "https://www.youtube.com/watch?v=op9kVnSso6Q"));
        }
        
        // Legs exercises
        else if (category.equalsIgnoreCase("Legs")) {
            exercises.add(new Exercise("9", "Squats", 
                "A fundamental lower body exercise targeting quadriceps, hamstrings, and glutes. " +
                "Essential for building leg strength and overall stability.",
                "Legs", experienceLevel, 20,
                baseImageUrl + "squats.jpg?alt=media",
                "https://www.youtube.com/watch?v=ultWZbUMPL8"));
                
            exercises.add(new Exercise("10", "Romanian Deadlifts", 
                "A posterior chain exercise focusing on hamstrings and glutes. " +
                "Excellent for developing hip hinge movement and lower body strength.",
                "Legs", experienceLevel, 15,
                baseImageUrl + "rdl.jpg?alt=media",
                "https://www.youtube.com/watch?v=JCXUYuzwNrM"));
                
            exercises.add(new Exercise("11", "Leg Press", 
                "A machine-based compound leg exercise targeting quadriceps, hamstrings, and glutes. " +
                "Good alternative to squats for beginners or variation in training.",
                "Legs", experienceLevel, 15,
                baseImageUrl + "legpress.jpg?alt=media",
                "https://www.youtube.com/watch?v=IZxyjW7MPJQ"));
                
            exercises.add(new Exercise("12", "Walking Lunges", 
                "A dynamic leg exercise that improves balance, coordination, and unilateral strength. " +
                "Works each leg independently for balanced development.",
                "Legs", experienceLevel, 15,
                baseImageUrl + "lunges.jpg?alt=media",
                "https://www.youtube.com/watch?v=L8fvypPrzzs"));
        }
        
        // Shoulders exercises
        else if (category.equalsIgnoreCase("Shoulders")) {
            exercises.add(new Exercise("13", "Military Press", 
                "The military press is a compound exercise that targets all three heads of the deltoid muscles. " +
                "It also engages the triceps and core muscles for stability.",
                "Shoulders", experienceLevel, 15,
                "https://cdn.pixabay.com/photo/2021/01/04/06/25/man-5886571_960_720.jpg",
                "https://www.youtube.com/watch?v=2yjwXTZQDDI"));
                
            exercises.add(new Exercise("14", "Lateral Raises", 
                "Lateral raises isolate the middle deltoid muscles to build shoulder width. " +
                "Use light to moderate weight and focus on proper form.",
                "Shoulders", experienceLevel, 12,
                "https://cdn.pixabay.com/photo/2021/01/04/06/25/man-5886571_960_720.jpg",
                "https://www.youtube.com/watch?v=3VcKaXpzqRo"));
                
            exercises.add(new Exercise("15", "Face Pulls", 
                "Face pulls target the rear deltoids and rotator cuff muscles. " +
                "This exercise improves shoulder health and posture.",
                "Shoulders", experienceLevel, 12,
                "https://cdn.pixabay.com/photo/2021/01/04/06/25/man-5886571_960_720.jpg",
                "https://www.youtube.com/watch?v=eIq5CB9JfKE"));
        }
        
        // Arms exercises
        else if (category.equalsIgnoreCase("Arms")) {
            exercises.add(new Exercise("16", "Barbell Curls", 
                "Barbell curls are a classic exercise for developing the biceps. " +
                "Focus on a full range of motion and controlled movement.",
                "Arms", experienceLevel, 12,
                "https://cdn.pixabay.com/photo/2016/11/22/22/24/adult-1850925_960_720.jpg",
                "https://www.youtube.com/watch?v=kwG2ipFRgfo"));
                
            exercises.add(new Exercise("17", "Tricep Pushdowns", 
                "Tricep pushdowns isolate the triceps muscles using a cable machine. " +
                "This exercise is effective for building arm definition.",
                "Arms", experienceLevel, 12,
                "https://cdn.pixabay.com/photo/2016/11/22/22/24/adult-1850925_960_720.jpg",
                "https://www.youtube.com/watch?v=2-LAMcpzODU"));
                
            exercises.add(new Exercise("18", "Hammer Curls", 
                "Hammer curls target the brachialis and brachioradialis muscles along with the biceps. " +
                "They help develop forearm strength and thickness.",
                "Arms", experienceLevel, 12,
                "https://cdn.pixabay.com/photo/2016/11/22/22/24/adult-1850925_960_720.jpg",
                "https://www.youtube.com/watch?v=zC3nLlEvin4"));
        }
        
        // Core exercises
        else if (category.equalsIgnoreCase("Core")) {
            exercises.add(new Exercise("19", "Planks", 
                "Planks are an isometric core exercise that builds endurance and stability. " +
                "They engage multiple muscle groups including the abs, back, and shoulders.",
                "Core", experienceLevel, 10,
                "https://cdn.pixabay.com/photo/2017/08/07/14/02/people-2604149_960_720.jpg",
                "https://www.youtube.com/watch?v=ASdvN_XEl_c"));
                
            exercises.add(new Exercise("20", "Russian Twists", 
                "Russian twists target the obliques through a rotational movement. " +
                "This exercise improves core strength and rotational power.",
                "Core", experienceLevel, 10,
                "https://cdn.pixabay.com/photo/2017/08/07/14/02/people-2604149_960_720.jpg",
                "https://www.youtube.com/watch?v=wkD8rjkodUI"));
                
            exercises.add(new Exercise("21", "Leg Raises", 
                "Leg raises target the lower abdominal muscles. " +
                "Focus on controlled movement to maximize effectiveness.",
                "Core", experienceLevel, 10,
                "https://cdn.pixabay.com/photo/2017/08/07/14/02/people-2604149_960_720.jpg",
                "https://www.youtube.com/watch?v=l4kQd9eWclE"));
        }
        
        // Cardio exercises
        else if (category.equalsIgnoreCase("Cardio")) {
            exercises.add(new Exercise("22", "High Knees", 
                "High knees are a high-intensity cardio exercise that elevates heart rate quickly. " +
                "This exercise also strengthens the hip flexors and improves coordination.",
                "Cardio", experienceLevel, 10,
                "https://cdn.pixabay.com/photo/2014/11/17/13/17/crossfit-534615_960_720.jpg",
                "https://www.youtube.com/watch?v=oDdkytliOqE"));
                
            exercises.add(new Exercise("23", "Burpees", 
                "Burpees are a full-body exercise that combines a push-up, jump, and squat. " +
                "They're excellent for cardiovascular endurance and total body conditioning.",
                "Cardio", experienceLevel, 15,
                "https://cdn.pixabay.com/photo/2014/11/17/13/17/crossfit-534615_960_720.jpg",
                "https://www.youtube.com/watch?v=dZgVxmf6jkA"));
                
            exercises.add(new Exercise("24", "Mountain Climbers", 
                "Mountain climbers are a dynamic exercise that targets the core while providing cardiovascular benefits. " +
                "They improve coordination, agility, and overall conditioning.",
                "Cardio", experienceLevel, 12,
                "https://cdn.pixabay.com/photo/2014/11/17/13/17/crossfit-534615_960_720.jpg",
                "https://www.youtube.com/watch?v=nmwgirgXLYM"));
        }
        
        // Full Body exercises - default category
        else {
            // Default for "Full Body" or any unrecognized category
            exercises.add(new Exercise("25", "Burpees", 
                "A comprehensive full-body exercise that combines a squat, push-up, and jump. " +
                "Great for cardio, strength, and endurance in one movement.",
                "Full Body", experienceLevel, 15,
                "https://cdn.pixabay.com/photo/2014/11/17/13/17/crossfit-534615_960_720.jpg",
                "https://www.youtube.com/watch?v=dZgVxmf6jkA"));
                
            exercises.add(new Exercise("26", "Kettlebell Swings", 
                "A dynamic exercise that works the posterior chain, core, and shoulders. " +
                "Excellent for power development and cardiovascular fitness.",
                "Full Body", experienceLevel, 12,
                "https://cdn.pixabay.com/photo/2018/01/01/01/15/fitness-3053567_960_720.jpg",
                "https://www.youtube.com/watch?v=YSxHifyI6s8"));
                
            exercises.add(new Exercise("27", "Thrusters", 
                "A compound movement combining a front squat and overhead press. " +
                "Effectively works multiple muscle groups and elevates heart rate.",
                "Full Body", experienceLevel, 15,
                "https://cdn.pixabay.com/photo/2015/07/02/10/23/training-828741_960_720.jpg",
                "https://www.youtube.com/watch?v=L219ltL15zk"));
                
            exercises.add(new Exercise("28", "Turkish Get-Up", 
                "A complex movement that improves stability, coordination, and strength. " +
                "Works practically every muscle in the body through multiple planes of motion.",
                "Full Body", experienceLevel, 10,
                "https://cdn.pixabay.com/photo/2017/08/07/14/02/man-2604149_960_720.jpg",
                "https://www.youtube.com/watch?v=jFK8FOGnXLw"));
                
            exercises.add(new Exercise("29", "Battle Ropes", 
                "A high-intensity exercise that builds upper body and core strength. " +
                "Great for conditioning and power endurance.",
                "Full Body", experienceLevel, 12,
                "https://cdn.pixabay.com/photo/2018/01/01/01/15/fitness-3053567_960_720.jpg",
                "https://www.youtube.com/watch?v=r2-JXSa8rIM"));
        }
        
        // Update adapter with exercises
        if (!exercises.isEmpty()) {
            android.util.Log.d("WorkoutListFragment", "Submitting " + exercises.size() + " exercises to adapter");
            exerciseAdapter.submitList(exercises);
            verifyRecyclerViewSetup();
        } else {
            android.util.Log.e("WorkoutListFragment", "No exercises loaded for category: " + category);
            // Submit an empty list to avoid null pointer exceptions
            exerciseAdapter.submitList(new ArrayList<>());
        }
    }

    private void setupClickListeners() {
        btnStartWorkout.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("category", category);
            args.putString("experience", experienceLevel);
            Navigation.findNavController(requireView())
                .navigate(R.id.action_workoutListFragment_to_activeWorkoutFragment, args);
        });
    }
} 