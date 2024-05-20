package com.ngltech.bytes.profile;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
    private Button locationButton;
    private Button passwordButton;
    private ProgressBar progressBar;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_TIMEOUT = 10000;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final String API_URL = Config.BASE_URL + "/update-profile/";
    private static final String FETCH_URL = Config.BASE_URL + "/profile/";

    private boolean locationFetched = false; // Boolean flag to track whether location has been fetched

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        emailEditText = findViewById(R.id.email_edit_text);
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        latitudeEditText = findViewById(R.id.latitude);
        longitudeEditText = findViewById(R.id.longitude);
        locationButton = findViewById(R.id.locationbtn);
        passwordButton = findViewById(R.id.passwordbtn);
        progressBar = findViewById(R.id.progressBar);

        String email = getIntent().getStringExtra("email");
        emailEditText.setText(email);

        // Fetch user info using the retrieved email
        fetchUserInfo(email);

        // Initialize location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Set click listener for the location button
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchLocation();
            }
        });
        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchLocation() {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Location fetched successfully
                    if (!locationFetched) {
                        handleLocation(location);
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                        Toast.makeText(EditProfileActivity.this, "Location fetched successfully", Toast.LENGTH_SHORT).show();
                        locationFetched = true; // Set flag to true to indicate location has been fetched
                    }
                    // Remove location updates after fetching location once
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    // Location provider disabled
                    showLocationSettingsDialog();
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // Location provider enabled
                    if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Request location updates only if permission is granted
                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
                    } else {
                        // Permission not granted, handle the case accordingly
                        Toast.makeText(EditProfileActivity.this, "Location permission not granted", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    // Location provider status changed
                }
            };

            // Request location updates with timeout
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
                // Schedule a timeout
                new android.os.Handler().postDelayed(() -> {
                    // Handle timeout, show a message to the user, etc.
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                }, LOCATION_TIMEOUT);
            } else {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void handleLocation(Location location) {
        latitudeEditText.setText(String.valueOf(location.getLatitude()));
        longitudeEditText.setText(String.valueOf(location.getLongitude()));
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setMessage("Location services are disabled. Do you want to enable them?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Open location settings
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        // Handle the case when the user declines to enable location services
                        Toast.makeText(EditProfileActivity.this, "Location services are required to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void fetchUserInfo(String email) {
        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(FETCH_URL + email);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    // Set authorization header with the token
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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                firstNameEditText.setText(firstName);
                                lastNameEditText.setText(lastName);
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

        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        JSONObject requestData = new JSONObject();
        try {
            // Add email to the JSON object
            requestData.put("firstName", firstName);
            requestData.put("lastName", lastName);
            requestData.put("email", email);
            requestData.put("latitude", latitude);
            requestData.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Retrieve token from SharedPreferences
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
                    // Hide progress bar after processing is done
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
