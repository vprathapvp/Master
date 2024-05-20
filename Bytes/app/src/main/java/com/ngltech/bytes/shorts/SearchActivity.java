package com.ngltech.bytes.shorts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button searchButton;
    private ViewPager2 viewPager;
    private VideoPagerAdapter pagerAdapter;
    private List<VideoDetails> videoDetailsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        viewPager = findViewById(R.id.viewPager);

        videoDetailsList = new ArrayList<>();
        pagerAdapter = new VideoPagerAdapter(this, videoDetailsList);
        viewPager.setAdapter(pagerAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = searchEditText.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    searchVideos(keyword);
                } else {
                    Toast.makeText(SearchActivity.this, "Please enter a keyword", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Perform search when the activity is created
        String initialKeyword = searchEditText.getText().toString().trim();
        if (!initialKeyword.isEmpty()) {
            searchVideos(initialKeyword);
        }
    }

    private void searchVideos(String keyword) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL(Config.BASE_URL + "/api/search/" + keyword);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
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
                        List<VideoDetails> searchResults = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String videoUrl = jsonObject.getString("url");
                            String name = jsonObject.getString("name");
                            String description = jsonObject.getString("description");

                            // For other fields like likeCount, dislikeCount, etc., you can set default values if needed

                            VideoDetails videoDetails = new VideoDetails(videoUrl, name, description);
                            searchResults.add(videoDetails);
                        }

                        // Update UI with search results
                        SearchActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoDetailsList.clear();
                                videoDetailsList.addAll(searchResults);
                                pagerAdapter.notifyDataSetChanged();
                                viewPager.setCurrentItem(0); // Reset ViewPager to first position
                            }
                        });
                    } else {
                        SearchActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Failed to fetch search results", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    SearchActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Failed to search videos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public static class VideoDetails {
        private String videoUrl;
        private String name;
        private String description;

        public VideoDetails(String videoUrl, String name, String description) {
            this.videoUrl = videoUrl;
            this.name = name;
            this.description = description;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    private static class VideoPagerAdapter extends FragmentStateAdapter {

        private final List<VideoDetails> videoDetailsList;

        public VideoPagerAdapter(SearchActivity activity, List<VideoDetails> videoDetailsList) {
            super(activity);
            this.videoDetailsList = videoDetailsList;
        }

        @Override
        public Fragment createFragment(int position) {
            if (position >= 0 && position < videoDetailsList.size()) {
                VideoDetails videoDetails = videoDetailsList.get(position);
                return VideoFragment.newInstance(videoDetails.getVideoUrl(), videoDetails.getName(), videoDetails.getDescription(), "", "", "", "");
            } else {
                return null;
            }
        }

        @Override
        public int getItemCount() {
            return videoDetailsList.size();
        }
    }
}
