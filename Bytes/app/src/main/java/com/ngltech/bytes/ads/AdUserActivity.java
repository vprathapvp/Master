package com.ngltech.bytes.ads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ngltech.bytes.R;

public class AdUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aduser);

        // Initialize EditText for email
        EditText emailEditText = findViewById(R.id.email_edit_text);

        // Retrieve email from LoginActivity
        String email = getIntent().getStringExtra("email");

        // Set the retrieved email to the EditText
        emailEditText.setText(email);

        // Initialize buttons
        Button uploadAdButton = findViewById(R.id.upload_ad_btn);
        Button yourUploadsButton = findViewById(R.id.youruploads_btn);

        // Set click listener for Upload Ad button
        uploadAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailEditText = findViewById(R.id.email_edit_text);
                String email = emailEditText.getText().toString().trim();

                // Start AdUploadActivity and pass the email
                Intent intent = new Intent(AdUserActivity.this, AdUploadActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        // Set click listener for Your uploads button
        yourUploadsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                // Start YourAdActivity and pass the email
                Intent intent = new Intent(AdUserActivity.this, YourAdActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }
}
