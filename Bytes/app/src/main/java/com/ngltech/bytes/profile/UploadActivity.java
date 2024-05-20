package com.ngltech.bytes.profile;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ngltech.bytes.R;
import com.ngltech.bytes.Config;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {

    private ListView listView;
    private EditText editTextgmail;
    private VideoAdapter adapter;
    private ArrayList<String> videoInfos;
    private String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Retrieve email from FragmentProfile
        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        editTextgmail = findViewById(R.id.editTextgmail);
        listView = findViewById(R.id.listView);
        videoInfos = new ArrayList<>();
        adapter = new VideoAdapter();
        listView.setAdapter(adapter);

        // Set email to editTextgmail
        editTextgmail.setText(email);

        // Fetch video infos directly when activity is created
        fetchVideoInfos();
    }

    private void fetchVideoInfos() {
        // Get email from editTextgmail
        email = editTextgmail.getText().toString().trim();

        // Check if email is empty
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with email included as a request parameter
                    URL url = new URL(Config.BASE_URL + "/api/bytes/byEmail?email=" + email);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Retrieve token from SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    // Set authorization header with the token
                    connection.setRequestProperty("Authorization", "Bearer " + token);

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONArray jsonArray = new JSONArray(response.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String id = jsonObject.getString("id");
                            String name = jsonObject.getString("name");
                            String fileUrl = jsonObject.getString("url");
                            String description = jsonObject.getString("description");
                            String videoInfo = "ID: " + id + "\n" + "Name: " + name + "\n" + "Download URL: " + fileUrl + "\n" + "Description: " + description;
                            videoInfos.add(videoInfo);
                        }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UploadActivity.this, "Failed to fetch video information", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UploadActivity.this, "Failed to fetch video information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private class VideoAdapter extends ArrayAdapter<String> {

        public VideoAdapter() {
            super(UploadActivity.this, 0, videoInfos);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.video_item, parent, false);
            }

            final String videoInfo = getItem(position);

            TextView videoNameTextView = convertView.findViewById(R.id.nameTextView);
            TextView videoDescriptionTextView = convertView.findViewById(R.id.descriptionTextView);
            Button deleteButton = convertView.findViewById(R.id.deleteButton);
            Button editButton = convertView.findViewById(R.id.editButton);

            // Split the videoInfo string into parts
            String[] parts = videoInfo.split("\n");

            // Extract video name and description from the parts
            String name = parts[1].substring(parts[1].indexOf(": ") + 2);
            String description = parts[3].substring(parts[3].indexOf(": ") + 2);

            // Set video name and description to the TextViews
            videoNameTextView.setText(name);
            videoDescriptionTextView.setText(description);

            videoNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideo(videoInfo);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteVideo(position);
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEditDialog(position);
                }
            });

            return convertView;
        }
    }

    private void playVideo(String videoInfo) {
        String[] parts = videoInfo.split("\n");
        String actualVideoUrl = parts[2].substring(parts[2].indexOf(": ") + 2);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(actualVideoUrl), "text/html");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found to view the video", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteVideo(final int position) {
        String videoInfo = videoInfos.get(position);
        String[] parts = videoInfo.split("\n");
        String videoUrl = parts[2].substring(parts[2].indexOf(": ") + 2);
        String videoId = extractVideoId(videoUrl);

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Config.BASE_URL + "/api/delete-bytes/" + videoId);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");

                    // Set authorization header with the token
                    connection.setRequestProperty("Authorization", "Bearer " + token);

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        // Check for success (200) or no content (204) response
                        videoInfos.remove(position);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                Toast.makeText(UploadActivity.this, "Video deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Handle other response codes
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UploadActivity.this, "Failed to delete video: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UploadActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private String extractVideoId(String videoUrl) {
        String[] parts = videoUrl.split("/");
        return parts[parts.length - 1];
    }

    private void openEditDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.edit_video_dialog, null);
        final EditText editTextVideoName = dialogView.findViewById(R.id.editTextVideoName);
        final EditText editTextVideoDescription = dialogView.findViewById(R.id.editTextVideoDescription);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        String videoInfo = videoInfos.get(position);
        String[] parts = videoInfo.split("\n");
        String videoName = parts[1].substring(parts[1].indexOf(": ") + 2);
        String videoDescription = parts[3].substring(parts[3].indexOf(": ") + 2);
        editTextVideoName.setText(videoName);
        editTextVideoDescription.setText(videoDescription);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedVideoName = editTextVideoName.getText().toString().trim();
                String updatedVideoDescription = editTextVideoDescription.getText().toString().trim();
                updateVideoDetails(position, updatedVideoName, updatedVideoDescription);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateVideoDetails(final int position, final String updatedVideoName, final String updatedVideoDescription) {
        String videoInfo = videoInfos.get(position);
        String[] parts = videoInfo.split("\n");
        String videoUrl = parts[2].substring(parts[2].indexOf(": ") + 2);
        final String videoId = extractVideoId(videoUrl);

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Config.BASE_URL + "/api/update-details/" + videoId);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer " + token); // Add Authorization header

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("name", updatedVideoName);
                    requestBody.put("description", updatedVideoDescription);

                    connection.setDoOutput(true);
                    connection.getOutputStream().write(requestBody.toString().getBytes());

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        videoInfos.set(position, "ID: " + videoId + "\n" + "Name: " + updatedVideoName + "\n" + "Download URL: " + videoUrl + "\n" + "Description: " + updatedVideoDescription);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                Toast.makeText(UploadActivity.this, "Video details updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UploadActivity.this, "Failed to update video details: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UploadActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
