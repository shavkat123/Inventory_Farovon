package com.inventory.farovon;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.DepartmentEntity;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.db.OrganizationEntity;
import com.inventory.farovon.db.PendingUploadEntity;
import com.inventory.farovon.model.OrganizationItem;
import com.inventory.farovon.ui.login.SessionManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
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
    private AppDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService networkExecutor = Executors.newFixedThreadPool(4);
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_inventory);

        sessionManager = new SessionManager(this);
        db = AppDatabase.getDatabase(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.organization_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_send_data).setOnClickListener(v -> sendCompletedData());

        loadDataFromDb();

        if (isNetworkAvailable()) {
            startService(new android.content.Intent(this, UploadService.class));
        }
    }

    private void sendCompletedData() {
        databaseExecutor.execute(() -> {
            List<DepartmentEntity> completedDepts = db.departmentDao().getCompletedDepartments();
            if (completedDepts.isEmpty()) {
                mainHandler.post(() -> Toast.makeText(this, "Нет выполненных помещений для отправки", Toast.LENGTH_SHORT).show());
                return;
            }

            for (DepartmentEntity dept : completedDepts) {
                db.pendingUploadDao().addToQueue(new PendingUploadEntity(dept.code));
                db.departmentDao().updateCompletionStatus(dept.id, false);
            }

            mainHandler.post(() -> {
                Toast.makeText(this, "Данные добавлены в очередь на отправку", Toast.LENGTH_SHORT).show();
                loadDataFromDb();
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.organization_inventory_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem statusItem = menu.findItem(R.id.action_status);
        if (isNetworkAvailable()) {
            statusItem.setIcon(R.drawable.ic_status_online);
        } else {
            statusItem.setIcon(R.drawable.ic_status_offline);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sync) {
            if (isNetworkAvailable()) {
                syncData();
            } else {
                Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void syncData() {
        Log.i(TAG, "Starting full synchronization process.");
        mainHandler.post(() -> {
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Начинается полная синхронизация...", Toast.LENGTH_SHORT).show();
        });
        syncOrganizationStructure(this::syncAllInventory);
    }

    private void syncOrganizationStructure(Runnable onComplete) {
        Log.i(TAG, "Step 1: Synchronizing organization structure.");
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
                Log.e(TAG, "Failed to synchronize organization structure.", e);
                mainHandler.post(() -> {
                    Toast.makeText(OrganizationInventoryActivity.this, "Ошибка синхронизации структуры", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String xmlString = response.body().string();
                        Log.d(TAG, "Successfully received organization structure XML.");
                        OrganizationXmlParser parser = new OrganizationXmlParser();
                        List<OrganizationItem> orgItems = parser.parse(xmlString);

                        databaseExecutor.execute(() -> {
                            Log.i(TAG, "Clearing old structure data and saving new structure.");
                            db.organizationDao().clearAll();
                            db.departmentDao().clearAll();
                            for (OrganizationItem orgItem : orgItems) {
                                OrganizationEntity orgEntity = new OrganizationEntity();
                                orgEntity.name = orgItem.getName();
                                long orgId = db.organizationDao().insert(orgEntity);
                                saveDepartmentsRecursive(orgItem.getChildren(), (int) orgId, "");
                            }
                            Log.i(TAG, "Organization structure synchronization complete.");
                            mainHandler.post(onComplete);
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Parsing or DB error on structure sync", e);
                        mainHandler.post(() -> {
                            Toast.makeText(OrganizationInventoryActivity.this, "Ошибка обработки структуры", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        });
                    }
                } else {
                    Log.e(TAG, "Server error during structure synchronization: " + response.code());
                    mainHandler.post(() -> {
                        Toast.makeText(OrganizationInventoryActivity.this, "Ошибка сервера при синхронизации структуры", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                }
            }
        });
    }

    private void saveDepartmentsRecursive(List<OrganizationItem> deptItems, int orgId, String parentRef) {
        if (deptItems == null || deptItems.isEmpty()) {
            return;
        }

        List<DepartmentEntity> deptEntities = new ArrayList<>();
        for (OrganizationItem deptItem : deptItems) {
            DepartmentEntity deptEntity = new DepartmentEntity();
            deptEntity.organizationId = orgId;
            deptEntity.code = deptItem.getCode();
            deptEntity.name = deptItem.getName();
            deptEntity.parentRef = parentRef;
            deptEntities.add(deptEntity);
        }
        db.departmentDao().insertAll(deptEntities);

        for (OrganizationItem deptItem : deptItems) {
            saveDepartmentsRecursive(deptItem.getChildren(), orgId, deptItem.getName());
        }
    }

    private void syncAllInventory() {
        Log.i(TAG, "Step 2: Synchronizing inventory for all locations.");
        mainHandler.post(() -> Toast.makeText(this, "Загрузка инвентаря для каждого помещения...", Toast.LENGTH_SHORT).show());

        databaseExecutor.execute(() -> {
            Log.i(TAG, "Clearing old inventory data.");
            db.inventoryItemDao().clearAll();
            List<DepartmentEntity> allDepartments = db.departmentDao().getAll();
            List<DepartmentEntity> locations = filterLocations(allDepartments);

            Log.i(TAG, "Found " + locations.size() + " locations to synchronize.");

            if (locations.isEmpty()) {
                mainHandler.post(() -> {
                    Toast.makeText(this, "Помещения не найдены. Синхронизация инвентаря пропущена.", Toast.LENGTH_LONG).show();
                    loadDataFromDb();
                });
                return;
            }

            AtomicInteger completedCount = new AtomicInteger(0);
            int total = locations.size();

            for (DepartmentEntity location : locations) {
                fetchInventoryForLocation(location, () -> {
                    int current = completedCount.incrementAndGet();
                    if (current == total) {
                        Log.i(TAG, "Full synchronization process completed successfully.");
                        mainHandler.post(() -> {
                            Toast.makeText(this, "Полная синхронизация завершена!", Toast.LENGTH_LONG).show();
                            loadDataFromDb();
                        });
                    }
                });
            }
        });
    }

    private List<DepartmentEntity> filterLocations(List<DepartmentEntity> allDepts) {
        List<DepartmentEntity> locations = new ArrayList<>();
        for (DepartmentEntity dept : allDepts) {
            if (dept.code != null && dept.code.matches("\\d+")) {
                locations.add(dept);
            }
        }
        return locations;
    }

    private void fetchInventoryForLocation(DepartmentEntity location, Runnable onComplete) {
        Log.d(TAG, "Fetching inventory for location: " + location.name + " (Code: " + location.code + ")");
        String ip = sessionManager.getIpAddress();
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();
        String url = "http://" + ip + "/my1c/hs/hw/say";
        String json = "{\"otdel\":\"" + location.code + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", Credentials.basic(username, password))
                .build();

        networkExecutor.execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String xmlString = response.body().string();
                    List<InventoryItemEntity> items = parseInventoryXml(xmlString, location.id, location.code);
                    if (!items.isEmpty()) {
                        db.inventoryItemDao().insertAll(items);
                        Log.i(TAG, "Successfully saved " + items.size() + " inventory items for location: " + location.name);
                    } else {
                        Log.i(TAG, "No inventory items found for location: " + location.name);
                    }
                } else {
                    Log.e(TAG, "Server error fetching inventory for location " + location.code + ": " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch or parse inventory for location: " + location.code, e);
            } finally {
                onComplete.run();
            }
        });
    }

    private List<InventoryItemEntity> parseInventoryXml(String xml, int resolvedDepartmentId, String roomCode) throws Exception {
        List<InventoryItemEntity> items = new ArrayList<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(xml));

        InventoryItemEntity currentItem = null;
        String text = null;
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("Product".equalsIgnoreCase(tagName)) {
                        currentItem = new InventoryItemEntity();
                        currentItem.departmentId = resolvedDepartmentId;
                        currentItem.code = "";
                        currentItem.name = "";
                        currentItem.rf = "";
                        currentItem.mol = "";
                        currentItem.location = "";
                    }
                    break;
                case XmlPullParser.TEXT:
                    text = parser.getText();
                    break;
                case XmlPullParser.END_TAG:
                    if (currentItem != null) {
                        if ("Code".equalsIgnoreCase(tagName)) {
                            currentItem.code = (text != null) ? text : "";
                        } else if ("Name".equalsIgnoreCase(tagName)) {
                            currentItem.name = (text != null) ? text : "";
                        } else if ("rf".equalsIgnoreCase(tagName)) {
                            currentItem.rf = (text != null) ? text : "";
                        } else if ("mol".equalsIgnoreCase(tagName)) {
                            currentItem.mol = (text != null) ? text : "";
                        } else if ("location".equalsIgnoreCase(tagName)) {
                            currentItem.location = (text != null) ? text : "";
                        } else if ("Product".equalsIgnoreCase(tagName)) {
                            currentItem.location = roomCode;
                            if (currentItem.code != null && !currentItem.code.isEmpty() && currentItem.name != null && !currentItem.name.isEmpty()) {
                                items.add(currentItem);
                            }
                            currentItem = null;
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        return items;
    }

    private void loadDataFromDb() {
        Log.d(TAG, "Loading data from DB to display.");
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        databaseExecutor.execute(() -> {
            List<OrganizationEntity> orgEntities = db.organizationDao().getAll();
            List<OrganizationItem> orgItems = new ArrayList<>();

            for (OrganizationEntity orgEntity : orgEntities) {
                OrganizationItem orgItem = new OrganizationItem(orgEntity.name, 0);
                orgItem.setId(orgEntity.id);
                List<DepartmentEntity> deptEntities = db.departmentDao().getByOrganizationId(orgEntity.id);

                Map<String, OrganizationItem> departmentMap = new HashMap<>();
                for (DepartmentEntity deptEntity : deptEntities) {
                    OrganizationItem deptItem = new OrganizationItem(deptEntity.name, 1);
                    deptItem.setId(deptEntity.id);
                    deptItem.setCode(deptEntity.code);
                    deptItem.setCompleted(deptEntity.isCompleted);
                    departmentMap.put(deptEntity.name, deptItem);
                }

                for (DepartmentEntity deptEntity : deptEntities) {
                    OrganizationItem deptItem = departmentMap.get(deptEntity.name);
                    if (deptEntity.parentRef != null && !deptEntity.parentRef.isEmpty() && departmentMap.containsKey(deptEntity.parentRef)) {
                        OrganizationItem parentItem = departmentMap.get(deptEntity.parentRef);
                        if (parentItem != null) {
                            parentItem.addChild(deptItem);
                            deptItem.setLevel(parentItem.getLevel() + 1);
                        }
                    } else {
                        orgItem.addChild(deptItem);
                    }
                }
                orgItems.add(orgItem);
            }

            mainHandler.post(() -> {
                Log.d(TAG, "Data loaded. Updating RecyclerView.");
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter = new OrganizationAdapter(orgItems);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
