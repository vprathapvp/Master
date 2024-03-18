package com.example.myapplication;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class success extends AppCompatActivity {
    private TextView textview_success;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.success_page);
        textview_success = findViewById(R.id.textview_success);

        // Receive the success message from MainActivity
        String successMessage = getIntent().getStringExtra("successMessage");

        // Display the success message
        textview_success.setText(successMessage);
    }

}



