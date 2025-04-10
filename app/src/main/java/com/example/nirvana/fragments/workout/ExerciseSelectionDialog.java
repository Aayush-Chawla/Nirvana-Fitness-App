package com.example.nirvana.fragments.workout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.ExerciseAdapter;
import com.example.nirvana.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseSelectionDialog extends DialogFragment {

    private ExerciseAdapter adapter;
    private OnExerciseSelectedListener listener;
    
    public interface OnExerciseSelectedListener {
        void onExerciseSelected(Exercise exercise);
    }
    
    /**
     * Create a new instance of ExerciseSelectionDialog
     */
    public static ExerciseSelectionDialog newInstance() {
        return new ExerciseSelectionDialog();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_exercise_selection, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView(view);
        
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
    }
    
    public void setOnExerciseSelectedListener(OnExerciseSelectedListener listener) {
        this.listener = listener;
    }

    private void setupRecyclerView(View view) {
        try {
            RecyclerView recyclerView = view.findViewById(R.id.exerciseSelectionList);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            
            // Create a separate listener instead of using 'this'
            ExerciseAdapter.OnExerciseClickListener clickListener = new ExerciseAdapter.OnExerciseClickListener() {
                @Override
                public void onExerciseClick(Exercise exercise) {
                    if (listener != null) {
                        listener.onExerciseSelected(exercise);
                        dismiss();
                    }
                }
            };
            
            adapter = new ExerciseAdapter(clickListener);
            recyclerView.setAdapter(adapter);
            
            List<Exercise> exercises = getDummyExercises();
            Log.d("ExerciseSelection", "Loading " + exercises.size() + " exercises");
            adapter.setExercises(exercises);
        } catch (Exception e) {
            Log.e("ExerciseSelection", "Error setting up RecyclerView", e);
        }
    }

    private List<Exercise> getDummyExercises() {
        List<Exercise> exercises = new ArrayList<>();
        
        // Chest exercises
        exercises.add(new Exercise("c1", "Bench Press", 
            "The bench press is a compound exercise that primarily targets the chest muscles. " +
            "It also engages the shoulders and triceps.", 
            "chest", "intermediate", 45, "", ""));
            
        exercises.add(new Exercise("c2", "Push-Ups", 
            "A fundamental bodyweight exercise that works the chest, shoulders, and triceps. " +
            "Great for beginners and can be modified for different difficulty levels.", 
            "chest", "beginner", 30, "", ""));
        
        // Add more exercises as needed
        
        return exercises;
    }
} 