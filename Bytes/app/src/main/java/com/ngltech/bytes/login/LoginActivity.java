package com.ngltech.bytes.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ngltech.bytes.ads.AdUserActivity;
import com.ngltech.bytes.MainActivity;
import com.ngltech.bytes.R;
import com.ngltech.bytes.signup.SignupActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin, btnRegister;
    Spinner roleSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.Password);
        btnLogin = findViewById(R.id.loginbtn);
        btnRegister = findViewById(R.id.registerbtn);
        roleSpinner = findViewById(R.id.roleSpinner);

        String[] options = {"User", "Advertiser"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        roleSpinner.setAdapter(adapter);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Display the selected item
                String selectedRole = parent.getItemAtPosition(position).toString();
                if (!selectedRole.equals("Select Role")) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        TextView forgotPasswordTextView = findViewById(R.id.forgotPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the ForgotPasswordActivity
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String selectedRole = roleSpinner.getSelectedItem().toString();
        String email = etUsername.getText().toString().trim(); // Fetch email from email EditText

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", username); // Assuming email is used as username
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new LoginTask(selectedRole, email).execute(jsonObject.toString());
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        private final String selectedRole;
        private final String email;

        public LoginTask(String selectedRole, String email) {
            this.selectedRole = selectedRole;
            this.email = email;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://192.168.184.71:8090/profile/login");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(params[0].getBytes());
                out.flush();
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }


        @Override
        protected void onPostExecute(String token) {
            if (token != null) {
                // Save token to SharedPreferences or any other preferred storage method
                saveToken(token);

                // Show toast message
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                // If selected role is "Advertiser", start AdUserActivity
                if (selectedRole.equals("User")) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("email", email); // Pass email to AdUserActivity
                    startActivity(intent);
                } else if (selectedRole.equals("Advertiser")) {
                    // Pass the email to MainActivity
                    Intent intent = new Intent(LoginActivity.this, AdUserActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                }

                finish(); // Finish LoginActivity to prevent going back to it when pressing back button from MainActivity or AdUserActivity
            } else {
                // Show toast message for failure
                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        }

    }

        private void saveToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }
}
