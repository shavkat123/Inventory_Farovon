package com.inventory.farovon.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.inventory.farovon.MainActivity;
import com.inventory.farovon.R;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText ipAddress = findViewById(R.id.ipAddress);
        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.loginButton);
        statusTextView = findViewById(R.id.statusTextView);

        SessionManager sessionManager = new SessionManager(this);
        ipAddress.setText(sessionManager.getIpAddress());
        username.setText(sessionManager.getUsername());
        password.setText(sessionManager.getPassword());

        loginButton.setOnClickListener(v -> {
            String ip = ipAddress.getText().toString().trim();
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (ip.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                statusTextView.setText("Please fill in all fields");
                return;
            }

            statusTextView.setTextColor(Color.DKGRAY);
            statusTextView.setText("Connecting...");
            authenticate(ip, user, pass);
        });
    }

    private void authenticate(String ip, String user, String pass) {
        String url = "http://" + ip + "/my1c/hs/checking/check";
        Log.d(TAG, "URL: " + url);
        String credential = Credentials.basic(user, pass);
        Log.d(TAG, "Credential: " + credential);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody body = RequestBody.create("{}", MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", credential)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network request failed", e);
                runOnUiThread(() -> {
                    statusTextView.setTextColor(Color.RED);
                    statusTextView.setText("Network Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string().trim();
                Log.d(TAG, "Response received. Code: " + response.code() + ", Body: " + responseBody);
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        if (responseBody.equalsIgnoreCase("ok")) {
                            statusTextView.setTextColor(Color.GREEN);
                            statusTextView.setText("Успешно");
                            SessionManager sessionManager = new SessionManager(LoginActivity.this);
                            sessionManager.createLoginSession(ip, user, pass);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }, 1000);
                        } else {
                            statusTextView.setTextColor(Color.RED);
                            statusTextView.setText("Unknown Response: " + responseBody);
                        }
                    } else {
                        statusTextView.setTextColor(Color.RED);
                        if (response.code() == 401) {
                            statusTextView.setText("Invalid credentials (401)");
                        } else {
                            statusTextView.setText("Error: " + response.code() + " " + response.message());
                        }
                    }
                });
            }
        });
    }
}