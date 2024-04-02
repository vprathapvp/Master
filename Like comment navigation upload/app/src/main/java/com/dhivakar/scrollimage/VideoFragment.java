package com.dhivakar.scrollimage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class VideoFragment extends Fragment {

    private static final String ARG_VIDEO_PATH = "video_path";
    private String videoPath;
    private int likeCount;
    private int dislikeCount;
    private SharedPreferences sharedPreferences;
    private VideoView videoView;

    public static VideoFragment newInstance(String videoPath) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_PATH, videoPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoPath = getArguments().getString(ARG_VIDEO_PATH);
        }
        sharedPreferences = requireActivity().getSharedPreferences("Likes", Context.MODE_PRIVATE);
        likeCount = sharedPreferences.getInt(videoPath + "_like", 0);
        dislikeCount = sharedPreferences.getInt(videoPath + "_dislike", 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        videoView = view.findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse(videoPath));
        MediaController mediaController = new MediaController(requireContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Set video playback listener
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // Start video playback when prepared
                videoView.start();
            }
        });

        final TextView likeCountTextView = view.findViewById(R.id.likeCount);
        likeCountTextView.setText(String.valueOf(likeCount));

        final TextView dislikeCountTextView = view.findViewById(R.id.dislikeCount);
        dislikeCountTextView.setText(String.valueOf(dislikeCount));

        // Like Button
        final ImageButton btnLike = view.findViewById(R.id.btnLike);
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCount++;
                likeCountTextView.setText(String.valueOf(likeCount));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(videoPath + "_like", likeCount);
                editor.apply();
            }
        });

        // Dislike Button
        final ImageButton btnDislike = view.findViewById(R.id.btnDislike);
        btnDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeCount++;
                dislikeCountTextView.setText(String.valueOf(dislikeCount));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(videoPath + "_dislike", dislikeCount);
                editor.apply();
            }
        });

        // Share Button
        ImageButton btnShare = view.findViewById(R.id.btnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("video/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoPath));
                startActivity(Intent.createChooser(shareIntent, "Share video using"));
            }
        });

        // Download Button
        ImageButton btnDownload = view.findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadVideo();
            }
        });

        // Playlist Button setup inside onCreateView
        ImageButton btnPlaylist = view.findViewById(R.id.btnPlaylist);
        btnPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlist(); // Call the playlist method when the button is clicked
            }
        });

        return view;
    }

    // Download video method
    private void downloadVideo() {
        // Set the directory where the video will be saved
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "ShortsDownloads");

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                // Unable to create directory
                Toast.makeText(requireContext(), "Failed to create directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Extract filename from video path
        String filename = videoPath.substring(videoPath.lastIndexOf("/") + 1);

        // Destination file
        File destFile = new File(directory, filename);

        try {
            // Copy video file to the destination directory
            FileInputStream inputStream = new FileInputStream(new File(videoPath));
            FileOutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Show toast message on successful download
            Toast.makeText(requireContext(), "Your Video Download Successful", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show toast message with error details
            Toast.makeText(requireContext(), "Download Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Playlist video method inside VideoFragment class
    private void playlist() {
        // Set the directory where the video will be saved
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "My Playlist");

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                // Unable to create directory
                Toast.makeText(requireContext(), "Failed to create directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Extract filename from video path
        String filename = videoPath.substring(videoPath.lastIndexOf("/") + 1);

        // Destination file
        File destFile = new File(directory, filename);

        try {
            // Copy video file to the destination directory
            FileInputStream inputStream = new FileInputStream(new File(videoPath));
            FileOutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Show toast message on successful download
            Toast.makeText(requireContext(), "Video added to the Playlist", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show toast message with error details
            Toast.makeText(requireContext(), "Failed to add Video in Playlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}