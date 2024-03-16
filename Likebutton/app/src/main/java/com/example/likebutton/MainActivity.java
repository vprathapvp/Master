package com.example.likebutton;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String LIKE_COUNT_KEY_PREFIX = "like_count_";
    private static final String DISLIKE_COUNT_KEY_PREFIX = "dislike_count_";
    private static final String SHARED_PREF_NAME = "like_button_prefs";

    private SharedPreferences sharedPreferences;

    private int likeCount1;
    private int dislikeCount1;
    private int likeCount2;
    private int dislikeCount2;
    private int likeCount3;
    private int dislikeCount3;
    private int likeCount4;
    private int dislikeCount4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        likeCount1 = sharedPreferences.getInt(LIKE_COUNT_KEY_PREFIX + "1", 0);
        dislikeCount1 = sharedPreferences.getInt(DISLIKE_COUNT_KEY_PREFIX + "1", 0);
        likeCount2 = sharedPreferences.getInt(LIKE_COUNT_KEY_PREFIX + "2", 0);
        dislikeCount2 = sharedPreferences.getInt(DISLIKE_COUNT_KEY_PREFIX + "2", 0);
        likeCount3 = sharedPreferences.getInt(LIKE_COUNT_KEY_PREFIX + "3", 0);
        dislikeCount3 = sharedPreferences.getInt(DISLIKE_COUNT_KEY_PREFIX + "3", 0);
        likeCount4 = sharedPreferences.getInt(LIKE_COUNT_KEY_PREFIX + "4", 0);
        dislikeCount4 = sharedPreferences.getInt(DISLIKE_COUNT_KEY_PREFIX + "4", 0);

        final TextView likeCountTextView1 = findViewById(R.id.likeCountTextView1);
        final TextView dislikeCountTextView1 = findViewById(R.id.dislikeCountTextView1);
        final TextView likeCountTextView2 = findViewById(R.id.likeCountTextView2);
        final TextView dislikeCountTextView2 = findViewById(R.id.dislikeCountTextView2);
        final TextView likeCountTextView3 = findViewById(R.id.likeCountTextView3);
        final TextView dislikeCountTextView3 = findViewById(R.id.dislikeCountTextView3);
        final TextView likeCountTextView4 = findViewById(R.id.likeCountTextView);
        final TextView dislikeCountTextView4 = findViewById(R.id.dislikeCountTextView);

        ImageButton likeButton1 = findViewById(R.id.likeButton1);
        ImageButton dislikeButton1 = findViewById(R.id.dislikeButton1);
        ImageButton likeButton2 = findViewById(R.id.likeButton2);
        ImageButton dislikeButton2 = findViewById(R.id.dislikeButton2);
        ImageButton likeButton3 = findViewById(R.id.likeButton3);
        ImageButton dislikeButton3 = findViewById(R.id.dislikeButton3);
        ImageButton likeButton4 = findViewById(R.id.likeButton4);
        ImageButton dislikeButton4 = findViewById(R.id.dislikeButton4);

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

        // Implement listeners for other buttons in a similar manner
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
