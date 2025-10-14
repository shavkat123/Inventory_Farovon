package com.inventory.farovon.ui.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.inventory.farovon.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginDialogFragment extends DialogFragment {

    private static final String TAG = "LoginDialogFragment";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextView statusTextView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);

        final EditText ipAddress = view.findViewById(R.id.ipAddress);
        final EditText username = view.findViewById(R.id.username);
        final EditText password = view.findViewById(R.id.password);
        final Button loginButton = view.findViewById(R.id.loginButton);
        statusTextView = view.findViewById(R.id.statusTextView);

        SessionManager sessionManager = new SessionManager(requireContext());
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

        builder.setView(view);
        return builder.create();
    }

    private void authenticate(String ip, String user, String pass) {
        String url = "http://" + ip + "/my1c/hs/checking/check";
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create("{}", MediaType.get("application/json; charset=utf-8"));
        String credential = Credentials.basic(user, pass);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", credential)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    statusTextView.setTextColor(Color.RED);
                    statusTextView.setText("Network Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string().trim();
                mainHandler.post(() -> {
                    if (response.isSuccessful()) {
                        if (responseBody.equalsIgnoreCase("ок")) {
                            statusTextView.setTextColor(Color.GREEN);
                            statusTextView.setText("Успешно");
                            SessionManager sessionManager = new SessionManager(requireContext());
                            sessionManager.createLoginSession(ip, user, pass);
                            // Dismiss after a short delay
                            new Handler(Looper.getMainLooper()).postDelayed(() -> dismiss(), 1000);
                        } else {
                            statusTextView.setTextColor(Color.RED);
                            statusTextView.setText("Unknown server response: " + responseBody);
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