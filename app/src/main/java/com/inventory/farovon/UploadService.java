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
        List<PendingUploadEntity> uploads = db.pendingUploadDao().getAll();

        if (uploads.isEmpty()) {
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String ip = sessionManager.getIpAddress();
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();
        String url = "http://" + ip + "/my1c/hs/hw/say";

        for (PendingUploadEntity upload : uploads) {
            RequestBody body = RequestBody.create(upload.jsonData, okhttp3.MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Authorization", Credentials.basic(username, password))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null && "ok".equals(response.body().string())) {
                    db.pendingUploadDao().delete(upload);
                    Log.d(TAG, "Successfully uploaded and deleted: " + upload.id);
                } else {
                    Log.e(TAG, "Server returned an error for upload: " + upload.id);
                }
            } catch (IOException e) {
                Log.e(TAG, "Network error during upload: " + upload.id, e);
            }
        }
    }
}
