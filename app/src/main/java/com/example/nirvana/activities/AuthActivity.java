package com.example.nirvana.activities;

import android.os.Bundle;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.nirvana.R;
import com.example.nirvana.fragments.auth.LoginFragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.auth_container, new LoginFragment())
//                    .commit();
//        }

        // Load FirstFragment by default
//        if (savedInstanceState == null) {
//            LoginFragment firstFragment = new LoginFragment();
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            transaction.add(R.id.auth_container, firstFragment);
//            transaction.commit();
//        }

//        ------- Navigation Component using NavController -------
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController);


        // Find the Toolbar and set it as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Ensure you're getting the NavController correctly
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController);
        } else {
            throw new IllegalStateException("NavHostFragment not found!");
        }

    }

//        ------- Navigation Component using NavController -------
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        return navController.navigateUp() || super.onSupportNavigateUp();
//    }

//    public void switchFragment(Fragment fragment) {
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.auth_container, fragment)
//                .addToBackStack(null)
//                .commit();
//    }
}
