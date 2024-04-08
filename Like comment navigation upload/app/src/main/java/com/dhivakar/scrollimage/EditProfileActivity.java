package com.dhivakar.scrollimage;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private EditText passwordEditText;

    // Modify this URL with your actual Spring Boot API endpoint
    private static final String API_URL = "http://192.168.87.71:8096/update-user/";
    private static final String FETCH_URL = "http://192.168.87.71:8096/user/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        emailEditText = findViewById(R.id.email_edit_text);
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        String email = getIntent().getStringExtra("email");
        emailEditText.setText(email);

        // Fetch user info using the retrieved email
        fetchUserInfo(email);

        // Find the "Save" button by its ID and set an OnClickListener
        findViewById(R.id.save_profile_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile(v);
            }
        });
    }

    private void fetchUserInfo(String email) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(FETCH_URL + email);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

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
                        String password = jsonObject.getString("password");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                firstNameEditText.setText(firstName);
                                lastNameEditText.setText(lastName);
                                passwordEditText.setText(password);
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
        String email = emailEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("firstName", firstName);
            requestData.put("lastName", lastName);
            requestData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(API_URL + email);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
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
                                Toast.makeText(EditProfileActivity.this, "User data updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EditProfileActivity.this, "Failed to update user data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditProfileActivity.this, "An error occurred while updating user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
