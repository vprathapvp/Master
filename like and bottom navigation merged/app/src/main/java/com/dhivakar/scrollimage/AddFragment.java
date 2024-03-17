
package com.dhivakar.scrollimage;
import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddFragment extends Fragment {

    private static final int REQUEST_CODE = 100;
    private static final int REQUEST_PERMISSIONS = 1001;

    private Button selectVideoButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add, container, false);

        selectVideoButton = rootView.findViewById(R.id.selectVideoButton);
        selectVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVideo();
            }
        });

        // Request runtime permissions for external storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            }
        }

        return rootView;
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri videoUri = data.getData();
                if (videoUri != null) {
                    saveVideoToStorage(videoUri);
                } else {
                    Toast.makeText(getActivity(), "Failed to get video URI", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveVideoToStorage(Uri videoUri) {
        try {
            ContentResolver contentResolver = getActivity().getContentResolver();
            String videoFileName = getFileNameFromUri(contentResolver, videoUri);
            if (videoFileName == null) {
                videoFileName = "video_" + System.currentTimeMillis() + ".mp4"; // Generate a random name
            }

            // Specify the custom folder name
            String customFolderName = "Dhivakar";
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), customFolderName);
            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e("SaveVideo", "Error creating directory");
                    Toast.makeText(getActivity(), "Error creating directory", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            File outputFile = new File(storageDir, videoFileName);

            InputStream inputStream = contentResolver.openInputStream(videoUri);
            OutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            Toast.makeText(getActivity(), "Video saved: " + outputFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("SaveVideo", "Error saving video: " + e.getMessage());
            Toast.makeText(getActivity(), "Error saving video", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(ContentResolver contentResolver, Uri uri) {
        String fileName = null;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            fileName = cursor.getString(index);
            cursor.close();
        }
        return fileName;
    }
}