package com.ngltech.bytes.ads;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.ngltech.bytes.R;

public class VideoPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        // Get the URI of the selected video from the intent
        String videoUriString = getIntent().getStringExtra("videoUri");
        Uri videoUri = Uri.parse(videoUriString);

        // Initialize VideoView
        VideoView videoView = findViewById(R.id.videoView);
        videoView.setVideoURI(videoUri);

        // Set up media controller to control playback
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Start video playback
        videoView.start();
    }
}
