package com.inventory.farovon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

        // Placeholder listeners for dialog buttons
        dialogView.findViewById(R.id.button_rfid).setOnClickListener(v -> Toast.makeText(this, "RFID Clicked", Toast.LENGTH_SHORT).show());
        dialogView.findViewById(R.id.button_barcode).setOnClickListener(v -> Toast.makeText(this, "Barcode Clicked", Toast.LENGTH_SHORT).show());
        dialogView.findViewById(R.id.button_sn).setOnClickListener(v -> Toast.makeText(this, "SN Clicked", Toast.LENGTH_SHORT).show());
        dialogView.findViewById(R.id.button_camera).setOnClickListener(v -> Toast.makeText(this, "Camera Clicked", Toast.LENGTH_SHORT).show());
        dialogView.findViewById(R.id.button_manual_input).setOnClickListener(v -> Toast.makeText(this, "Manual Input Clicked", Toast.LENGTH_SHORT).show());

        builder.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}