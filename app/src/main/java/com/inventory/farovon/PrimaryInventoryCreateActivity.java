package com.inventory.farovon;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.PrimaryInventoryDocument;
import com.rscja.deviceapi.RFIDWithUHFUART;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrimaryInventoryCreateActivity extends AppCompatActivity {

    private static final String TAG = "PrimaryInventory";
    private Spinner spinnerType;
    private EditText etName, etColor, etCharacteristics, etInventoryNumber;
    private Button btnWriteRfid, btnFinish;
    private RFIDWithUHFUART mReader;
    private boolean isWriting = false;
    private AlertDialog writeDialog;
    private TextView tvStatus;
    private AppDatabase db;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primary_inventory_create);

        spinnerType = findViewById(R.id.spinner_type);
        etName = findViewById(R.id.et_name);
        etColor = findViewById(R.id.et_color);
        etCharacteristics = findViewById(R.id.et_characteristics);
        etInventoryNumber = findViewById(R.id.et_inventory_number);
        btnWriteRfid = findViewById(R.id.btn_write_rfid);
        btnFinish = findViewById(R.id.btn_finish);

        String[] types = {"Здание", "Машина", "Оборудование для завода", "Айти техника"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        db = AppDatabase.getDatabase(this);

        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "RFID Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        btnWriteRfid.setOnClickListener(v -> showWriteDialog());
        btnFinish.setOnClickListener(v -> saveDocument());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mReader != null) {
            new InitTask().execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReader != null) {
            mReader.free();
        }
    }

    private void showWriteDialog() {
        String invNumber = etInventoryNumber.getText().toString().trim();
        if (TextUtils.isEmpty(invNumber)) {
            Toast.makeText(this, "Введите инвентарный номер", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_scan_write, null);
        builder.setView(view);
        builder.setCancelable(false);
        writeDialog = builder.create();
        if (writeDialog.getWindow() != null) {
            writeDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Add key listener to dialog
        writeDialog.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == 139 || keyCode == 280 || keyCode == 293) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "Dialog KeyDown: " + keyCode);
                    writeRfidTag();
                    return true;
                }
            }
            return false;
        });

        tvStatus = view.findViewById(R.id.tv_status);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> {
            isWriting = false;
            writeDialog.dismiss();
        });

        writeDialog.show();
        isWriting = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "Activity KeyDown: " + keyCode);
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) { // Trigger keys
            if (isWriting && writeDialog != null && writeDialog.isShowing()) {
                writeRfidTag();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void writeRfidTag() {
        Log.d(TAG, "writeRfidTag called");
        String invNumber = etInventoryNumber.getText().toString().trim();
        if (TextUtils.isEmpty(invNumber)) return;

        runOnUiThread(() -> {
            tvStatus.setText("Запись...");
            tvStatus.setTextColor(getResources().getColor(android.R.color.black));
        });

        String hexData = stringToHex(invNumber);

        // Pad to 24 hex chars (96 bits) which is standard for many tags
        while (hexData.length() < 24) {
            hexData = hexData + "0";
        }
        if (hexData.length() > 24) {
             hexData = hexData.substring(0, 24);
        }

        final String dataToWrite = hexData;

        new Thread(() -> {
            // Attempt to write to User bank (3), start address 0, length 6 words (24 chars)
            // Access password default "00000000"
            boolean success = false;
            if (mReader != null) {
                Log.d(TAG, "Calling mReader.writeData to User Memory with: " + dataToWrite);
                success = mReader.writeData("00000000", 3, 0, 6, dataToWrite);
                Log.d(TAG, "mReader.writeData returned: " + success);
            } else {
                Log.e(TAG, "mReader is null");
            }

            final boolean finalSuccess = success;
            runOnUiThread(() -> {
                if (finalSuccess) {
                    tvStatus.setText("Метка успешна записана");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    new Handler().postDelayed(() -> {
                        if (writeDialog != null && writeDialog.isShowing()) {
                            writeDialog.dismiss();
                        }
                        isWriting = false;
                    }, 1500);
                } else {
                    tvStatus.setText("Метка не записана повторите попытку");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            });
        }).start();
    }

    private String stringToHex(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append(String.format("%02x", (int) c));
        }
        return sb.toString();
    }

    private void saveDocument() {
        String name = etName.getText().toString().trim();
        String inv = etInventoryNumber.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(inv)) {
             Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
             return;
        }

        PrimaryInventoryDocument doc = new PrimaryInventoryDocument();
        doc.date = System.currentTimeMillis();
        doc.type = spinnerType.getSelectedItem().toString();
        doc.name = name;
        doc.color = etColor.getText().toString().trim();
        doc.characteristics = etCharacteristics.getText().toString().trim();
        doc.inventoryNumber = inv;
        doc.status = "Created";

        databaseExecutor.execute(() -> {
            db.primaryInventoryDao().insert(doc);
            runOnUiThread(() -> {
                Toast.makeText(this, "Документ создан", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    class InitTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            if (mReader == null) return false;
            return mReader.init(PrimaryInventoryCreateActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(PrimaryInventoryCreateActivity.this, "Init fail", Toast.LENGTH_SHORT).show();
            } else {
                 mReader.setPower(30);
            }
        }
    }
}
