package com.example.myapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CommentFragment extends Activity {

    private EditText commentEditText;
    private Button postCommentButton;
    private ListView commentListView;

    private ArrayList<String> commentsList;
    private ArrayAdapter<String> commentsAdapter;
    private SharedPreferences sharedPreferences;
    private String imageIdentifier; // Unique identifier for the image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_comment);

        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);
        commentListView = findViewById(R.id.commentListView);

        // Get the unique identifier for the image from the intent
        imageIdentifier = getIntent().getStringExtra("imageIdentifier");

        // Initialize SharedPreferences with a unique key for each image
        sharedPreferences = getPreferences(MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("Comments_" + imageIdentifier, MODE_PRIVATE);

        // Load comments from SharedPreferences
        commentsList = new ArrayList<>(loadComments());
        commentsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, commentsList);
        commentListView.setAdapter(commentsAdapter);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the comment from the EditText
                String newComment = commentEditText.getText().toString();

                // Check if the comment is not empty
                if (!newComment.isEmpty()) {
                    // Add the comment to the list
                    commentsList.add(newComment);

                    // Save comments to SharedPreferences
                    saveComments(new HashSet<>(commentsList));

                    // Notify the adapter that the data set has changed
                    commentsAdapter.notifyDataSetChanged();

                    // Clear the EditText after posting the comment
                    commentEditText.setText("");
                }
            }
        });
    }

    // Save comments to SharedPreferences
    private void saveComments(Set<String> commentsSet) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("comments", commentsSet);
        editor.apply();
    }

    // Load comments from SharedPreferences
    private Set<String> loadComments() {
        return sharedPreferences.getStringSet("comments", new HashSet<>());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload comments when the activity is resumed
        commentsList.clear();
        commentsList.addAll(loadComments());
        commentsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        // Save comments before finishing the activity
        saveComments(new HashSet<>(commentsList));
        super.onBackPressed();
    }
}
