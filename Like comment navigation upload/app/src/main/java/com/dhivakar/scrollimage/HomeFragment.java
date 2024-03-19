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

        final TextView likeCountTextView1 = view.findViewById(R.id.likeCountTextView1);
        final TextView dislikeCountTextView1 = view.findViewById(R.id.dislikeCountTextView1);
        final TextView likeCountTextView2 = view.findViewById(R.id.likeCountTextView2);
        final TextView dislikeCountTextView2 = view.findViewById(R.id.dislikeCountTextView2);
        final TextView likeCountTextView3 = view.findViewById(R.id.likeCountTextView3);
        final TextView dislikeCountTextView3 = view.findViewById(R.id.dislikeCountTextView3);
        final TextView likeCountTextView4 = view.findViewById(R.id.likeCountTextView);
        final TextView dislikeCountTextView4 = view.findViewById(R.id.dislikeCountTextView);

        ImageButton likeButton1 = view.findViewById(R.id.likeButton1);
        ImageButton dislikeButton1 = view.findViewById(R.id.dislikeButton1);
        ImageButton likeButton2 = view.findViewById(R.id.likeButton2);
        ImageButton dislikeButton2 = view.findViewById(R.id.dislikeButton2);
        ImageButton likeButton3 = view.findViewById(R.id.likeButton3);
        ImageButton dislikeButton3 = view.findViewById(R.id.dislikeButton3);
        ImageButton likeButton4 = view.findViewById(R.id.likeButton4);
        ImageButton dislikeButton4 = view.findViewById(R.id.dislikeButton4);

        likeCountTextView1.setText(String.valueOf(likeCount1));
        dislikeCountTextView1.setText(String.valueOf(dislikeCount1));
        likeCountTextView2.setText(String.valueOf(likeCount2));
        dislikeCountTextView2.setText(String.valueOf(dislikeCount2));
        likeCountTextView3.setText(String.valueOf(likeCount3));
        dislikeCountTextView3.setText(String.valueOf(dislikeCount3));
        likeCountTextView4.setText(String.valueOf(likeCount4));
        dislikeCountTextView4.setText(String.valueOf(dislikeCount4));

        likeButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCount1++;
                likeCountTextView1.setText(String.valueOf(likeCount1));
                saveCountToPreferences(1, likeCount1, true);
            }
        });

        dislikeButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeCount1++;
                dislikeCountTextView1.setText(String.valueOf(dislikeCount1));
                saveCountToPreferences(1, dislikeCount1, false);
            }

        });

        likeButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCount2++;
                likeCountTextView2.setText(String.valueOf(likeCount2));
                saveCountToPreferences(2, likeCount2, true);
            }
        });

        dislikeButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeCount2++;
                dislikeCountTextView2.setText(String.valueOf(dislikeCount2));
                saveCountToPreferences(2, dislikeCount2, false);
            }
        });

        likeButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCount3++;
                likeCountTextView3.setText(String.valueOf(likeCount3));
                saveCountToPreferences(3, likeCount3, true);
            }
        });

        dislikeButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeCount3++;
                dislikeCountTextView3.setText(String.valueOf(dislikeCount3));
                saveCountToPreferences(3, dislikeCount3, false);
            }
        });

        likeButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCount4++;
                likeCountTextView4.setText(String.valueOf(likeCount4));
                saveCountToPreferences(4, likeCount4, true);
            }
        });

        dislikeButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeCount4++;
                dislikeCountTextView4.setText(String.valueOf(dislikeCount4));
                saveCountToPreferences(4, dislikeCount4, false);
            }
        });

        // Load comments from SharedPreferences
        loadComments();

        // Set onClickListener for commentButton1
        commentButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = editText1.getText().toString().trim();
                if (!comment.isEmpty()) {
                    commentTextView1.append(comment + "\n");
                    editText1.setText("");
                    // Save comments to SharedPreferences
                    saveComments();
                    showToast("Comment added Successfully");
                } else {
                    showToast("Please type a comment");
                }
            }
        });
        commentButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = editText2.getText().toString().trim();
                if (!comment.isEmpty()) {
                    commentTextView2.append(comment + "\n");
                    editText2.setText("");
                    // Save comments to SharedPreferences
                    saveComments();
                    showToast("Comment added Successfully");
                } else {
                    showToast("Please type a comment");
                }
            }
        });
        commentButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = editText3.getText().toString().trim();
                if (!comment.isEmpty()) {
                    commentTextView3.append(comment + "\n");
                    editText3.setText("");
                    // Save comments to SharedPreferences
                    saveComments();
                    showToast("Comment added Successfully");
                } else {
                    showToast("Please type a comment");
                }
            }
        });
        commentButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = editText4.getText().toString().trim();
                if (!comment.isEmpty()) {
                    commentTextView4.append(comment + "\n");
                    editText4.setText("");
                    // Save comments to SharedPreferences
                    saveComments();
                    showToast("Comment added Successfully");
                } else {
                    showToast("Please type a comment");
                }
            }
        });

        // Set onClickListener for allcommentButton1
        allcommentButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of the TextView when the button is clicked
                if (commentTextView1.getVisibility() == View.VISIBLE) {
                    commentTextView1.setVisibility(View.GONE);
                } else {
                    commentTextView1.setVisibility(View.VISIBLE);
                }
            }
        });
        allcommentButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of the TextView when the button is clicked
                if (commentTextView2.getVisibility() == View.VISIBLE) {
                    commentTextView2.setVisibility(View.GONE);
                } else {
                    commentTextView2.setVisibility(View.VISIBLE);
                }
            }
        });
        allcommentButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of the TextView when the button is clicked
                if (commentTextView3.getVisibility() == View.VISIBLE) {
                    commentTextView3.setVisibility(View.GONE);
                } else {
                    commentTextView3.setVisibility(View.VISIBLE);
                }
            }
        });
        allcommentButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of the TextView when the button is clicked
                if (commentTextView4.getVisibility() == View.VISIBLE) {
                    commentTextView4.setVisibility(View.GONE);
                } else {
                    commentTextView4.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    // Method to show a toast message
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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
