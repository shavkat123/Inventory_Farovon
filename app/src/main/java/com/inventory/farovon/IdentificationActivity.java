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
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemDao;
import com.inventory.farovon.ui.ScanModeBottomSheetFragment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdentificationActivity extends AppCompatActivity implements ScanModeBottomSheetFragment.ScanModeListener {

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

        ExtendedFloatingActionButton fabScan = findViewById(R.id.fab_scan);
        fabScan.setOnClickListener(view -> showScanModeDialog());

        updateUI();
    }

    private void performSearch(String query) {
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

            final List<InventoryItemEntity> finalItems = items;
            if (finalItems != null && !finalItems.isEmpty()) {
                runOnUiThread(() -> {
                    resultsList.addAll(0, finalItems);
                    adapter.notifyItemRangeInserted(0, finalItems.size());
                    updateUI();
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(IdentificationActivity.this, "Объекты не найдены", Toast.LENGTH_SHORT).show();
                });
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
        ScanModeBottomSheetFragment bottomSheet = new ScanModeBottomSheetFragment();
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public void onScanModeSelected(String mode) {
        switch (mode) {
            case "RFID":
                currentScanMode = ScanMode.RFID;
                Toast.makeText(this, "Функция RFID-сканирования в разработке", Toast.LENGTH_LONG).show();
                // TODO: Start RFID scanning logic
                break;
            case "BARCODE":
                currentScanMode = ScanMode.BARCODE;
                showManualInputDialog("Введите штрих-код");
                break;
            case "SN":
                currentScanMode = ScanMode.SN;
                showManualInputDialog("Введите серийный номер");
                break;
            case "CAMERA":
                currentScanMode = ScanMode.CAMERA;
                Toast.makeText(this, "Функция сканирования камерой в разработке", Toast.LENGTH_LONG).show();
                // TODO: Start Camera scanning logic
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
                performSearch(input);
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
}