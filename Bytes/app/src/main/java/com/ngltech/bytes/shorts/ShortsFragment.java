package com.ngltech.bytes.shorts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.ngltech.bytes.R;
import com.ngltech.bytes.Config;
import com.ngltech.bytes.ads.AdsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShortsFragment extends Fragment {

    private ViewPager2 viewPager;
    private List<VideoDetails> videoDetailsList = new ArrayList<>();
    private VideoPagerAdapter pagerAdapter;

    private AppCompatImageButton searchButton;
    private int swipeCounter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shorts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.viewPager);
        pagerAdapter = new VideoPagerAdapter(this, videoDetailsList);
        viewPager.setAdapter(pagerAdapter);

        fetchVideoDetails();

        searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                swipeCounter++;
                if (swipeCounter >= 9) {
                    swipeCounter = 0;
                    Intent intent = new Intent(requireContext(), AdsActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void fetchVideoDetails() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL(Config.BASE_URL + "/api/bytes");
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
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String videoId = jsonObject.getString("id");
                            String videoUrl = jsonObject.getString("url");
                            String name = jsonObject.getString("name");
                            String description = jsonObject.getString("description");
                            String firstName = jsonObject.getString("firstname");
                            String lastName = jsonObject.getString("lastname");

                            VideoDetails videoDetails = new VideoDetails(videoId, videoUrl, name, description, firstName, lastName);
                            videoDetailsList.add(videoDetails);
                        }

                        Collections.shuffle(videoDetailsList);

                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pagerAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(requireContext(), "Failed to fetch video details", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Failed to fetch video details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private static class VideoDetails {
        private String id;
        private String url;
        private String name;
        private String description;
        private String firstName;
        private String lastName;

        public VideoDetails(String id, String url, String name, String description, String firstName, String lastName) {
            this.id = id;
            this.url = url;
            this.name = name;
            this.description = description;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

    private static class VideoPagerAdapter extends FragmentStateAdapter {

        private final List<VideoDetails> videoDetailsList;

        public VideoPagerAdapter(@NonNull Fragment fragment, List<VideoDetails> videoDetailsList) {
            super(fragment);
            this.videoDetailsList = videoDetailsList;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position >= 0 && position < videoDetailsList.size()) {
                VideoDetails videoDetails = videoDetailsList.get(position);
                return VideoFragment.newInstance(videoDetails.getUrl(), videoDetails.getName(), videoDetails.getDescription(),
                        videoDetails.getId(), videoDetails.getFirstName(), videoDetails.getLastName(), "");
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
