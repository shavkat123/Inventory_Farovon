package com.inventory.farovon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdentificationActivity extends AppCompatActivity implements ScanModeBottomSheetFragment.ScanModeListener, ScanOrManualInputDialog.ScanOrManualInputListener {

    private RFIDWithUHFUART mReader;
    private boolean isScanning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onScanModeSelected(int modeId) {
        // NOTE: The hardware barcode scanner on the target device is assumed to be a keyboard wedge.
        // Therefore, we use a dialog with an EditText to capture the scanned data for both
        // barcode and serial number modes.
        if (modeId == R.id.btn_rfid) {
            toggleRfidScanning();
        } else if (modeId == R.id.btn_barcode) {
            ScanOrManualInputDialog.newInstance("Сканирование штрихкода").show(getSupportFragmentManager(), "ScanOrManualInputDialog");
        } else if (modeId == R.id.btn_sn) {
            ScanOrManualInputDialog.newInstance("Ввод серийного номера").show(getSupportFragmentManager(), "ScanOrManualInputDialog");
        } else if (modeId == R.id.btn_camera) {
            // NOTE: GalleryFragment is the project's designated component for camera-based barcode scanning,
            // despite its name suggesting image selection.
            Intent intent = new Intent(this, CameraScanActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        executorService.execute(() -> {
            if (mReader.init(this)) {
                mReader.setPower(30);
                boolean ok = mReader.startInventoryTag();
                if (!ok) {
                    handler.post(() -> Toast.makeText(IdentificationActivity.this, "Не удалось запустить инвентарь", Toast.LENGTH_SHORT).show());
                    return;
                }
                isScanning = true;
                handler.post(() -> Toast.makeText(IdentificationActivity.this, "RFID сканирование начато", Toast.LENGTH_SHORT).show());
                handler.post(pollRunnable);
            } else {
                handler.post(() -> Toast.makeText(IdentificationActivity.this, "Ошибка инициализации ридера", Toast.LENGTH_SHORT).show());
            }
        });
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
                    handler.post(() -> Toast.makeText(IdentificationActivity.this, "Отсканировано: " + epc, Toast.LENGTH_SHORT).show());
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
}