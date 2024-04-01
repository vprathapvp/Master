package com.dhivakar.scrollimage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private TextView usernameTextView; // Add this line
    private TextView emailTextView;
    private String imagePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImageView = rootView.findViewById(R.id.profile_image);
        usernameTextView = rootView.findViewById(R.id.usernameTextView);
        emailTextView = rootView.findViewById(R.id.emailTextView);


        ImageButton editImageButton = rootView.findViewById(R.id.edit_image_button);
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open gallery to select an image
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        // Load saved image if available
        loadImageFromInternalStorage();

        // Find the TextView for the playlist
        TextView playlistTextView = rootView.findViewById(R.id.playlist);
        // Set onClick listener to navigate to PlaylistActivity
        playlistTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start PlaylistActivity
                startActivity(new Intent(getActivity(), PlaylistActivity.class));
            }
        });

        // Find the TextView for the download
        TextView downloadsTextView = rootView.findViewById(R.id.downloads);
        // Set onClick listener to navigate to DownloadActivity
        downloadsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start DownloadActivity
                startActivity(new Intent(getActivity(), DownloadActivity.class));
            }
        });

        // Find the upload for the download
        TextView uploadsTextView = rootView.findViewById(R.id.uploads);
        // Set onClick listener to navigate to upload
        uploadsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start upload
                startActivity(new Intent(getActivity(), UploadActivity.class));
            }
        });

        // Find the logout
        TextView logoutTextView = rootView.findViewById(R.id.logout);
        // Set onClick listener to navigate to logout
        logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start logout
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        return rootView;
    }
    private void updateUserInfo(String username, String email) {
        usernameTextView.setText(username);
        emailTextView.setText(email);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                // Set the new image
                profileImageView.setImageBitmap(bitmap);
                // Save the image to internal storage
                saveImageToInternalStorage(bitmap);
                // Set the rounded shape
                setRoundedShape();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to save image to internal storage
    private void saveImageToInternalStorage(Bitmap bitmap) {
        Context context = getActivity();
        if (context == null) return;

        File directory = context.getFilesDir();
        File file = new File(directory, "profile_image.jpg");
        imagePath = file.getAbsolutePath();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load image from internal storage
    private void loadImageFromInternalStorage() {
        Context context = getActivity();
        if (context == null) return;

        File file = new File(context.getFilesDir(), "profile_image.jpg");
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profileImageView.setImageBitmap(bitmap);
            setRoundedShape();
        }
    }

    // Method to set rounded shape
    private void setRoundedShape() {
        Drawable originalDrawable = profileImageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) originalDrawable).getBitmap();
        RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        roundedDrawable.setCircular(true);
        profileImageView.setImageDrawable(roundedDrawable);
    }

}
