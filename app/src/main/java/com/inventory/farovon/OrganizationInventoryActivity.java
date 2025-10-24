package com.inventory.farovon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.model.OrganizationItem;
import com.inventory.farovon.ui.login.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrganizationInventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private OrganizationAdapter adapter;
    private SessionManager sessionManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_inventory);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.organization_recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new TreeItemDecoration(
                ContextCompat.getColor(this, R.color.colorPrimary),
                4f,
                64
        ));

        fetchOrganizationStructure();
    }

    private void fetchOrganizationStructure() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        String ip = sessionManager.getIpAddress();
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();
        String url = "http://" + ip + "/my1c/hs/checking/schema";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create("", null);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", Credentials.basic(username, password))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrganizationInventoryActivity.this, "Ошибка сети: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String xmlString = response.body().string();
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("ref=\"([^\"]*)\"([^\"]*)\"\"");
                        java.util.regex.Matcher matcher = pattern.matcher(xmlString);
                        xmlString = matcher.replaceAll("ref=\"$1&quot;$2&quot;\"");
                        java.io.InputStream is = new java.io.ByteArrayInputStream(xmlString.getBytes());

                        OrganizationXmlParser parser = new OrganizationXmlParser();
                        List<OrganizationItem> items = parser.parse(is);
                        mainHandler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter = new OrganizationAdapter(items);
                            recyclerView.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        mainHandler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(OrganizationInventoryActivity.this, "Ошибка парсинга: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    mainHandler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(OrganizationInventoryActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}