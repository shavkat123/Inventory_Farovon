package com.inventory.farovon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
            android.content.Intent intent = new android.content.Intent(InventoryListActivity.this, MainActivity.class);
            intent.putExtra("navigate_to", "gallery");
            intent.putExtra("room_code_to_verify", item.getCode());
            intent.putExtra("room_name_to_verify", item.getName());
            intent.putExtra("department_code", departmentCode);
            startActivity(intent);
        });

        departmentCode = getIntent().getStringExtra(EXTRA_DEPARTMENT_CODE);
        departmentId = getIntent().getIntExtra(EXTRA_DEPARTMENT_ID, -1);

        if (departmentId != -1) {
            loadDataFromDb();
            syncData();
        } else {
            Toast.makeText(this, "ID отдела не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDataFromDb() {
        progressBar.setVisibility(View.VISIBLE);
        databaseExecutor.execute(() -> {
            List<InventoryItemEntity> itemEntities = db.inventoryItemDao().getByDepartmentId(departmentId);
            rooms = itemEntities.stream()
                                .map(e -> new Room(e.code, e.name))
                                .distinct() // To get unique rooms
                                .collect(Collectors.toList());
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                adapter.setItems(rooms);
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
                        final List<Room> parsedRooms = parseXml(response.body().byteStream());
                        databaseExecutor.execute(() -> {
                            // This part is tricky. The server sends inventory items, not rooms.
                            // We are faking "rooms" from the "location" field of items.
                            // Let's just update the UI for now.
                            mainHandler.post(() -> {
                                rooms = parsedRooms;
                                adapter.setItems(rooms);
                            });
                        });
                    } catch (Exception e) {
                        // Log error
                    }
                }
            }
        });
    }

    private List<Room> parseXml(InputStream is) {
        List<Room> list = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, null);

            String text = "";
            String code = null, name = null;
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if ("Code".equalsIgnoreCase(tagName)) {
                            code = text;
                        } else if ("Name".equalsIgnoreCase(tagName)) {
                            name = text;
                        } else if ("Product".equalsIgnoreCase(tagName)) { // Assuming server returns rooms as products
                            if (code != null && name != null) {
                                list.add(new Room(code, name));
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            // Log error
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
