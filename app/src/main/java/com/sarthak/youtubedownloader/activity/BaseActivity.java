package com.sarthak.youtubedownloader.activity;

import static java.util.Objects.nonNull;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sarthak.youtubedownloader.R;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupToolbar(String title) {
        // All common functionality for toolbar goes here
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (nonNull(toolbar)) {
            setSupportActionBar(toolbar);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setTitle(title);

        }

        TextView toolbarTitleTextView = findViewById(R.id.tool_bar_title);
        if (nonNull(toolbarTitleTextView)) {
            toolbarTitleTextView.setText(title);
        }
    }
}
