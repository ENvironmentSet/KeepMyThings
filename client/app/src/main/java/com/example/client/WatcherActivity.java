package com.example.client;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WatcherActivity extends AppCompatActivity {
    String sessionID;
    VideoView liveStreamVideoView;
    TextView missings;
    Button nextCamButton;
    List<String> camURLs = new ArrayList<String>();;
    int currentCamIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watcher);

        Intent intent = getIntent();
        sessionID = intent.getStringExtra("sessionID");

        liveStreamVideoView = (VideoView) findViewById(R.id.LiveStreamVideoView);
        missings = (TextView) findViewById(R.id.Missings);
        nextCamButton = (Button) findViewById(R.id.nextCamButton);

        loadCamURLs();

        liveStreamVideoView.setOnPreparedListener(mediaPlayer -> {
            liveStreamVideoView.start();
        });

        liveStreamVideoView.setOnCompletionListener(mediaPlayer -> {
            startLiveStreamDownload();
        });

        nextCamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCamIndex = (currentCamIndex + 1) % camURLs.size();
                liveStreamVideoView.stopPlayback();
                startLiveStreamDownload();
            }
        });

        loadMissings();
    }

    private void loadCamURLs() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    try {
                        URL url = new URL("http://10.0.2.2:8000/stream");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("Cookie", "sessionid="+sessionID);
                        conn.connect();

                        try {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;

                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuilder.append(line).append("\n");
                            }

                            bufferedReader.close();

                            return stringBuilder.toString();
                        } finally {
                            conn.disconnect();
                        }
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (Exception e) {
                    Log.e("WatcherActivity", "Exception in doInBackground: " + e.getMessage());
                }

                Log.e("WatcherActivity", "failed to load cams");
                throw new Error("failed to load cams");
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONArray streams = new JSONArray(result);

                    for (int i = 0; i < streams.length(); i++) {
                        camURLs.add(streams.getJSONObject(i).getString("video"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startLiveStreamDownload();
            }
        }.execute();
    }

    private void startLiveStreamDownload() {
        String camURL = camURLs.get(currentCamIndex);
        Uri uri = Uri.parse("http://10.0.2.2:8000" + camURL);
        liveStreamVideoView.setVideoURI(uri);
    }

    private void loadMissings() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    try {
                        URL url = new URL("http://10.0.2.2:8000/history");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("Cookie", "sessionid="+sessionID);
                        conn.connect();

                        try {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;

                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuilder.append(line).append("\n");
                            }

                            bufferedReader.close();
                            Thread.sleep(3000);
                            return stringBuilder.toString();
                        } finally {
                            conn.disconnect();
                        }
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (Exception e) {
                    Log.e("WatcherActivity", "Exception in doInBackground: " + e.getMessage());
                }

                Log.e("WatcherActivity", "failed to load cams");
                throw new Error("failed to load cams");
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONArray historyS = new JSONArray(result);

                    for (int i = 0; i < historyS.length(); i++) {
                        JSONObject history = historyS.getJSONObject(i);
                        JSONArray lost = history.getJSONArray("lost");
                        String lost_text = "";

                        for (int j = 0; j < lost.length(); j++)
                            lost_text += lost.getString(j);
                        missings.setText(lost_text);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loadMissings();
            }
        }.execute();
    }
}
