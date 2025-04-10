package com.example.nirvana.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nirvana.R;
import com.example.nirvana.databinding.FragmentProfileSetupBinding;

public class ProfileSetupFragment extends Fragment {

    private FragmentProfileSetupBinding binding;
    private SetupFragmentAdapter setupFragmentAdapter;
    private int currentPage = 0;

    public ProfileSetupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileSetupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager();
    }

    private void setupViewPager() {
        setupAdapter();
        
        // Setup page change listener
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateBottomNavigation();
            }
        });
        
        // Disable swiping
        binding.viewPager.setUserInputEnabled(false);
    }

    private void setupAdapter() {
        setupFragmentAdapter = new SetupFragmentAdapter(getChildFragmentManager(), getLifecycle());
        setupFragmentAdapter.addFragment(new BasicInfoFragment(this));
        setupFragmentAdapter.addFragment(new BodyMetricsFragment(this));
        setupFragmentAdapter.addFragment(new FitnessGoalsFragment(this));
        setupFragmentAdapter.addFragment(new HealthInfoFragment(this));
        setupFragmentAdapter.addFragment(new SchedulePreferencesFragment(this));
        binding.viewPager.setAdapter(setupFragmentAdapter);
        
        // Update bottom navigation and page indicator
        int totalPages = setupFragmentAdapter.getItemCount();
        setupTabIndicator(totalPages);
        updateBottomNavigation();
    }

    private void setupTabIndicator(int pageCount) {
        // Clear existing tabs
        binding.pageIndicator.removeAllTabs();
        
        // Add tabs for each page
        for (int i = 0; i < pageCount; i++) {
            binding.pageIndicator.addTab(binding.pageIndicator.newTab());
        }
    }
    
    private void updateBottomNavigation() {
        // Show/hide previous button based on position
        binding.btnPrevious.setVisibility(currentPage > 0 ? View.VISIBLE : View.INVISIBLE);
        
        // Update button text for last page
        binding.btnNext.setText(currentPage == setupFragmentAdapter.getItemCount() - 1 ? 
                R.string.finish : R.string.next);
                
        // Update page title and description
        updatePageInfo(currentPage);
    }
    
    private void updatePageInfo(int position) {
        switch (position) {
            case 0:
                binding.tvPageTitle.setText(R.string.basic_info_title);
                binding.tvPageDescription.setText(R.string.basic_info_description);
                break;
            case 1:
                binding.tvPageTitle.setText(R.string.body_metrics_title);
                binding.tvPageDescription.setText(R.string.body_metrics_description);
                break;
            case 2:
                binding.tvPageTitle.setText(R.string.fitness_goals_title);
                binding.tvPageDescription.setText(R.string.fitness_goals_description);
                break;
            case 3:
                binding.tvPageTitle.setText(R.string.health_info_title);
                binding.tvPageDescription.setText(R.string.health_info_description);
                break;
            case 4:
                binding.tvPageTitle.setText(R.string.schedule_preferences_title);
                binding.tvPageDescription.setText(R.string.schedule_preferences_description);
                break;
        }
    }
    
    public void goToNextPage() {
        if (currentPage < setupFragmentAdapter.getItemCount() - 1) {
            binding.viewPager.setCurrentItem(currentPage + 1);
        }
    }
    
    public void goToPreviousPage() {
        if (currentPage > 0) {
            binding.viewPager.setCurrentItem(currentPage - 1);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    private static class SetupFragmentAdapter extends FragmentStateAdapter {
        private final java.util.List<Fragment> fragmentList = new java.util.ArrayList<>();
        
        public SetupFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }
        
        public void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }
        
        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
    
    // Dummy nested fragment classes
    public static class BasicInfoFragment extends Fragment {
        public BasicInfoFragment(ProfileSetupFragment parent) {}
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.page_basic_information, container, false);
        }
    }
    
    public static class BodyMetricsFragment extends Fragment {
        public BodyMetricsFragment(ProfileSetupFragment parent) {}
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.page_body_metrics, container, false);
        }
    }
    
    public static class FitnessGoalsFragment extends Fragment {
        public FitnessGoalsFragment(ProfileSetupFragment parent) {}
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.page_fitness_goals, container, false);
        }
    }
    
    public static class HealthInfoFragment extends Fragment {
        public HealthInfoFragment(ProfileSetupFragment parent) {}
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.page_health_information, container, false);
        }
    }
    
    public static class SchedulePreferencesFragment extends Fragment {
        public SchedulePreferencesFragment(ProfileSetupFragment parent) {}
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.page_schedule_preferences, container, false);
        }
    }
} 