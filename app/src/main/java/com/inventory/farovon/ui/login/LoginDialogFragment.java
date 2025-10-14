package com.inventory.farovon.ui.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

        SessionManager sessionManager = new SessionManager(requireContext());
        ipAddress.setText(sessionManager.getIpAddress());
        username.setText(sessionManager.getUsername());
        password.setText(sessionManager.getPassword());

        loginButton.setOnClickListener(v -> {
            String ip = ipAddress.getText().toString().trim();
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (ip.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            authenticate(ip, user, pass);
        });

        builder.setView(view);
        return builder.create();
    }

    private void authenticate(String ip, String user, String pass) {
        String url = "http://" + ip + "/my1c/hs/hw/say";
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
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.optString("status").equals("1")) {
                            mainHandler.post(() -> {
                                SessionManager sessionManager = new SessionManager(requireContext());
                                sessionManager.createLoginSession(ip, user, pass);
                                Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                dismiss();
                            });
                        } else {
                            mainHandler.post(() -> {
                                Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        mainHandler.post(() -> {
                            Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    mainHandler.post(() -> {
                        Toast.makeText(getContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}