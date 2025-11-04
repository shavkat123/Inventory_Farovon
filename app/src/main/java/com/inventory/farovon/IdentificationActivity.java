package com.inventory.farovon;

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

public class IdentificationActivity extends AppCompatActivity {

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
    }

    private void showScanModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_scan_mode, null);
        builder.setView(dialogView);

        final Button btnRfid = dialogView.findViewById(R.id.button_rfid);
        final Button btnBarcode = dialogView.findViewById(R.id.button_barcode);
        final Button btnSn = dialogView.findViewById(R.id.button_sn);
        final Button btnCamera = dialogView.findViewById(R.id.button_camera);

        View.OnClickListener tabClickListener = v -> {
            btnRfid.setBackgroundColor(v.getId() == R.id.button_rfid ? ContextCompat.getColor(this, R.color.colorPrimary) : ContextCompat.getColor(this, R.color.colorLightGreyBackground));
            btnRfid.setTextColor(v.getId() == R.id.button_rfid ? ContextCompat.getColor(this, android.R.color.white) : ContextCompat.getColor(this, android.R.color.darker_gray));

            btnBarcode.setBackgroundColor(v.getId() == R.id.button_barcode ? ContextCompat.getColor(this, R.color.colorPrimary) : ContextCompat.getColor(this, R.color.colorLightGreyBackground));
            btnBarcode.setTextColor(v.getId() == R.id.button_barcode ? ContextCompat.getColor(this, android.R.color.white) : ContextCompat.getColor(this, android.R.color.darker_gray));

            btnSn.setBackgroundColor(v.getId() == R.id.button_sn ? ContextCompat.getColor(this, R.color.colorPrimary) : ContextCompat.getColor(this, R.color.colorLightGreyBackground));
            btnSn.setTextColor(v.getId() == R.id.button_sn ? ContextCompat.getColor(this, android.R.color.white) : ContextCompat.getColor(this, android.R.color.darker_gray));

            btnCamera.setBackgroundColor(v.getId() == R.id.button_camera ? ContextCompat.getColor(this, R.color.colorPrimary) : ContextCompat.getColor(this, R.color.colorLightGreyBackground));
            btnCamera.setTextColor(v.getId() == R.id.button_camera ? ContextCompat.getColor(this, android.R.color.white) : ContextCompat.getColor(this, android.R.color.darker_gray));
        };

        btnRfid.setOnClickListener(tabClickListener);
        btnBarcode.setOnClickListener(tabClickListener);
        btnSn.setOnClickListener(tabClickListener);
        btnCamera.setOnClickListener(tabClickListener);

        dialogView.findViewById(R.id.button_scanner_settings).setOnClickListener(v -> showScannerPowerDialog());

        builder.create().show();
    }

    private void showScannerPowerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_scanner_power, null);
        builder.setView(dialogView);
        builder.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}