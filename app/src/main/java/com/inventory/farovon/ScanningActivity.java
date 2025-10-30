package com.inventory.farovon;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.ui.login.SessionManager;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
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

public class ScanningActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_CODE = "room_code";
    public static final String EXTRA_ROOM_NAME = "room_name";
    public static final String EXTRA_DEPARTMENT_ID = "department_id";
    private static final String TAG = "ScanningActivity";

    private RFIDWithUHFUART mReader;
    private boolean isScanning = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService scanningExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private InventoryAdapter adapter;
    private AppDatabase db;
    private SessionManager sessionManager;
    private List<Nomenclature> unscannedItems = new ArrayList<>();
    private int departmentId; // This needs to be resolved.
    private String roomCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        sessionManager = new SessionManager(this);
        db = AppDatabase.getDatabase(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
            getSupportActionBar().setTitle(roomName != null ? roomName : "Инвентаризация");
        }

        roomCode = getIntent().getStringExtra(EXTRA_ROOM_CODE);
        departmentId = getIntent().getIntExtra(EXTRA_DEPARTMENT_ID, -1);

        recyclerView = findViewById(R.id.inventory_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter();
        recyclerView.setAdapter(adapter);

        loadDataFromDb();
        syncData();

        FloatingActionButton fab = findViewById(R.id.fab_scan);
        fab.setOnClickListener(view -> showPowerDialog());

        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "SDK init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadDataFromDb() {
        progressBar.setVisibility(View.VISIBLE);
        databaseExecutor.execute(() -> {
            // Need to find the department by roomCode to get its ID
            // For now, let's assume we have it. This needs a fix.
            List<InventoryItemEntity> itemEntities = db.inventoryItemDao().getByDepartmentId(departmentId);
            List<Nomenclature> items = new ArrayList<>();
            for (InventoryItemEntity entity : itemEntities) {
                items.add(new Nomenclature(entity.code, entity.name, entity.rf, entity.mol, entity.location));
            }
            unscannedItems = new ArrayList<>(items);
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                adapter.setItems(items);
            });
        });
    }

    private void syncData() {
        if (roomCode == null) return;

        String serverIP = sessionManager.getIpAddress();
        String url = "http://" + serverIP + "/my1c/hs/hw/say";

        OkHttpClient client = new OkHttpClient();
        String json = "{\"odel\":\"" + roomCode + "\"}";
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
                 mainHandler.post(() -> Toast.makeText(ScanningActivity.this, "Ошибка синхронизации", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        final List<Nomenclature> items = parseXml(response.body().byteStream());

                        databaseExecutor.execute(() -> {
                            db.inventoryItemDao().clearByDepartmentId(departmentId);
                            List<InventoryItemEntity> itemEntities = new ArrayList<>();
                            for (Nomenclature item : items) {
                                InventoryItemEntity entity = new InventoryItemEntity();
                                entity.departmentId = departmentId;
                                entity.code = item.getCode();
                                entity.name = item.getName();
                                entity.rf = item.getRfid();
                                entity.mol = item.getMol() != null ? item.getMol() : "";
                                entity.location = item.getLocation() != null ? item.getLocation() : "";
                                itemEntities.add(entity);
                            }
                            db.inventoryItemDao().insertAll(itemEntities);

                            mainHandler.post(() -> {
                                unscannedItems = new ArrayList<>(items);
                                loadDataFromDb();
                            });
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Parsing or DB error", e);
                    }
                }
            }
        });
    }

    private void checkCompletionAndFinish() {
        if (adapter.areAllItemsFound()) {
            databaseExecutor.execute(() -> {
                db.departmentDao().updateCompletionStatus(departmentId, true);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Помещение проинвентаризировано!", Toast.LENGTH_LONG).show();
                    finish();
                });
            });
        }
    }

    private List<Nomenclature> parseXml(InputStream is) {
        List<Nomenclature> list = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, null);

            String text = "";
            String code = null, name = null, rf = null, mol = null, location = null;
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
                        } else if ("rf".equalsIgnoreCase(tagName)) {
                            rf = text;
                        } else if ("mol".equalsIgnoreCase(tagName)) {
                            mol = text;
                        } else if ("location".equalsIgnoreCase(tagName)) {
                            location = text;
                        } else if ("Product".equalsIgnoreCase(tagName)) {
                            if (code != null && name != null && rf != null) {
                                list.add(new Nomenclature(code, name, rf, mol, location));
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "XML parse error", e);
        }
        return list;
    }


    private void showPowerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_scanner_power, null);
        builder.setView(dialogView);

        final SeekBar seekBar = dialogView.findViewById(R.id.seekbar_power);
        final TextView powerValue = dialogView.findViewById(R.id.tv_power_value);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int currentPower = prefs.getInt("scanner_power", 15);

        seekBar.setProgress(currentPower - 1);
        powerValue.setText("Мощность: " + currentPower);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                powerValue.setText("Мощность: " + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            int newPower = seekBar.getProgress() + 1;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("scanner_power", newPower);
            editor.apply();
            Toast.makeText(this, "Мощность установлена: " + newPower, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F9:
            case KeyEvent.KEYCODE_F10:
            case 280:
            case 293:
                if (event.getRepeatCount() == 0) {
                    startScanning();
                    return true;
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F9:
            case KeyEvent.KEYCODE_F10:
            case 280:
            case 293:
                stopScanning();
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void startScanning() {
        if (isScanning) return;
        if (mReader == null) {
            Toast.makeText(this, "Ридер не инициализирован", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mReader.init(this)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int power = prefs.getInt("scanner_power", 15);
            mReader.setPower(power);

            boolean ok = mReader.startInventoryTag();
            if (!ok) {
                Toast.makeText(this, "Не удалось запустить сканирование", Toast.LENGTH_SHORT).show();
                return;
            }
            isScanning = true;
            scanningExecutor.submit(scanningRunnable);
        } else {
            Toast.makeText(this, "Ошибка инициализации ридера", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScanning() {
        if (!isScanning) return;
        isScanning = false;
        if (mReader != null) {
            mReader.stopInventory();
        }
    }

    private final Runnable scanningRunnable = new Runnable() {
        @Override
        public void run() {
            while (isScanning) {
                if (mReader != null) {
                    UHFTAGInfo info = mReader.readTagFromBuffer();
                    if (info != null) {
                        String epc = info.getEPC();
                        if (epc != null) {
                            mainHandler.post(() -> {
                                adapter.addFoundRfid(epc);
                                checkCompletionAndFinish();
                            });
                        }
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
    }

    @Override
    protected void onDestroy() {
        stopScanning();
        if (mReader != null) mReader.free();
        super.onDestroy();
    }
}
