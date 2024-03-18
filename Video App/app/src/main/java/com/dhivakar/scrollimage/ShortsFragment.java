package com.dhivakar.scrollimage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import androidx.fragment.app.Fragment;

public class ShortsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shorts, container, false);

        // Initialize VideoViews
        VideoView videoView1 = view.findViewById(R.id.s1);

        // Set video paths
        videoView1.setVideoPath("/storage/emulated/0/Movies/Dhivakar/shorts1.mp4");


        // Initialize separate MediaControllers
        MediaController mediaController1 = new MediaController(requireContext());


        // Attach separate MediaControllers to VideoViews
        videoView1.setMediaController(mediaController1);


        // Set anchor views to avoid overlapping controls
        mediaController1.setAnchorView(videoView1);

        // Start playing videos



        return view;
    }
}
