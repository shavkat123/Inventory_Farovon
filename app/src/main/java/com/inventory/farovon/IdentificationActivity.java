package com.inventory.farovon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.inventory.farovon.db.InventoryItemEntity;
import android.os.Handler;
import android.os.Looper;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;
import android.view.KeyEvent;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemDao;
import com.inventory.farovon.ui.ScanModeBottomSheetFragment;
import com.inventory.farovon.ui.ScanOrManualInputDialog;
import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdentificationActivity extends AppCompatActivity implements ScanModeBottomSheetFragment.ScanModeListener, ScanOrManualInputDialog.ScanListener {

    @Override
    public void onScanCompleted(String scannedData) {
        performSearch(scannedData, false);
    }

    private enum ScanMode {
        NONE,
        RFID,
        BARCODE,
        SN,
        CAMERA,
        MANUAL
    }

    private ScanMode currentScanMode = ScanMode.NONE;
    private RecyclerView recyclerView;
    private IdentificationAdapter adapter;
    private List<InventoryItemEntity> resultsList = new ArrayList<>();
    private View emptyStateView;
    private AppDatabase db;
    private InventoryItemDao inventoryItemDao;
    private ExecutorService databaseExecutor;

    private RFIDWithUHFUART mReader;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ExecutorService rfidExecutor;
    private Set<String> foundEpcSet = new HashSet<>();
    private boolean isRfidScanning = false;
    private static final String TAG = "IdentificationActivity";
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ExtendedFloatingActionButton fabScan;
    private ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_view_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IdentificationAdapter(resultsList);
        recyclerView.setAdapter(adapter);

        emptyStateView = findViewById(R.id.empty_state_view);

        db = AppDatabase.getDatabase(getApplicationContext());
        inventoryItemDao = db.inventoryItemDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        fabScan = findViewById(R.id.fab_scan);
        fabScan.setOnClickListener(view -> {
            if (isRfidScanning) {
                stopRfidScanning();
            } else {
                showScanModeDialog();
            }
        });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String scannedCode = result.getData().getStringExtra("scanned_code");
                        if (scannedCode != null) {
                            performSearch(scannedCode, false);
                        }
                    }
                });

        updateUI();

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }

    private void performSearch(String query, boolean isRfidScan) {
        databaseExecutor.execute(() -> {
            List<InventoryItemEntity> items = null;
            switch (currentScanMode) {
                case RFID:
                    items = inventoryItemDao.findByRfid(query);
                    break;
                case BARCODE:
                    items = inventoryItemDao.findByBarcode(query);
                    break;
                case MANUAL:
                    items = inventoryItemDao.findByQuery(query);
                    break;
                case SN:
                    items = inventoryItemDao.findBySerialNumber(query);
                    break;
            }

            if (items != null && !items.isEmpty()) {
                final List<InventoryItemEntity> finalItems = items;
                runOnUiThread(() -> {
                    resultsList.addAll(0, finalItems);
                    adapter.notifyItemRangeInserted(0, finalItems.size());
                    updateUI();
                });
            } else {
                if (isRfidScan) {
                    // If it's an RFID scan and the item is not found, show it as "Unknown"
                    runOnUiThread(() -> {
                        InventoryItemEntity unknownItem = new InventoryItemEntity();
                        unknownItem.rf = query;
                        unknownItem.name = "Неизвестный объект";
                        unknownItem.code = "—";
                        unknownItem.mol = "—";
                        unknownItem.location = "—";
                        resultsList.add(0, unknownItem);
                        adapter.notifyItemInserted(0);
                        updateUI();
                    });
                } else {
                    // For other modes, show a "not found" message
                    runOnUiThread(() -> {
                        Toast.makeText(IdentificationActivity.this, "Объекты не найдены", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updateUI() {
        if (resultsList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void showScanModeDialog() {
        ScanModeBottomSheetFragment bottomSheet = ScanModeBottomSheetFragment.newInstance(currentScanMode.name());
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public void onScanModeSelected(String mode) {
        // Stop any ongoing scan when mode changes
        stopRfidScanning();

        switch (mode) {
            case "RFID":
                currentScanMode = ScanMode.RFID;
                foundEpcSet.clear(); // Reset for a new scanning session
                Toast.makeText(this, "Режим RFID активирован. Нажмите курок для сканирования.", Toast.LENGTH_SHORT).show();
                break;
            case "BARCODE":
                currentScanMode = ScanMode.BARCODE;
                new ScanOrManualInputDialog().show(getSupportFragmentManager(), "ScanOrManualInputDialog");
                break;
            case "SN":
                currentScanMode = ScanMode.SN;
                showManualInputDialog("Введите серийный номер");
                break;
            case "CAMERA":
                currentScanMode = ScanMode.CAMERA;
                Intent intent = new Intent(this, CameraScanActivity.class);
                cameraLauncher.launch(intent);
                break;
            case "MANUAL":
                currentScanMode = ScanMode.MANUAL;
                showManualInputDialog("Ручной ввод");
                break;
        }
    }

    private void showManualInputDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_manual_input, null);
        builder.setView(dialogView);

        final EditText editText = dialogView.findViewById(R.id.edit_text_input);
        final TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        dialogTitle.setText(title);

        builder.setPositiveButton("Найти", (dialog, which) -> {
            String input = editText.getText().toString().trim();
            if (!input.isEmpty()) {
                performSearch(input, false);
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.identification_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear) {
            resultsList.clear();
            adapter.notifyDataSetChanged();
            updateUI();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRfidReader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRfidScanning();
        if (mReader != null) {
            mReader.free();
        }
    }

    private void initRfidReader() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
            mReader.init(getApplicationContext());
            Log.i(TAG, "RFID Reader initialized successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize RFID Reader", e);
            Toast.makeText(this, "Ошибка инициализации RFID", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRfidScanning() {
        if (isRfidScanning) {
            isRfidScanning = false;
            if (mReader != null) {
                mReader.stopInventory();
            }
            if (rfidExecutor != null && !rfidExecutor.isShutdown()) {
                rfidExecutor.shutdown();
            }
            Log.i(TAG, "RFID scanning stopped.");
            handler.post(() -> {
                fabScan.setText("Сканировать");
                fabScan.setIconResource(R.drawable.ic_scan_to_search);
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // F9, F10, 280, 293 - key codes for hardware trigger on Chainway C72
        if ((keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10 || keyCode == 280 || keyCode == 293) && currentScanMode == ScanMode.RFID) {
            if (!isRfidScanning) {
                startRfidScanning();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10 || keyCode == 280 || keyCode == 293) {
            if (isRfidScanning) {
                stopRfidScanning();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void startRfidScanning() {
        if (mReader == null) {
            Toast.makeText(this, "RFID ридер не инициализирован", Toast.LENGTH_SHORT).show();
            return;
        }
        isRfidScanning = true;
        mReader.startInventoryTag();
        rfidExecutor = Executors.newSingleThreadExecutor();
        rfidExecutor.execute(() -> {
            while (isRfidScanning) {
                UHFTAGInfo tag = mReader.readTagFromBuffer();
                if (tag != null) {
                    String epc = tag.getEPC();
                    Log.d(TAG, "RFID Tag Found: " + epc);
                    boolean isNew = foundEpcSet.add(epc);
                    if (isNew) {
                        if (toneGenerator != null) {
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                        }
                        handler.post(() -> performSearch(epc, true));
                    }
                }
            }
        });
        Log.i(TAG, "RFID scanning started.");
        handler.post(() -> {
            fabScan.setText("Остановить");
            fabScan.setIconResource(R.drawable.ic_stop);
        });
    }
}