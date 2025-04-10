package com.example.nirvana.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nirvana.R;
import com.example.nirvana.activities.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileSetupFragment extends Fragment {

    private ViewPager2 viewPager;
    private LinearProgressIndicator progressIndicator;
    private Button btnNext, btnPrevious;
    private TextView tvPageTitle, tvPageDescription;
    
    // Page 1 - Basic Information
    private EditText etFullName, etAge;
    private RadioGroup rgGender;
    private String selectedGender = "";
    
    // Page 2 - Body Metrics
    private EditText etHeight, etWeight;
    private TextView tvBMI, tvBMIDescription;
    private RadioGroup rgHeightUnit, rgWeightUnit;
    private boolean usingMetric = true;
    
    // Page 3 - Fitness Goals
    private RadioGroup rgFitnessGoal, rgActivityLevel;
    private String activityLevel = "";
    private String fitnessGoal = "";
    
    // Page 4 - Dietary Preferences
    private ChipGroup chipGroupDiet;
    private RadioGroup rgMealsPerDay;
    private List<String> selectedDietaryPreferences = new ArrayList<>();
    
    // Page 5 - Health Information
    private ChipGroup chipGroupHealthConditions;
    private RadioGroup rgSleepPattern;
    private EditText etWaterIntake;
    private List<String> selectedHealthConditions = new ArrayList<>();
    
    // Page 6 - Schedule Preferences
    private ChipGroup workoutDaysChipGroup, workoutTimeChipGroup, workoutTypeChipGroup;
    private RadioGroup workoutDurationRadioGroup;
    private ChipGroup equipmentChipGroup;
    private List<String> selectedWorkoutDays = new ArrayList<>();
    private String selectedWorkoutTime = "";
    private List<String> selectedWorkoutTypes = new ArrayList<>();
    private String workoutDuration = "30"; // Default duration in minutes
    private List<String> selectedEquipment = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Map<String, Object> userProfile = new HashMap<>();
    private final int TOTAL_PAGES = 6;
    private int currentPage = 0;

    public ProfileSetupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind common views
        viewPager = view.findViewById(R.id.viewPager);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        tvPageTitle = view.findViewById(R.id.tvPageTitle);
        tvPageDescription = view.findViewById(R.id.tvPageDescription);
        
        // Set up ViewPager with adapter
        setupViewPager();
        
        // Update UI for first page
        updatePageUI(0);
        
        // Set up button listeners
        setupButtonListeners();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize all collections
        initializeCollections();
    }

    private void initializeCollections() {
        // Initialize all collection variables to avoid null pointer exceptions
        if (selectedDietaryPreferences == null) {
            selectedDietaryPreferences = new ArrayList<>();
        }
        
        if (selectedHealthConditions == null) {
            selectedHealthConditions = new ArrayList<>();
        }
        
        if (selectedWorkoutDays == null) {
            selectedWorkoutDays = new ArrayList<>();
        }
        
        if (selectedWorkoutTypes == null) {
            selectedWorkoutTypes = new ArrayList<>();
        }
        
        if (selectedEquipment == null) {
            selectedEquipment = new ArrayList<>();
        }
        
        if (selectedWorkoutTime == null) {
            selectedWorkoutTime = "";
        }
        
        if (workoutDuration == null) {
            workoutDuration = "30"; // Default to 30 minutes
        }
        
        Log.d("ProfileSetup", "Collections initialized: " +
                "selectedWorkoutDays=" + selectedWorkoutDays.size() + ", " +
                "selectedWorkoutTypes=" + selectedWorkoutTypes.size() + ", " +
                "selectedEquipment=" + selectedEquipment.size());
    }
    
    private void setupViewPager() {
        // Disable swipe gesture
        viewPager.setUserInputEnabled(false);
        
        // Create and set adapter
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(requireActivity(), TOTAL_PAGES);
        viewPager.setAdapter(adapter);
        
        // Set page change callback
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updatePageUI(position);
            }
        });
    }
    
    private void setupButtonListeners() {
        btnNext.setOnClickListener(v -> {
            if (validateCurrentPage()) {
                saveCurrentPageData();
                if (currentPage < TOTAL_PAGES - 1) {
                    viewPager.setCurrentItem(currentPage + 1, true);
                } else {
                    saveProfileToFirestore();
                }
            }
        });
        
        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 0) {
                viewPager.setCurrentItem(currentPage - 1, true);
            }
        });
    }
    
    private void updatePageUI(int position) {
        // Update progress indicator
        progressIndicator.setProgress((position + 1) * 100 / TOTAL_PAGES);
        
        // Show/hide previous button based on position
        btnPrevious.setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);
        
        // Update button text for last page
        btnNext.setText(position == TOTAL_PAGES - 1 ? "FINISH" : "NEXT");
        
        // Update page title and description
        switch (position) {
            case 0:
                tvPageTitle.setText("Basic Information");
                tvPageDescription.setText("Let's start with some basic information about you");
                initBasicInfoControls();
                break;
            case 1:
                tvPageTitle.setText("Body Metrics");
                tvPageDescription.setText("Let's get accurate measurements to personalize your fitness plan");
                initBodyMetricsControls();
                break;
            case 2:
                tvPageTitle.setText("Fitness Goals");
                tvPageDescription.setText("What are your fitness goals and activity level?");
                initFitnessGoalsControls();
                break;
            case 3:
                tvPageTitle.setText("Dietary Preferences");
                tvPageDescription.setText("Tell us about your dietary preferences");
                initDietaryPreferencesControls();
                break;
            case 4:
                tvPageTitle.setText("Health Information");
                tvPageDescription.setText("Additional health information to tailor your experience");
                initHealthInfoControls();
                break;
            case 5:
                tvPageTitle.setText("Schedule Preferences");
                tvPageDescription.setText("Tell us about your workout schedule preferences");
                initSchedulePreferencesControls();
                break;
        }
    }

    private void initBasicInfoControls() {
        View view = getPageView(0);
        if (view != null) {
            etFullName = view.findViewById(R.id.etFullName);
        etAge = view.findViewById(R.id.etAge);
            rgGender = view.findViewById(R.id.rgGender);
            
            if (rgGender != null) {
                rgGender.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton rb = view.findViewById(checkedId);
                    if (rb != null) {
                        selectedGender = rb.getText().toString();
                    }
                });
            }
            
            // Restore values if already set
            if (etFullName != null && userProfile.containsKey("name")) {
                etFullName.setText((String) userProfile.get("name"));
            }
            if (etAge != null && userProfile.containsKey("age")) {
                etAge.setText(String.valueOf(userProfile.get("age")));
            }
            if (rgGender != null && userProfile.containsKey("gender")) {
                String gender = (String) userProfile.get("gender");
                if ("Male".equals(gender)) {
                    rgGender.check(R.id.rbMale);
                } else if ("Female".equals(gender)) {
                    rgGender.check(R.id.rbFemale);
                } else if ("Other".equals(gender)) {
                    rgGender.check(R.id.rbOther);
                }
            }
        }
    }
    
    private void initBodyMetricsControls() {
        View view = getPageView(1);
        if (view != null) {
            etHeight = view.findViewById(R.id.etHeight);
        etWeight = view.findViewById(R.id.etWeight);
            tvBMI = view.findViewById(R.id.tvBMI);
            tvBMIDescription = view.findViewById(R.id.tvBMIDescription);
            rgHeightUnit = view.findViewById(R.id.rgHeightUnit);
            rgWeightUnit = view.findViewById(R.id.rgWeightUnit);
            
            // Set up unit toggles - only if RadioGroups were found
            if (rgHeightUnit != null) {
                rgHeightUnit.setOnCheckedChangeListener((group, checkedId) -> {
                    // Convert height based on selected unit
                    if (!TextUtils.isEmpty(etHeight.getText())) {
                        try {
                            float height = Float.parseFloat(etHeight.getText().toString());
                            if (checkedId == R.id.rbCm && !usingMetric) {
                                // Convert inches to cm
                                height = height * 2.54f;
                                etHeight.setText(String.format("%.0f", height));
                                usingMetric = true;
                            } else if (checkedId == R.id.rbFeet && usingMetric) {
                                // Convert cm to inches
                                height = height / 2.54f;
                                etHeight.setText(String.format("%.1f", height));
                                usingMetric = false;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                });
            }
            
            if (rgWeightUnit != null) {
                rgWeightUnit.setOnCheckedChangeListener((group, checkedId) -> {
                    // Convert weight based on selected unit
                    if (!TextUtils.isEmpty(etWeight.getText())) {
                        try {
                            float weight = Float.parseFloat(etWeight.getText().toString());
                            if (checkedId == R.id.rbKg && !usingMetric) {
                                // Convert lbs to kg
                                weight = weight * 0.453592f;
                                etWeight.setText(String.format("%.1f", weight));
                            } else if (checkedId == R.id.rbLbs && usingMetric) {
                                // Convert kg to lbs
                                weight = weight / 0.453592f;
                                etWeight.setText(String.format("%.1f", weight));
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                });
            }
            
            // Setup BMI calculation on text change - only if EditTexts were found
            if (etHeight != null && etWeight != null) {
                setupBMICalculation();
                
                // Restore values if already set
                if (userProfile.containsKey("height")) {
                    etHeight.setText(String.valueOf(userProfile.get("height")));
                }
                if (userProfile.containsKey("weight")) {
                    etWeight.setText(String.valueOf(userProfile.get("weight")));
                }
                
                // Calculate BMI if both values are set
                calculateBMI();
            }
        }
    }
    
    private void setupBMICalculation() {
        View.OnFocusChangeListener calculateBMIListener = (v, hasFocus) -> {
            if (!hasFocus) {
                calculateBMI();
            }
        };
        
        etHeight.setOnFocusChangeListener(calculateBMIListener);
        etWeight.setOnFocusChangeListener(calculateBMIListener);
    }
    
    private void calculateBMI() {
        if (etHeight == null || etWeight == null || tvBMI == null || tvBMIDescription == null) {
            return;
        }

        if (!TextUtils.isEmpty(etHeight.getText()) && !TextUtils.isEmpty(etWeight.getText())) {
            try {
                float height = Float.parseFloat(etHeight.getText().toString());
                float weight = Float.parseFloat(etWeight.getText().toString());
                
                // Convert to metric if needed
                if (!usingMetric) {
                    height = height * 2.54f; // inches to cm
                    weight = weight * 0.453592f; // lbs to kg
                }
                
                // Convert cm to meters for BMI calculation
                height = height / 100;
                
                // Calculate BMI
                float bmi = weight / (height * height);
                
                // Update BMI text
                tvBMI.setText(String.format("%.1f", bmi));
                
                // Set BMI description
                if (bmi < 18.5) {
                    tvBMIDescription.setText("Underweight");
                } else if (bmi < 25) {
                    tvBMIDescription.setText("Normal weight");
                } else if (bmi < 30) {
                    tvBMIDescription.setText("Overweight");
                } else {
                    tvBMIDescription.setText("Obese");
                }
                
                // Save BMI to profile
                userProfile.put("bmi", bmi);
                
            } catch (NumberFormatException ignored) {
                tvBMI.setText("--");
                tvBMIDescription.setText("Enter your height and weight to see your BMI");
            }
        }
    }
    
    private void initFitnessGoalsControls() {
        View view = getPageView(2);
        if (view != null) {
            rgFitnessGoal = view.findViewById(R.id.rgFitnessGoal);
            rgActivityLevel = view.findViewById(R.id.rgActivityLevel);
            
            // Set listeners for radio groups
            if (rgFitnessGoal != null) {
                rgFitnessGoal.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton rb = view.findViewById(checkedId);
                    if (rb != null) {
                        fitnessGoal = rb.getText().toString();
                    }
                });
                
                // Restore previously selected value if exists
                if (userProfile.containsKey("fitnessGoal")) {
                    String storedGoal = (String) userProfile.get("fitnessGoal");
                    setRadioButtonByText(rgFitnessGoal, storedGoal);
                }
            }
            
            if (rgActivityLevel != null) {
                rgActivityLevel.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton rb = view.findViewById(checkedId);
                    if (rb != null) {
                        activityLevel = rb.getText().toString();
                    }
                });
                
                // Restore previously selected value if exists
                if (userProfile.containsKey("activityLevel")) {
                    String storedActivity = (String) userProfile.get("activityLevel");
                    setRadioButtonByText(rgActivityLevel, storedActivity);
                }
            }
        }
    }
    
    private void initDietaryPreferencesControls() {
        View view = getPageView(3);
        if (view != null) {
            chipGroupDiet = view.findViewById(R.id.chipGroupDiet);
            rgMealsPerDay = view.findViewById(R.id.rgMealsPerDay);
            
            // Set up chip group listener
            if (chipGroupDiet != null) {
                setupChipGroup(chipGroupDiet, selectedDietaryPreferences);
                
                if (userProfile.containsKey("dietaryPreferences")) {
                    @SuppressWarnings("unchecked")
                    List<String> preferences = (List<String>) userProfile.get("dietaryPreferences");
                    if (preferences != null) {
                        selectedDietaryPreferences.clear();
                        selectedDietaryPreferences.addAll(preferences);
                        
                        // Check corresponding chips
                        for (int i = 0; i < chipGroupDiet.getChildCount(); i++) {
                            Chip chip = (Chip) chipGroupDiet.getChildAt(i);
                            if (preferences.contains(chip.getText().toString())) {
                                chip.setChecked(true);
                            }
                        }
                    }
                }
            }
            
            // Set up meal count radio group
            if (rgMealsPerDay != null) {
                rgMealsPerDay.setOnCheckedChangeListener((group, checkedId) -> {
                    // Extract the number from the radio button text (e.g., "3 meals per day" -> "3")
                    RadioButton rb = view.findViewById(checkedId);
                    if (rb != null) {
                        String mealsText = rb.getText().toString();
                        try {
                            String numMeals = mealsText.split(" ")[0]; // Get the first word which should be the number
                            userProfile.put("mealsPerDay", Integer.parseInt(numMeals));
                        } catch (Exception e) {
                            Log.e("ProfileSetup", "Error parsing meals: " + e.getMessage());
                        }
                    }
                });
                
                // Restore saved value
                if (userProfile.containsKey("mealsPerDay")) {
                    int meals = ((Number) userProfile.get("mealsPerDay")).intValue();
                    switch (meals) {
                        case 2:
                            rgMealsPerDay.check(R.id.rbMeals2);
                            break;
                        case 3:
                            rgMealsPerDay.check(R.id.rbMeals3);
                            break;
                        case 4:
                            rgMealsPerDay.check(R.id.rbMeals4);
                            break;
                        case 5:
                            rgMealsPerDay.check(R.id.rbMeals5);
                            break;
                        case 6:
                            rgMealsPerDay.check(R.id.rbMeals6);
                            break;
                    }
                } else {
                    // Default to 3 meals
                    rgMealsPerDay.check(R.id.rbMeals3);
                }
            }
        }
    }
    
    private void initHealthInfoControls() {
        View view = getPageView(4);
        if (view != null) {
            chipGroupHealthConditions = view.findViewById(R.id.chipGroupHealthConditions);
            rgSleepPattern = view.findViewById(R.id.rgSleepPattern);
            etWaterIntake = view.findViewById(R.id.etWaterIntake);
            
            // Set chip group listener
            if (chipGroupHealthConditions != null) {
                setupChipGroup(chipGroupHealthConditions, selectedHealthConditions);
                
                if (userProfile.containsKey("healthConditions")) {
                    @SuppressWarnings("unchecked")
                    List<String> conditions = (List<String>) userProfile.get("healthConditions");
                    if (conditions != null) {
                        selectedHealthConditions.clear();
                        selectedHealthConditions.addAll(conditions);
                        
                        // Check corresponding chips
                        for (int i = 0; i < chipGroupHealthConditions.getChildCount(); i++) {
                            Chip chip = (Chip) chipGroupHealthConditions.getChildAt(i);
                            if (conditions.contains(chip.getText().toString())) {
                                chip.setChecked(true);
                            }
                        }
                    }
                }
            }
            
            // Restore values for water intake
            if (etWaterIntake != null && userProfile.containsKey("waterIntake")) {
                etWaterIntake.setText(String.valueOf(userProfile.get("waterIntake")));
            }
            
            // Restore values for sleep pattern
            if (rgSleepPattern != null && userProfile.containsKey("sleepPattern")) {
                String sleep = (String) userProfile.get("sleepPattern");
                if ("Less than 6 hours".equals(sleep)) {
                    rgSleepPattern.check(R.id.rbSleepLess6);
                } else if ("6-8 hours".equals(sleep)) {
                    rgSleepPattern.check(R.id.rbSleep6to8);
                } else if ("More than 8 hours".equals(sleep)) {
                    rgSleepPattern.check(R.id.rbSleepMore8);
                }
            }
        }
    }
    
    private void initSchedulePreferencesControls() {
        // Make sure collections are initialized
        initializeCollections();
        
        Log.d("ProfileSetup", "Initializing Schedule Preferences Controls");
        View view = getPageView(5);
        if (view != null) {
            Log.d("ProfileSetup", "Schedule Preferences View obtained successfully");
            
            workoutDaysChipGroup = view.findViewById(R.id.workout_days_chip_group);
            workoutTimeChipGroup = view.findViewById(R.id.workout_time_chip_group);
            workoutTypeChipGroup = view.findViewById(R.id.workout_type_chip_group);
            workoutDurationRadioGroup = view.findViewById(R.id.workout_duration_radio_group);
            equipmentChipGroup = view.findViewById(R.id.available_equipment_chip_group);
            
            // Log findings for debugging
            Log.d("ProfileSetup", "workoutDaysChipGroup: " + (workoutDaysChipGroup != null ? "found" : "not found"));
            Log.d("ProfileSetup", "workoutTimeChipGroup: " + (workoutTimeChipGroup != null ? "found" : "not found"));
            Log.d("ProfileSetup", "workoutTypeChipGroup: " + (workoutTypeChipGroup != null ? "found" : "not found"));
            Log.d("ProfileSetup", "workoutDurationRadioGroup: " + (workoutDurationRadioGroup != null ? "found" : "not found"));
            Log.d("ProfileSetup", "equipmentChipGroup: " + (equipmentChipGroup != null ? "found" : "not found"));
            
            // Set up workout duration radio group
            if (workoutDurationRadioGroup != null) {
                workoutDurationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton rb = view.findViewById(checkedId);
                    if (rb != null) {
                        String durationText = rb.getText().toString();
                        try {
                            // Extract number from text (e.g., "30 minutes" -> "30")
                            workoutDuration = durationText.split(" ")[0];
                            userProfile.put("workoutDuration", workoutDuration);
                        } catch (Exception e) {
                            Log.e("ProfileSetup", "Error parsing duration: " + e.getMessage());
                        }
                    }
                });
                
                // Select default or saved value
                if (userProfile.containsKey("workoutDuration")) {
                    String savedDuration = userProfile.get("workoutDuration").toString();
                    switch (savedDuration) {
                        case "15":
                            workoutDurationRadioGroup.check(R.id.rb_duration_15);
                            break;
                        case "30":
                            workoutDurationRadioGroup.check(R.id.rb_duration_30);
                            break;
                        case "45":
                            workoutDurationRadioGroup.check(R.id.rb_duration_45);
                            break;
                        case "60":
                            workoutDurationRadioGroup.check(R.id.rb_duration_60);
                            break;
                        case "90":
                            workoutDurationRadioGroup.check(R.id.rb_duration_90);
                            break;
                        case "120":
                            workoutDurationRadioGroup.check(R.id.rb_duration_120);
                            break;
                        default:
                            // Default to 30 minutes
                            workoutDurationRadioGroup.check(R.id.rb_duration_30);
                            break;
                    }
                } else {
                    // Default to 30 minutes
                    workoutDurationRadioGroup.check(R.id.rb_duration_30);
                }
            }
            
            // Set up chip group listeners
            if (workoutDaysChipGroup != null) {
                setupChipGroup(workoutDaysChipGroup, selectedWorkoutDays);
                
                // Restore values if already set
                if (userProfile.containsKey("workoutDays")) {
                    @SuppressWarnings("unchecked")
                    List<String> days = (List<String>) userProfile.get("workoutDays");
                    if (days != null) {
                        selectedWorkoutDays.clear();
                        selectedWorkoutDays.addAll(days);
                        
                        // Check corresponding chips
                        for (int i = 0; i < workoutDaysChipGroup.getChildCount(); i++) {
                            Chip chip = (Chip) workoutDaysChipGroup.getChildAt(i);
                            if (days.contains(chip.getText().toString())) {
                                chip.setChecked(true);
                            }
                        }
                    }
                }
            }
            
            // Special handling for single-selection workout time chip group
            if (workoutTimeChipGroup != null) {
                for (int i = 0; i < workoutTimeChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) workoutTimeChipGroup.getChildAt(i);
                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        String time = buttonView.getText().toString();
                        if (isChecked) {
                            selectedWorkoutTime = time;
                        } else if (selectedWorkoutTime.equals(time)) {
                            selectedWorkoutTime = "";
                        }
                    });
                }
                
                if (userProfile.containsKey("workoutTime")) {
                    String time = (String) userProfile.get("workoutTime");
                    for (int i = 0; i < workoutTimeChipGroup.getChildCount(); i++) {
                        Chip chip = (Chip) workoutTimeChipGroup.getChildAt(i);
                        if (chip.getText().toString().equals(time)) {
                            chip.setChecked(true);
                            break;
                        }
                    }
                }
            }
            
            if (workoutTypeChipGroup != null) {
                setupChipGroup(workoutTypeChipGroup, selectedWorkoutTypes);
                
                if (userProfile.containsKey("workoutTypes")) {
                    @SuppressWarnings("unchecked")
                    List<String> types = (List<String>) userProfile.get("workoutTypes");
                    if (types != null) {
                        selectedWorkoutTypes.clear();
                        selectedWorkoutTypes.addAll(types);
                        
                        // Check corresponding chips
                        for (int i = 0; i < workoutTypeChipGroup.getChildCount(); i++) {
                            Chip chip = (Chip) workoutTypeChipGroup.getChildAt(i);
                            if (types.contains(chip.getText().toString())) {
                                chip.setChecked(true);
                            }
                        }
                    }
                }
            }
            
            if (equipmentChipGroup != null) {
                setupChipGroup(equipmentChipGroup, selectedEquipment);
                
                if (userProfile.containsKey("equipment")) {
                    @SuppressWarnings("unchecked")
                    List<String> equipment = (List<String>) userProfile.get("equipment");
                    if (equipment != null) {
                        selectedEquipment.clear();
                        selectedEquipment.addAll(equipment);
                        
                        // Check corresponding chips
                        for (int i = 0; i < equipmentChipGroup.getChildCount(); i++) {
                            Chip chip = (Chip) equipmentChipGroup.getChildAt(i);
                            if (equipment.contains(chip.getText().toString())) {
                                chip.setChecked(true);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void setupChipGroup(ChipGroup chipGroup, List<String> selectedItems) {
        if (chipGroup == null || selectedItems == null) {
            return;
        }

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View childView = chipGroup.getChildAt(i);
            if (childView instanceof Chip) {
                Chip chip = (Chip) childView;
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    String item = buttonView.getText().toString();
                    if (isChecked) {
                        if (!selectedItems.contains(item)) {
                            selectedItems.add(item);
                        }
                    } else {
                        selectedItems.remove(item);
                    }
                });
            }
        }
    }
    
    private View getPageView(int position) {
        try {
            // Log the attempt to get a page view
            Log.d("ProfileSetup", "Getting page view for position: " + position + ", current page: " + currentPage);
            
            // If it's the current page, try to get it directly from the ViewPager
            if (position == currentPage && viewPager != null) {
                // Get the RecyclerView inside ViewPager2
                RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
                if (recyclerView != null) {
                    // Get the ViewHolder for the current position
                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                    if (viewHolder != null) {
                        Log.d("ProfileSetup", "Successfully got view from ViewPager for position: " + position);
                        return viewHolder.itemView;
                    } else {
                        Log.d("ProfileSetup", "ViewHolder is null for position: " + position);
                    }
                } else {
                    Log.d("ProfileSetup", "RecyclerView is null in ViewPager");
                }
            }
            
            // If we couldn't get the view from ViewPager or it's not the current page,
            // create a new view (used mainly for validation before switching pages)
            ProfilePagerAdapter adapter = (ProfilePagerAdapter) viewPager.getAdapter();
            if (adapter != null) {
                View view = adapter.createPageView(getLayoutInflater(), position);
                Log.d("ProfileSetup", "Created new view for position: " + position);
                return view;
            } else {
                Log.e("ProfileSetup", "Adapter is null");
            }
        } catch (Exception e) {
            Log.e("ProfileSetup", "Error getting page view: " + e.getMessage(), e);
        }
        
        Log.e("ProfileSetup", "Failed to get page view for position: " + position);
        return null;
    }
    
    private boolean validateCurrentPage() {
        try {
            View pageView = getPageView(currentPage);
            if (pageView == null) {
                showToast("Unable to validate page");
                return false;
            }

            switch (currentPage) {
                case 0: // Basic Information
                    EditText etFullName = pageView.findViewById(R.id.etFullName);
                    EditText etAge = pageView.findViewById(R.id.etAge);
                    
                    if (etFullName == null || etAge == null) {
                        showToast("Unable to access form fields");
                        return false;
                    }
                    
                    String fullName = etFullName.getText().toString().trim();
                    String ageStr = etAge.getText().toString().trim();
                    
                    if (fullName.isEmpty()) {
                        showToast("Please enter your full name");
                        return false;
                    }
                    
                    if (ageStr.isEmpty()) {
                        showToast("Please enter your age");
                        return false;
                    }
                    
                    try {
                        int age = Integer.parseInt(ageStr);
                        if (age <= 0 || age > 120) {
                            showToast("Please enter a valid age");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        showToast("Please enter a valid age");
                        return false;
                    }
                    
                    return true;
                    
                case 1: // Body Metrics
                    EditText etHeight = pageView.findViewById(R.id.etHeight);
                    EditText etWeight = pageView.findViewById(R.id.etWeight);
                    
                    if (etHeight == null || etWeight == null) {
                        Log.e("ProfileSetup", "Height or weight fields are null");
                        showToast("Unable to access body metrics fields");
                        return false;
                    }
                    
                    String heightStr = etHeight.getText().toString().trim();
                    String weightStr = etWeight.getText().toString().trim();
                    
                    if (heightStr.isEmpty()) {
                        showToast("Please enter your height");
                        return false;
                    }
                    
                    if (weightStr.isEmpty()) {
                        showToast("Please enter your weight");
                        return false;
                    }
                    
                    try {
                        float height = Float.parseFloat(heightStr);
                        if (height <= 0 || height > 300) {
                            showToast("Please enter a valid height");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        showToast("Please enter a valid height");
                        return false;
                    }
                    
                    try {
                        float weight = Float.parseFloat(weightStr);
                        if (weight <= 0 || weight > 500) {
                            showToast("Please enter a valid weight");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        showToast("Please enter a valid weight");
                        return false;
                    }
                    
                    return true;
                    
                case 2: // Fitness Goals
                    RadioGroup rgFitnessGoal = pageView.findViewById(R.id.rgFitnessGoal);
                    RadioGroup rgActivityLevel = pageView.findViewById(R.id.rgActivityLevel);
                    
                    if (rgFitnessGoal == null || rgActivityLevel == null) {
                        showToast("Unable to validate page");
                        return false;
                    }
                    
                    // Check if fitness goal is selected
                    if (rgFitnessGoal.getCheckedRadioButtonId() == -1) {
                        showToast("Please select your fitness goal");
                        return false;
                    }
                    
                    // Check if activity level is selected
                    if (rgActivityLevel.getCheckedRadioButtonId() == -1) {
                        showToast("Please select your activity level");
                        return false;
                    }
                    
                    return true;
                    
                case 3: // Dietary Preferences
                    // Validate spinnerMealsPerDay input
                    RadioGroup rgMealsPerDay = pageView.findViewById(R.id.rgMealsPerDay);
                    if (rgMealsPerDay == null) {
                        showToast("Please select number of meals per day");
                        return false;
                    }
                    
                    if (rgMealsPerDay.getCheckedRadioButtonId() == -1) {
                        showToast("Please select a valid number of meals");
                        return false;
                    }
                    
                    return true;
                    
                case 4: // Health Information
                    EditText etWaterIntake = pageView.findViewById(R.id.etWaterIntake);
                    if (etWaterIntake != null && !TextUtils.isEmpty(etWaterIntake.getText())) {
                        try {
                            int water = Integer.parseInt(etWaterIntake.getText().toString());
                            if (water < 0 || water > 10) {
                                showToast("Please enter a valid water intake (0-10 liters)");
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            showToast("Please enter a valid water intake");
                            return false;
                        }
                    }
                    return true;
                    
                case 5: // Schedule Preferences
                    Log.d("ProfileSetup", "Validating Schedule Preferences page");
                    
                    // Add detailed error checking for each UI component
                    ChipGroup workoutDaysChipGroup = pageView.findViewById(R.id.workout_days_chip_group);
                    if (workoutDaysChipGroup == null) {
                        Log.e("ProfileSetup", "workoutDaysChipGroup is null");
                        showToast("Unable to access workout days field");
                        return false;
                    }
                    
                    ChipGroup workoutTimeChipGroup = pageView.findViewById(R.id.workout_time_chip_group);
                    if (workoutTimeChipGroup == null) {
                        Log.e("ProfileSetup", "workoutTimeChipGroup is null");
                        showToast("Unable to access workout time field");
                        return false;
                    }
                    
                    ChipGroup workoutTypeChipGroup = pageView.findViewById(R.id.workout_type_chip_group);
                    if (workoutTypeChipGroup == null) {
                        Log.e("ProfileSetup", "workoutTypeChipGroup is null");
                        showToast("Unable to access workout type field");
                        return false;
                    }
                    
                    RadioGroup workoutDurationRadioGroup = pageView.findViewById(R.id.workout_duration_radio_group);
                    if (workoutDurationRadioGroup == null) {
                        Log.e("ProfileSetup", "workoutDurationRadioGroup is null");
                        showToast("Unable to access workout duration field");
                        return false;
                    }
                    
                    ChipGroup equipmentChipGroup = pageView.findViewById(R.id.available_equipment_chip_group);
                    if (equipmentChipGroup == null) {
                        Log.e("ProfileSetup", "equipmentChipGroup is null");
                        showToast("Unable to access equipment field");
                        return false;
                    }
                    
                    // Check if any workout days are selected
                    boolean hasSelectedDay = false;
                    for (int i = 0; i < workoutDaysChipGroup.getChildCount(); i++) {
                        Chip chip = (Chip) workoutDaysChipGroup.getChildAt(i);
                        if (chip.isChecked()) {
                            hasSelectedDay = true;
                            break;
                        }
                    }
                    if (!hasSelectedDay) {
                        showToast("Please select at least one workout day");
                        return false;
                    }
                    
                    // Check if workout time is selected
                    boolean hasSelectedTime = false;
                    for (int i = 0; i < workoutTimeChipGroup.getChildCount(); i++) {
                        Chip chip = (Chip) workoutTimeChipGroup.getChildAt(i);
                        if (chip.isChecked()) {
                            hasSelectedTime = true;
                            break;
                        }
                    }
                    if (!hasSelectedTime) {
                        showToast("Please select your preferred workout time");
                        return false;
                    }
                    
                    // Check if workout type is selected
                    boolean hasSelectedType = false;
                    for (int i = 0; i < workoutTypeChipGroup.getChildCount(); i++) {
                        Chip chip = (Chip) workoutTypeChipGroup.getChildAt(i);
                        if (chip.isChecked()) {
                            hasSelectedType = true;
                            break;
                        }
                    }
                    if (!hasSelectedType) {
                        showToast("Please select at least one workout type");
                        return false;
                    }
                    
                    // Check workout duration
                    if (workoutDurationRadioGroup.getCheckedRadioButtonId() == -1) {
                        showToast("Please select a workout duration");
                        return false;
                    }
                    
                    // Check if equipment is selected
                    boolean hasSelectedEquipment = false;
                    for (int i = 0; i < equipmentChipGroup.getChildCount(); i++) {
                        Chip chip = (Chip) equipmentChipGroup.getChildAt(i);
                        if (chip.isChecked()) {
                            hasSelectedEquipment = true;
                            break;
                        }
                    }
                    if (!hasSelectedEquipment) {
                        showToast("Please select at least one piece of equipment");
                        return false;
                    }
                    
                    Log.d("ProfileSetup", "Schedule Preferences validation successful");
                    return true;
                    
                default:
                    return true;
            }
        } catch (Exception e) {
            Log.e("ProfileSetup", "Error validating page: " + e.getMessage(), e);
            showToast("Unable to validate page: " + e.getMessage());
            return false;
        }
    }
    
    private void saveCurrentPageData() {
        switch (currentPage) {
            case 0: // Basic Information
                if (etFullName != null && etAge != null) {
                    userProfile.put("name", etFullName.getText().toString());
                    userProfile.put("age", Integer.parseInt(etAge.getText().toString()));
                    userProfile.put("gender", selectedGender);
                }
                break;
                
            case 1: // Body Metrics
                if (etHeight != null && etWeight != null) {
                    String heightStr = etHeight.getText().toString().trim();
                    String weightStr = etWeight.getText().toString().trim();
                    
                    if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
                        try {
                            float height = Float.parseFloat(heightStr);
                            float weight = Float.parseFloat(weightStr);
                            
                            // Convert to metric for storage if using imperial
                            if (!usingMetric) {
                                height = height * 2.54f; // inches to cm
                                weight = weight * 0.453592f; // lbs to kg
                            }
                            
                            userProfile.put("height", height);
                            userProfile.put("weight", weight);
                            userProfile.put("heightUnit", usingMetric ? "cm" : "in");
                            userProfile.put("weightUnit", usingMetric ? "kg" : "lb");
                        } catch (NumberFormatException e) {
                            Log.e("ProfileSetup", "Error parsing height/weight: " + e.getMessage());
                            showToast("Please enter valid numbers for height and weight");
                        }
                    } else {
                        Log.e("ProfileSetup", "Height or weight is empty in saveCurrentPageData");
                    }
                }
                break;
                
            case 2: // Fitness Goals
                if (!TextUtils.isEmpty(fitnessGoal)) {
                    userProfile.put("fitnessGoal", fitnessGoal);
                } else {
                    // Get the selected fitness goal from radio group
                    int fitnessGoalId = rgFitnessGoal.getCheckedRadioButtonId();
                    if (fitnessGoalId != -1) {
                        RadioButton rb = getView().findViewById(fitnessGoalId);
                        if (rb != null) {
                            userProfile.put("fitnessGoal", rb.getText().toString());
                        }
                    }
                }
                
                if (!TextUtils.isEmpty(activityLevel)) {
                    userProfile.put("activityLevel", activityLevel);
                } else {
                    // Get the selected activity level from radio group
                    int activityLevelId = rgActivityLevel.getCheckedRadioButtonId();
                    if (activityLevelId != -1) {
                        RadioButton rb = getView().findViewById(activityLevelId);
                        if (rb != null) {
                            userProfile.put("activityLevel", rb.getText().toString());
                        }
                    }
                }
                break;
                
            case 3: // Dietary Preferences
                if (selectedDietaryPreferences != null) {
                    userProfile.put("dietaryPreferences", new ArrayList<>(selectedDietaryPreferences));
                }
                
                // Get the selected meals per day from radio group
                int mealsId = rgMealsPerDay.getCheckedRadioButtonId();
                if (mealsId != -1) {
                    RadioButton rb = getView().findViewById(mealsId);
                    if (rb != null) {
                        String mealsText = rb.getText().toString();
                        try {
                            String numMeals = mealsText.split(" ")[0]; // Get the first word (number)
                            userProfile.put("mealsPerDay", Integer.parseInt(numMeals));
                        } catch (Exception e) {
                            Log.e("ProfileSetup", "Error parsing meals: " + e.getMessage());
                            userProfile.put("mealsPerDay", 3); // Default to 3 if parsing fails
                        }
                    }
                }
                break;
                
            case 4: // Health Information
                if (selectedHealthConditions != null) {
                    userProfile.put("healthConditions", new ArrayList<>(selectedHealthConditions));
                }
                
                if (etWaterIntake != null && !TextUtils.isEmpty(etWaterIntake.getText())) {
                    userProfile.put("waterIntake", Integer.parseInt(etWaterIntake.getText().toString()));
                }
                
                if (rgSleepPattern != null) {
                    int sleepId = rgSleepPattern.getCheckedRadioButtonId();
                    if (sleepId != -1) {
                        RadioButton rb = getView().findViewById(sleepId);
                        if (rb != null) {
                            userProfile.put("sleepPattern", rb.getText().toString());
                        }
                    }
                }
                break;
                
            case 5: // Schedule Preferences
                if (selectedWorkoutDays != null) {
                    userProfile.put("workoutDays", new ArrayList<>(selectedWorkoutDays));
                }
                
                if (selectedWorkoutTime != null && !selectedWorkoutTime.isEmpty()) {
                    userProfile.put("workoutTime", selectedWorkoutTime);
                }
                
                if (selectedWorkoutTypes != null) {
                    userProfile.put("workoutTypes", new ArrayList<>(selectedWorkoutTypes));
                }
                
                // Get workout duration from radio group
                if (workoutDurationRadioGroup != null) {
                    int durationId = workoutDurationRadioGroup.getCheckedRadioButtonId();
                    if (durationId != -1) {
                        RadioButton rb = getView().findViewById(durationId);
                        if (rb != null) {
                            String durationText = rb.getText().toString();
                            try {
                                String duration = durationText.split(" ")[0]; // Get the first word (number)
                                userProfile.put("workoutDuration", duration);
                                workoutDuration = duration;
                            } catch (Exception e) {
                                Log.e("ProfileSetup", "Error parsing duration: " + e.getMessage());
                                userProfile.put("workoutDuration", "30"); // Default to 30 if parsing fails
                            }
                        }
                    } else {
                        // Use the workoutDuration variable we've been tracking
                        userProfile.put("workoutDuration", workoutDuration);
                    }
                }
                
                if (selectedEquipment != null) {
                    userProfile.put("equipment", new ArrayList<>(selectedEquipment));
                }
                break;
        }
    }
    
    private void saveProfileToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable buttons during save
        btnNext.setEnabled(false);
        btnPrevious.setEnabled(false);
        
        // Add email to profile
        userProfile.put("email", user.getEmail());
        // Mark profile as completed
        userProfile.put("profileCompleted", true);

        // Save to Firestore - in users/{uid}/profile/details
        db.collection("users").document(user.getUid())
                .collection("profile").document("details")
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Re-enable buttons
                    btnNext.setEnabled(true);
                    btnPrevious.setEnabled(true);
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Add a generic helper method for initializing dropdowns at the appropriate place
    private void initDropdownMenu(AutoCompleteTextView dropdown, String[] options, String defaultValue) {
        if (dropdown == null) return;
        
        // Create an adapter with a custom layout that won't auto-dismiss
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            requireContext(),
            R.layout.simple_dropdown_item,  // Use a custom layout (we'll create this)
            options
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                // Prevent the dropdown from getting focus
                view.setFocusable(false);
                return view;
            }
        };
        
        // Set adapter
        dropdown.setAdapter(adapter);
        
        // Critical: prevent auto-dismissal by using INPUT_METHOD_NOT_NEEDED
        dropdown.setInputType(0); // No input type
        
        // Set initial value
        if (!TextUtils.isEmpty(defaultValue)) {
            dropdown.setText(defaultValue, false);
        }
        
        // Make it act like a button
        dropdown.setFocusable(false);
        dropdown.setClickable(true);
        
        // Set up a simple click listener
        dropdown.setOnClickListener(v -> {
            // Force show dropdown
            dropdown.showDropDown();
        });
        
        // Set a listener for item selection
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            // Set the text without triggering filter
            dropdown.setText(options[position], false);
            // Hide dropdown after selection
            dropdown.dismissDropDown();
        });
        
        // Make sure dropdown shows enough items
        dropdown.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        
        // Override the default behavior to prevent auto-dismiss
        dropdown.setOnDismissListener(() -> {
            // Do nothing, which prevents some auto-dismiss scenarios
        });
    }

    // Utility method to set radio button by text value
    private void setRadioButtonByText(RadioGroup radioGroup, String text) {
        if (radioGroup == null || TextUtils.isEmpty(text)) return;
        
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View child = radioGroup.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                if (rb.getText().toString().equals(text)) {
                    rb.setChecked(true);
                    return;
                }
            }
        }
    }
}

