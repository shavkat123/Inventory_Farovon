package com.inventory.farovon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

public class RfidActivity extends AppCompatActivity {

    private RFIDWithUHFUART mReader;
    private boolean isScanning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TagAdapter tagAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tagAdapter = new TagAdapter();
        recyclerView.setAdapter(tagAdapter);

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "SDK init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        btnStart.setOnClickListener(v -> startScanning());
        btnStop.setOnClickListener(v -> stopScanning());
    }

    private void startScanning() {
        if (mReader != null && mReader.init(this)) {
            mReader.setPower(30); // мощность антенны 0–30
            isScanning = true;
            tagAdapter.clearTags();
            handler.post(scanRunnable);
            Toast.makeText(this, "Сканирование запущено", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ошибка инициализации ридера", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScanning() {
        isScanning = false;
        if (mReader != null) {
            mReader.free();
        }
        handler.removeCallbacks(scanRunnable);
        Toast.makeText(this, "Сканирование остановлено", Toast.LENGTH_SHORT).show();
    }

    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if (isScanning && mReader != null) {
                UHFTAGInfo tag = mReader.inventorySingleTag();
                if (tag != null) {
                    tagAdapter.addTag(tag.getEPC());
                }
                handler.postDelayed(this, 200);
            }
        }
    };

    @Override
    protected void onDestroy() {
        stopScanning();
        super.onDestroy();
    }
}
