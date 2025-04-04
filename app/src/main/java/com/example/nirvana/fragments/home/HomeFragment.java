package com.example.nirvana.fragments.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.data.models.Blog;
import com.example.nirvana.ui.adapters.BlogAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements BlogAdapter.OnBlogClickListener {

    private TextView txtWelcome;
    private TextView txtStepCount;
    private TextView txtActiveMinutes;
    private TextView txtWorkoutsCompleted;
    private TextView txtCaloriesBurned;
    private TextView txtCurrentHeartRate;
    private TextView txtRewardPoints;
    private TextView txtNextReward;
    private ProgressBar progressCalories;
    private ProgressBar progressRewards;
    private LineChart chartHeartRate;
    private RecyclerView recyclerBlogs;
    private BlogAdapter blogAdapter;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupClickListeners();
        setupHeartRateChart();
        setupBlogRecyclerView();
        loadDashboardData();
    }

    private void initializeViews(View view) {
        txtWelcome = view.findViewById(R.id.txtWelcome);
        txtStepCount = view.findViewById(R.id.txtStepCount);
        txtActiveMinutes = view.findViewById(R.id.txtActiveMinutes);
        txtWorkoutsCompleted = view.findViewById(R.id.txtWorkoutsCompleted);
        txtCaloriesBurned = view.findViewById(R.id.txtCaloriesBurned);
        txtCurrentHeartRate = view.findViewById(R.id.txtCurrentHeartRate);
        txtRewardPoints = view.findViewById(R.id.txtRewardPoints);
        txtNextReward = view.findViewById(R.id.txtNextReward);
        progressCalories = view.findViewById(R.id.progressCalories);
        progressRewards = view.findViewById(R.id.progressRewards);
        chartHeartRate = view.findViewById(R.id.chartHeartRate);
        recyclerBlogs = view.findViewById(R.id.recyclerBlogs);
    }

    private void setupClickListeners() {
        View view = requireView();
        // Calories Card Click
        view.findViewById(R.id.cardCalories).setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_caloriesDetailFragment)
        );

        // Heart Rate Card Click
        view.findViewById(R.id.cardHeartRate).setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_heartRateDetailFragment)
        );

        // Gym Membership Card Click
        view.findViewById(R.id.cardGymMembership).setOnClickListener(v -> {
            if (checkLocationPermission()) {
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_gymListFragment);
            }
        });

        // Rewards Card Click
        view.findViewById(R.id.cardRewards).setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_rewardsFragment)
        );
    }

    private void setupHeartRateChart() {
        List<Entry> entries = new ArrayList<>();
        // Add sample data points (replace with real data)
        entries.add(new Entry(0f, 75f));
        entries.add(new Entry(1f, 78f));
        entries.add(new Entry(2f, 80f));
        entries.add(new Entry(3f, 77f));

        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));

        LineData lineData = new LineData(dataSet);
        chartHeartRate.setData(lineData);
        chartHeartRate.getDescription().setEnabled(false);
        chartHeartRate.invalidate();
    }

    private void setupBlogRecyclerView() {
        recyclerBlogs.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        blogAdapter = new BlogAdapter(new ArrayList<>(), this);
        recyclerBlogs.setAdapter(blogAdapter);
        loadBlogs();
    }

    private void loadDashboardData() {
        // TODO: Load real data from a repository/database
        txtWelcome.setText("Welcome back, User!");
        txtStepCount.setText("5,432");
        txtActiveMinutes.setText("45");
        txtWorkoutsCompleted.setText("2");
        txtCaloriesBurned.setText("450 / 2000 kcal");
        txtCurrentHeartRate.setText("75 BPM");
        txtRewardPoints.setText("250 Points");
        txtNextReward.setText("50 more points until next reward!");
        
        progressCalories.setProgress(22); // (450/2000) * 100
        progressRewards.setProgress(83); // (250/300) * 100
    }

    private void loadBlogs() {
        // TODO: Load real blog data from an API/database
        List<Blog> blogs = new ArrayList<>();
        blogs.add(new Blog(
            "1",
            "10 Essential Workout Tips",
            "Maximize your workout efficiency with these proven tips...",
            "https://example.com/image1.jpg",
            "John Doe",
            "2024-04-04",
            "Full blog content here..."
        ));
        blogs.add(new Blog(
            "2",
            "Nutrition Guide for Fitness",
            "Learn about the right nutrition to support your fitness goals...",
            "https://example.com/image2.jpg",
            "Jane Smith",
            "2024-04-03",
            "Full blog content here..."
        ));
        blogAdapter.updateBlogs(blogs);
    }

    @Override
    public void onBlogClick(Blog blog) {
        Bundle args = new Bundle();
        args.putString("blogId", blog.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_blogDetailFragment, args);
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_gymListFragment);
            }
        }
    }
}

