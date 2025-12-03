package com.inventory.farovon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.DepartmentEntity;
import com.inventory.farovon.db.RoomEntity;
import com.inventory.farovon.ui.login.SessionManager;
import java.io.IOException;
import java.io.InputStream;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class InventoryListActivity extends AppCompatActivity {

    private List<RoomEntity> rooms = new ArrayList<>();
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

    private BroadcastReceiver inventoryCompletionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.inventory.farovon.INVENTORY_COMPLETED".equals(intent.getAction())) {
                String completedRoomCode = intent.getStringExtra("room_code");
                if (completedRoomCode != null) {
                    for (int i = 0; i < rooms.size(); i++) {
                        RoomEntity room = rooms.get(i);
                        if (completedRoomCode.equals(room.code)) {
                            room.isCompleted = true;
                            adapter.notifyItemChanged(i);

                            databaseExecutor.execute(() -> {
                                db.roomDao().updateCompletionStatus(completedRoomCode, true);
                            });
                            break;
                        }
                    }
                }
            }
        }
    };

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
            intent.putExtra("room_code_to_verify", item.code);
            intent.putExtra("room_name_to_verify", item.name);
            intent.putExtra("department_code", departmentCode);
            intent.putExtra("department_id", departmentId);
            startActivity(intent);
        });

        adapter.setOnItemClickListener(item -> showRoomDetails(item));

        departmentCode = getIntent().getStringExtra(EXTRA_DEPARTMENT_CODE);
        departmentId = getIntent().getIntExtra(EXTRA_DEPARTMENT_ID, -1);

        if (departmentId != -1) {
            loadDataFromDb();
            syncData();
        } else {
            Toast.makeText(this, "ID отдела не найден", Toast.LENGTH_SHORT).show();
        }

        IntentFilter filter = new IntentFilter("com.inventory.farovon.INVENTORY_COMPLETED");
        registerReceiver(inventoryCompletionReceiver, filter);
    }

    private void loadDataFromDb() {
        progressBar.setVisibility(View.VISIBLE);
        databaseExecutor.execute(() -> {
            rooms = db.roomDao().getByDepartmentId(departmentId);
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
                 mainHandler.post(() -> Toast.makeText(InventoryListActivity.this, "Работа в оффлайн режиме", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        final List<RoomEntity> parsedRooms = parseXml(response.body().byteStream());

                        for (RoomEntity r : parsedRooms) {
                            r.departmentId = departmentId;
                        }

                        databaseExecutor.execute(() -> {
                            db.roomDao().deleteByDepartmentId(departmentId);
                            if (!parsedRooms.isEmpty()) {
                                db.roomDao().insertAll(parsedRooms);
                            }

                            rooms = parsedRooms;
                            mainHandler.post(() -> {
                                adapter.setItems(rooms);
                                Toast.makeText(InventoryListActivity.this, "Список помещений обновлен", Toast.LENGTH_SHORT).show();
                            });
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void showRoomDetails(RoomEntity room) {
        databaseExecutor.execute(() -> {
            DepartmentEntity dept = db.departmentDao().getById(room.departmentId);
            String departmentName = dept != null ? dept.name : "Неизвестно";

            mainHandler.post(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_details, null);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                TextView tvName = dialogView.findViewById(R.id.tv_room_name);
                TextView tvDept = dialogView.findViewById(R.id.tv_department);
                TextView tvLoc = dialogView.findViewById(R.id.tv_location);
                TextView tvMol = dialogView.findViewById(R.id.tv_mol);
                Button btnClose = dialogView.findViewById(R.id.btn_close);

                tvName.setText(room.name);
                tvDept.setText(departmentName);
                tvLoc.setText(room.code);
                tvMol.setText(room.mol != null ? room.mol : "");

                btnClose.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            });
        });
    }

    private List<RoomEntity> parseXml(InputStream is) {
        List<RoomEntity> list = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, null);

            String text = "";
            String code = null, name = null, mol = null;
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
                        } else if ("MOL".equalsIgnoreCase(tagName)) {
                            mol = text;
                        } else if ("Product".equalsIgnoreCase(tagName)) {
                            if (code != null && name != null) {
                                list.add(new RoomEntity(code, name, 0, mol != null ? mol : ""));
                            }
                            code = null;
                            name = null;
                            mol = null;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        unregisterReceiver(inventoryCompletionReceiver);
    }
}
