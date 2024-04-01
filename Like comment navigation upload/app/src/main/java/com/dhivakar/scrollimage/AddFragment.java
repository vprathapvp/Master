package com.dhivakar.scrollimage;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddFragment extends Fragment {

    private static final String TAG = "AddFragment";
    private static final int REQUEST_CODE_VIDEO_PICK = 101;
    // Update SERVER_URL to point to your Spring Boot backend
    private static final String SERVER_URL = "http://192.168.2.62:8080/upload";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        // Launch intent to pick a video from the device
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_CODE_VIDEO_PICK);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VIDEO_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                new UploadVideoTask(this).execute(videoUri);
            } else {
                Log.e(TAG, "Video URI is null");
                Toast.makeText(getActivity(), "Failed to get video URI", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class UploadVideoTask extends AsyncTask<Uri, Void, String> {
        private final WeakReference<Fragment> fragmentRef;

        UploadVideoTask(Fragment fragment) {
            this.fragmentRef = new WeakReference<>(fragment);
        }

        @Override
        protected String doInBackground(Uri... uris) {
            Uri videoUri = uris[0];
            Fragment fragment = fragmentRef.get();
            if (fragment == null || fragment.getActivity() == null) {
                return "Fragment or Activity is null";
            }
            try {
                return uploadVideo(fragment.getActivity(), videoUri);
            } catch (IOException e) {
                e.printStackTrace();
                return "Failed to upload video: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, result);
            Fragment fragment = fragmentRef.get();
            if (fragment != null && fragment.getActivity() != null) {
                Toast.makeText(fragment.getActivity(), result, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static String uploadVideo(Activity activity, Uri videoUri) throws IOException {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        try {
            URL url = new URL(SERVER_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            String boundary = "-----" + Long.toHexString(System.currentTimeMillis());
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"video\"; filename=\"" + videoUri.getLastPathSegment() + "\"\r\n");
            outputStream.writeBytes("Content-Type: video/mp4\r\n\r\n");

            InputStream inputStream = null;
            try {
                inputStream = activity.getContentResolver().openInputStream(videoUri);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            outputStream.writeBytes("\r\n");
            outputStream.writeBytes("--" + boundary + "--\r\n");
            outputStream.flush();

            int serverResponseCode = connection.getResponseCode();
            if (serverResponseCode == HttpURLConnection.HTTP_OK) {
                return "Video uploaded successfully";
            } else {
                return "Failed to upload video. Server response code: " + serverResponseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException: " + e.getMessage();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
