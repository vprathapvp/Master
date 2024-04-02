package com.dhivakar.scrollimage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileFragment extends Fragment {
    private ImageView profileImageView;
    private String imagePath;
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
        profileImageView = rootView.findViewById(R.id.profile_image);

        // Retrieve email from MainActivity
        String email = getActivity().getIntent().getStringExtra("email");
        emailEditText.setText(email);

        // Load saved image if available
        loadImageFromInternalStorage();

        // Fetch user info using the retrieved email
        fetchUserInfo(email);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Your onActivityResult code if needed
    }

    // Method to save image to internal storage
    private void saveImageToInternalStorage(Bitmap bitmap) {
        Context context = getActivity();
        if (context == null) return;

        File directory = context.getFilesDir();
        File file = new File(directory, "profile_image.jpg");
        imagePath = file.getAbsolutePath();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load image from internal storage
    private void loadImageFromInternalStorage() {
        Context context = getActivity();
        if (context == null) return;

        File file = new File(context.getFilesDir(), "profile_image.jpg");
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profileImageView.setImageBitmap(bitmap);
            setRoundedShape();
        }
    }

    // Method to set rounded shape
    private void setRoundedShape() {
        Drawable originalDrawable = profileImageView.getDrawable();
        if (originalDrawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) originalDrawable).getBitmap();
            RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedDrawable.setCircular(true);
            profileImageView.setImageDrawable(roundedDrawable);
        } else {
            // Handle the case where the drawable is not a BitmapDrawable
            // You can provide a default drawable or log an error
            // For now, let's just log a message
            Log.e("ProfileFragment", "Drawable is not a BitmapDrawable");
        }
    }

    private void fetchUserInfo(String email) {
        String apiUrl = "http://192.168.87.71:8097/user/" + email; // Replace with your actual API endpoint

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
