package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText usernameInput, passwordInput;
    Button loginButton, signupButton;
    String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameInput = (EditText) findViewById(R.id.Username);
        passwordInput = (EditText) findViewById(R.id.Password);
        loginButton = (Button) findViewById(R.id.LoginButton);
        signupButton = (Button) findViewById(R.id.SignupButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = usernameInput.getText().toString();
                password = passwordInput.getText().toString();

                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... voids) {
                        try {
                            try {
                                URL url = new URL("http://10.0.2.2:8000/auth/login");
                                String urlParameters = "username=" + username + "&password=" + password;
                                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                conn.setRequestProperty("charset", "utf-8");
                                conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
                                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                                    wr.write(postData);
                                }
                                conn.connect();

                                if (conn.getResponseCode() == 200) {
                                    Map<String, List<String>> headerFields = conn.getHeaderFields();
                                    List<String> cookiesHeader = headerFields.get("Set-Cookie");

                                    if (cookiesHeader != null) {
                                        for (String cookie : cookiesHeader) {
                                            if (cookie.contains("sessionid")) {
                                                String[] parts = cookie.split(";");
                                                return parts[0].split("=")[1]; // sessionid
                                            }
                                        }
                                    }
                                }
                                conn.disconnect();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } catch (Exception e) {
                            Log.e("LogInTask", "Exception in doInBackground: " + e.getMessage());
                        }

                        Log.e("LogInTask", "Failed to login");
                        throw new Error("failed to login");
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.putExtra("sessionID", result);

                        startActivity(intent);
                    }
                }.execute();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);

                startActivity(intent);
            }
        });
    }
}