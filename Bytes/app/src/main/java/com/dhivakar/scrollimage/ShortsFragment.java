package com.dhivakar.scrollimage;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShortsFragment extends Fragment {

    private ViewPager2 viewPager;
    private List<String> shortsVideoPaths = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shorts, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.viewPager);
        loadShortsVideos();
        viewPager.setAdapter(new VideoPagerAdapter(this, shortsVideoPaths));
    }

    private void loadShortsVideos() {
        shortsVideoPaths.clear();
        File shortsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "shorts");
        if (shortsFolder.exists() && shortsFolder.isDirectory()) {
            File[] files = shortsFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp4")) {
                        shortsVideoPaths.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private static class VideoPagerAdapter extends FragmentStateAdapter {

        private final List<String> videoPaths;

        public VideoPagerAdapter(@NonNull Fragment fragment, List<String> videoPaths) {
            super(fragment);
            this.videoPaths = videoPaths;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position >= 0 && position < videoPaths.size()) {
                return VideoFragment.newInstance(videoPaths.get(position));
            } else {
                return null;
            }
        }

        @Override
        public int getItemCount() {
            return videoPaths.size();
        }
    }
}
