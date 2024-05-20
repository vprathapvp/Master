package com.ngltech.bytes;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ngltech.bytes.Upload.AddFragment;
import com.ngltech.bytes.profile.ProfileFragment;
import com.ngltech.bytes.shorts.ShortsFragment;
import com.ngltech.bytes.login.LoginActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user has logged in before
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // If the user has not logged in before, redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Finish MainActivity to prevent returning to it after login
            return;
        }

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.home) {
                selectedFragment = new ShortsFragment();
            } else if (item.getItemId() == R.id.add) {
                selectedFragment = new AddFragment();
            } else if (item.getItemId() == R.id.profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        // Set the default fragment when the activity is created
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ShortsFragment())
                .commit();
    }
}
