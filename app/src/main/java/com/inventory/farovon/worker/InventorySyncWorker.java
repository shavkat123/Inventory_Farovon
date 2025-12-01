package com.inventory.farovon.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
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

public class InventorySyncWorker extends Worker {

    private static final String TAG = "InventorySyncWorker";

    public InventorySyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(context);
        SessionManager sessionManager = new SessionManager(context);
        List<PendingUploadEntity> uploads = db.pendingUploadDao().getAll();

        if (uploads.isEmpty()) {
            return Result.success();
        }

        OkHttpClient client = new OkHttpClient();
        String ip = sessionManager.getIpAddress();
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();
        String url = "http://" + ip + "/my1c/hs/hw/say";

        if (ip == null || ip.isEmpty()) {
             return Result.failure();
        }

        boolean allSuccess = true;

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
                    allSuccess = false;
                }
            } catch (IOException e) {
                Log.e(TAG, "Network error during upload: " + upload.id, e);
                return Result.retry();
            }
        }

        return Result.success();
    }
}
