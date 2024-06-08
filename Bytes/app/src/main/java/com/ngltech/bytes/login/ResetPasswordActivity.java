package com.ngltech.bytes.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ngltech.bytes.R;
import com.ngltech.bytes.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextgmail, editTextNewPassword, editTextConfirmPassword;
    private Button buttonUpdatePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        editTextgmail = findViewById(R.id.editTextgmail);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonUpdatePassword = findViewById(R.id.buttonUpdatePassword);

        // Retrieve email from intent and pre-fill in the email field
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            editTextgmail.setText(email);
        }

        buttonUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextgmail.getText().toString().trim();
                String newPassword = editTextNewPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ResetPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateUserPassword(email, newPassword);
            }
        });
    }

    private void updateUserPassword(final String email, final String newPassword) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String response = null;

                try {
                    URL url = new URL(Config.BASE_URL + "/password"); // Note the http:// prefix
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("PUT");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + getBearerToken()); // Add bearer token
                    urlConnection.setDoOutput(true);

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("email", email);
                    requestBody.put("password", newPassword);

                    OutputStream outputStream = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write(requestBody.toString());
                    writer.close();
                    outputStream.close();

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = urlConnection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        response = stringBuilder.toString();
                    } else {
                        response = "Failed to update password. HTTP Status Code: " + responseCode;
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    response = "Exception: " + e.getMessage();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return response;
            }

            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    Toast.makeText(ResetPasswordActivity.this, response, Toast.LENGTH_SHORT).show();
                    // Return to LoginActivity
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Finish the current activity
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    // Method to retrieve bearer token from SharedPreferences
    private String getBearerToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }
}
