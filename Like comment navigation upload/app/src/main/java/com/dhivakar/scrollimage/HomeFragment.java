package com.dhivakar.scrollimage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private static final String COMMENT_PREF_NAME = "comment_prefs";
    private static final String COMMENT_KEY = "comments";
    private static final String LIKE_COUNT_KEY_PREFIX = "like_count_";
    private static final String DISLIKE_COUNT_KEY_PREFIX = "dislike_count_";
    private SharedPreferences sharedPreferences;

    private int likeCount1;
    private int dislikeCount1;
    private int likeCount2;
    private int dislikeCount2;
    private int likeCount3;
    private int dislikeCount3;
    private int likeCount4;
    private int dislikeCount4;

    private EditText editText1;
    private TextView commentTextView1;
    private EditText editText2;
    private TextView commentTextView2;
    private EditText editText3;
    private TextView commentTextView3;
    private EditText editText4;
    private TextView commentTextView4;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences(COMMENT_PREF_NAME, Context.MODE_PRIVATE);

        editText1 = view.findViewById(R.id.editText1);
        editText2 = view.findViewById(R.id.editText2);
        editText3 = view.findViewById(R.id.editText3);
        editText4 = view.findViewById(R.id.editText4);
        commentTextView1 = view.findViewById(R.id.commentTextView1);
        commentTextView2 = view.findViewById(R.id.commentTextView2);
        commentTextView3 = view.findViewById(R.id.commentTextView3);
        commentTextView4 = view.findViewById(R.id.commentTextView4);
        final Button commentButton1 = view.findViewById(R.id.commentButton1);
        final Button commentButton2 = view.findViewById(R.id.commentButton2);
        final Button commentButton3 = view.findViewById(R.id.commentButton3);
        final Button commentButton4 = view.findViewById(R.id.commentButton4);

        final Button allcommentButton1 = view.findViewById(R.id.allcommentButton1);
        final Button allcommentButton2 = view.findViewById(R.id.allcommentButton2);
        final Button allcommentButton3 = view.findViewById(R.id.allcommentButton3);
        final Button allcommentButton4 = view.findViewById(R.id.allcommentButton4);



        likeCount1 = sharedPreferences.getInt(LIKE_COUNT_KEY_PREFIX + "1", 0);
        dislikeCount1 = sharedPreferences.getInt(DISLIKE_COUNT_KEY_PREFIX + "1", 0);
        likeCount2 = sharedPreferences.getInt(LIKE_COUNT_KEY_PREFIX + "2", 0);
        dislikeCount2 = sharedPreferences.getInt(DISLIKE_COUNT_KEY_PREFIX + "2", 0);
        likeCount3 = sharedPreferences.getInt(LIKE_COUNT_KEY_PREFIX + "3", 0);
        dislikeCount3 = sharedPreferences.getInt(DISLIKE_COUNT_KEY_PREFIX + "3", 0);
        likeCount4 = sharedPreferences.getInt(LIKE_COUNT_KEY_PREFIX + "4", 0);
        dislikeCount4 = sharedPreferences.getInt(DISLIKE_COUNT_KEY_PREFIX + "4", 0);

        // Load existing comments from SharedPreferences
        loadComments();




        // Set onClickListener for commentButton1, commentButton2, commentButton3, and commentButton4 (for adding comments)
        commentButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment(editText1, commentTextView1);
            }
        });
        commentButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment(editText2, commentTextView2);
            }
        });
        commentButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment(editText3, commentTextView3);
            }
        });
        commentButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment(editText4, commentTextView4);
            }
        });

        // Set onClickListener for allcommentButton1, allcommentButton2, allcommentButton3, and allcommentButton4 (for toggling comment visibility)
        allcommentButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCommentVisibility(commentTextView1);
            }
        });
        allcommentButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCommentVisibility(commentTextView2);
            }
        });
        allcommentButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCommentVisibility(commentTextView3);
            }
        });
        allcommentButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCommentVisibility(commentTextView4);
            }
        });
    }

    // Method to show a toast message
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Method to add a comment to the specified TextView
    private void addComment(EditText editText, TextView commentTextView) {
        String comment = editText.getText().toString().trim();
        if (!comment.isEmpty()) {
            commentTextView.append(comment + "\n");
            editText.setText("");
            // Save comments to SharedPreferences
            saveComments();
            showToast("Comment added Successfully");
        } else {
            showToast("Please type a comment");
        }
    }

    // Method to toggle the visibility of a TextView
    private void toggleCommentVisibility(TextView commentTextView) {
        // Toggle visibility
        if (commentTextView.getVisibility() == View.VISIBLE) {
            commentTextView.setVisibility(View.GONE);
        } else {
            commentTextView.setVisibility(View.VISIBLE);
        }
    }

    // Method to save comments to SharedPreferences
    private void saveComments() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(COMMENT_KEY + "1", commentTextView1.getText().toString());
        editor.putString(COMMENT_KEY + "2", commentTextView2.getText().toString());
        editor.putString(COMMENT_KEY + "3", commentTextView3.getText().toString());
        editor.putString(COMMENT_KEY + "4", commentTextView4.getText().toString());
        editor.apply();
    }

    // Method to load comments from SharedPreferences
    private void loadComments() {
        String comments1 = sharedPreferences.getString(COMMENT_KEY + "1", "");
        String comments2 = sharedPreferences.getString(COMMENT_KEY + "2", "");
        String comments3 = sharedPreferences.getString(COMMENT_KEY + "3", "");
        String comments4 = sharedPreferences.getString(COMMENT_KEY + "4", "");
        commentTextView1.setText(comments1);
        commentTextView2.setText(comments2);
        commentTextView3.setText(comments3);
        commentTextView4.setText(comments4);
    }

    private void hideAllCommentsExcept(int index) {
        // Hide all commentTextViews except the one with the given index
        switch (index) {
            case 1:
                commentTextView2.setVisibility(View.GONE);
                commentTextView3.setVisibility(View.GONE);
                commentTextView4.setVisibility(View.GONE);
                break;
            case 2:
                commentTextView1.setVisibility(View.GONE);
                commentTextView3.setVisibility(View.GONE);
                commentTextView4.setVisibility(View.GONE);
                break;
            case 3:
                commentTextView1.setVisibility(View.GONE);
                commentTextView2.setVisibility(View.GONE);
                commentTextView4.setVisibility(View.GONE);
                break;
            case 4:
                commentTextView1.setVisibility(View.GONE);
                commentTextView2.setVisibility(View.GONE);
                commentTextView3.setVisibility(View.GONE);
                break;
        }
    }

    private void saveCountToPreferences(int index, int count, boolean isLike) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isLike) {
            editor.putInt(LIKE_COUNT_KEY_PREFIX + index, count);
        } else {
            editor.putInt(DISLIKE_COUNT_KEY_PREFIX + index, count);
        }
        editor.apply();
    }
}
