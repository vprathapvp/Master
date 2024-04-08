package com.dhivakar.scrollimage;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SignupActivity extends AppCompatActivity {

    EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etFirstName = findViewById(R.id.firstname);
        etLastName = findViewById(R.id.lastname);
        etEmail = findViewById(R.id.emailid);
        etPassword = findViewById(R.id.regpassword);
        etConfirmPassword = findViewById(R.id.regconpassword);
        btnRegister = findViewById(R.id.newregisterbtn);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOTP();
            }
        });
    }

    private void sendOTP() {
        final String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(SignupActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate OTP
        final String otp = generateOTP();

        // Send OTP to email
        sendOTP(email, otp);

        // Display dialog for OTP verification
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_verify_otp, null);
        builder.setView(dialogView);

        final TextView tvOTPStatus = dialogView.findViewById(R.id.tvOTPStatus);
        final EditText etEnteredOTP = dialogView.findViewById(R.id.etEnteredOTP);

        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredOTP = etEnteredOTP.getText().toString().trim();
                if (enteredOTP.equals(otp)) {
                    // OTP verification successful
                    dialog.dismiss();
                    signUp();
                    Toast.makeText(SignupActivity.this, "OTP verified successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Incorrect OTP
                    tvOTPStatus.setText("Incorrect OTP. Please try again.");
                    etEnteredOTP.setText("");
                    Toast.makeText(SignupActivity.this, "Incorrect OTP. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private String generateOTP() {
        // Generate a random 6-digit OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void sendOTP(final String email, final String otp) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                // Replace these placeholders with your actual Gmail SMTP server details
                String host = "smtp.gmail.com";
                String port = "587"; // Gmail SMTP port
                String senderEmail = "dtubeotp@gmail.com"; // Your Gmail address
                String password = "ucwc psax cgxr uikx"; // Your Gmail password

                Properties properties = new Properties();
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.host", host);
                properties.put("mail.smtp.port", port);

                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, password);
                    }
                });

                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(senderEmail));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                    message.setSubject("OTP for Registering to D TUBE app");
                    message.setText("Your OTP is: " + otp);

                    Transport.send(message);

                    return true;
                } catch (MessagingException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    // Display a toast message indicating OTP sent successfully
                    Toast.makeText(SignupActivity.this, "OTP sent to your email", Toast.LENGTH_SHORT).show();
                } else {
                    // Display a toast message indicating failure to send OTP
                    Toast.makeText(SignupActivity.this, "Failed to send OTP. Please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void signUp() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            // Set text color to red
            etConfirmPassword.setTextColor(Color.RED);
            // Show toast message
            Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Reset text color to default
            etConfirmPassword.setTextColor(Color.BLACK);
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("firstName", firstName);
            jsonObject.put("lastName", lastName);
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SignUpTask().execute(jsonObject.toString());
    }


    private class SignUpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://192.168.87.71:8096/register");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(params[0].getBytes());
                out.flush();

                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
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
        protected void onPostExecute(String result) {
            if (result != null) {
                // Show toast message
                Toast.makeText(SignupActivity.this, result, Toast.LENGTH_SHORT).show();

                // Start LoginActivity
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Finish SignUpActivity to prevent going back to it when pressing back button from LoginActivity
            } else {
                // Show toast message for failure
                Toast.makeText(SignupActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
