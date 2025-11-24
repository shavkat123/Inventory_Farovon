package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.AssetMovementDao;
import com.inventory.farovon.db.AssetMovementDocument;
import com.inventory.farovon.ui.assetmovement.AssetMovementAdapter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssetMovementListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AssetMovementAdapter adapter;
    private AssetMovementDao assetMovementDao;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mol_movement_list); // Reusing layout

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Перемещение МП");
        }

        recyclerView = findViewById(R.id.documents_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        assetMovementDao = db.assetMovementDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        findViewById(R.id.fab_add_document).setOnClickListener(v -> {
            Intent intent = new Intent(this, AssetMovementActivity.class);
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
            List<AssetMovementDocument> documents = assetMovementDao.getAllDocuments();
            runOnUiThread(() -> {
                adapter = new AssetMovementAdapter(documents, documentId -> {
                    Intent intent = new Intent(this, AssetMovementDetailActivity.class);
                    intent.putExtra(AssetMovementDetailActivity.EXTRA_DOCUMENT_ID, documentId);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mol_movement_list_menu, menu); // Reusing menu

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });

        return true;
    }

    private void performSearch(String query) {
        databaseExecutor.execute(() -> {
            List<AssetMovementDocument> documents;
            if (query == null || query.trim().isEmpty()) {
                documents = assetMovementDao.getAllDocuments();
            } else {
                documents = assetMovementDao.searchDocuments(query);
            }

            runOnUiThread(() -> {
                adapter = new AssetMovementAdapter(documents, documentId -> {
                    Intent intent = new Intent(this, AssetMovementDetailActivity.class);
                    intent.putExtra(AssetMovementDetailActivity.EXTRA_DOCUMENT_ID, documentId);
                    startActivity(intent);
                });
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
