package com.example.nirvana.fragments.diet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.nirvana.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.nirvana.models.FoodItem;
import com.example.nirvana.utils.FirebaseHelper;

public class LogDietFragment extends Fragment implements FoodSearchDialog.OnFoodSelectedListener {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabSearchFood;
    private Button btnAddFood;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_diet, container, false);
        
        // Initialize views
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        fabSearchFood = view.findViewById(R.id.fabSearchFood);
//        btnAddFood = view.findViewById(R.id.btnAddFood);

        // Setup toolbar back button
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Setup ViewPager with tabs
        setupViewPager();

        // Setup FAB click listener
        fabSearchFood.setOnClickListener(v -> showFoodSearchDialog());

//        btnAddFood.setOnClickListener(v -> showFoodSearchDialog());

        return view;
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return MealFragment.newInstance("Breakfast");
                    case 1: return MealFragment.newInstance("Lunch");
                    case 2: return MealFragment.newInstance("Dinner");
                    case 3: return MealFragment.newInstance("Snacks");
                    default: return MealFragment.newInstance("Other");
                }
            }

            @Override
            public int getItemCount() {
                return 5;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Breakfast"); break;
                case 1: tab.setText("Lunch"); break;
                case 2: tab.setText("Dinner"); break;
                case 3: tab.setText("Snacks"); break;
                case 4: tab.setText("Other"); break;
            }
        }).attach();
    }

    private void showFoodSearchDialog() {
        FoodSearchDialog dialog = FoodSearchDialog.newInstance(this);
        dialog.show(getChildFragmentManager(), "food_search");
    }

    @Override
    public void onFoodSelected(FoodItem foodItem, String servingSize) {
        String mealType = getMealType(viewPager.getCurrentItem());
        
        FirebaseHelper.logFood(mealType, foodItem, servingSize, new FirebaseHelper.OnFoodLoggedListener() {
            @Override
            public void onSuccess() {
                String message = String.format("%s (%s) added to %s", 
                    foodItem.getName(), 
                    servingSize,
                    mealType);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                
                // Refresh the current meal fragment
                refreshCurrentMealFragment();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getMealType(int position) {
        switch (position) {
            case 0: return "Breakfast";
            case 1: return "Lunch";
            case 2: return "Dinner";
            case 3: return "Snacks";
            default: return "Other";
        }
    }

    private void refreshCurrentMealFragment() {
        Fragment currentFragment = getChildFragmentManager()
            .findFragmentByTag("f" + viewPager.getId() + ":" + viewPager.getCurrentItem());
        if (currentFragment instanceof MealFragment) {
            ((MealFragment) currentFragment).refreshFoodList();
        }
    }
}