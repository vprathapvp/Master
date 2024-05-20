package com.ngltech.bytes.ads;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ActivityNotFoundException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ngltech.bytes.R;
import com.ngltech.bytes.profile.UploadActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class YourAdActivity extends AppCompatActivity {

    private ListView listView;
    private EditText editTextgmail;
    private AdAdapter adapter;
    private ArrayList<String> adInfos;
    private String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        editTextgmail = findViewById(R.id.editTextgmail);
        listView = findViewById(R.id.listView);
        adInfos = new ArrayList<>();
        adapter = new AdAdapter();
        listView.setAdapter(adapter);

        // Set email to editTextgmail
        editTextgmail.setText(email);

        // Fetch ad infos directly when activity is created
        fetchAdInfos();
    }

    private void fetchAdInfos() {
        // Get email from editTextgmail
        email = editTextgmail.getText().toString().trim();

        // Check if email is empty
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with email included as a request parameter
                    URL url = new URL("http://192.168.184.71:8090/business/api/ads-by-email?email=" + email);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Retrieve token from SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    // Set authorization header with the token
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
                            String adId = jsonObject.getString("ad_id");
                            String adUrl = jsonObject.getString("ad_url");
                            String distanceRange = jsonObject.getString("distance_range");
                            String paymentAmount = jsonObject.getString("payment_amount");
                            String subscription = jsonObject.getString("subscription");
                            String adInfo = "Ad ID: " + adId + "\n" + "Ad URL: " + adUrl + "\n" + "Distance Range: " + distanceRange + "\n" + "Payment Amount: " + paymentAmount + "\n" + "Subscription: " + subscription;
                            adInfos.add(adInfo);
                        }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(YourAdActivity.this, "Failed to fetch ad information", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(YourAdActivity.this, "Failed to fetch ad information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private class AdAdapter extends ArrayAdapter<String> {

        public AdAdapter() {
            super(YourAdActivity.this, 0, adInfos);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.ad_item, parent, false);
            }

            final String adInfo = getItem(position);

            TextView adIdTextView = convertView.findViewById(R.id.adIdTextView);
            TextView adUrlTextView = convertView.findViewById(R.id.adUrlTextView);
            TextView distanceRangeTextView = convertView.findViewById(R.id.distanceRangeValueTextView);
            TextView paymentAmountTextView = convertView.findViewById(R.id.paymentAmountValueTextView);
            TextView subscriptionTextView = convertView.findViewById(R.id.subscriptionValueTextView);
            Button editButton = convertView.findViewById(R.id.editButton);
            Button deleteButton = convertView.findViewById(R.id.deleteButton);

            // Split the adInfo string into parts
            String[] parts = adInfo.split("\n");

            // Extract ad details from the parts
            String adId = parts[0].substring(parts[0].indexOf(": ") + 2);
            final String adUrl = parts[1].substring(parts[1].indexOf(": ") + 2);
            String distanceRange = parts[2].substring(parts[2].indexOf(": ") + 2);
            String paymentAmount = parts[3].substring(parts[3].indexOf(": ") + 2);
            String subscription = parts[4].substring(parts[4].indexOf(": ") + 2);

            // Set ad details to the TextViews
            adIdTextView.setText(adId);
            adUrlTextView.setText(adUrl);
            distanceRangeTextView.setText(distanceRange);
            paymentAmountTextView.setText(paymentAmount);
            subscriptionTextView.setText(subscription);

            // Edit Button Click Listener
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEditDialog(position);
                }
            });

            // Delete Button Click Listener
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteAd(position);
                }
            });

            adUrlTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewAd(adUrl);
                }
            });

            return convertView;
        }
    }

    private void viewAd(String adUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(adUrl), "text/html");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found to view the ad", Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteAd(final int position) {
        String adInfo = adInfos.get(position);
        String[] parts = adInfo.split("\n");
        String adId = parts[0].substring(parts[0].indexOf(": ") + 2);

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://192.168.184.71:8090/business/api/ad/" + adId);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setRequestProperty("Authorization", "Bearer " + token);

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Check for success (200) response
                        adInfos.remove(position);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                Toast.makeText(YourAdActivity.this, "Ad deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        // Handle case where ad is not found
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(YourAdActivity.this, "Ad not found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Handle other response codes
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(YourAdActivity.this, "Failed to delete ad: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(YourAdActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }


    private String extractVideoId(String videoUrl) {
        String[] parts = videoUrl.split("/");
        return parts[parts.length - 1];
    }

    private void openEditDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(YourAdActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.edit_ad_dialog, null);
        final EditText editTextSubscription = dialogView.findViewById(R.id.editTextSubscription);
        final EditText editTextDistanceRange = dialogView.findViewById(R.id.editTextDistanceRange);
        final EditText editTextLatitude = dialogView.findViewById(R.id.editTextLatitude);
        final EditText editTextLongitude = dialogView.findViewById(R.id.editTextLongitude);
        final EditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        String adInfo = adInfos.get(position);
        String[] parts = adInfo.split("\n");
        String subscription = parts[4].substring(parts[4].indexOf(": ") + 2);
        String distanceRange = parts[2].substring(parts[2].indexOf(": ") + 2);
        editTextSubscription.setText(subscription);
        editTextDistanceRange.setText(distanceRange);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedSubscription = editTextSubscription.getText().toString().trim();
                String updatedDistanceRange = editTextDistanceRange.getText().toString().trim();
                String updatedLatitude = editTextLatitude.getText().toString().trim();
                String updatedLongitude = editTextLongitude.getText().toString().trim();
                String updatedEmail = editTextEmail.getText().toString().trim();
                updateAdDetails(position, updatedSubscription, updatedDistanceRange, updatedEmail);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateAdDetails(final int position, final String subscription, final String distanceRange, final String email) {
        String adInfo = adInfos.get(position);
        String[] parts = adInfo.split("\n");
        String adId = parts[0].substring(parts[0].indexOf(": ") + 2);

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Construct the URL with adId and query parameters
                    String urlString = "http://192.168.184.71:8090/business/update-ad/" + adId +
                            "?subscription=" + subscription +
                            "&distanceRange=" + distanceRange +
                            "&latitude=" + "your_latitude_here" +
                            "&longitude=" + "your_longitude_here" +
                            "&email=" + email;
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");
                    connection.setRequestProperty("Authorization", "Bearer " + token);

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(YourAdActivity.this, "Ad details updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(YourAdActivity.this, "Failed to update ad details: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(YourAdActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}