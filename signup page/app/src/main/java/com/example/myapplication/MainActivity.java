package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private EditText editTextDOB;
    private Button buttonSignUp;
    private TextView textViewSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        editTextDOB = findViewById(R.id.editTextDOB);
        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewSuccess = findViewById(R.id.textview_success); // Note the change here

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SuccessActivity
                Intent intent = new Intent(MainActivity.this, success.class);

                intent.putExtra("successMessage", "Sign up successful!");
                // Show signup success message on current activity
                startActivity(intent);
            }
        });


    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String dob = dayOfMonth + "/" + (month + 1) + "/" + year;
                        editTextDOB.setText(dob);
                    }
                },
                year, month, dayOfMonth);

        datePickerDialog.show();
    }
}


