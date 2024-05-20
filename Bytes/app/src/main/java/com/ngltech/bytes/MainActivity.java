package com.ngltech.bytes;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ngltech.bytes.Upload.AddFragment;
import com.ngltech.bytes.profile.ProfileFragment;
import com.ngltech.bytes.shorts.ShortsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;


            if (item.getItemId() == R.id.home   ) {
                selectedFragment = new ShortsFragment();
            } else if (item.getItemId() == R.id.add) {
                selectedFragment = new AddFragment();
            } else if (item.getItemId() == R.id.profile) {
                selectedFragment = new ProfileFragment(); // Instantiate ProfileFragment
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
