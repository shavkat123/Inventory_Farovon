package com.inventory.farovon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemEntity;
import android.util.Log;
import android.util.Log;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdentificationActivity extends AppCompatActivity implements ScanModeBottomSheetFragment.ScanModeListener, ScanOrManualInputDialog.ScanOrManualInputListener {

    private static final String TAG = "IdentificationActivity"; // Тег для логирования

    private enum ScanMode {
        NONE,
        RFID,
        BARCODE,
        SN,
        CAMERA
    }

    private ScanMode currentScanMode = ScanMode.NONE;
    private RFIDWithUHFUART mReader;
    private boolean isScanning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AppDatabase db;
    private IdentificationAdapter adapter;

    @Override
    public void onScanModeSelected(int modeId) {
        String modeName = "";
        if (modeId == R.id.btn_rfid) {
            currentScanMode = ScanMode.RFID;
            modeName = "RFID";
        } else if (modeId == R.id.btn_barcode) {
            currentScanMode = ScanMode.BARCODE;
            modeName = "Штрихкод";
        } else if (modeId == R.id.btn_sn) {
            currentScanMode = ScanMode.SN;
            modeName = "Серийный номер";
        } else if (modeId == R.id.btn_camera) {
            currentScanMode = ScanMode.CAMERA;
            modeName = "Камера";
            // NOTE: GalleryFragment is the project's designated component for camera-based barcode scanning,
            // despite its name suggesting image selection.
            Intent intent = new Intent(this, CameraScanActivity.class);
            startActivity(intent);
        }

        if (!modeName.isEmpty() && currentScanMode == ScanMode.RFID) {
            Toast.makeText(this, "Режим " + modeName + " выбран. Нажмите курок для сканирования.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        db = AppDatabase.getDatabase(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view_identification);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IdentificationAdapter();
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabScan = findViewById(R.id.fab_scan);
        fabScan.setOnClickListener(view -> showScanModeDialog());

        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "SDK init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRfidScanning() {
        if (!isScanning) {
            startRfidScanning();
        } else {
            stopRfidScanning();
        }
    }

    private void startRfidScanning() {
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
                Toast.makeText(this, "Не удалось запустить инвентарь", Toast.LENGTH_SHORT).show();
                return;
            }
            isScanning = true;
            handler.post(() -> Toast.makeText(IdentificationActivity.this, "RFID сканирование начато", Toast.LENGTH_SHORT).show());
            executorService.execute(pollRunnable);
        } else {
            Toast.makeText(this, "Ошибка инициализации ридера", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRfidScanning() {
        if (isScanning && mReader != null) {
            try {
                mReader.stopInventory();
            } catch (Exception ignored) {
            }
        }
        isScanning = false;
        handler.removeCallbacks(pollRunnable);
        if (mReader != null) {
            mReader.free();
        }
        Toast.makeText(this, "RFID сканирование остановлено", Toast.LENGTH_SHORT).show();
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isScanning || mReader == null) return;

            UHFTAGInfo info;
            while ((info = mReader.readTagFromBuffer()) != null) {
                String epc = info.getEPC();
                if (epc != null) {
                    Log.d(TAG, "Отсканирована метка EPC: " + epc); // Логируем полученную метку
                    executorService.execute(() -> {
                        InventoryItemEntity item = db.inventoryItemDao().findByRfid(epc);
                        if (item != null) {
                            Log.d(TAG, "Найден товар в БД: " + item.name); // Логируем найденный товар
                            handler.post(() -> {
                                Log.d(TAG, "Добавление товара в адаптер: " + item.name); // Логируем добавление в адаптер
                                adapter.addItem(item);
                            });
                        } else {
                            Log.d(TAG, "Товар с EPC " + epc + " не найден в БД."); // Логируем, если товар не найден
                        }
                    });
                }
            }
            handler.postDelayed(this, 60);
        }
    };

    @Override
    public void onCodeEntered(String code) {
        Toast.makeText(this, "Введено: " + code, Toast.LENGTH_SHORT).show();
    }

    private void showScanModeDialog() {
        ScanModeBottomSheetFragment bottomSheet = new ScanModeBottomSheetFragment();
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRfidScanning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRfidScanning();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F9:
            case KeyEvent.KEYCODE_F10:
            case 280:
            case 293:
                if (event.getRepeatCount() == 0) {
                    if (currentScanMode == ScanMode.RFID) {
                        startRfidScanning();
                    } else if (currentScanMode == ScanMode.BARCODE) {
                        ScanOrManualInputDialog.newInstance("Сканирование штрихкода").show(getSupportFragmentManager(), "ScanOrManualInputDialog");
                    } else if (currentScanMode == ScanMode.SN) {
                        ScanOrManualInputDialog.newInstance("Ввод серийного номера").show(getSupportFragmentManager(), "ScanOrManualInputDialog");
                    }
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
                if (currentScanMode == ScanMode.RFID) {
                    stopRfidScanning();
                }
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}