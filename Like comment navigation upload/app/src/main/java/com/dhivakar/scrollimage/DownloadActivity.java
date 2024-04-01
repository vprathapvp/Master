package com.dhivakar.scrollimage;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> videoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        listView = findViewById(R.id.listView);
        videoList = new ArrayList<>();

        // Path to your "download" folder
        String path = "/storage/emulated/0/Movies/ShortsDownloads";
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".mp4")) { // Check if it's a video file
                    videoList.add(file.getName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, videoList);
        listView.setAdapter(adapter);

        // Set item click listener to play the selected video
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String videoPath = path + "/" + videoList.get(position);
            playVideo(videoPath);
        });
    }

    private void playVideo(String videoPath) {
        VideoView videoView = new VideoView(this);
        videoView.setVideoURI(Uri.parse(videoPath));
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        setContentView(videoView);
        videoView.start();
    }
}
