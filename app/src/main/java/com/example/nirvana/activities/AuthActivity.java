package com.example.nirvana.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.nirvana.R;
import com.example.nirvana.Fragments.LoginFragment;

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
        if (savedInstanceState == null) {
            LoginFragment firstFragment = new LoginFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.auth_container, firstFragment);
            transaction.commit();
        }



    }

//    public void switchFragment(Fragment fragment) {
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.auth_container, fragment)
//                .addToBackStack(null)
//                .commit();
//    }
}
