package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    Button watcherButton, historyButton;
    String sessionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        Intent intent = getIntent();
        sessionID = intent.getStringExtra("sessionID");

        watcherButton = (Button) findViewById(R.id.watcherButton);
        historyButton = (Button) findViewById(R.id.historyButton); // implement this

        watcherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WatcherActivity.class);
                intent.putExtra("sessionID", sessionID);

                startActivity(intent);
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                intent.putExtra("sessionID", sessionID);

                startActivity(intent);
            }
        });
    }
}
