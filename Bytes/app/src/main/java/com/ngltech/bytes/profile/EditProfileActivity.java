package com.ngltech.bytes.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ngltech.bytes.R;
import com.ngltech.bytes.Config;
import com.ngltech.bytes.login.ForgotPasswordActivity;

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

public class EditProfileActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private Button passwordButton;
    private ProgressBar progressBar;

    private static final String API_URL = Config.BASE_URL + "/update-profile/";
    private static final String FETCH_URL = Config.BASE_URL + "/profile/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        emailEditText = findViewById(R.id.email_edit_text);
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        latitudeEditText = findViewById(R.id.latitude);
        longitudeEditText = findViewById(R.id.longitude);
        passwordButton = findViewById(R.id.passwordbtn);
        progressBar = findViewById(R.id.progressBar);

        String email = getIntent().getStringExtra("email");
        emailEditText.setText(email);

        fetchUserInfo(email);

        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchUserInfo(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(FETCH_URL + email);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token);

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        inputStream.close();

                        JSONObject jsonObject = new JSONObject(response.toString());
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");
                        String latitude = jsonObject.getString("latitude");
                        String longitude = jsonObject.getString("longitude");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                firstNameEditText.setText(firstName);
                                lastNameEditText.setText(lastName);
                                latitudeEditText.setText(latitude);
                                longitudeEditText.setText(longitude);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EditProfileActivity.this, "Failed to fetch user information", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    urlConnection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditProfileActivity.this, "An error occurred while fetching user information", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void saveProfile(View view) {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String latitude = latitudeEditText.getText().toString();
        String longitude = longitudeEditText.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("firstName", firstName);
            requestData.put("lastName", lastName);
            requestData.put("email", email);
            requestData.put("latitude", latitude);
            requestData.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(API_URL + email);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("PUT");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                    urlConnection.setDoOutput(true);

                    OutputStream outputStream = urlConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    bufferedWriter.write(requestData.toString());
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EditProfileActivity.this, "User profile updated successfully", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("firstName", firstName);
                                resultIntent.putExtra("lastName", lastName);
                                setResult(AppCompatActivity.RESULT_OK, resultIntent);
                                finish();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EditProfileActivity.this, "Failed to update user profile", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditProfileActivity.this, "An error occurred while updating user profile", Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }
}
