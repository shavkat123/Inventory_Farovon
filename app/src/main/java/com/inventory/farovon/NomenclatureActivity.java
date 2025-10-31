package com.inventory.farovon; // <-- поправь пакет при необходимости

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.ArrayList;

public class NomenclatureActivity extends AppCompatActivity {

    private RFIDWithUHFUART mReader;
    private boolean isScanning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private NomenclatureAdapter adapter;
    private MaterialButton btnScan;   // scanRef
    private MaterialButton btnInv;    // button7

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nomenclature);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Список инвентаря");
        }


        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NomenclatureAdapter();
        rv.setAdapter(adapter);

        btnScan = findViewById(R.id.scanRef);
        btnInv  = findViewById(R.id.button7);

        // получаем список предполагаемых товаров
        ArrayList<Nomenclature> items = null;
        try {
            // если Serializable
            items = (ArrayList<Nomenclature>) getIntent().getSerializableExtra("items");
        } catch (Exception ignored) {}

        if (items == null) items = new ArrayList<>();
        adapter.setItems(items);

        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "SDK init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        btnScan.setOnClickListener(v -> {
            if (!isScanning) startScanningSafe();
            else             stopScanning();
        });

        btnInv.setOnClickListener(v ->
                Toast.makeText(this, "Инвентаризация: в разработке", Toast.LENGTH_SHORT).show());
    }

    private void startScanningSafe() {
        stopScanning();            // на всякий случай
        adapter.clearCounts();     // обнуляем счётчики
        handler.postDelayed(this::startScanning, 120);
    }

    private void startScanning() {
        if (mReader == null) {
            Toast.makeText(this, "Ридер не инициализирован", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mReader.init(this)) {
            mReader.setPower(30); // 0..30
            boolean ok = mReader.startInventoryTag(); // потоковый инвентарь
            if (!ok) {
                Toast.makeText(this, "Не удалось запустить инвентарь", Toast.LENGTH_SHORT).show();
                return;
            }
            isScanning = true;
            btnScan.setText("Стоп");
            handler.post(pollRunnable);
        } else {
            Toast.makeText(this, "Ошибка инициализации ридера", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScanning() {
        if (isScanning && mReader != null) {
            try { mReader.stopInventory(); } catch (Exception ignored) {}
        }
        isScanning = false;
        btnScan.setText("Проверка");
        handler.removeCallbacks(pollRunnable);
        if (mReader != null) mReader.free();
    }

    // читаем буфер и инкрементим scanCount у подходящей строки (сопоставление по rfid)
    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            if (!isScanning || mReader == null) return;

            UHFTAGInfo info;
            int burst = 0;
            while ((info = mReader.readTagFromBuffer()) != null) {
                String epc = info.getEPC();
                if (epc != null) adapter.incrementByEpc(epc);
                if (++burst > 200) break;
            }
            handler.postDelayed(this, 60);
        }
    };

    @Override protected void onPause() {
        super.onPause();
        stopScanning();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F9:
            case KeyEvent.KEYCODE_F10:
            case 280:
            case 293:
                if (event.getRepeatCount() == 0) {
                    btnScan.performClick();
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
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override protected void onDestroy() {
        stopScanning();
        super.onDestroy();
    }
}
