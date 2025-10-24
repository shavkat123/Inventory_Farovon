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

import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemEntity;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InventoryListActivity extends AppCompatActivity {

    public static final String EXTRA_DEPARTMENT_CODE = "department_code";
    public static final String EXTRA_DEPARTMENT_ID = "department_id";
    private static final String TAG = "InventoryListActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private InventoryAdapter adapter;
    private SessionManager sessionManager;
    private AppDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private int departmentId;
    private String departmentCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

        sessionManager = new SessionManager(this);
        db = AppDatabase.getDatabase(this);

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

        departmentCode = getIntent().getStringExtra(EXTRA_DEPARTMENT_CODE);
        departmentId = getIntent().getIntExtra(EXTRA_DEPARTMENT_ID, -1);

        if (departmentId != -1) {
            loadDataFromDb();
            syncData();
        } else {
            Toast.makeText(this, "ID помещения не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDataFromDb() {
        progressBar.setVisibility(View.VISIBLE);
        databaseExecutor.execute(() -> {
            List<InventoryItemEntity> itemEntities = db.inventoryItemDao().getByDepartmentId(departmentId);
            List<Nomenclature> items = new ArrayList<>();
            for (InventoryItemEntity entity : itemEntities) {
                items.add(new Nomenclature(entity.code, entity.name, entity.rf));
            }
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                adapter.setItems(items);
            });
        });
    }

    private void syncData() {
        if (departmentCode == null) return;

        String serverIP = sessionManager.getIpAddress();
        String url = "http://" + serverIP + "/my1c/hs/hw/say";

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
                 mainHandler.post(() -> Toast.makeText(InventoryListActivity.this, "Ошибка синхронизации", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String xmlResponse = response.body().string();
                        final List<Nomenclature> items = parseXml(xmlResponse);

                        databaseExecutor.execute(() -> {
                            db.inventoryItemDao().clearByDepartmentId(departmentId);
                            List<InventoryItemEntity> itemEntities = new ArrayList<>();
                            for (Nomenclature item : items) {
                                InventoryItemEntity entity = new InventoryItemEntity();
                                entity.departmentId = departmentId;
                                entity.code = item.getCode();
                                entity.name = item.getName();
                                entity.rf = item.getRfid();
                                itemEntities.add(entity);
                            }
                            db.inventoryItemDao().insertAll(itemEntities);

                            mainHandler.post(() -> loadDataFromDb());
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Parsing or DB error", e);
                    }
                }
            }
        });
    }

    private List<Nomenclature> parseXml(String xmlResponse) {
        // Same parsing logic as before
        List<Nomenclature> list = new ArrayList<>();
        if (xmlResponse == null || xmlResponse.isEmpty()) return list;
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
