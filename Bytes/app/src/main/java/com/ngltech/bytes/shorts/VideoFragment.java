package com.ngltech.bytes.shorts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ngltech.bytes.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VideoFragment extends Fragment {

    // Constants for argument keys
    private static final String ARG_VIDEO_PATH = "video_path";
    private static final String ARG_NAME = "name";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_VIDEO_ID = "video_id";
    private static final String ARG_FIRST_NAME = "first_name";
    private static final String ARG_LAST_NAME = "last_name";
    private static final String ARG_TOKEN = "token";

    // Member variables
    private String videoPath;
    private String name;
    private String description;
    private String videoId;
    private String firstName;
    private String lastName;
    private String token;

    private VideoView videoView;
    private MediaController mediaController;
    private ImageButton likeButton;
    private ImageButton shareButton;
    private ImageButton downloadButton; // Add download button

    private static final int REQUEST_STORAGE_PERMISSION = 1;

    public static VideoFragment newInstance(String videoPath, String name, String description, String videoId, String firstName, String lastName, String token) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_PATH, videoPath);
        args.putString(ARG_NAME, name);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_VIDEO_ID, videoId);
        args.putString(ARG_FIRST_NAME, firstName);
        args.putString(ARG_LAST_NAME, lastName);
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoPath = getArguments().getString(ARG_VIDEO_PATH);
            name = getArguments().getString(ARG_NAME);
            description = getArguments().getString(ARG_DESCRIPTION);
            videoId = getArguments().getString(ARG_VIDEO_ID);
            firstName = getArguments().getString(ARG_FIRST_NAME);
            lastName = getArguments().getString(ARG_LAST_NAME);
            token = getArguments().getString(ARG_TOKEN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        videoView = view.findViewById(R.id.videoView);
        mediaController = new MediaController(requireContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Set video URI
        videoView.setVideoURI(Uri.parse(videoPath));

        // Set loop mode
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true); // Play video in loop mode
                mediaPlayer.start(); // Start video playback
            }
        });

        // Set up UI elements
        TextView tvName = view.findViewById(R.id.tvname);
        tvName.setText(name);

        TextView tvDescription = view.findViewById(R.id.tvdescription);
        tvDescription.setText(description);

        TextView tvFirstName = view.findViewById(R.id.tvfname);
        tvFirstName.setText(firstName); // Set first name

        TextView tvLastName = view.findViewById(R.id.tvlname);
        tvLastName.setText(lastName); // Set last name

        // Like button setup
        likeButton = view.findViewById(R.id.btnLike);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeVideo();
            }
        });

        // Share button setup
        shareButton = view.findViewById(R.id.btnShare);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareVideo();
            }
        });

        // Download button setup
        downloadButton = view.findViewById(R.id.btnDownload);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadVideo();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    private String getAuthorizationToken() {
        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }

    private void likeVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Make an HTTP POST request to like the video
                    URL url = new URL("http://192.168.184.71:8090/api/like/" + videoId);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");

                    // Set Authorization header with the token
                    connection.setRequestProperty("Authorization", "Bearer " + token);

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update UI to reflect successful like
                                Toast.makeText(requireContext(), "Video liked", Toast.LENGTH_SHORT).show();
                                // You can update UI elements here if needed
                            }
                        });
                    } else {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Handle failed like request
                                Toast.makeText(requireContext(), "Failed to like video", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Handle IOException
                            Toast.makeText(requireContext(), "Failed to like video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void shareVideo() {
        // Create an Intent to share the video URL
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, videoPath);
        startActivity(Intent.createChooser(shareIntent, "Share Video"));
    }

    private void downloadVideo() {
        // Check storage permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request permission
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            // Permission granted, proceed with download
            downloadVideoNow();
        }
    }

    private void downloadVideoNow() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://192.168.184.71:8090/api/bytes/" + videoId);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Add Authorization header with the token
                    connection.setRequestProperty("Authorization", "Bearer " + token);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String fileName = connection.getHeaderField("Content-Disposition");
                        fileName = fileName.substring(fileName.lastIndexOf("=") + 1);
                        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                        File outputFile = new File(directory, fileName);

                        InputStream inputStream = connection.getInputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        OutputStream outputStream = new FileOutputStream(outputFile);
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                        inputStream.close();

                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(requireContext(), "Video downloaded successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(requireContext(), "Failed to download video. Response code: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Failed to download video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

}
