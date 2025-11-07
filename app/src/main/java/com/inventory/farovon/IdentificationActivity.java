package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.inventory.farovon.ui.ScanModeBottomSheetFragment;
import com.rscja.deviceapi.RFIDWithUHFUART;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdentificationActivity extends AppCompatActivity implements ScanModeBottomSheetFragment.ScanModeListener {

    private RFIDWithUHFUART mReader;
    private ExecutorService executor;
    private Handler handler;
    private boolean isScanning = false;

    private ExtendedFloatingActionButton fabScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fabScan = findViewById(R.id.fab_scan);
        fabScan.setOnClickListener(view -> {
            if (isScanning) {
                stopRfidScanning();
            } else {
                showScanModeDialog();
            }
        });

        handler = new Handler(Looper.getMainLooper());

        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "SDK init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRfidScanning();
        if (mReader != null) {
            mReader.free();
        }
    }

    private void startRfidScanning() {
        if (mReader == null) {
            postToast("Ридер не инициализирован");
            return;
        }
        if (isScanning) {
            return;
        }

        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            if (!mReader.init(this)) {
                postToast("Ошибка инициализации ридера");
                return;
            }

            setIsScanning(true);

            mReader.setPower(30);
            if (!mReader.startInventoryTag()) {
                postToast("Не удалось запустить инвентарь");
                mReader.free();
                setIsScanning(false);
                return;
            }

            while (isScanning) {
                String[] tags = mReader.readTagFromBuffer();
                if (tags != null) {
                    for (String tag : tags) {
                        handler.post(() -> {
                            Toast.makeText(IdentificationActivity.this, "Найдена метка: " + tag, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (mReader != null) {
                mReader.stopInventory();
            }
        });
    }

    private void stopRfidScanning() {
        if (!isScanning) {
            return;
        }
        setIsScanning(false);
    }

    private void setIsScanning(boolean scanning) {
        isScanning = scanning;
        handler.post(() -> {
            if (scanning) {
                fabScan.setText("ОСТАНОВИТЬ");
            } else {
                fabScan.setText("СКАНИРОВАТЬ");
            }
        });
        if (!scanning && executor != null && !executor.isShutdown()) {
             executor.shutdown();
        }
    }

    private void postToast(final String message) {
        handler.post(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
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
    public void onRfidSelected() {
        startRfidScanning();
    }

    @Override
    public void onBarcodeSelected() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (Exception e) {
            Toast.makeText(this, "Сканер штрих-кодов не найден", Toast.LENGTH_LONG).show();
        }
    }
}
