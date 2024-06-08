package com.ngltech.bytes.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.VideoView;

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
import java.util.List;
import java.util.Random;

public class AdsActivity extends AppCompatActivity {

    private VideoView videoView;
    private List<String> adUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);

        videoView = findViewById(R.id.videoView);
        // Disable touch events for the VideoView
        videoView.setOnTouchListener((v, event) -> true);

        // Fetch ad URLs from the Spring Boot backend
        fetchAdUrls();
    }

    @Override
    public void onBackPressed() {
        // Check if the video is playing
        if (videoView.isPlaying()) {
            // Ignore back button press
            return;
        }
        super.onBackPressed();
    }

    private void fetchAdUrls() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Retrieve token from SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    // Make an HTTP GET request to fetch ad details from the Spring Boot backend
                    URL url = new URL(Config.BASE_URL + "/business/api/ads");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Set Authorization header with the token
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
                            String adUrl = jsonObject.getString("ad_url");

                            // Add ad URL to the list
                            adUrls.add(adUrl);
                        }

                        // Play a random ad after fetching ad URLs
                        playRandomAd();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AdsActivity.this, "Failed to fetch ad details", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AdsActivity.this, "Failed to fetch ad details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void playRandomAd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!adUrls.isEmpty()) {
                        // Choose a random ad URL
                        Random random = new Random();
                        int index = random.nextInt(adUrls.size());
                        String adUrl = adUrls.get(index);

                        // Set the video path to the VideoView
                        videoView.setVideoPath(adUrl);
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                // Finish the activity after the ad is fully played
                                finish();
                            }
                        });
                        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                                Toast.makeText(AdsActivity.this, "Error occurred while playing the video", Toast.LENGTH_SHORT).show();
                                finish();
                                return true;
                            }
                        });
                        videoView.start();
                    } else {
                        Toast.makeText(AdsActivity.this, "No ad URLs available", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AdsActivity.this, "Error occurred while playing the video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
