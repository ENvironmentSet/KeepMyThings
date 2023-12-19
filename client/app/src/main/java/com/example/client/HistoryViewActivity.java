package com.example.client;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HistoryViewActivity extends AppCompatActivity {
    String sessionID, date, thumbnail, footage, lost;

    TextView historyContent;
    ImageView thumbnailView;
    VideoView footageVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_view);

        Intent intent = getIntent();
        sessionID = intent.getStringExtra("sessionID");
        date = intent.getStringExtra("date");
        thumbnail = intent.getStringExtra("thumbnail");
        footage = intent.getStringExtra("footage");
        lost = intent.getStringExtra("lost");

        historyContent = (TextView) findViewById(R.id.historyContent);
        thumbnailView = (ImageView) findViewById(R.id.thumbnailView);
        footageVideoView = (VideoView) findViewById(R.id.footageVideoView);

        historyContent.setText(lost + "이(가) 없습니다. (" + date + ")");

        try {
            footageVideoView.setVideoURI(Uri.parse("http://10.0.2.2:8000"+footage));
        } catch (Exception e) {}

        footageVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                footageVideoView.start();
            }
        });

        footageVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                footageVideoView.start();
            }
        });

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                try {
                    // Open a connection to the URL
                    URL url = new URL("http://10.0.2.2:8000"+thumbnail);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    // Read the input stream into a Bitmap
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();

                    return bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                thumbnailView.setImageBitmap(result);
            }
        }.execute();
    }
}
