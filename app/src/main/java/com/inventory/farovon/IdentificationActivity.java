package com.inventory.farovon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

        FloatingActionButton fabScan = findViewById(R.id.fab_scan);
        fabScan.setOnClickListener(view -> showScanModeDialog());
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
}