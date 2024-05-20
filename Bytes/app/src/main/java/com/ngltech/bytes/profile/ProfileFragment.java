package com.ngltech.bytes.profile;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ngltech.bytes.R;
import com.ngltech.bytes.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileFragment extends Fragment {

    private EditText emailEditText;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri filePath;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        emailEditText = rootView.findViewById(R.id.email_edit_text);
        firstNameTextView = rootView.findViewById(R.id.first_name_text_view);
        lastNameTextView = rootView.findViewById(R.id.last_name_text_view);
        ImageButton editImageButton = rootView.findViewById(R.id.edit_image_button);

        // Retrieve email from MainActivity
        String email = getActivity().getIntent().getStringExtra("email");
        emailEditText.setText(email);

        // Set OnClickListener for edit_image_button
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open file picker when the button is clicked
                openFileChooser();
            }
        });

        // Fetch user info using the retrieved email
        fetchUserInfo(email);

        // Find the TextViews for different options
        TextView editProfileTextView = rootView.findViewById(R.id.editprofile);
        TextView historyTextView = rootView.findViewById(R.id.history);
        TextView uploadsTextView = rootView.findViewById(R.id.uploads);
        TextView helpTextView = rootView.findViewById(R.id.help);
        TextView logoutTextView = rootView.findViewById(R.id.logout);
        TextView deleteTextView = rootView.findViewById(R.id.deleteuser);

        // Set click listeners for each option
        editProfileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the email from the EditText
                String email = emailEditText.getText().toString().trim();

                // Create an intent to start EditProfileActivity
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                // Put the email as an extra in the intent
                intent.putExtra("email", email);
                // Start the activity
                startActivity(intent);
            }
        });

        historyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        uploadsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the email from the EditText
                String email = emailEditText.getText().toString().trim();

                // Create an intent to start UploadActivity
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                // Put the email as an extra in the intent
                intent.putExtra("email", email);
                // Start the activity
                startActivity(intent);
            }
        });

        helpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HelpActivity.class);
                startActivity(intent);
            }
        });

        logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear any user session or data if necessary
                // For example, logout the user and navigate to LoginActivity
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish(); // Optional: finish the current activity to prevent going back
            }
        });

        // Set OnClickListener for deleteuser TextView
        deleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to show delete confirmation dialog
                showDeleteConfirmationDialog();
            }
        });

        return rootView;
    }

    // Method to open file chooser
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI
            filePath = data.getData();

            try {
                // Get the bitmap from the URI
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);

                // Resize the bitmap to avoid drawing too large bitmap
                int maxWidth = 1024; // Set your desired maximum width
                int maxHeight = 1024; // Set your desired maximum height
                bitmap = scaleBitmap(bitmap, maxWidth, maxHeight);

                // Set the selected image bitmap to profile_image ImageView
                ImageView profileImageView = rootView.findViewById(R.id.profile_image);
                profileImageView.setImageBitmap(bitmap); // Update profile image directly with bitmap

                // Upload the selected image to the server
                uploadProfileImageToServer(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to scale bitmap to desired width and height
    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;
        float scale = Math.min(scaleWidth, scaleHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }


    // Method to show delete confirmation dialog
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete this account permanently?");

        // Add OK button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Perform account deletion logic here
                // Make a request to the server to delete the user
                deleteAccount();
            }
        });

        // Add Cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just dismiss the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method to delete the user account via API call to Spring Boot server
    private void deleteAccount() {
        String email = emailEditText.getText().toString().trim();
        String apiUrl = "http://192.168.184.71:8090/delete-profile/" + email; // Replace with your actual API endpoint

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("DELETE");

                    // Set authorization header with the token
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token);

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Successful deletion, show toast and navigate back to login activity
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish(); // Optional: finish the current activity to prevent going back
                            }
                        });
                    } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        // User not found
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Handle other error responses
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Failed to delete account. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle exceptions
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "An error occurred while deleting account.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    // Method to fetch user information via API call to Spring Boot server
    private void fetchUserInfo(String email) {
        String apiUrl = "http://192.168.184.71:8090/profile/" + email; // Replace with your actual API endpoint

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl);
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
                        // Parse JSON response and extract first name and last name
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");

                        // Update UI in the main thread
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                firstNameTextView.setText(firstName);
                                lastNameTextView.setText(lastName);
                            }
                        });
                    } else {
                        // Handle unsuccessful response
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(requireContext(), "Failed to fetch user information. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    urlConnection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    // Handle exceptions
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "An error occurred while fetching user information.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void uploadProfileImageToServer(Bitmap bitmap) {
        String email = emailEditText.getText().toString().trim();
        String apiUrl = "http://192.168.184.71:8090/upload-profile/" + email; // Replace with your actual backend URL

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // Convert bitmap to byte array
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] bitmapBytes = byteArrayOutputStream.toByteArray();

                    // Create multipart request
                    String boundary = "-------------" + System.currentTimeMillis();
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";

                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                    connection.setDoOutput(true);

                    // Write bitmap data to output stream
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write((twoHyphens + boundary + lineEnd).getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"" + lineEnd).getBytes());
                    outputStream.write(("Content-Type: image/jpeg" + lineEnd).getBytes());
                    outputStream.write((lineEnd).getBytes());
                    outputStream.write(bitmapBytes);
                    outputStream.write((lineEnd).getBytes());
                    outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
                    outputStream.flush();
                    outputStream.close();

                    // Get response from the server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Disconnect connection
                    connection.disconnect();

                    // Return response from the server
                    return response.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (result != null) {
                    Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
