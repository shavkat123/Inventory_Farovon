package com.inventory.farovon;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.Nullable;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.PendingUploadEntity;
import com.inventory.farovon.ui.login.SessionManager;
import java.io.IOException;
import java.util.List;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadService extends IntentService {

    private static final String TAG = "UploadService";

    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        List<PendingUploadEntity> queue = db.pendingUploadDao().getQueue();

        if (queue.isEmpty()) {
            Log.d(TAG, "Upload queue is empty. Nothing to do.");
            return;
        }

        List<String> roomCodes = new java.util.ArrayList<>();
        for (PendingUploadEntity entity : queue) {
            roomCodes.add(entity.roomCode);
        }

        // Format to: {"inventoried_rooms": ["code1", "code2"]}
        org.json.JSONObject jsonObject = new org.json.JSONObject();
        try {
            jsonObject.put("inventoried_rooms", new org.json.JSONArray(roomCodes));
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Failed to create JSON object", e);
            return;
        }
        String jsonPayload = jsonObject.toString();

        OkHttpClient client = new OkHttpClient();
        String ip = sessionManager.getIpAddress();
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();

        if (ip == null || username == null) {
            Log.e(TAG, "Session details not found. Aborting upload.");
            return;
        }

        String url = "http://" + ip + "/my1c/hs/hw/say";
        RequestBody body = RequestBody.create(jsonPayload, okhttp3.MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", Credentials.basic(username, password))
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                if ("ok".equalsIgnoreCase(responseBody.trim())) {
                    db.pendingUploadDao().deleteFromQueue(roomCodes);
                    Log.d(TAG, "Successfully uploaded " + roomCodes.size() + " completed rooms.");
                } else {
                    Log.e(TAG, "Server returned an error: " + responseBody);
                }
            } else {
                Log.e(TAG, "Server returned a non-successful response: " + response.code());
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error during upload.", e);
        }
    }
}
