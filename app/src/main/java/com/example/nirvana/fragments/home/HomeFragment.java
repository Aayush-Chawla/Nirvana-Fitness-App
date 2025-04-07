package com.example.nirvana.fragments.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nirvana.R;
import com.example.nirvana.adapters.BlogAdapter;
import com.example.nirvana.models.BlogPost;
import com.example.nirvana.models.GymMembership;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements BlogAdapter.OnBlogClickListener {

    private static final String TAG = "HomeFragment";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

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
    private DatabaseReference userRef;
    private String currentUserId;
    private CardView cardGymMembership;
    private TextView tvGymName, tvMembershipType, tvMembershipStatus;

    // Add these variables to keep track of listeners
    private ValueEventListener dailyStatsListener;
    private ValueEventListener heartRateListener;
    private ValueEventListener rewardsListener;
    private ValueEventListener gymMembershipListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupFirebase();
        setupRecyclerView();
        setupHeartRateChart();
        
        // Load all data
        loadDashboardData();
        loadBlogs();
        loadGymMembership();
        
        if (checkLocationPermission()) {
            loadNearbyGyms();
        }

        return view;
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
        cardGymMembership = view.findViewById(R.id.cardGymMembership);
        tvGymName = view.findViewById(R.id.tvGymName);
        tvMembershipType = view.findViewById(R.id.tvMembershipType);
        tvMembershipStatus = view.findViewById(R.id.tvMembershipStatus);
    }

    private void setupFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId);
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerBlogs.setLayoutManager(layoutManager);
        blogAdapter = new BlogAdapter(new ArrayList<>(), this);
        recyclerBlogs.setAdapter(blogAdapter);
    }

    private void setupHeartRateChart() {
        chartHeartRate.getDescription().setEnabled(false);
        chartHeartRate.setTouchEnabled(true);
        chartHeartRate.setDragEnabled(true);
        chartHeartRate.setScaleEnabled(true);
        chartHeartRate.setPinchZoom(true);
        chartHeartRate.setDrawGridBackground(false);
    }

    private void loadDashboardData() {
        if (userRef == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // Load daily stats
        dailyStatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Skip if fragment is not attached
                
                if (snapshot.exists()) {
                    txtStepCount.setText(String.format(Locale.getDefault(), "%d steps", 
                        snapshot.child("steps").getValue(Integer.class)));
                    txtActiveMinutes.setText(String.format(Locale.getDefault(), "%d mins", 
                        snapshot.child("active_minutes").getValue(Integer.class)));
                    txtWorkoutsCompleted.setText(String.format(Locale.getDefault(), "%d workouts", 
                        snapshot.child("workouts").getValue(Integer.class)));
                    txtCaloriesBurned.setText(String.format(Locale.getDefault(), "%d kcal", 
                        snapshot.child("calories").getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return; // Skip if fragment is not attached
                Log.e(TAG, "Failed to load daily stats", error.toException());
            }
        };
        userRef.child("daily_stats").child(today).addValueEventListener(dailyStatsListener);

        // Load heart rate data
        heartRateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Skip if fragment is not attached
                
                List<Entry> entries = new ArrayList<>();
                float index = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Integer heartRate = dataSnapshot.getValue(Integer.class);
                    if (heartRate != null) {
                        entries.add(new Entry(index++, heartRate));
                        if (index == entries.size()) { // Last entry
                            txtCurrentHeartRate.setText(String.format(Locale.getDefault(), "%d bpm", heartRate));
                        }
                    }
                }
                updateHeartRateChart(entries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return; // Skip if fragment is not attached
                Log.e(TAG, "Failed to load heart rate data", error.toException());
            }
        };
        userRef.child("heart_rate").limitToLast(7).addValueEventListener(heartRateListener);

        // Load rewards data
        rewardsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Skip if fragment is not attached
                
                if (snapshot.exists()) {
                    int points = snapshot.child("points").getValue(Integer.class);
                    int nextReward = snapshot.child("next_reward").getValue(Integer.class);
                    txtRewardPoints.setText(String.format(Locale.getDefault(), "%d pts", points));
                    txtNextReward.setText(String.format(Locale.getDefault(), "%d pts to next reward", nextReward - points));
                    progressRewards.setProgress((points * 100) / nextReward);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return; // Skip if fragment is not attached
                Log.e(TAG, "Failed to load rewards data", error.toException());
            }
        };
        userRef.child("rewards").addValueEventListener(rewardsListener);
    }

    private void updateHeartRateChart(List<Entry> entries) {
        // Safety check to prevent crashes
        if (!isAdded()) return;
        
        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chartHeartRate.setData(lineData);
        chartHeartRate.invalidate();
    }

    private void loadBlogs() {
        // Using mock data for now
        List<BlogPost> mockPosts = new ArrayList<>();
        
        BlogPost post1 = new BlogPost();
        post1.setId("1");
        post1.setTitle("10 Essential Workout Tips for Beginners");
        post1.setDescription("Starting your fitness journey? Here are the key things you need to know.");
        post1.setAuthor("John Smith");
        post1.setImageUrl("https://cdn.pixabay.com/photo/2017/08/07/14/02/man-2604149_960_720.jpg");
        post1.setPublishedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        mockPosts.add(post1);

        BlogPost post2 = new BlogPost();
        post2.setId("2");
        post2.setTitle("Nutrition Guide: Eating for Muscle Growth");
        post2.setDescription("Learn about the best foods and timing for optimal muscle development.");
        post2.setAuthor("Sarah Johnson");
        post2.setImageUrl("https://cdn.pixabay.com/photo/2017/03/26/11/53/hors-doeuvre-2175326_960_720.jpg");
        post2.setPublishedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        mockPosts.add(post2);

        BlogPost post3 = new BlogPost();
        post3.setId("3");
        post3.setTitle("The Benefits of Morning Workouts");
        post3.setDescription("Discover why exercising in the morning can boost your productivity.");
        post3.setAuthor("Mike Wilson");
        post3.setImageUrl("https://cdn.pixabay.com/photo/2017/07/02/19/24/dumbbells-2465478_960_720.jpg");
        post3.setPublishedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        mockPosts.add(post3);

        blogAdapter.updateBlogs(mockPosts);
    }

    private void loadGymMembership() {
        if (userRef == null) return;

        gymMembershipListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Skip if fragment is not attached
                
                if (snapshot.exists()) {
                    GymMembership membership = snapshot.getValue(GymMembership.class);
                    if (membership != null) {
                        tvGymName.setText(membership.getGymName());
                        tvMembershipType.setText(membership.getMembershipType());
                        tvMembershipStatus.setText(membership.getStatus());
                        cardGymMembership.setVisibility(View.VISIBLE);
                    }
                } else {
                    // No active membership, show nearby gyms option
                    tvGymName.setText("Find a Gym");
                    tvMembershipType.setText("Tap to discover nearby gyms");
                    tvMembershipStatus.setText("No Active Membership");
                    cardGymMembership.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return; // Skip if fragment is not attached
                Log.e(TAG, "Error loading gym membership: " + error.getMessage());
            }
        };
        userRef.child("gymMembership").addValueEventListener(gymMembershipListener);

        // Set click listener for gym membership card
        cardGymMembership.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_homeFragment_to_gymListFragment);
        });
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
                // Permission granted, load gym locations
                loadNearbyGyms();
            } else {
                Toast.makeText(requireContext(), "Location permission is required to show nearby gyms", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadNearbyGyms() {
        // TODO: Implement gym location loading
    }

    @Override
    public void onBlogClick(BlogPost blogPost) {
        // Navigate to blog detail screen
        Bundle args = new Bundle();
        args.putParcelable("blog_post", (Parcelable) blogPost);
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_blogDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listeners to prevent callbacks after fragment is detached
        removeListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove listeners again for safety
        removeListeners();
    }

    private void removeListeners() {
        if (userRef != null) {
            if (dailyStatsListener != null) {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                userRef.child("daily_stats").child(today).removeEventListener(dailyStatsListener);
                dailyStatsListener = null;
            }
            if (heartRateListener != null) {
                userRef.child("heart_rate").removeEventListener(heartRateListener);
                heartRateListener = null;
            }
            if (rewardsListener != null) {
                userRef.child("rewards").removeEventListener(rewardsListener);
                rewardsListener = null;
            }
            if (gymMembershipListener != null) {
                userRef.child("gymMembership").removeEventListener(gymMembershipListener);
                gymMembershipListener = null;
            }
        }
    }
}

