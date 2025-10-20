package com.inventory.farovon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.rfid.RfidManager;
import com.inventory.farovon.ui.ScannedTagAdapter;

import java.util.HashSet;
import java.util.Set;

public class IdentificationActivity extends AppCompatActivity implements RfidManager.RfidListener {

    private RfidManager rfidManager;
    private ScannedTagAdapter scannedTagAdapter;
    private TextView tvStatus, tvTagCount, tvEmptyView;
    private RecyclerView rvScannedTags;

    private boolean isScanning = false;
    // Use a Set to prevent duplicate tags from being processed by the listener,
    // as the SDK might send the same tag multiple times per second.
    private final Set<String> uniqueTags = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        setupToolbar();
        setupUI();

        rfidManager = new RfidManager(this, this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupUI() {
        tvStatus = findViewById(R.id.tv_status);
        tvTagCount = findViewById(R.id.tv_tag_count);
        tvEmptyView = findViewById(R.id.tv_empty_view);
        rvScannedTags = findViewById(R.id.rv_scanned_tags);

        rvScannedTags.setLayoutManager(new LinearLayoutManager(this));
        scannedTagAdapter = new ScannedTagAdapter();
        rvScannedTags.setAdapter(scannedTagAdapter);

        updateEmptyViewVisibility();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initialize the RFID scanner
        boolean success = rfidManager.init();
        if (!success) {
            // Show an error message if initialization fails
            Toast.makeText(this, "Не удалось инициализировать сканер", Toast.LENGTH_LONG).show();
            tvStatus.setText("Статус: Ошибка");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop scanning and release resources to save battery
        if (isScanning) {
            rfidManager.stopScan();
            isScanning = false;
        }
        rfidManager.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle trigger press to start/stop scanning
        // Common key codes for hardware triggers are F9, F10, or specific scan keys.
        if (keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10) {
            if (event.getRepeatCount() == 0) { // Act only on the first press
                toggleScan();
            }
            return true; // Consume the event
        }
        return super.onKeyDown(keyCode, event);
    }

    private void toggleScan() {
        if (!isScanning) {
            startScanning();
        } else {
            stopScanning();
        }
    }

    private void startScanning() {
        isScanning = true;
        tvStatus.setText("Статус: Сканирование...");
        uniqueTags.clear();
        scannedTagAdapter.clearTags();
        updateTagCount();
        updateEmptyViewVisibility();
        rfidManager.startScan();

        // --- FOR DEMO/TESTING WITHOUT HARDWARE ---
        // This part simulates a scan. You can remove this when using the actual device.
        new Handler(Looper.getMainLooper()).postDelayed(this::simulateScan, 1000);
        // -----------------------------------------
    }

    private void stopScanning() {
        isScanning = false;
        tvStatus.setText("Статус: Остановлено");
        rfidManager.stopScan();
    }

    private void simulateScan() {
        if (!isScanning) return;
        onRfidTagScanned("DEMO_TAG_" + (scannedTagAdapter.getItemCount() + 1));
        onRfidTagScanned("DEMO_TAG_2_REPEATED");
        onRfidTagScanned("DEMO_TAG_2_REPEATED");
        new Handler(Looper.getMainLooper()).postDelayed(this::simulateScan, 1500);
    }


    @Override
    public void onRfidTagScanned(String tagId) {
        // This callback might be from a background thread, so post to the UI thread.
        runOnUiThread(() -> {
            boolean isNewTag = !uniqueTags.contains(tagId);
            if (isNewTag) {
                uniqueTags.add(tagId);
            }

            // Add tag to adapter (increments count if exists, adds if new)
            scannedTagAdapter.addTag(tagId);

            if (isNewTag) {
                updateTagCount();
                updateEmptyViewVisibility();
            }
        });
    }

    @Override
    public void onRfidStatusChanged(String status) {
        runOnUiThread(() -> tvStatus.setText("Статус: " + status));
    }

    private void updateTagCount() {
        tvTagCount.setText("Меток: " + uniqueTags.size());
    }

    private void updateEmptyViewVisibility() {
        if (scannedTagAdapter.getItemCount() == 0) {
            tvEmptyView.setVisibility(View.VISIBLE);
            rvScannedTags.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            rvScannedTags.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}