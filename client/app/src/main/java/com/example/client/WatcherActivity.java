package com.example.client;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WatcherActivity extends AppCompatActivity {
    String streamID;
    Bitmap bmImg;
    ImageView ImgView;
    TextView Missings;
    StreamView task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watcher);

        Intent intent = getIntent();
        streamID = intent.getStringExtra("streamID");

        ImgView = (ImageView) findViewById(R.id.ImgView);
        Missings = (TextView) findViewById(R.id.Missings);

        task = new StreamView();
        task.execute();

        streamText();
    }

    private class StreamView extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                URL myFileUrl = new URL("http://10.0.2.2:8000/image/"+streamID+"/");
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();

                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return bmImg;
        }

        protected void onPostExecute(Bitmap img) {
            ImgView.setImageBitmap(bmImg);

            task = new StreamView();
            task.execute();
        }
    }

    private void streamText() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://10.0.2.2:8000/get-missings/"+streamID+"/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();

                    try {
                        InputStream in = new BufferedInputStream(conn.getInputStream());
                        return readStream(in);
                    } finally {
                        conn.disconnect();
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Missings.setText(result);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                streamText();
            }
        }.execute();
    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }
}
