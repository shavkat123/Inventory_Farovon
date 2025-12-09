package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.PrimaryInventoryDocument;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrimaryInventoryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PrimaryInventoryAdapter adapter;
    private AppDatabase db;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primary_inventory_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Первичная инвентаризация");
        }

        db = AppDatabase.getDatabase(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PrimaryInventoryAdapter();
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(PrimaryInventoryListActivity.this, PrimaryInventoryCreateActivity.class);
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
            List<PrimaryInventoryDocument> documents = db.primaryInventoryDao().getAll();
            runOnUiThread(() -> adapter.setDocuments(documents));
        });
    }

    private void searchDocuments(String query) {
        databaseExecutor.execute(() -> {
            List<PrimaryInventoryDocument> documents;
            if (query == null || query.trim().isEmpty()) {
                documents = db.primaryInventoryDao().getAll();
            } else {
                documents = db.primaryInventoryDao().findByQuery(query.trim());
            }
            runOnUiThread(() -> adapter.setDocuments(documents));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.primary_inventory_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchDocuments(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchDocuments(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
