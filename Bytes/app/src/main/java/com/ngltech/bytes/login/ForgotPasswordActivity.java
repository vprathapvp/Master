package com.ngltech.bytes.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ngltech.bytes.R;
import com.ngltech.bytes.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button getOTPButton;
    private EditText otpEditText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.username);
        getOTPButton = findViewById(R.id.otp_button);
        progressBar = findViewById(R.id.progressBar);

        getOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show progress bar
                progressBar.setVisibility(View.VISIBLE);

                // Execute AsyncTask to perform network operation
                new GenerateOTPAsyncTask().execute(email);
            }
        });
    }

    private class GenerateOTPAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String result = "";

            try {
                URL url = new URL(Config.BASE_URL + "/forget-password/generateOTP");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("email", email);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes());
                os.flush();
                os.close();

                InputStream inputStream;
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }

                bufferedReader.close();
                conn.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // Hide progress bar
            progressBar.setVisibility(View.GONE);

            if (result != null && !result.isEmpty()) {
                showOTPDialog(result); // Show dialog to enter OTP
            } else {
                Toast.makeText(ForgotPasswordActivity.this, "Failed to generate OTP", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showOTPDialog(final String otp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter OTP");

        // Set up the input
        otpEditText = new EditText(this);
        builder.setView(otpEditText);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredOTP = otpEditText.getText().toString().trim();
                if (enteredOTP.equals(otp)) {
                    // Validate entered OTP
                    new ValidateOTPAsyncTask().execute(enteredOTP, otp);
                } else {
                    // Show error message if OTP is incorrect
                    Toast.makeText(ForgotPasswordActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    // Keep the dialog open
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class ValidateOTPAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String enteredOTP = params[0];
            String receivedOTP = params[1];
            String result = "";

            try {
                URL url = new URL(Config.BASE_URL + "/validateOTP");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("otp", enteredOTP); // Sending entered OTP to server

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes());
                os.flush();
                os.close();

                InputStream inputStream;
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }

                bufferedReader.close();
                conn.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if (result.equals("OTP validated successfully for email")) {
                    // Display success message and navigate to next activity
                    Toast.makeText(ForgotPasswordActivity.this, "OTP validated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", emailEditText.getText().toString().trim()); // Pass email to ResetPasswordActivity
                    startActivity(intent);
                    finish(); // Finish the current activity
                } else {
                    // Display failure message
                    Toast.makeText(ForgotPasswordActivity.this, "Invalid OTP or OTP expired.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle null response
                Toast.makeText(ForgotPasswordActivity.this, "Failed to validate OTP", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
