package com.example.client;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

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

public class HistoryActivity extends AppCompatActivity {
    LinearLayout historyList;
    String sessionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        Intent intent = getIntent();
        sessionID = intent.getStringExtra("sessionID");

        historyList = (LinearLayout) findViewById(R.id.HistoryList);

        loadHistory();
    }

    private void loadHistory() {
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
                       String date = history.getString("date");
                       JSONArray lost = history.getJSONArray("lost");
                       String lost_text = "";

                       for (int j = 0; j < lost.length(); j++)
                           lost_text += lost.getString(j);

                       Button historyButton = new Button(getApplicationContext());
                       historyButton.setText("다음 물건이 사라졌습니다: " + lost_text + "(" + date +")");

                        String finalLost_text = lost_text;
                        historyButton.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                              try {
                                  Intent intent = new Intent(getApplicationContext(), HistoryViewActivity.class); // change this
                                  intent.putExtra("sessionID", sessionID);
                                  intent.putExtra("date", date);
                                  intent.putExtra("thumbnail", history.getString("thumbnail"));
                                  intent.putExtra("footage", history.getString("footage"));
                                  intent.putExtra("lost", finalLost_text);

                                  startActivity(intent);
                              } catch (JSONException e) {
                                  e.printStackTrace();
                              }
                           }
                       });

                       historyList.addView(historyButton);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}
