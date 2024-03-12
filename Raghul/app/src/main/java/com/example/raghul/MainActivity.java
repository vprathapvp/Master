package com.example.raghul;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button loginbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginbutton = findViewById(R.id.loginbutton);

    }

    public void myIntent(View view) {
        Intent intent = new Intent(this,MainActivity2.class);
        startActivity(intent);
    }

}