package com.ngltech.bytes.signup;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ngltech.bytes.R;
import com.ngltech.bytes.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignupActivity extends AppCompatActivity {

    EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword, etLatitude, etLongitude, etOTP;
    Button btnRegister;
    LocationManager locationManager;
    LocationListener locationListener;
    private String generatedOTP;
    ProgressBar progressBar;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_TIMEOUT = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etFirstName = findViewById(R.id.firstname);
        etLastName = findViewById(R.id.lastname);
        etEmail = findViewById(R.id.emailid);
        etPassword = findViewById(R.id.regpassword);
        etConfirmPassword = findViewById(R.id.regconpassword);
        etLatitude = findViewById(R.id.latitude);
        etLongitude = findViewById(R.id.longitude);
        etOTP = findViewById(R.id.etEnteredOTP);
        btnRegister = findViewById(R.id.newregisterbtn);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show progress bar
                progressBar.setVisibility(View.VISIBLE);
                // Check location permissions and fetch location
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ActivityCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermission();
                } else {
                    // Permission already granted
                    fetchLocation();
                }
            }
        });
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    private void fetchLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Location fetched successfully
                    handleLocation(location);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    // Location provider disabled
                    showLocationSettingsDialog();
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // Location provider enabled
                    if (ActivityCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Request location updates only if permission is granted
                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
                    } else {
                        // Permission not granted, handle the case accordingly
                        Toast.makeText(SignupActivity.this, "Location permission not granted", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    // Location provider status changed
                }
            };

            // Request location updates with timeout
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            // Schedule a timeout
            new android.os.Handler().postDelayed(() -> {
                locationManager.removeUpdates(locationListener);
                // Handle timeout, show a message to the user, etc.
                progressBar.setVisibility(View.GONE); // Hide progress bar
            }, LOCATION_TIMEOUT);
        }
    }

    private void handleLocation(Location location) {
        etLatitude.setText(String.valueOf(location.getLatitude()));
        etLongitude.setText(String.valueOf(location.getLongitude()));
        generateAndSendOTP(); // Call generateAndSendOTP() method here
    }


    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
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
                        Toast.makeText(SignupActivity.this, "Location services are required to upload the video", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void generateAndSendOTP() {
        final String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(SignupActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        new GenerateOTPTask().execute(email);
    }

    private class GenerateOTPTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://192.168.184.71:8090/signup/generateOTP");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                JSONObject requestData = new JSONObject();
                requestData.put("email", params[0]);

                OutputStream os = urlConnection.getOutputStream();
                os.write(requestData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }
                    bufferedReader.close();
                    generatedOTP = response.toString().replaceAll("[^0-9]", ""); // Extracting only digits from the result
                    return "OTP generated successfully and sent to email: " + etEmail.getText().toString().trim();
                } else {
                    return null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE); // Hide progress bar
            if (result != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setMessage(result)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                if (generatedOTP != null) {
                                    showOTPDialog();
                                }
                            }
                        });
                builder.create().show();
            } else {
                Toast.makeText(SignupActivity.this, "Failed to generate OTP", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showOTPDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter OTP");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String otp = input.getText().toString().trim();
                if (!TextUtils.isEmpty(otp)) {
                    new ValidateOTPTask().execute(otp);
                } else {
                    Toast.makeText(SignupActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
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

    private void registerUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String latitude = etLatitude.getText().toString().trim();
        String longitude = etLongitude.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setTextColor(Color.RED);
            Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        } else {
            etConfirmPassword.setTextColor(Color.BLACK);
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("firstName", firstName);
            jsonObject.put("lastName", lastName);
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SignUpTask().execute(jsonObject.toString());
    }

    private class SignUpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://192.168.184.71:8090/signup/create-profile");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(params[0].getBytes());
                out.flush();

                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(SignupActivity.this, result, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignupActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ValidateOTPTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://192.168.184.71:8090/validateOTP");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                JSONObject requestData = new JSONObject();
                requestData.put("otp", params[0]);

                OutputStream os = urlConnection.getOutputStream();
                os.write(requestData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "OTP validated successfully for email";
                } else {
                    return "Invalid OTP or OTP expired.";
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return "Failed to validate OTP";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(SignupActivity.this, result, Toast.LENGTH_SHORT).show();
                if (result.equals("OTP validated successfully for email")) {
                    // OTP validation successful, proceed with user registration
                    registerUser();
                } else {
                    // OTP validation failed, show error message or retry OTP entry
                    // For example:
                    // showOTPDialog();
                }
            } else {
                Toast.makeText(SignupActivity.this, "Failed to validate OTP", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
