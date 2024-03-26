package com.dhivakar.scrollimage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView username= (TextView)  findViewById(R.id.username);
        TextView password= (TextView)  findViewById(R.id.Password);
        MaterialButton loginbtn=(MaterialButton) findViewById(R.id.loginbtn);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LOGNI();


            }
        });

    }public void LOGNI(){
        Intent intent =new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}