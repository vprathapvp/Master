package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Declare EditText globally
    private EditText commentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assuming you have references to your ImageButtons
        ImageButton commentButton1 = findViewById(R.id.imageButton1);
        ImageButton commentButton2 = findViewById(R.id.imageButton2);
        ImageButton commentButton3 = findViewById(R.id.imageButton3);
        ImageButton commentButton4 = findViewById(R.id.imageButton4);

        // Set OnClickListener for each comment button
        // Set OnClickListener for each comment button
        commentButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog("image1"); // Replace "image1" with the actual identifier for the first image
            }
        });

        commentButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog("image2"); // Replace "image2" with the actual identifier for the second image
            }
        });

        commentButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog("image3"); // Replace "image3" with the actual identifier for the third image
            }
        });

        commentButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog("image4"); // Replace "image4" with the actual identifier for the fourth image
            }
        });

    }


    // Method to show the comment dialog
    private void showCommentDialog(String imageIdentifier) {
        Intent intent = new Intent(MainActivity.this, CommentFragment.class);
        intent.putExtra("imageIdentifier", imageIdentifier);
        startActivity(intent);
    }

}
