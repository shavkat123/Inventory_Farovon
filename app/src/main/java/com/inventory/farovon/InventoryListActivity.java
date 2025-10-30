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
import com.inventory.farovon.db.DepartmentDao;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.ui.login.SessionManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class InventoryListActivity extends AppCompatActivity {

    private List<Room> rooms = new ArrayList<>();
    public static final String EXTRA_DEPARTMENT_CODE = "department_code";
    public static final String EXTRA_DEPARTMENT_ID = "department_id";
    private static final String TAG = "InventoryListActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private RoomAdapter adapter;
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
            getSupportActionBar().setTitle("Список помещений");
        }

        recyclerView = findViewById(R.id.inventory_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnScanClickListener(item -> {
            databaseExecutor.execute(() -> {
                DepartmentDao dao = db.departmentDao();
                final int deptId = dao.getIdByCode(item.getCode());

                mainHandler.post(() -> {
                    android.content.Intent intent = new android.content.Intent(InventoryListActivity.this, ScanningActivity.class);
                    intent.putExtra(ScanningActivity.EXTRA_ROOM_CODE, item.getCode());
                    intent.putExtra(ScanningActivity.EXTRA_ROOM_NAME, item.getName());
                    intent.putExtra(ScanningActivity.EXTRA_DEPARTMENT_ID, deptId);
                    startActivity(intent);
                });
            });
        });

        departmentCode = getIntent().getStringExtra(EXTRA_DEPARTMENT_CODE);
        departmentId = getIntent().getIntExtra(EXTRA_DEPARTMENT_ID, -1);

        if (departmentId != -1) {
            loadInitialData();
            syncDataWithServer();
        } else {
            Toast.makeText(this, "ID отдела не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInitialData() {
        progressBar.setVisibility(View.VISIBLE);
        databaseExecutor.execute(() -> {
            List<InventoryItemEntity> itemsInDept = db.inventoryItemDao().getByDepartmentId(departmentId);

            Map<String, String> locationMap = itemsInDept.stream()
                .filter(item -> item.location != null && !item.location.isEmpty())
                .collect(Collectors.toMap(
                    item -> item.location, // key is location code
                    item -> item.location, // value is location name (assuming they are the same)
                    (existing, replacement) -> existing // if duplicate keys, keep existing
                ));

            rooms = locationMap.entrySet().stream()
                .map(entry -> new Room(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                adapter.setItems(rooms);
            });
        });
    }

    private void syncDataWithServer() {
        if (departmentCode == null) return;

        String serverIP = sessionManager.getIpAddress();
        String url = "http://" + serverIP + "/my1c/hs/hw/say";
        String json = "{\"odel\":\"" + departmentCode + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        String credentials = okhttp3.Credentials.basic(sessionManager.getUsername(), sessionManager.getPassword());

        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", credentials)
            .build();

        new OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                 mainHandler.post(() -> Toast.makeText(InventoryListActivity.this, "Ошибка синхронизации", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<InventoryItemEntity> inventoryItems = parseInventoryXml(response.body().byteStream());

                        databaseExecutor.execute(() -> {
                            db.inventoryItemDao().clearByDepartmentId(departmentId);
                            for(InventoryItemEntity item : inventoryItems) {
                                item.departmentId = departmentId;
                            }
                            db.inventoryItemDao().insertAll(inventoryItems);

                            // Reload data from DB to refresh the UI
                            loadInitialData();
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при обработке ответа от сервера", e);
                    }
                }
            }
        });
    }

    private List<InventoryItemEntity> parseInventoryXml(InputStream is) {
        List<InventoryItemEntity> list = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, "UTF-8");

            InventoryItemEntity currentItem = null;
            String text = "";
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("Product".equalsIgnoreCase(tagName)) {
                            currentItem = new InventoryItemEntity();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (currentItem != null) {
                            if ("Code".equalsIgnoreCase(tagName)) {
                                currentItem.code = text;
                            } else if ("Name".equalsIgnoreCase(tagName)) {
                                currentItem.name = text;
                            } else if ("rf".equalsIgnoreCase(tagName)) {
                                currentItem.rf = text;
                            } else if ("mol".equalsIgnoreCase(tagName)) {
                                currentItem.mol = text;
                            } else if ("location".equalsIgnoreCase(tagName)) {
                                currentItem.location = text;
                            } else if ("Product".equalsIgnoreCase(tagName)) {
                                list.add(currentItem);
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка парсинга XML", e);
        }
        return list;
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
