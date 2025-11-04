package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.inventory.farovon.ui.ScanModeBottomSheetFragment;

import com.rscja.deviceapi.RFIDWithUHFUART;

public class IdentificationActivity extends AppCompatActivity implements ScanModeBottomSheetFragment.ScanModeListener {

    private RFIDWithUHFUART mReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ExtendedFloatingActionButton fabScan = findViewById(R.id.fab_scan);
        fabScan.setOnClickListener(view -> showScanModeDialog());

        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "SDK init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startRfidScanning() {
        if (mReader == null) {
            Toast.makeText(this, "Ридер не инициализирован", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mReader.init(this)) {
            mReader.setPower(30);
            boolean ok = mReader.startInventoryTag();
            if (!ok) {
                Toast.makeText(this, "Не удалось запустить инвентарь", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Ошибка инициализации ридера", Toast.LENGTH_SHORT).show();
        }
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