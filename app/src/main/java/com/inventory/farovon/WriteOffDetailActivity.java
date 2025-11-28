package com.inventory.farovon;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemDao;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.db.WriteOffDao;
import com.inventory.farovon.db.WriteOffDocument;
import com.inventory.farovon.db.WriteOffItem;
import com.inventory.farovon.ui.writeoff.WriteOffDetailAdapter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WriteOffDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "document_id";

    private WriteOffDao writeOffDao;
    private InventoryItemDao inventoryItemDao;
    private ExecutorService databaseExecutor;
    private RecyclerView recyclerView;
    private WriteOffDetailAdapter adapter;

    private TextView tvName;
    private TextView tvOrganization;
    private TextView tvDepartment;
    private TextView tvDate;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_off_detail);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long documentId = getIntent().getLongExtra(EXTRA_DOCUMENT_ID, -1);
        if (documentId == -1) {
            finish();
            return;
        }

        initViews();

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        writeOffDao = db.writeOffDao();
        inventoryItemDao = db.inventoryItemDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        loadDocument(documentId);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.items_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvName = findViewById(R.id.tv_name);
        tvOrganization = findViewById(R.id.tv_organization);
        tvDepartment = findViewById(R.id.tv_department);
        tvDate = findViewById(R.id.tv_date);
        tvStatus = findViewById(R.id.tv_status);
    }

    private void loadDocument(long documentId) {
        databaseExecutor.execute(() -> {
            WriteOffDocument document = writeOffDao.getDocumentById(documentId);
            List<WriteOffItem> items = writeOffDao.getItemsForDocument(documentId);
            List<InventoryItemEntity> inventoryItems = new ArrayList<>();

            for (WriteOffItem item : items) {
                if (item.rfid != null) {
                    List<InventoryItemEntity> found = inventoryItemDao.findByRfid(item.rfid);
                    if (found != null && !found.isEmpty()) {
                        inventoryItems.add(found.get(0));
                    } else {
                        InventoryItemEntity unknown = new InventoryItemEntity();
                        unknown.rf = item.rfid;
                        unknown.name = "Неизвестный объект (" + item.rfid + ")";
                        inventoryItems.add(unknown);
                    }
                }
            }

            runOnUiThread(() -> {
                bindDocumentData(document);
                adapter = new WriteOffDetailAdapter(inventoryItems);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    private void bindDocumentData(WriteOffDocument document) {
        if (document == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(document.date)));

        tvName.setText(document.name);
        tvOrganization.setText(document.organization);
        tvDepartment.setText(document.department);
        tvStatus.setText(document.status);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
