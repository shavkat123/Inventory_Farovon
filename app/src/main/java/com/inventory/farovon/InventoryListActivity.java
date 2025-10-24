package com.inventory.farovon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.ui.login.SessionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InventoryListActivity extends AppCompatActivity {

    public static final String EXTRA_DEPARTMENT_CODE = "department_code";
    private static final String TAG = "InventoryListActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private InventoryAdapter adapter;
    private SessionManager sessionManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Список инвентаря");
        }

        recyclerView = findViewById(R.id.inventory_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter();
        recyclerView.setAdapter(adapter);

        String departmentCode = getIntent().getStringExtra(EXTRA_DEPARTMENT_CODE);
        if (departmentCode != null && !departmentCode.isEmpty()) {
            fetchInventory(departmentCode);
        } else {
            Toast.makeText(this, "Код помещения не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchInventory(String departmentCode) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        String serverIP = sessionManager.getIpAddress();
        String url = "http://" + serverIP + "/my1c/hs/hw/say";
        Log.d(TAG, "Requesting URL: " + url);

        OkHttpClient client = new OkHttpClient();

        String json = "{\"odel\":\"" + departmentCode + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        String credentials = okhttp3.Credentials.basic(sessionManager.getUsername(), sessionManager.getPassword());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", credentials)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network request failed", e);
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(InventoryListActivity.this, "Ошибка сети: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String xmlResponse = response.body().string();
                        Log.d(TAG, "Original XML: " + xmlResponse);
                        final List<Nomenclature> items = parseXml(xmlResponse);
                        mainHandler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.setItems(items);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Parsing or response reading failed", e);
                        mainHandler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(InventoryListActivity.this, "Ошибка обработки ответа: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    Log.e(TAG, "Request not successful. Code: " + response.code());
                    mainHandler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(InventoryListActivity.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private List<Nomenclature> parseXml(String xmlResponse) {
        List<Nomenclature> list = new ArrayList<>();
        if (xmlResponse == null || xmlResponse.isEmpty()) {
            return list;
        }
        try {
            InputStream stream = new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("Product");
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String code = element.getElementsByTagName("Code").item(0).getTextContent();
                    String name = element.getElementsByTagName("Name").item(0).getTextContent();
                    String rf = element.getElementsByTagName("rf").item(0).getTextContent();
                    list.add(new Nomenclature(code, name, rf));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "XML parse error", e);
        }
        return list;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
