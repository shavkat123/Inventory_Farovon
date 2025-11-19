package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.MolMovementDao;
import com.inventory.farovon.db.MolMovementDocument;
import com.inventory.farovon.ui.molmovement.MolMovementAdapter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MolMovementListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MolMovementAdapter adapter;
    private MolMovementDao molMovementDao;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mol_movement_list);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.documents_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        molMovementDao = db.molMovementDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        findViewById(R.id.fab_add_document).setOnClickListener(v -> {
            Intent intent = new Intent(this, MolMovementActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDocuments();
    }

    private void loadDocuments() {
        databaseExecutor.execute(() -> {
            List<MolMovementDocument> documents = molMovementDao.getAllDocuments();
            runOnUiThread(() -> {
                adapter = new MolMovementAdapter(documents);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
