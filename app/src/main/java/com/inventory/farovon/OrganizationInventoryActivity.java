package com.inventory.farovon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

    private static final String TAG = "OrgInventoryActivity";

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
        Log.d(TAG, "Requesting URL: " + url);

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
                Log.e(TAG, "Network request failed", e);
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrganizationInventoryActivity.this, "Ошибка сети: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                mainHandler.post(() -> Log.d(TAG, "Response code: " + response.code()));
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String xmlString = response.body().string();
                        Log.d(TAG, "Original XML: " + xmlString);

                        // This new logic replaces the flawed regex-based sanitization.
                        // It manually parses attribute values to correctly handle nested quotes.
                        StringBuilder sb = new StringBuilder();
                        int cursor = 0;
                        while(cursor < xmlString.length()) {
                            int nextAttrStart = xmlString.indexOf("=\"", cursor);
                            if (nextAttrStart == -1) {
                                // No more attributes, append the rest of the string
                                sb.append(xmlString.substring(cursor));
                                break;
                            }

                            // Append the text before the attribute value starts
                            // (up to and including the opening quote)
                            sb.append(xmlString, cursor, nextAttrStart + 2);

                            int valueStart = nextAttrStart + 2;
                            int valueEnd = -1;
                            int searchCursor = valueStart;

                            // Find the correct closing quote for the attribute value
                            while (searchCursor < xmlString.length()) {
                                int nextQuote = xmlString.indexOf('"', searchCursor);
                                if (nextQuote == -1) {
                                    // Malformed, no closing quote at all
                                    valueEnd = -1;
                                    break;
                                }

                                // A quote is a "real" closing quote if it's the end of the string
                                // or followed by a space, '>', or '/'
                                if (nextQuote + 1 >= xmlString.length()) {
                                    valueEnd = nextQuote;
                                    break;
                                }
                                char charAfter = xmlString.charAt(nextQuote + 1);
                                if (charAfter == ' ' || charAfter == '>' || charAfter == '/') {
                                    valueEnd = nextQuote;
                                    break;
                                }

                                // This was an internal quote, continue searching after it
                                searchCursor = nextQuote + 1;
                            }

                            if (valueEnd != -1) {
                                String value = xmlString.substring(valueStart, valueEnd);
                                // Sanitize the extracted value
                                String sanitizedValue = value.replace("\"", "&quot;");
                                sb.append(sanitizedValue);
                                // Append the closing quote
                                sb.append('"');
                                cursor = valueEnd + 1;
                            } else {
                                // Malformed attribute, append the rest and give up
                                sb.append(xmlString.substring(valueStart));
                                break;
                            }
                        }
                        String sanitizedXml = sb.toString();
                        Log.d(TAG, "Sanitized XML: " + sanitizedXml);

                        java.io.InputStream is = new java.io.ByteArrayInputStream(sanitizedXml.getBytes());

                        OrganizationXmlParser parser = new OrganizationXmlParser();
                        List<OrganizationItem> items = parser.parse(is);
                        Log.d(TAG, "Parsing successful. Item count: " + items.size());

                        mainHandler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter = new OrganizationAdapter(items);
                            recyclerView.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Parsing failed", e);
                        mainHandler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(OrganizationInventoryActivity.this, "Ошибка парсинга: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    Log.e(TAG, "Request not successful. Code: " + response.code());
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