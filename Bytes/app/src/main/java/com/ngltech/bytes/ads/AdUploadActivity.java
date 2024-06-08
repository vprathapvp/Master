package com.ngltech.bytes.ads;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.ngltech.bytes.Config;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ngltech.bytes.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AdUploadActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_TIMEOUT = 10000; // 10 seconds timeout

    private EditText editTextEmail;
    private Spinner subscriptionSpinner;
    private Spinner distanceSpinner;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private Button buttonSelectVideo;
    private Button buttonUpload;
    private Uri selectedVideoUri;
    private TextView textViewSelectedVideo;

    private String boundary = "***"; // Boundary string
    private String token;
    private LocationManager locationManager;
    private LocationListener locationListener;

    // Custom Pair class
    private static class Pair<F, S> {
        F first;
        S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adupload);

        editTextEmail = findViewById(R.id.email_edit_text);
        subscriptionSpinner = findViewById(R.id.Sub_Spinner);
        distanceSpinner = findViewById(R.id.Distance_Spinner);
        editTextLatitude = findViewById(R.id.latitude);
        editTextLongitude = findViewById(R.id.longitude);
        buttonSelectVideo = findViewById(R.id.buttonSelectVideo);
        buttonUpload = findViewById(R.id.buttonUpload);
        textViewSelectedVideo = findViewById(R.id.textViewSelectedVideo);

        String email = getIntent().getStringExtra("email");
        editTextEmail.setText(email);

        // Setting up subscription spinner
        List<String> subscriptionItems = new ArrayList<>();
        subscriptionItems.add("Monthly");
        subscriptionItems.add("Quarterly");
        subscriptionItems.add("Half-yearly");
        subscriptionItems.add("Annual");

        ArrayAdapter<String> subscriptionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subscriptionItems);
        subscriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subscriptionSpinner.setAdapter(subscriptionAdapter);

        // Setting up distance spinner
        List<String> distanceItems = new ArrayList<>();
        distanceItems.add("20km");
        distanceItems.add("50km");
        distanceItems.add("80km");
        distanceItems.add("200km");

        ArrayAdapter<String> distanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distanceItems);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(distanceAdapter);

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

        buttonSelectVideo.setOnClickListener(v -> openVideoChooser());
        buttonUpload.setOnClickListener(v -> {



            // Check location permissions and fetch location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            } else {
                // Permission already granted
                fetchLocation();
            }
        });
    }

    private void openVideoChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST_CODE);
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
                    if (ActivityCompat.checkSelfPermission(AdUploadActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Request location updates only if permission is granted
                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
                    } else {
                        // Permission not granted, handle the case accordingly
                        Toast.makeText(AdUploadActivity.this, "Location permission not granted", Toast.LENGTH_SHORT).show();
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
            }, LOCATION_TIMEOUT);
        }
    }

    private void handleLocation(Location location) {
        editTextLatitude.setText(String.valueOf(location.getLatitude()));
        editTextLongitude.setText(String.valueOf(location.getLongitude()));
        uploadVideo(); // initiate video upload after fetching location
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdUploadActivity.this);
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
                        Toast.makeText(AdUploadActivity.this, "Location services are required to upload the video", Toast.LENGTH_SHORT).show();

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void uploadVideo() {
        String email = editTextEmail.getText().toString();
        String subscription = subscriptionSpinner.getSelectedItem().toString();
        String distanceRange = distanceSpinner.getSelectedItem().toString();
        String latitude = editTextLatitude.getText().toString();
        String longitude = editTextLongitude.getText().toString();

        if (email.isEmpty() || subscription.isEmpty() || selectedVideoUri == null || latitude.isEmpty() || longitude.isEmpty()) {
            Toast.makeText(AdUploadActivity.this, "Please fill all the required details", Toast.LENGTH_SHORT).show();
            return;
        }

        UploadTask uploadTask = new UploadTask(email, subscription, distanceRange, selectedVideoUri, latitude, longitude, token);
        uploadTask.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedVideoUri = data.getData();
            textViewSelectedVideo.setText("Selected Video: " + getFileName(selectedVideoUri));
            textViewSelectedVideo.setVisibility(View.VISIBLE);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex);
                    } else {
                        Log.e("getFileName", "DISPLAY_NAME column not found");
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private class UploadTask extends AsyncTask<Void, Void, Pair<Integer, String>> {

        private final String email;
        private final String subscription;
        private final String distanceRange;
        private final Uri video;
        private final String latitude;
        private final String longitude;
        private final String token;

        public UploadTask(String email, String subscription, String distanceRange, Uri video, String latitude, String longitude, String token) {
            this.email = email;
            this.subscription = subscription;
            this.distanceRange = distanceRange;
            this.video = video;
            this.latitude = latitude;
            this.longitude = longitude;
            this.token = token;
        }

        @Override
        protected Pair<Integer, String> doInBackground(Void... voids) {
            return uploadToServer(email, subscription, distanceRange, video, latitude, longitude, token);
        }

        @Override
        protected void onPostExecute(Pair<Integer, String> response) {
            super.onPostExecute(response);


            int responseCode = response.first;
            String responseMessage = response.second;

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                // Extract payment amount from response message
                double paymentAmount = extractPaymentAmount(responseMessage);
                // Show payment amount in AlertDialog
                showPaymentAmountDialog(paymentAmount);
            } else {
                Toast.makeText(AdUploadActivity.this, "Upload failed. Response Code: " + responseCode, Toast.LENGTH_SHORT).show();
            }
        }

        private void showPaymentAmountDialog(double paymentAmount) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AdUploadActivity.this);
            builder.setTitle("Payment Amount");
            builder.setMessage("Payment amount: " + paymentAmount);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // You can add any action here if needed
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private Pair<Integer, String> uploadToServer(String email, String subscription, String distanceRange, Uri videoUri, String latitude, String longitude, String token) {
            String targetURL = Config.BASE_URL + "/business/upload-ad";

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(targetURL).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                // Set Authorization header with the token
                connection.setRequestProperty("Authorization", "Bearer " + token);

                connection.setDoOutput(true);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                // Add email parameter
                outputPostParameters(outputStream, "email", email);
                // Add description parameter
                outputPostParameters(outputStream, "subscription", subscription);
                outputPostParameters(outputStream, "distanceRange", distanceRange);
                // Add latitude parameter
                outputPostParameters(outputStream, "latitude", latitude);
                // Add longitude parameter
                outputPostParameters(outputStream, "longitude", longitude);
                // Add video file
                outputVideoFile(outputStream, videoUri);

                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                Log.d("UploadResponse", "Response Code: " + responseCode);

                // Read response message
                InputStream inputStream = connection.getInputStream();
                String responseMessage = readStream(inputStream);

                return new Pair<>(responseCode, responseMessage);
            } catch (IOException e) {
                e.printStackTrace();
                return new Pair<>(HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to upload file: " + e.getMessage());
            }
        }

        private void outputPostParameters(DataOutputStream outputStream, String key, String value) throws IOException {
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
            outputStream.writeBytes(value + "\r\n");
        }

        private void outputVideoFile(DataOutputStream outputStream, Uri videoUri) throws IOException {
            String fileName = getFileName(videoUri);
            if (fileName != null) {
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: video/mp4" + lineEnd);
                outputStream.writeBytes(lineEnd);

                try (InputStream inputStream = getContentResolver().openInputStream(videoUri)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            } else {
                Log.e("outputVideoFile", "File name is null");
            }
        }

        private String readStream(InputStream inputStream) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                stringBuilder.append(new String(buffer, 0, bytesRead));
            }
            return stringBuilder.toString();
        }

        private double extractPaymentAmount(String responseMessage) {
            // Assuming response message format is "Uploaded the file successfully: filename. Payment amount: amount"
            String[] parts = responseMessage.split("\\. Payment amount: ");
            if (parts.length == 2) {
                String amountStr = parts[1];
                try {
                    return Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return 0.0;
        }
    }
}
