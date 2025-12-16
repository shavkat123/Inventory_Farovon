package com.inventory.farovon;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.PrimaryInventoryDocument;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrimaryInventoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "extra_document_id";
    private TextView tvName, tvType, tvColor, tvCharacteristics, tvInventoryNumber, tvRfidHex, tvDate, tvStatus;
    private AppDatabase db;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primary_inventory_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Детали документа");
        }

        long documentId = getIntent().getLongExtra(EXTRA_DOCUMENT_ID, -1);
        if (documentId == -1) {
            Toast.makeText(this, "Ошибка: ID документа не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvName = findViewById(R.id.tv_name);
        tvType = findViewById(R.id.tv_type);
        tvColor = findViewById(R.id.tv_color);
        tvCharacteristics = findViewById(R.id.tv_characteristics);
        tvInventoryNumber = findViewById(R.id.tv_inventory_number);
        tvRfidHex = findViewById(R.id.tv_rfid_hex);
        tvDate = findViewById(R.id.tv_date);
        tvStatus = findViewById(R.id.tv_status);

        db = AppDatabase.getDatabase(this);
        loadDocument(documentId);
    }

    private void loadDocument(long id) {
        databaseExecutor.execute(() -> {
            PrimaryInventoryDocument doc = db.primaryInventoryDao().getById(id);
            runOnUiThread(() -> {
                if (doc != null) {
                    populateUI(doc);
                } else {
                    Toast.makeText(this, "Документ не найден", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void populateUI(PrimaryInventoryDocument doc) {
        tvName.setText(doc.name);
        tvType.setText(doc.type);
        tvColor.setText(doc.color);
        tvCharacteristics.setText(doc.characteristics);
        tvInventoryNumber.setText(doc.inventoryNumber);

        // Convert inventory number to HEX to show what was written
        if (doc.inventoryNumber != null) {
            tvRfidHex.setText(stringToHex(doc.inventoryNumber));
        }

        tvDate.setText(dateFormat.format(new Date(doc.date)));
        tvStatus.setText(doc.status);
    }

    private String stringToHex(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append(String.format("%02x", (int) c));
        }
        // Pad to 24 chars to match what is written to tag
        String hex = sb.toString();
        while (hex.length() < 24) {
            hex = hex + "0";
        }
        if (hex.length() > 24) {
             hex = hex.substring(0, 24);
        }
        return hex;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
