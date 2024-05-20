package com.ngltech.bytes.Upload;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.ngltech.bytes.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddFragment extends Fragment {

    private static final int PICK_VIDEO_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_TIMEOUT = 10000; // 10 seconds timeout

    private EditText editTextEmail;
    private EditText editTextDescription;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private Button buttonSelectVideo;
    private Button buttonUpload;
    private Uri selectedVideoUri;
    private TextView textViewSelectedVideo;
    private ProgressBar progressBar;
    private String boundary = "***"; // Boundary string
    private String token;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextLatitude = view.findViewById(R.id.latitude);
        editTextLongitude = view.findViewById(R.id.longitude);
        buttonSelectVideo = view.findViewById(R.id.buttonSelectVideo);
        buttonUpload = view.findViewById(R.id.buttonUpload);
        textViewSelectedVideo = view.findViewById(R.id.textViewSelectedVideo);
        progressBar = view.findViewById(R.id.progressBar);

        String email = getActivity().getIntent().getStringExtra("email");
        editTextEmail.setText(email);

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

        buttonSelectVideo.setOnClickListener(v -> openVideoChooser());
        buttonUpload.setOnClickListener(v -> {
            // Show progress bar
            progressBar.setVisibility(View.VISIBLE);


            // Check location permissions and fetch location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            } else {
                // Permission already granted
                fetchLocation();
            }
        });

        return view;
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
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
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
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Request location updates only if permission is granted
                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
                    } else {
                        // Permission not granted, handle the case accordingly
                        Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(requireContext(), "Location fetched successfully", Toast.LENGTH_SHORT).show();
        uploadVideo(); // initiate video upload after fetching location
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
                        Toast.makeText(requireContext(), "Location services are required to upload the video", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void uploadVideo() {
        String email = editTextEmail.getText().toString();
        String description = editTextDescription.getText().toString();
        String latitude = editTextLatitude.getText().toString();
        String longitude = editTextLongitude.getText().toString();

        if (email.isEmpty() || description.isEmpty() || selectedVideoUri == null || latitude.isEmpty() || longitude.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all the required details", Toast.LENGTH_SHORT).show();
            return;
        }

        UploadTask uploadTask = new UploadTask(email, description, selectedVideoUri, latitude, longitude, token);
        uploadTask.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedVideoUri = data.getData();
            textViewSelectedVideo.setText("Selected Video: " + getFileName(selectedVideoUri));
            textViewSelectedVideo.setVisibility(View.VISIBLE);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
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

    private class UploadTask extends AsyncTask<Void, Void, Integer> {

        private final String email;
        private final String description;
        private final Uri video;
        private final String latitude;
        private final String longitude;
        private final String token;

        public UploadTask(String email, String description, Uri video, String latitude, String longitude, String token) {
            this.email = email;
            this.description = description;
            this.video = video;
            this.latitude = latitude;
            this.longitude = longitude;
            this.token = token;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return uploadToServer(email, description, video, latitude, longitude, token);
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            // Hide progress bar
            progressBar.setVisibility(View.GONE);
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Upload failed. Response Code: " + responseCode, Toast.LENGTH_SHORT).show();
            }
        }

        private Integer uploadToServer(String email, String description, Uri videoUri, String latitude, String longitude, String token) {
            String targetURL = "http://192.168.184.71:8090/api/upload";

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
                outputPostParameters(outputStream, "description", description);
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

                return responseCode;
            } catch (IOException e) {
                e.printStackTrace();
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
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

                try (InputStream inputStream = requireContext().getContentResolver().openInputStream(videoUri)) {
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
    }
}
