package com.dhivakar.scrollimage;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileFragment extends Fragment {

    private EditText emailEditText;
    private TextView firstNameTextView;
    private TextView lastNameTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        emailEditText = rootView.findViewById(R.id.email_edit_text);
        firstNameTextView = rootView.findViewById(R.id.first_name_text_view);
        lastNameTextView = rootView.findViewById(R.id.last_name_text_view);

        // Retrieve email from MainActivity
        String email = getActivity().getIntent().getStringExtra("email");
        emailEditText.setText(email);

        // Fetch user info using the retrieved email
        fetchUserInfo(email);

        // Find the TextViews for different options
        TextView editProfileTextView = rootView.findViewById(R.id.editprofile);
        TextView historyTextView = rootView.findViewById(R.id.history);
        TextView playlistTextView = rootView.findViewById(R.id.playlist);
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

        playlistTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlaylistActivity.class);
                startActivity(intent);
            }
        });

        uploadsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadActivity.class);
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
        String apiUrl = "http://192.168.87.71:8096/delete-user/" + email; // Replace with your actual API endpoint

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("DELETE");

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
                    } else {
                        // Handle unsuccessful response
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

    private void fetchUserInfo(String email) {
        String apiUrl = "http://192.168.87.71:8096/user/" + email; // Replace with your actual API endpoint

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl);
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
                        // Parse JSON response and extract first name and last name
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");

                        // Update UI in the main thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                firstNameTextView.setText(firstName);
                                lastNameTextView.setText(lastName);
                            }
                        });
                    } else {
                        // Handle unsuccessful response
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Failed to fetch user information. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    urlConnection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    // Handle exceptions
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "An error occurred while fetching user information.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
